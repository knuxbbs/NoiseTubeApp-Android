/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2012
 *  Portions contributed by University College London (ExCiteS group), 2012
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2012
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.app.core;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import net.noisetube.api.config.NTAccount;
import net.noisetube.api.exception.AuthenticationException;
import net.noisetube.api.io.FileTracker;
import net.noisetube.api.io.NTWebAPI;
import net.noisetube.api.model.NTCoordinates;
import net.noisetube.api.model.NTLocation;
import net.noisetube.api.model.NTMeasurement;
import net.noisetube.api.model.Track;
import net.noisetube.api.ui.TrackUIAdapter;
import net.noisetube.api.util.CyclicQueue;
import net.noisetube.api.util.Logger;
import net.noisetube.api.util.Measurement;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.ui.model.TrackData;

import java.io.File;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * @author sbarthol, mstevens, humberto
 */
public class AndroidNTService extends Service {

    static private AndroidNTService instance;
    CyclicQueue<TrackData> userTraces;
    CyclicQueue<Measurement> nonLocatedMeasurements;
    TrackUIAdapter adapter = new TrackUIAdapter() {
        @Override
        public void newMeasurement(final Track track, final NTMeasurement newMeasurement, NTMeasurement savedMeasurement) {
            super.newMeasurement(track, newMeasurement, savedMeasurement);
            saveMapMeasure(newMeasurement, track);
        }
    };
    private AndroidPreferences preferences;
    private AndroidNTClient androidNTClient;
    private Logger log;
    private Track track;
    private ArrayList<Measurement> noiseMapMeasureBuffer;
    private int count = 0;

    static public AndroidNTService getInstance() {
        return instance;
    }

    /**
     * Called when the service is first created.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        if (instance == null) {
            instance = this;
        }

        //Get log instance:
        log = Logger.getInstance();
        log.info("AndroidNTService - onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new InitializeAndroidNTClient().execute(this); //will call initializeActivity when successful
        return START_STICKY;
    }

    public Context getAppContext() {
        return getApplicationContext();
    }

    public TelephonyManager getTelephonyManager() {
        return (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    }

    public NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void destroyService() {
        androidNTClient = null;
        preferences = null;
        instance = null;

        if (track != null) {
            track.stop();
        }

        track = null;

        Log.e("track", "Exiting NoiseTube Mobile\n\n");
        log.disableFileMode();
        //Make sure the client is properly disposed of (static variables are set to null etc).
        androidNTClient.dispose();
        stopSelf();
    }

    private void runGarbageCollector() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FileTracker.removeOlderLogs();
            }
        });
        t.run();

    }

    public AndroidPreferences getPreferences() {
        return preferences;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public Track getTrack() {
        return track;
    }

    public void resetTrack() {
        this.track = null;
    }

    public Track newTrack() {
        track = new Track();
        if (nonLocatedMeasurements != null)
            nonLocatedMeasurements.clear();
        else
            nonLocatedMeasurements = new CyclicQueue<Measurement>(5);
        if (noiseMapMeasureBuffer != null)
            noiseMapMeasureBuffer.clear();
        else
            noiseMapMeasureBuffer = new ArrayList<Measurement>();

        track.addTrackUIListener(adapter);
        userTraces.offer(new TrackData(track.getCreatedDate()));
        return track;
    }

    public void saveMapMeasure(NTMeasurement newMeasurement, Track track) {
        NTLocation location = newMeasurement.getLocation();

        if (location != null && location.getCoordinates() != null && location.getCoordinates().hasValidLocation()) {
            NTCoordinates coordinates = location.getCoordinates();
            Measurement item = new Measurement(newMeasurement.getTimeStamp(), coordinates.getLatitude(), coordinates.getLongitude(), newMeasurement.getLeqDBA());
            addNoiseMapMeasure(item);
            logUserMeasurement(track.hashCode(), item);
            flushBuffer(coordinates, track);
        } else {
            Measurement item = new Measurement(newMeasurement.getTimeStamp(), newMeasurement.getLeqDBA());
            nonLocatedMeasurements.offer(item);
        }

    }

    private void flushBuffer(NTCoordinates coordinates, Track track) {
        if (!nonLocatedMeasurements.isEmpty()) {
            for (Measurement item : nonLocatedMeasurements.getValues()) {
                item.setLatitude(coordinates.getLatitude());
                item.setLongitude(coordinates.getLongitude());
                addNoiseMapMeasure(item);
                logUserMeasurement(track.hashCode(), item);
            }
            nonLocatedMeasurements.clear();
        }
    }

    public synchronized ArrayList<Measurement> getNoiseMapMeasureBuffer() {
        return new ArrayList<Measurement>(noiseMapMeasureBuffer);
    }

    public ArrayList<TrackData> getUserMeasurementsTraces() {
        ArrayList<TrackData> list = new ArrayList<TrackData>();
        if (userTraces != null) {
            list.addAll(userTraces.getValues());
        }
        return list;
    }

    public void logUserMeasurement(int trackHashCode, Measurement item) {
        try {
            TrackData trackData = userTraces.getElementByHashCode(trackHashCode);
            trackData.addNoiseMapMeasure(item);
        } catch (NoSuchElementException e) {
            log.error(e, "logUserMeasurement - " + e.getMessage());
        }
    }

    public void updateTrackID(int trackHashCode, int id) {
        try {
            if (trackHashCode != -1) {
                TrackData trackData = userTraces.getElementByHashCode(trackHashCode);
                trackData.setId(String.valueOf(id));
            }
        } catch (NoSuchElementException e) {
            log.error(e, "AndroidNTService - updateTrackID");
        }
    }

    public void setUserTracesCapacity(int capacity) {
        userTraces.setCapacity(capacity);
    }


    public void addNoiseMapMeasure(Measurement item) {
        noiseMapMeasureBuffer.add(item);
    }


    /**
     * @author mstevens
     */
    public class InitializeAndroidNTClient extends AsyncTask<ContextWrapper, Void, AndroidNTClient> {
        @Override
        protected AndroidNTClient doInBackground(ContextWrapper... wrappers) {
            try {
                AndroidNTClient ntClient = new AndroidNTClient(wrappers[0]);
                return ntClient;
            } catch (Exception e) {
                log.error(e, "AndroidNTService - doInBackground");
                return null;
            }
        }

        @Override
        protected void onPostExecute(AndroidNTClient ntClient) {
            androidNTClient = ntClient;
            boolean created = false;
            if (androidNTClient != null) {
                created = true;
                log.info("AndroidNTService - creationFinished");
                preferences = (AndroidPreferences) ntClient.getPreferences();

                noiseMapMeasureBuffer = new ArrayList<Measurement>();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NTAccount account = preferences.getAccount();
                        preferences.getCalibration(); // check for calibrations

                        if (account != null) {
                            File[] files = FileTracker.getTracksPendingToUpload();

                            if (files != null) {
                                NTWebAPI ntWebAPI = new NTWebAPI(preferences.getAccount());
                                if (ntWebAPI.ping()) {
                                    for (File file : files) {
                                        try {
                                            String rp = ntWebAPI.uploadTrackFile(file);

                                            if (!rp.equals("-1")) {
                                                String[] fileNameTokens = file.getName().split("_");
                                                String newName = fileNameTokens[0] + "_" + rp + "_" + fileNameTokens[2];
                                                file.renameTo(new File(preferences.getDataFolderPath() + newName));

                                            }
                                        } catch (AuthenticationException e) {
                                            log.error(e, "AndroidNTService - Uploading pending tracks");
                                        } catch (NullPointerException e) {
                                            log.error(e, "AndroidNTService");
                                        }
                                    }
                                }

                            }

                        }

                        userTraces = FileTracker.loadUserTraces();
                        nonLocatedMeasurements = new CyclicQueue<Measurement>(5);
                        FileTracker.removeOlderLogs();

                    }
                });
                t.start();
                //enable file mode in the logger:
                if (preferences != null) {
                    log.enableFileMode();
                    log.disableLogBuffer(); //we don't need to log buffer now that all messages are directly saved to file

                }

            } else {
                stopSelf();
            }

            //---send a broadcast to inform the basic_menu activity that the service has been created and initialized
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtra("CREATED", created);
            broadcastIntent.setAction("SERVICE_CREATION_ACTION");
            getBaseContext().sendBroadcast(broadcastIntent);

        }
    }
}



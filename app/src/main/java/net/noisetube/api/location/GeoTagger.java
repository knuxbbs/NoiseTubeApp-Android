/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation)
 *
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2012
 *  Portions contributed by University College London (ExCiteS group), 2012
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

package net.noisetube.api.location;

import net.noisetube.api.NTClient;
import net.noisetube.api.Processor;
import net.noisetube.api.Startable;
import net.noisetube.api.config.Preferences;
import net.noisetube.api.model.NTLocation;
import net.noisetube.api.model.NTMeasurement;
import net.noisetube.api.model.Track;
import net.noisetube.api.ui.LocationUI;
import net.noisetube.api.util.Logger;

import java.io.Serializable;

/**
 * @author mstevens, maisonneuve
 */
public abstract class GeoTagger implements Processor, Startable, Serializable {

    //STATICS:
    public static final int GPS_STATE_DISABLED = 0;
    protected int gpsState = GPS_STATE_DISABLED;
    public static final int GPS_STATE_SUSPENDED = 1;
    public static final int GPS_STATE_WAITING = 2;
    public static final int GPS_STATE_GOT_FIX = 3;
    /**
     *
     */
    private static final long serialVersionUID = -618397028572272568L;
    protected final int MARK_AS_OLD_AFTER_USES = 5;
    protected final int MAX_LOCATION_REUSE = 20;
    protected final int SUSPEND_GPS_AFTER_MINUTES = 5;
    protected final int RETRY_GPS_AFTER_MINUTES = 5;

    //DYNAMICS:
    protected Logger log = Logger.getInstance();
    protected Preferences preferences;

    protected LocationUI ui;

    protected boolean running = false;
    protected NTLocation lastLocation = null;
    protected int lastLocationTimesUsed = 0;
    protected long gpsSuspendedSince;

    public GeoTagger() {
        preferences = NTClient.getInstance().getPreferences();
    }

    public static String getGPSStateString(int gpsState) {
        switch (gpsState) {
            case GPS_STATE_DISABLED:
                return "Disabled";
            case GPS_STATE_WAITING:
                return "Waiting for fix";
            case GPS_STATE_GOT_FIX:
                return "Got fix";
            case GPS_STATE_SUSPENDED:
                return "Temporarily suspended";
            default:
                return "unknown";
        }
    }

    public void setUI(LocationUI ui) {
        this.ui = ui;
    }

    public void start() {
        running = true;
        if (preferences.isUseGPS())
            enableGPS();
        //log.debug("LocationComponent started");
    }

    public void stop() {
        if (isGPSEnabled())
            disableGPS();
        else
            setLastLocation(null);
        running = false;
        //log.debug("LocationComponent stopped");
    }

    public boolean isRunning() {
        return running;
    }

    public void enableGPS() {
        if (startGPS())
            setGPSState(GPS_STATE_WAITING);
        else
            setGPSState(GPS_STATE_DISABLED);
    }

    public void disableGPS() {
        stopGPS();
        setLastLocation(null);
        setGPSState(GPS_STATE_DISABLED);
    }

    protected void suspendGPS() {
        if (!preferences.isForceGPS()) {
            stopGPS();
            gpsSuspendedSince = System.currentTimeMillis();
            setGPSState(GPS_STATE_SUSPENDED);
        }
    }

    protected abstract boolean startGPS();

    protected abstract void stopGPS();

    public void toggleGPS() {
        if (isGPSEnabled())
            disableGPS();
        else
            enableGPS();
    }

    public abstract boolean isGPSEnabled();

    public int getGPSState() {
        return gpsState;
    }

    protected void setGPSState(int newGPSState) {
        int oldState = gpsState;
        gpsState = newGPSState;
        if (gpsState != oldState && ui != null) {
            ui.gpsStateChanged(gpsState, oldState);
            //log.debug("New GPS state: " + getGPSStateString(newGPSState));
        }
    }

    /**
     * This method is used whenever we want to manually set the location using a tag (e.g. "Home").
     * This comes in handy whenever no GPS signal is available.
     *
     * @param locationTag The tag of the current location
     */
    public synchronized void setLocationTag(String locationTag) {
        if (locationTag != null && !locationTag.equals("")) {
            //TODO commented by humberto
//            if (lastLocation != null && lastLocation.hasCoordinates() && lastLocationTimesUsed < MAX_LOCATION_REUSE)
            if (lastLocation != null && lastLocation.hasCoordinates())
                lastLocation.setLocationTag(locationTag); //add tag to lastLocation with coordinates
            else
                setLastLocation(new NTLocation(locationTag)); //set new location (with tag only)
            //log.debug("Manual location: " + lastLocation.toString());
        }
    }

    public boolean gotFix() {
        return lastLocation != null && lastLocation.hasCoordinates();
//        return lastLocation != null && lastLocation.hasCoordinates() && lastLocationTimesUsed <= MARK_AS_OLD_AFTER_USES;
    }

    public abstract void updateToLastKnownLocation();

    /**
     * @return the lastLocation
     */
    public synchronized NTLocation getLastLocation() {
        NTLocation locationToReturn = lastLocation;
        if (locationToReturn != null && preferences.isUseGPS()) {

//            lastLocationTimesUsed++;
            if (isGPSEnabled()) {
                if (!gotFix()) {
                    //log.debug("Location is old, gps state = waiting");
                    setGPSState(GPS_STATE_WAITING);
                }
                //check for coordinates because tag-only locations can be reused as long as the user wants
//                if (lastLocation.hasCoordinates() && lastLocationTimesUsed > MAX_LOCATION_REUSE) {
//                    //log.debug("GPS location is too old, no longer used");
//                    updateToLastKnownLocation(); //don't use this GPS location anymore (too old)
//                }
                if (!preferences.isForceGPS() && System.currentTimeMillis() - lastLocation.getCoordinates().getTimeStamp() > SUSPEND_GPS_AFTER_MINUTES * 60 * 1000)
                    suspendGPS(); //suspend GPS to save power
            } else {
                if (gpsSuspendedSince > 0 && System.currentTimeMillis() - gpsSuspendedSince > RETRY_GPS_AFTER_MINUTES * 60 * 1000) {
                    enableGPS(); //retry GPS
                    gpsSuspendedSince = 0;
                }
            }
        }
        return locationToReturn;
    }

    protected synchronized void setLastLocation(NTLocation location) {
        lastLocation = location;
        lastLocationTimesUsed = 0;
        if (location != null && location.hasCoordinates())
            setGPSState(GPS_STATE_GOT_FIX);
    }

    public void process(NTMeasurement newMeasurement, Track track) {
        if (running)
            newMeasurement.setLocation(getLastLocation());
    }

    public void reset() {
        //does nothing (for now)
    }

    public String getName() {
        return "LocationComponent";
    }

}
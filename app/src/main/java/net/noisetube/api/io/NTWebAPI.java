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

package net.noisetube.api.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import net.noisetube.api.config.NTAccount;
import net.noisetube.api.exception.AuthenticationException;
import net.noisetube.api.io.saving.HttpSaver;
import net.noisetube.api.model.NTMeasurement;
import net.noisetube.api.model.Saveable;
import net.noisetube.api.model.TaggedInterval;
import net.noisetube.api.model.Track;
import net.noisetube.api.util.ErrorCallback;
import net.noisetube.api.util.JSONUtils;
import net.noisetube.api.util.URLUTF8Encoder;
import net.noisetube.app.util.ImageUtils;
import net.noisetube.app.util.NTUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mstevens, maisonneuve, sbarthol, humberto
 */
public class NTWebAPI extends SLMWebAPI {

    private static final boolean DEFAULT_ASYNC = true;
    private boolean async = DEFAULT_ASYNC;
    private final String APP_KEY = "C78249FDEC5AB9FEABF95A64F3BE7";
    NTAccount account;
    private ErrorCallback errorCallback;


    public NTWebAPI() {
        super(); //!!!
        //config api base according to the environment
        /*if(NTClient.getInstance().isRunningInEmulator())
            apiBaseURL = DEV_API_BASE_URL; */
    }

    public NTWebAPI(NTAccount account) {
        this();
        this.account = account;
    }

    public void setErrorCallBack(ErrorCallback errorCallBack) {
        this.errorCallback = errorCallBack;
    }

    public boolean authenticated() {
        return account != null;
    }

    public void logout() {
        account = null;
    }

    /**
     * @param username
     * @param password
     * @return an account object if login was successful, null if username/password combination was incorrect
     * @throws Exception in case of a connection or server problem
     *                   <p/>
     *                   TODO encrypt username & password
     */
    public NTAccount login(String username, String password) throws AuthenticationException {
        if (authenticated())
            logout(); //discard current account
        String userAPIkey = null; //throws exception in case of connection problem or HttpResponseCode != OK
        try {
            userAPIkey = httpClient.getRequest(apiBaseURL + "authenticate?login=" + username.toLowerCase() + "&password=nte-" + NTUtils.encryptPassword(password));
        } catch (Exception e) {
            throw new AuthenticationException(e.getCause());
        }
        if (userAPIkey.length() == 40) {    //API key received: correct login
            Bitmap avatar = getUserAvatar(userAPIkey);

            account = new NTAccount(username, userAPIkey, avatar);
            return account;
        } else if (userAPIkey.equalsIgnoreCase("error"))
            return null; //incorrect login
        else {
            throw new AuthenticationException("Login failed, unknown server response: " + userAPIkey);
        }
    }

    // true if the username is available, false in other case
    public boolean checkUsername(String username) throws AuthenticationException {

        String response = "false"; //throws exception in case of connection problem or HttpResponseCode != OK
        try {
            response = httpClient.getRequest(apiBaseURL + "check_login?login=" + username + "&appkey=" + APP_KEY);
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }

        return !Boolean.valueOf(response);
    }


    public boolean changePassword(String oldPassword, String newPassword) throws AuthenticationException {
        boolean response = false;
        if (authenticated()) {
            try {
                response = httpClient.sendPostRequest(apiBaseURL + "change_password?oldpassword=" + oldPassword + "&newpassword=" + newPassword + "&key=" + account.getAPIKey(), "", "text/plain");
            } catch (Exception e) {
                throw new AuthenticationException(e.getMessage());
            }
        } else {
            throw new AuthenticationException();
        }

        return response;
    }

    public boolean updateUserAvatar(File file) throws AuthenticationException {
        boolean response = false;
        if (authenticated()) {
            try {
                response = httpClient.updateUserAvatar(apiBaseURL + "change_user_photo", file, account.getAPIKey());
            } catch (Exception e) {
                throw new AuthenticationException(e.getMessage());
            }
        } else {
            throw new AuthenticationException();
        }

        return response;
    }

    public String uploadTrackFile(File file) throws AuthenticationException {
        String id = "-1";
        if (authenticated()) {
            try {
                id = httpClient.uploadTrackFile(apiBaseURL + "batch_upload", file, account.getAPIKey());
            } catch (Exception e) {
                throw new AuthenticationException(e.getMessage());
            }
        } else {
            throw new AuthenticationException();
        }

        return id;
    }

    public String registerUser(String userName, String password, String email, String hometown) throws AuthenticationException {
        String response = null;
        HttpParams p;

        try {
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
            nameValuePair.add(new BasicNameValuePair("login", userName));
            nameValuePair.add(new BasicNameValuePair("password", password));
            nameValuePair.add(new BasicNameValuePair("email", email));
            nameValuePair.add(new BasicNameValuePair("hometown", hometown));
            nameValuePair.add(new BasicNameValuePair("mobilebrand", Build.BRAND));
            nameValuePair.add(new BasicNameValuePair("model", Build.MODEL));
            nameValuePair.add(new BasicNameValuePair("appkey", APP_KEY));


            HttpResponse rp = httpClient.postRequest(apiBaseURL + "register_user", nameValuePair);
            StringBuilder stringBuilder = new StringBuilder();
            if (rp.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = rp.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                JSONObject object = new JSONObject(stringBuilder.toString());

                if (object.getBoolean("value")) {
                    response = object.getString("key");
                }
            }
        } catch (JSONException e1) {
            throw new AuthenticationException("Method: registerUser Error: " + e1.getMessage());
        } catch (IOException e1) {
            throw new AuthenticationException("Method: registerUser Error: " + e1.getMessage());
        } catch (Exception e1) {
            throw new AuthenticationException("Method: registerUser Error: " + e1.getMessage());
        }


        return response;
    }

    public Bitmap getUserAvatar(String userAPIKey) throws AuthenticationException {
        ByteArrayOutputStream response = null; //throws exception in case of connection problem or HttpResponseCode != OK
        Bitmap bmp = null;
        try {
            response = httpClient.sendGetRequest(apiBaseURL + "get_user_photo?key=" + userAPIKey);

            byte[] b = response.toByteArray();
            bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
            final Bitmap avatar = bmp;

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    ImageUtils.saveToInternalSorage(avatar);
                }
            });
            t.run();


        } catch (Exception e) {
            throw new AuthenticationException(e.getCause());
        }

        return bmp;
    }

    public boolean changeDataPolicySetting(boolean status) throws AuthenticationException {
        boolean response = false;

        try {
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
            nameValuePair.add(new BasicNameValuePair("public", String.valueOf(status)));
            nameValuePair.add(new BasicNameValuePair("key", account.getAPIKey()));

            HttpResponse rp = httpClient.postRequest(apiBaseURL + "change_settings", nameValuePair);
            if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                response = true;
            }
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }

        return response;
    }

    public void startTrack(Track track) throws Exception {
        if (authenticated()) {
            String url = apiBaseURL + "newsession?key=" + account.getAPIKey() + "&" + track.getMetaDataString("=", "&", true, new URLUTF8Encoder.URLStringEncoder());
            String response;
            try {
                response = httpClient.getRequest(url);
                if (!response.substring(0, 2).equalsIgnoreCase("ok"))
                    throw new Exception("Server response: " + response);
                track.setTrackID(Integer.parseInt(response.substring(3, response.length()))); //set the trackID!!!
                log.info("New track started (ID in NoiseTube database: " + track.getTrackID() + ")");
            } catch (Exception e) {
                throw e;
            }
        } else
            throw new Exception("Not logged in");
    }

    public void endTrack(Track track) throws Exception {

        if (authenticated()) {

            String url = apiBaseURL + "endsession?key=" + account.getAPIKey() + "&track=" + track.getTrackID();
            //log.debug("Ending track (" + url + ")");
            try {

                httpClient.getRequest(url);
                log.info("Track " + track.getTrackID() + " ended on server");

            } catch (Exception e) {
                log.error("Could not end track: " + e.getMessage());
                throw e;

            }
        } else
            throw new Exception("Not logged in");
    }

    public void sendData(Track track, Saveable saveable) throws Exception {
        if (authenticated()) {
            //Determine API action based on type of savable:
            String action = null;
            if (saveable instanceof NTMeasurement)
                action = "update";
            else if (saveable instanceof TaggedInterval)
                action = "taginterval";
            if (action == null)
                throw new IllegalArgumentException("Unknown savable type");

            //Build URL:
            String url = apiBaseURL + action + "?" + saveable.toUrl() + "&track=" + track.getTrackID() + "&key=" + account.getAPIKey();

            //Send:
            try {
                if (async)
                    httpClient.getRequestAsync(url, "send data", errorCallback);
                else
                    httpClient.getRequest(url);
            } catch (Exception e) {
                log.debug("Could not send data: " + e.getMessage());
                throw e;
            }
        } else
            throw new Exception("Not logged in");
    }

    public void sendBatch(Track track, HttpSaver.Cache cache) throws Exception {
        if (cache.getSize() > 0) {
            if (authenticated()) {
                if (!track.isTrackIDSet()) {    //this is a new track
                    startTrack(track); //throws SaveException if failed
                }
                //Assemble JSON:
                StringBuilder jsonBff = new StringBuilder();

                //	First the measurements:
                jsonBff.append("{\"measures\":[");
                ArrayList<Saveable> measurements = cache.getMeasurements();
                final int size = measurements.size() - 1;

                for (int i = 0; i < size; i++) {
                    jsonBff.append(((NTMeasurement) measurements.get(i)).toJSON());
                    jsonBff.append(",");
                }
                if (size >= 0) {
                    jsonBff.append(((NTMeasurement) measurements.get(size)).toJSON());

                }
                jsonBff.append("],");

                //	Then the tagged intervals:
                jsonBff.append("\"taggedIntervals\":[");
                ArrayList<Saveable> taggedIntervals = cache.getTaggedIntervals();
                final int sizeT = taggedIntervals.size() - 1;

                for (int i = 0; i < sizeT; i++) {
                    jsonBff.append(((TaggedInterval) taggedIntervals.get(i)).toJSON());
                    jsonBff.append(",");
                }

                if (sizeT >= 0) {
                    jsonBff.append(((TaggedInterval) taggedIntervals.get(sizeT)).toJSON());

                }
                jsonBff.append("]}");

                //send the JSON:
                log.debug("Sending a batch of " + cache.getSize() + " measurements and tagged intervals of track " + track.getTrackID());
                try {
                    httpClient.postRequest(apiBaseURL + "upload?key=" + account.getAPIKey() + "&track=" + track.getTrackID(), jsonBff.toString(), "application/json");
                } catch (Exception e) {
                    throw new Exception("Could not send batch: " + e.getMessage());
                }
                cache.clear(); //only if sending was successful
            } else
                throw new Exception("Not logged in");
        }
    }


    public void postLog(String logMessage) throws Exception {
        if (authenticated()) {
            try {
                httpClient.postRequest(apiBaseURL + "postlog?key=" + account.getAPIKey(), "{log:\"" + JSONUtils.escape(logMessage) + "\"}", "application/json");
            } catch (Exception e) {
                log.error(e, "Could not send log message"); //don't this here, could cause endless loop
            }
            log.debug("Log posted");
        } else
            throw new Exception("Not logged in");
    }

}
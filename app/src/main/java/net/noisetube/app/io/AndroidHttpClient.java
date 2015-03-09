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

package net.noisetube.app.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import net.noisetube.api.io.HttpClient;
import net.noisetube.api.io.IInputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

/**
 * @author mstevens, sbarthol
 */
public class AndroidHttpClient extends HttpClient {

    private DefaultHttpClient httpClient;

    /**
     * @param agent Note: setting the time-outs seems to cause A LOT more connection problems than without, so we don't use them (for now)
     */
    public AndroidHttpClient(String agent) {
        super(agent);

        HttpParams httpParameters = new BasicHttpParams();
        httpParameters.setParameter("http.useragent", agent);
        httpParameters.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        //HttpConnectionParams.setConnectionTimeout(httpParameters, timeout); //Set the timeout in milliseconds until a connection is established
        //HttpConnectionParams.setSoTimeout(httpParameters, timeout); //Set the default socket timeout in milliseconds which is the timeout for waiting for data.
        //ConnManagerParams.setTimeout(httpParameters, timeout);

        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        //SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
        //sslSocketFactory.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
        //registry.register(new Scheme("https", sslSocketFactory, 443));

//        final ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(httpParameters, registry);
//        httpClient = new DefaultHttpClient(manager, httpParameters);
        httpClient = new DefaultHttpClient(httpParameters);
    }

    /**
     * Sending a POST Request
     *
     * @param url
     * @param body
     * @param mimeType
     * @throws Exception
     */
    @Override
    public void postRequest(String url, String body, String mimeType) throws Exception {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", mimeType);
            httpPost.setHeader("Content-Type", mimeType);
            httpPost.setHeader("User-Agent", agent);
            httpPost.setEntity(new StringEntity(body, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost); //response contains the response message and the status code
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                throw new IOException("HTTP response code: " + response.getStatusLine().getStatusCode());
        } catch (RuntimeException re) {
            if (httpPost != null)
                httpPost.abort(); //!!!
            throw new Exception("POST request (MIME type: " + mimeType + ") failed for URL: " + url, re);
        } catch (Exception e) {
            throw new Exception("HTTP POST request (MIME type: " + mimeType + ") failed " +
                    (e instanceof HttpResponseException ?
                            "(response code: " + ((HttpResponseException) e).getStatusCode() + ") " :
                            "") +
                    "for URL: " + url, e);
        }
    }

    @Override
    public HttpResponse postRequest(String url, List<NameValuePair> params) throws Exception {
        HttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            //Encoding POST data
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            response = httpClient.execute(httpPost);
        } catch (UnsupportedEncodingException e) {
            throw new Exception(e.getMessage());
        } catch (FileNotFoundException e) {
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }


        return response;
    }

    @Override
    public boolean sendPostRequest(String url, String body, String mimeType) throws Exception {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", mimeType);
            httpPost.setHeader("Content-Type", mimeType);
            httpPost.setHeader("User-Agent", agent);
            httpPost.setEntity(new StringEntity(body, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost); //response contains the response message and the status code
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            } else {
                throw new IOException("HTTP response code: " + response.getStatusLine().getStatusCode());
            }


        } catch (RuntimeException re) {
            if (httpPost != null)
                httpPost.abort(); //!!!
            throw new Exception("POST request (MIME type: " + mimeType + ") failed for URL: " + url, re);
        } catch (Exception e) {
            throw new Exception("HTTP POST request (MIME type: " + mimeType + ") failed " +
                    (e instanceof HttpResponseException ?
                            "(response code: " + ((HttpResponseException) e).getStatusCode() + ") " :
                            "") +
                    "for URL: " + url, e);
        }

    }

    @Override
    public boolean updateUserAvatar(String url, File file, String userKey) throws Exception {

        try {
            HttpPost httpPost = new HttpPost(url);

            //Create the bitmap from the Uri
            Bitmap b = BitmapFactory.decodeFile(file.getPath());
            //Scale it down
            b = Bitmap.createScaledBitmap(b, b.getWidth() / 2, b.getHeight() / 2, false);
            //create a new file for saving the scaled down version
            File root = Environment.getExternalStorageDirectory();
            //TOOD random generate the tmp file to avoid race conditions.
            File tmp = new File(root, "tmp.jpg");
            FileOutputStream bao = null;

            bao = new FileOutputStream(tmp);

            //compress the scaled down and write it to the tmp file
            b.compress(Bitmap.CompressFormat.JPEG, 70, bao);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody bin = new FileBody(tmp);
            builder.addPart("file", bin);
            builder.addPart("key", new StringBody(userKey, ContentType.TEXT_PLAIN));

            /* Building the content of the post */
//            MultipartEntity reqEntity = new MultipartEntity();
//            FileBody bin = new FileBody(tmp);
//            reqEntity.addPart("file", bin);
//
//            reqEntity.addPart("key",new StringBody(userKey));
//            httpPost.setEntity(reqEntity);

            httpPost.setEntity(builder.build());

            HttpResponse response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            }

        } catch (FileNotFoundException e) {
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
        return false;

    }

    @Override
    public String uploadTrackFile(String url, File file, String userKey) throws Exception {

        try {
            HttpPost httpPost = new HttpPost(url);


            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody bin = new FileBody(file);
            builder.addPart("file", bin);
            builder.addPart("key", new StringBody(userKey, ContentType.TEXT_PLAIN));

            httpPost.setEntity(builder.build());

            HttpResponse rp = httpClient.execute(httpPost);

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
                    return object.getString("track");
                } else {
                    // log
                    log.error(object.getString("msg"));
                }
            }
        } catch (FileNotFoundException e) {
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
        return "-1";

    }

    public ByteArrayOutputStream sendGetRequest(String url) throws Exception {
        HttpGet httpGet = null;
        HttpEntity entity = null;
        try {
            httpGet = new HttpGet(url);
            httpGet.setHeader("User-Agent", agent);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                throw new IOException("HTTP response code: " + response.getStatusLine().getStatusCode());

            InputStream inputStream = response.getEntity().getContent();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            try {
                // instream is content got from httpentity.getContent()
                while ((len = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                baos.close();
            } catch (IOException e) {
                log.error(e, "sendGetRequest");
            }


            return baos; //closes the stream

        } catch (RuntimeException re) {
            if (httpGet != null)
                httpGet.abort(); //!!!
            throw new Exception("HTTP GET request failed for URL: " + url, re);
        } catch (Exception e) {
            throw new Exception("HTTP GET request failed for URL: " + url, e);
        } finally {
            try {
                if (entity != null)
                    entity.consumeContent(); //!!!
            } catch (Exception e) {
                log.error(e, "sendGetRequest");
            }
        }
    }


    /**
     * Sends a GET request and processes the response with an IInputStreamProcessor
     *
     * @param url
     * @param reader to process the response with
     * @throws Exception
     */
    public void getRequest(String url, IInputStreamReader reader) throws Exception {

        HttpGet httpGet = null;
        HttpEntity entity = null;
        try {
            httpGet = new HttpGet(url);
            httpGet.setHeader("User-Agent", agent);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                throw new IOException("HTTP response code: " + response.getStatusLine().getStatusCode());
            entity = response.getEntity();
            reader.read(entity.getContent()); //closes the stream
        } catch (RuntimeException re) {
            if (httpGet != null)
                httpGet.abort(); //!!!
            throw new Exception("HTTP GET request failed for URL: " + url, re);
        } catch (Exception e) {
            throw new Exception("HTTP GET request failed for URL: " + url, e);
        } finally {
            try {
                if (entity != null)
                    entity.consumeContent(); //!!!
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    protected InputStreamToStringReader getInputStreamToStringReader() {
        return new AndroidInputStreamToStringReader();
    }

    /**
     * @author mstevens
     */
    protected class AndroidInputStreamToStringReader extends InputStreamToStringReader {

        private String characterEncoding = null;

        public AndroidInputStreamToStringReader() {
        }

        public AndroidInputStreamToStringReader(String characterEncoding) {
            this.characterEncoding = characterEncoding;
        }

        /*
        * To convert the InputStream to String we use the
        * Reader.read(char[] buffer) method. We iterate until the
        * Reader return -1 which means there's no more data to
        * read. We use the StringWriter class to produce the string.
        */
        public void read(InputStream inputStream) throws IOException {
            if (inputStream != null) {
                Writer writer = new StringWriter();
                char[] buffer = new char[512];
                try {
                    Reader reader = new BufferedReader((characterEncoding == null ?
                            new InputStreamReader(inputStream) :
                            new InputStreamReader(inputStream, characterEncoding)),
                            buffer.length);
                    int n;
                    while ((n = reader.read(buffer)) != -1)
                        writer.write(buffer, 0, n);
                } finally {
                    inputStream.close(); //!!!
                }
                string = writer.toString();
            } else {
                string = null;
            }
        }

    }

}

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

import net.noisetube.api.SLMClient;
import net.noisetube.api.audio.calibration.Calibration;
import net.noisetube.api.audio.calibration.CalibrationsList;
import net.noisetube.api.audio.calibration.CalibrationsParser;
import net.noisetube.api.util.Logger;

import java.io.InputStream;

/**
 * Only for SLM library, only includes pinging and calibration downloading
 *
 * @author mstevens
 */
public class SLMWebAPI {

    protected static final String AGENT = SLMClient.getInstance().getClientType() + "/" + SLMClient.getInstance().getClientVersion();
    static final String DEFAULT_API_BASE_URL = "http://www.noisetube.net/api/";
    protected String apiBaseURL = DEFAULT_API_BASE_URL;
    static final String DEV_API_BASE_URL = "http://192.168.6.167:4000/api/";
    protected Logger log = Logger.getInstance();
    protected HttpClient httpClient;


    public SLMWebAPI() {
        httpClient = SLMClient.getInstance().getHttpClient(AGENT);
    }


    public CalibrationsList downloadCalibrations(final CalibrationsParser parser) {
        try {
            DownloadedCalibrationsReader reader = new DownloadedCalibrationsReader(parser);
            httpClient.getRequest(apiBaseURL + "mobilecalibrations", reader);
            return reader.calibrationsList;
        } catch (Exception e) {
            log.error(e, "Could not download calibrations XML file from server");
            return null;
        }
    }

    public boolean ping() {
        String response = null;
        try {
            response = httpClient.getRequest(apiBaseURL + "ping");
        } catch (Exception ignore) {
            log.error(ignore, "ping method");
        }

        return (response != null && response.equals("ok") ? true : false);

    }

    /**
     * @author mstevens
     */
    class DownloadedCalibrationsReader implements IInputStreamReader {

        CalibrationsList calibrationsList = null;
        private CalibrationsParser parser;

        public DownloadedCalibrationsReader(CalibrationsParser parser) {
            this.parser = parser;
        }

        public void read(InputStream inputStream) throws Exception {
            calibrationsList = parser.parseList(inputStream, Calibration.SOURCE_DOWNLOADED); //closes the stream
        }

    }


}
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

package net.noisetube.api.audio.calibration;

import net.noisetube.api.SLMClient;
import net.noisetube.api.util.Logger;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.transform.stream.StreamResult;

import jlibs.xml.sax.XMLDocument;

/**
 * @author mstevens, humberto
 */
public class CalibrationsList {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private Logger log = Logger.getInstance();
    private int source;
    private Date lastChanged;
    private ArrayList<Calibration> calibrations;

    /**
     * @param calibrations
     * @param calibrations
     */
    public CalibrationsList(int source, Date lastChanged, ArrayList<Calibration> calibrations) {
        this.source = source;
        this.lastChanged = lastChanged;
        this.calibrations = calibrations;
    }

    /**
     * @param lastChangedStr
     * @param calibrations
     * @param calibrations
     */
    public CalibrationsList(int source, String lastChangedStr, ArrayList<Calibration> calibrations) throws ParseException {
        this(source, DATE_FORMAT.parse(lastChangedStr), calibrations);
    }

    public void saveToFile() {
        SLMClient client = SLMClient.getInstance();
        String folder = client.getDataFolderPath();
        if (folder == null)
            return;
        try {
            XMLDocument xml = new XMLDocument(new StreamResult(new File(folder + CalibrationFactory.CALIBRATIONS_XML_FILENAME)), false, 4, null);

            xml.startDocument();
            xml.startElement("calibrations");
            xml.addAttribute("lastChanged", DATE_FORMAT.format(getLastChanged()));
            for (Calibration item : calibrations) {
                item.parseToXML(xml);
            }
            xml.endElement("calibrations");
            xml.endDocument();

        } catch (Exception e) {
            log.error(e, "Could not write calibrations to file");
        }
    }

    /**
     * @return the source
     */
    public int getSource() {
        return source;
    }

    /**
     * @return the lastChanged
     */
    public Date getLastChanged() {
        return lastChanged;
    }

    /**
     * @return the calibrations
     */
    public ArrayList<Calibration> getCalibrations() {
        return calibrations;
    }

    /**
     * @return the number of available calibrations
     */
    public int getCount() {
        return calibrations.size();
    }

}


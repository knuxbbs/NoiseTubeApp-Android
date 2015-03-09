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

package net.noisetube.api.io.saving;

import net.noisetube.api.config.Preferences;
import net.noisetube.api.model.Saveable;
import net.noisetube.api.model.Track;
import net.noisetube.api.util.XMLUtils;

import org.xml.sax.SAXException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

import jlibs.xml.sax.XMLDocument;

/**
 * File based track and measurement saving class
 *
 * @author mstevens, humberto
 */
public class FileSaver extends Saver {

    String filePath;
    XMLDocument xml;
    File file = null;
    private String folderPath;

    public FileSaver(Track track) {
        super(track);
    }

    /**
     * @see
     */
    public void start() {
        if (!running) {
            try {
                this.folderPath = preferences.getDataFolderPath();
                if (folderPath == null)
                    throw new NullPointerException("folderPath is null");

                filePath = folderPath + "TrackID_" + getTrackID() + "_" + XMLUtils.dateToString(track.getCreatedDate()) + ".xml";
                file = new File(filePath);
                xml = new XMLDocument(new StreamResult(file), false, 4, null);

            } catch (TransformerConfigurationException e) {
                log.error(e, "Start method");
                return;
            }
        }
        running = true;
        if (ntClient.isFirstRun()) {
            try {
                xml.startDocument();
                xml.startElement("NoiseTube-Mobile-Session");
                xml.addAttribute("startTime", XMLUtils.timeDateValue(track.getStartTime()));

                if (preferences.isAuthenticated())
                    xml.addAttribute("userKey", preferences.getAccount().getAPIKey());

                HashMap<String, String> metadata = track.getMetaData();
                for (Map.Entry<String, String> entry : metadata.entrySet()) {
                    xml.addAttribute(entry.getKey(), entry.getValue());
                }
            } catch (SAXException e) {
                log.error(e, "Start method");
                return;
            }
        }

        log.debug("FileSaver started (file: " + file.getAbsolutePath() + ")");
        if (preferences.getSavingMode() == Preferences.SAVE_FILE)
            setStatus("Saving to file: " + file.getName());
    }

    public void pause() {
        if (running) {
            paused = true;
            try {
                xml.addComment(XMLUtils.timeStampToString(System.currentTimeMillis()) + ": measuring paused");
            } catch (SAXException e) {
                log.error(e, "pause");
                return;
            }
        }
    }

    public void resume() {
        if (paused) {
            paused = false; //resume from pause
            try {
                xml.addComment(XMLUtils.timeStampToString(System.currentTimeMillis()) + ": measuring resumed");
            } catch (SAXException e) {
                log.error(e, "resume");
                return;
            }

        }
    }

    /**
     * @see
     */
    public void stop() {
        if (running) {
            try {
                xml.endElement("NoiseTube-Mobile-Session");
                xml.endDocument();

                if (track.getTrackID() != -1) {
                    String newfilePath = folderPath + "TrackID_" + getTrackID() + "_" + XMLUtils.dateToString(track.getCreatedDate()) + ".xml";
                    file.renameTo(new File(newfilePath));
                }

            } catch (SAXException e) {
                log.error(e, "stop method");
            }

            running = false;

            //log.debug("FileSaver stopped" + (force ? " (forced)" : ""));
            if (preferences.getSavingMode() == Preferences.SAVE_FILE)
                setStatus("Stopped");
        }
    }

    private String getTrackID() {
        return (track.getTrackID() == -1) ? "PENDING" : String.valueOf(track.getTrackID());
    }

    /**
     * @see Saver#save(net.noisetube.api.model.Saveable)
     */
    @Override
    public void save(Saveable saveable) {
        if (running && saveable != null) //don't check for paused here, otherwise taggedintervals are not saved & last pre-paused measurement may be lost as well(?)
            try {
                saveable.parseToXML(xml);
            } catch (SAXException e) {
                log.error(e, "save method");
            }
    }


    public void enableBatchMode() {
    }

}

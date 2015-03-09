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
import net.noisetube.api.config.Device;
import net.noisetube.api.util.Logger;

import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jlibs.xml.sax.XMLDocument;

/**
 * @author mstevens, maisonneuve
 */
public class Calibration {

    //STATICS------------------------------------------------------------------
    public static final int SOURCE_NONE = -1;
    private int source = SOURCE_NONE;
    public static final int SOURCE_DOWNLOADED = 0;
    public static final int SOURCE_PREVIOUSLY_DOWNLOADED = 1;
    public static final int SOURCE_RESOURCE = 2;
    public static final int SOURCE_HARDCODED = 3;
    public static final int SOURCE_HARDCODED_FOR_EXPERIMENT = 4;
    public static final int SOURCE_DUMMY = 5;
    public static final int SOURCE_USER_PREFERECES = 6;
    public static final char CREDIBILITY_INDEX_A = 'A'; //same as B but specific to a single physical instance of a device model
    public static final char CREDIBILITY_INDEX_B = 'B'; //internal (=SonyCSL/VUB) professional in ideal conditions (verified)
    public static final char CREDIBILITY_INDEX_C = 'C'; //internal professional (verified)
    public static final char CREDIBILITY_INDEX_D = 'D'; //internal professional (unverified)
    public static final char CREDIBILITY_INDEX_E = 'E'; //external professional (unverified)
    public static final char CREDIBILITY_INDEX_F = 'F'; //end user (unverified)
    public static final char CREDIBILITY_INDEX_G = 'G'; //brand match, model mismatch
    public static final char CREDIBILITY_INDEX_H = 'H'; //brand and model mismatch (default used)
    public static final char CREDIBILITY_INDEX_X = 'X'; //no calibration at all
    //DYNAMICS-----------------------------------------------------------------
    private String deviceBrand;
    private String deviceModel;
    private boolean canBeUsedAsBrandDefault = false;
    private boolean canBeUsedAsOverallDefault = false;
    private char credibilityIndex;
    private String creator;
    private String comment;
    private List<CorrectionPair> correctionPairs;
    private boolean manuallyChanged = false;

    public Calibration(double[][] calibrationArray, char credibilityIndex, int source) {
        this(null, null, calibrationArray, credibilityIndex, source);
    }

    public Calibration(String deviceBrand, String deviceModel, double[][] calibrationArray, char credibilityIndex, int source) {
        this(deviceBrand, deviceModel, credibilityIndex, false, false, source);
        for (int i = 0; i < calibrationArray.length; i++)
            correctionPairs.add(new CorrectionPair(calibrationArray[i][Corrector.INPUT_IDX], calibrationArray[i][Corrector.OUTPUT_IDX]));
    }

    public Calibration(String deviceBrand, String deviceModel, char credibilityIndex, boolean canBeUsedAsBrandDefault, boolean canBeUsedAsOverallDefault, int source) {
        this.deviceBrand = deviceBrand;
        this.deviceModel = deviceModel;
        if (credibilityIndex >= CREDIBILITY_INDEX_A && credibilityIndex <= CREDIBILITY_INDEX_H || credibilityIndex == CREDIBILITY_INDEX_X)
            this.credibilityIndex = credibilityIndex;
        else
            Logger.getInstance().error("Invalid credibilityIndex: " + credibilityIndex);
        this.source = source;
        this.canBeUsedAsBrandDefault = canBeUsedAsBrandDefault;
        this.canBeUsedAsOverallDefault = canBeUsedAsOverallDefault;
        correctionPairs = new ArrayList<CorrectionPair>();
    }

    public static String getSourceString(int source) {
        switch (source) {
            case SOURCE_NONE:
                return "unknown";
            case SOURCE_DOWNLOADED:
                return "downloaded from NoiseTube.net";
            case SOURCE_PREVIOUSLY_DOWNLOADED:
                return "Saved previous download from NoiseTube.net";
            case SOURCE_RESOURCE:
                return "loaded from resources";
            case SOURCE_HARDCODED:
                return "hard-coded";
            case SOURCE_HARDCODED_FOR_EXPERIMENT:
                return "hard-coded for experiment";
            case SOURCE_DUMMY:
                return "hard-coded non-functional dummy";
            case SOURCE_USER_PREFERECES:
                return "user preferences";
            default:
                return "unknown";
        }
    }

    /**
     * @return the deviceBrand
     */
    public String getDeviceBrand() {
        return deviceBrand;
    }

    /**
     * @return the deviceModel
     */
    public String getDeviceModel() {
        return deviceModel;
    }

    /**
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the source
     */
    public int getSource() {
        return source;
    }

    /**
     * @return the credibilityIndex
     */
    public char getCredibilityIndex() {
        return credibilityIndex;
    }

    /**
     * @return the credibilityIndex
     */
    public char getEffeciveCredibilityIndex() {
        char effective = credibilityIndex;
        Device device = SLMClient.getInstance().getDevice();
        if (device.getBrand() == null || device.getModel() == null)
            effective = CREDIBILITY_INDEX_H; //brand/model unknown
        else {
            if (this.deviceBrand == null || !device.getBrand().equalsIgnoreCase(this.deviceBrand))
                effective = CREDIBILITY_INDEX_H; //brand mismatch (model too most likely)
            else if (this.deviceModel == null || !device.getModel().equalsIgnoreCase(this.deviceModel))
                effective = CREDIBILITY_INDEX_G; //model mismatch (but brand matched)
        }
        return (char) Math.max(credibilityIndex, effective); //NOT Math.min (we are comparing characters)
    }

    /**
     * @return the correctionPairs
     */
    public List<CorrectionPair> getCorrectionPairs() {
        return correctionPairs;
    }

    /**
     * @return the canBeUsedAsBrandDefault
     */
    public boolean canBeUsedAsBrandDefault() {
        return canBeUsedAsBrandDefault;
    }

    /**
     * @param canBeUsedAsBrandDefault the canBeUsedAsBrandDefault to set
     */
    public void setCanBeUsedAsBrandDefault(boolean canBeUsedAsBrandDefault) {
        this.canBeUsedAsBrandDefault = canBeUsedAsBrandDefault;
    }

    /**
     * @return the canBeUsedAsOverallDefault
     */
    public boolean canBeUsedAsOverallDefault() {
        return canBeUsedAsOverallDefault;
    }

    /**
     * @param canBeUsedAsOverallDefault the canBeUsedAsOverallDefault to set
     */
    public void setCanBeUsedAsOverallDefault(boolean canBeUsedAsOverallDefault) {
        this.canBeUsedAsOverallDefault = canBeUsedAsOverallDefault;
    }

    /**
     * @return the manuallyChanged
     */
    public boolean isManuallyChanged() {
        return manuallyChanged;
    }

    /**
     * Only for use in the parsers!
     *
     * @param cPair
     */
    public void addCorrectionPair(CorrectionPair cPair) {
        addCorrectionPair(cPair, null);
    }

    public void addCorrectionPair(CorrectionPair cPair, String username) {
        correctionPairs.add(cPair);
        if (username != null)
            manuallyChanged(username);
    }

    public void removeCorrectionPair(CorrectionPair cPair, String username) {
        correctionPairs.remove(cPair);
        manuallyChanged(username);
    }

    private void manuallyChanged(String byUser) {
        if (!manuallyChanged) {
            comment = "Created by user " + byUser + " based on " + this.toString();
            creator = byUser;
            source = SOURCE_USER_PREFERECES;
            deviceBrand = SLMClient.getInstance().getDevice().getBrand();
            deviceModel = SLMClient.getInstance().getDevice().getModel();
            credibilityIndex = CREDIBILITY_INDEX_F; //end user made
            this.manuallyChanged = true;
        }
    }

    public String toString() {
        return "Calibration for " + (deviceBrand != null ? (deviceBrand + " " + (deviceModel != null ? deviceModel : "(generic model)")) : "unknown device")
                + " (source: " + getSourceString(source) + ")";
    }

    public void parseToXML(XMLDocument xml) throws SAXException {
        xml.startElement("calibration");

        if (deviceBrand != null)
            xml.addAttribute("deviceBrand", deviceBrand);
        if (deviceModel != null)
            xml.addAttribute("deviceModel", deviceModel);

        xml.addAttribute("credibilityIndex", String.valueOf(credibilityIndex));
        if (canBeUsedAsOverallDefault)
            xml.addAttribute("overallDefault", "true");
        if (canBeUsedAsBrandDefault)
            xml.addAttribute("brandDefault", "true");


        if (creator != null && !creator.equals("")) {
            xml.startElement("creator");
            xml.addText(creator);
            xml.endElement("creator");
        }
        if (comment != null && !comment.equals("")) {
            xml.startElement("comment");
            xml.addText(comment);
            xml.endElement("comment");
        }

        for (CorrectionPair correctionPair : correctionPairs) {
            correctionPair.parseToXML(xml);
        }
        xml.endElement("calibration");

    }

    public Corrector getCorrector() {
        if (correctionPairs.isEmpty())
            return null;
        double[][] calibrationArray = new double[correctionPairs.size()][2];
        Collections.sort(correctionPairs);
        int i = 0;
        for (CorrectionPair item : correctionPairs) {
            calibrationArray[i][Corrector.OUTPUT_IDX] = item.getOutput();
            calibrationArray[i][Corrector.INPUT_IDX] = item.getInput();
            i++;
        }

        return new Corrector(calibrationArray);
    }
}

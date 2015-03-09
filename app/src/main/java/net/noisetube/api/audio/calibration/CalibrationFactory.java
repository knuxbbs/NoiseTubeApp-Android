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
import net.noisetube.api.io.SLMWebAPI;
import net.noisetube.api.util.Logger;
import net.noisetube.app.config.AndroidPreferences;

import java.io.File;
import java.io.FileInputStream;


/**
 * Factory class for Calibrations, using various sources to get calibration specifications
 *
 * @author mstevens
 */
public class CalibrationFactory {

    //STATIC---------------------------------------------------------
    public static final String CALIBRATIONS_XML_FILENAME = "calibrations.xml";

    private static final String DEFAULT_CALIBRATION_XML_NODE =
            "<calibration deviceBrandID=\"2\" deviceBrand=\"Nokia\" deviceModel=\"5230\" credibilityIndex=\"B\" overallDefault=\"true\" brandDefault=\"true\">\n\t<creator>BrusSense-VUB</creator>\n\t<comment>\n\t\tValues obtained by 10th degree polynomial regression of the calibration points of 11 separately calibrated Nokia 5230 devices.\n\t\tCalibration done by Ellie D'Hondt in summer-autumn 2010 in an anechoic chamber at the VUB.\n\t\tRegression done by Matthias Stevens in November 2011.\n\t</comment>\t\n\t<correction input=\"24.186232000222276\" output=\"30.00000119605167\"/>\n\t<correction input=\"25.275949001342457\" output=\"35.000001181492784\"/>\n\t<correction input=\"26.68680900279276\" output=\"40.000001006570955\"/>\n\t<correction input=\"28.784411004949003\" output=\"45.00000087417067\"/>\n\t<correction input=\"33.424043004659126\" output=\"50.00000027302083\"/>\n\t<correction input=\"42.25612598236026\" output=\"54.99999997813444\"/>\n\t<correction input=\"49.31082896454885\" output=\"59.99999987210208\"/>\n\t<correction input=\"58.9337909402532\" output=\"64.9999998414196\"/>\n\t<correction input=\"66.33700592156188\" output=\"69.9999995701437\"/>\n\t<correction input=\"71.53530390843744\" output=\"75.0000002167144\"/>\n\t<correction input=\"76.44825889603342\" output=\"80.00000046478817\"/>\n\t<correction input=\"80.82507288498303\" output=\"85.0000005311158\"/>\n\t<correction input=\"83.92979787714435\" output=\"90.00000064296182\"/>\n\t<correction input=\"86.23962087131261\" output=\"95.00000048807124\"/>\n\t<correction input=\"88.26074686620976\" output=\"99.99999888468301\"/>\n\t<correction input=\"90.6186978602565\" output=\"104.90000009140931\"/>\n</calibration>";

    //DYNAMIC--------------------------------------------------------
    private Logger log = Logger.getInstance();
    private SLMClient client;
    private Device device;
    private CalibrationsList calibrationsList = null;

    public CalibrationFactory() {
        this.client = SLMClient.getInstance();
        this.device = client.getDevice();

        try {
            //Initialize the calibrations list, trying several approaches...
            CalibrationsParser parser = client.getCalibrationParser();

            //Fetch calibrations list in the application resources (may be outdated)

            CalibrationsList builtinCL = null;
            File file = new File(AndroidPreferences.getInstance().getDataFolderPath() + CALIBRATIONS_XML_FILENAME);
            if (file.exists()) {
                log.debug("Loading calibration settings from resources");
                builtinCL = parser.parseList(new FileInputStream(file), Calibration.SOURCE_RESOURCE);
                log.debug("builtinCL: " + builtinCL);
            }


            CalibrationsList onlineCL = null;

            //Try to download calibrations list from the NoiseTube server
            if (device.supportsInternetAccess()) {
                log.debug("Trying to download new calibration settings");
                onlineCL = (new SLMWebAPI()).downloadCalibrations(parser);
            }

            //If that didn't work, try to parse a previously downloaded copy of the list
            if (onlineCL == null /*!!!*/ && device.supportsFileAccess() && client.getDataFolderPath() != null) {
                log.debug("Trying to reload previously downloaded calibration settings");
                onlineCL = parser.parseList(client.getFileInputStream(client.getDataFolderPath() + CALIBRATIONS_XML_FILENAME), Calibration.SOURCE_PREVIOUSLY_DOWNLOADED);
            }

            //If that also failed, or if the downloaded list is older than the build-in one (should not happen)
            if (builtinCL != null && (onlineCL == null || builtinCL.getLastChanged().after(onlineCL.getLastChanged()))) //!!!
                calibrationsList = builtinCL;
            else
                calibrationsList = onlineCL;
        } catch (Exception e) {
            log.error(e, "Exception upon fetching/parsing calibrationslist");
        }

        if (calibrationsList != null) {
            log.info(" - Got " + calibrationsList.getCount() + " available calibrations from source: " + Calibration.getSourceString(calibrationsList.getSource()) + " (last changed on " + CalibrationsList.DATE_FORMAT.format(calibrationsList.getLastChanged()) + ")");
            if (calibrationsList.getSource() == Calibration.SOURCE_DOWNLOADED && device.supportsFileAccess())
                calibrationsList.saveToFile(); //save downloaded calibrations to local file
        } else
            log.error("Unable to get calibrations from website, filesystem or application resources");
    }

    public Calibration getCalibration() {
        return getCalibration(device.getBrand(), device.getModel());
    }

    public Calibration getCalibration(String brand, String model) {
        if (calibrationsList != null) {    //a CalibrationsList was successfully parsed from either the website, the filesystem or the application resources
            Calibration overallDefault = null;
            Calibration brandDefault = null;
            //First try to select fitting calibration (brand and model should match), meanwhile look for brand- and overall defaults

            for (Calibration c : calibrationsList.getCalibrations()) {
                if (brand != null && brand.equalsIgnoreCase(c.getDeviceBrand())) {
                    if (model != null && model.equalsIgnoreCase(c.getDeviceModel()))
                        return c; //brand and model match
                    if (model != null && model.equalsIgnoreCase(c.getDeviceBrand() + " " + c.getDeviceModel())) //for certain HTCs
                        return c; //brand and model match
                    if (c.canBeUsedAsBrandDefault())
                        brandDefault = c; //found default for this brand
                }
                if (c.canBeUsedAsOverallDefault())
                    overallDefault = c;    //found overall default
            }

            //No fitting calibration found, use the brand default if there is one
            if (brandDefault != null)
                return brandDefault;
            //Still no fitting calibration found, use the "overall default" if there is one
            if (overallDefault != null)
                return overallDefault;
            //STILL nothing found? --> something really went wrong (source file didn't include an overall default)...
            throw new IllegalStateException("No (fitting) calibration found!");
        } else {    //No calibrations list could be parsed, we we will use the single hard-coded calibration (DEFAULT_CALIBRATION_XML_NODE)
            Calibration defaultCal = client.getCalibrationParser().parseCalibration(DEFAULT_CALIBRATION_XML_NODE, Calibration.SOURCE_HARDCODED);
            if (defaultCal != null) {
                defaultCal.setCanBeUsedAsOverallDefault(true); //just to be sure
                return defaultCal;
            } else
                throw new IllegalStateException("Calibration fetching failed completely"); //this should never happen
        }
    }

    public Calibration getDummyCalibation() {
        return getDummyCalibation(device.getBrand(), device.getModel());
    }

    public Calibration getDummyCalibation(String brand, String model) {
        return new Calibration(brand, model, Calibration.CREDIBILITY_INDEX_X, true, true, Calibration.SOURCE_DUMMY);
    }

}

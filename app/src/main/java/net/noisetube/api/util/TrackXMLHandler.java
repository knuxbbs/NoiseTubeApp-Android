package net.noisetube.api.util;

import net.noisetube.api.model.TaggedInterval;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Humberto
 */
public class TrackXMLHandler extends DefaultHandler {

    protected Logger log = Logger.getInstance();
    boolean currentElement = false;
    String currentValue = null;
    private Date startTime;
    private String credibility;
    private String deviceJavaPlatform;
    private String devicePlatformVersion;
    private String client;
    private String deviceBrand;
    private String deviceModel;
    private String devicePlatform;
    private String calibration;
    private String deviceModelVersion;
    private String deviceJavaPlatformVersion;
    private String clientVersion;
    private String clientBuildDate;
    private List<TaggedInterval> taggedIntervals;
    private List<Measurement> measurements;
    private TaggedInterval taggedInterval;
    private Measurement measurement;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = true;

        if (qName.equals("NoiseTube-Mobile-Session")) {

            startTime = parseDateWithZone(attributes.getValue("startTime"));
            credibility = attributes.getValue("credibility");
            deviceJavaPlatform = attributes.getValue("deviceJavaPlatform");
            devicePlatformVersion = attributes.getValue("devicePlatformVersion");
            client = attributes.getValue("client");
            deviceBrand = attributes.getValue("deviceBrand");
            deviceModel = attributes.getValue("deviceModel");
            devicePlatform = attributes.getValue("devicePlatform");
            calibration = attributes.getValue("calibration");
            deviceModelVersion = attributes.getValue("deviceModelVersion");
            deviceJavaPlatformVersion = attributes.getValue("deviceJavaPlatformVersion");
            clientVersion = attributes.getValue("clientVersion");
            clientBuildDate = attributes.getValue("clientBuildDate");

            taggedIntervals = new ArrayList<TaggedInterval>();
            measurements = new ArrayList<Measurement>();


        } else if (qName.equalsIgnoreCase("taggedInterval")) {

            List<String> tags = Arrays.asList(attributes.getValue("tags").split(","));

            taggedInterval = new TaggedInterval();
            taggedInterval.setBeginMeasurement(Integer.valueOf(attributes.getValue("beginIndex")));
            taggedInterval.setEndMeasurement(Integer.valueOf(attributes.getValue("endIndex")));
            taggedInterval.setTags(new ArrayList<String>(tags));

            taggedIntervals.add(taggedInterval);

        } else if (qName.equals("measurement") && attributes.getValue("location") != null) {
            String[] values = attributes.getValue("location").split(":|,");

            measurement = new Measurement();
            measurement.setLoudness(Double.parseDouble(attributes.getValue("loudness")));
            measurement.setTimeStamp(parseDateWithZone(attributes.getValue("timeStamp")));
            measurement.setLatitude(Double.valueOf(values[1]));
            measurement.setLongitude(Double.valueOf(values[2]));
            measurements.add(measurement);

        }
    }

    private Date parseDateWithZone(String value) {
        Date date;
        try {
            String[] dateArray = value.split("T");
            String[] temp = dateArray[0].split("-");
            date = XMLUtils.stringToDate(temp[2] + "-" + temp[1] + "-" + temp[0] + " " + dateArray[1].substring(0, 8));

        } catch (ParseException e) {
            date = new Date();
            log.error(e, "Error in startTime: " + e.getMessage());
        }
        return date;
    }


    public Date getStartTime() {
        return startTime;
    }

    public String getCredibility() {
        return credibility;
    }

    public String getDeviceJavaPlatform() {
        return deviceJavaPlatform;
    }

    public String getDevicePlatformVersion() {
        return devicePlatformVersion;
    }

    public String getClient() {
        return client;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDevicePlatform() {
        return devicePlatform;
    }

    public String getCalibration() {
        return calibration;
    }

    public String getDeviceModelVersion() {
        return deviceModelVersion;
    }

    public String getDeviceJavaPlatformVersion() {
        return deviceJavaPlatformVersion;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getClientBuildDate() {
        return clientBuildDate;
    }

    public List<TaggedInterval> getTaggedIntervals() {
        return taggedIntervals;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }
}



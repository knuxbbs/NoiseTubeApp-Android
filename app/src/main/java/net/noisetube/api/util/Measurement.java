package net.noisetube.api.util;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Humberto
 */
public class Measurement implements Serializable {

    private Date timeStamp;
    private double latitude;
    private double longitude;
    private double loudness;

    public Measurement() {
        this.latitude = -1;
        this.longitude = -1;
    }

    public Measurement(Date timeStamp, double loudness) {
        this.timeStamp = timeStamp;
        this.loudness = loudness;
    }

    public Measurement(Date timeStamp, double latitude, double longitude, double loudness) {
        this.timeStamp = timeStamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.loudness = loudness;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLoudness() {
        return loudness;
    }

    public void setLoudness(double loudness) {
        this.loudness = loudness;
    }


    @Override
    public String toString() {
        return "Measurement{" +
                "timeStamp=" + timeStamp +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", loudness=" + loudness +
                '}';
    }
}

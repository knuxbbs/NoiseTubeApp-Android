package net.noisetube.app.ui.model;

import net.noisetube.api.util.Measurement;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Humberto
 */
public class TrackData implements Serializable {

    private String id;
    private List<Measurement> measurements;
    private Date created;


    public TrackData() {
        id = "PENDING";
        measurements = new ArrayList<Measurement>();
        created = new Date();

    }

    public TrackData(Date date) {
        id = "PENDING";
        measurements = new ArrayList<Measurement>();
        created = date;

    }


    public TrackData(String id, Date created, List<Measurement> measurements) {
        this.id = id;
        this.created = created;
        this.measurements = measurements;

        if (id.equals("-1")) {
            this.id = "PENDING";
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (id.equals("-1")) {
            id = "PENDING";
        }
    }

    public int getTotalMeasurements() {
        return measurements.size();
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void addNoiseMapMeasure(Measurement item) {
        measurements.add(item);
    }

    public String getFormattedCreationDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");
        String date = sdf.format(created);
        return date;
    }

    public Date getCreationDate() {
        return created;
    }

    @Override
    public int hashCode() {
        int hash = -1;
        if (created != null) {
            hash = 1;
            hash = hash * 31 + created.hashCode();
        }
        return hash;

    }


}

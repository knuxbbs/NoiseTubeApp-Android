package net.noisetube.app.ui.model;

import net.noisetube.api.model.SLMMeasurement;
import net.noisetube.api.model.TaggedInterval;
import net.noisetube.api.model.Track;
import net.noisetube.api.util.Logger;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.ui.widget.MultiSpinner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Humberto
 */
public class TagMeasureModel {

    private Track track;
    private int indexFirstM = 0;
    private int indexLastM = 0;
    private AndroidPreferences preferences;


    public TagMeasureModel(Track track, int indexFirstM, int indexLastM) {
        this.track = track;
        this.indexFirstM = indexFirstM;
        this.indexLastM = indexLastM;
        preferences = AndroidPreferences.getInstance();
    }

    public void taggingSegment(String tagsValues, List<MultiSpinner.MultiSpinnerItem> items) {

        try {
            if (track != null) {
                ArrayList<String> tags = new ArrayList<String>();
                //typed tags:
                for (String tag : tagsValues.split(",")) {
                    if (!tag.isEmpty()) {
                        tags.add(tag.trim());
                    }
                }
                //selected tags:
                for (MultiSpinner.MultiSpinnerItem i : items)
                    if (i.isSelected())
                        tags.add(i.toString());
                if (track != null && !tags.isEmpty()) {
                    //insert tagged interval:
                    ArrayList<SLMMeasurement> measurements = track.getMeasurementsList();
                    int numbertFirstM = measurements.get(indexFirstM).getNumber();
                    int numbertLastM = measurements.get(indexLastM).getNumber();
                    track.addTaggedInterval(new TaggedInterval(numbertFirstM, numbertLastM, tags));
                    //save tags for later:
                    preferences.updateTagsList(tags);
                }
            }

        } catch (Exception e) {
            Logger.getInstance().error(e, "upon apply in TagMeasureDialog");
            return;
        }

    }

}



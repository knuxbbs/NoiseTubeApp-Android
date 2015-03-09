package net.noisetube.app.ui.delegate;

import android.os.AsyncTask;

import net.noisetube.api.model.Track;
import net.noisetube.app.ui.model.TagMeasureModel;
import net.noisetube.app.ui.widget.MultiSpinner;

import java.util.List;

/**
 * @author Humberto
 */

public class TagMeasureViewModel {

    private TagMeasureModel model;


    public TagMeasureViewModel(Track track, int numberFirstM, int numberLastM) {
        this.model = new TagMeasureModel(track, numberFirstM, numberLastM);
    }

    public void invokeTagggingAction(final String tags, final List<MultiSpinner.MultiSpinnerItem> items) {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                model.taggingSegment(tags, items);
                return null;
            }
        };
        task.execute();
    }
}

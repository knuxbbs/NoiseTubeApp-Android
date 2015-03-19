package net.noisetube.app.ui.delegate;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.noisetube.R;
import net.noisetube.app.core.AndroidNTService;
import net.noisetube.app.ui.MyTracesActivity;
import net.noisetube.app.ui.NoiseMapActivity;
import net.noisetube.app.ui.model.TrackData;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Humberto
 */
public class MyTracesViewModel {

    AndroidNTService service;
    private MyTracesActivity activity;


    public MyTracesViewModel(MyTracesActivity activity) {
        this.activity = activity;

    }

    public void populateTraceItems() {
        ViewGroup container = (ViewGroup) activity.findViewById(R.id.trace_items_list);
        service = AndroidNTService.getInstance();

        if (service != null) {
            List<TrackData> traces = service.getUserMeasurementsTraces();
            if (!traces.isEmpty())
                activity.hideEmptyMessage();

            Comparator<TrackData> comparator = new Comparator<TrackData>() {
                @Override
                public int compare(TrackData lhs, TrackData rhs) {

                    long lhsTime = lhs.getCreationDate().getTime();
                    long rhsTime = rhs.getCreationDate().getTime();
                    if (lhsTime < rhsTime) {
                        return 1;
                    } else if (lhsTime > rhsTime) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };


            Collections.sort(traces, comparator);
            final int to = traces.size() - 1;
            for (int i = 0; i < to; i++) {
                container.addView(makeTraceItem(traces.get(i), container));
                container.addView(makeTraceSeparator(container));
            }

            if (to >= 0) {
                container.addView(makeTraceItem(traces.get(to), container));
            }
        }


    }


    private View makeTraceItem(final TrackData item, ViewGroup container) {

        View view = activity.getLayoutInflater().inflate(R.layout.trace_item, container, false);

        ImageView map = (ImageView) view.findViewById(R.id.icon_map);
        TextView trackId = (TextView) view.findViewById(R.id.track_id);
        TextView measurements = (TextView) view.findViewById(R.id.total_measurements);
        TextView date = (TextView) view.findViewById(R.id.track_date);

        trackId.setText((item.getId().equals("-1") ? "PENDING" : item.getId()));
        measurements.setText(String.valueOf(item.getTotalMeasurements()));
        date.setText(item.getFormattedCreationDate());

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, NoiseMapActivity.class);
                intent.putExtra("TRACK", item);
                activity.startActivity(intent);
                activity.finish();
            }
        });

        return view;
    }

    private View makeTraceSeparator(ViewGroup container) {
        View view = activity.getLayoutInflater().inflate(R.layout.trace_separator, container, false);
        return view;
    }
}

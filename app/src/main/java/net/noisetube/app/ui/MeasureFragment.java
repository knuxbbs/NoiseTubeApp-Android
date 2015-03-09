package net.noisetube.app.ui;


import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.noisetube.R;
import net.noisetube.api.TrackStatistics;
import net.noisetube.api.model.NTMeasurement;
import net.noisetube.api.model.Track;
import net.noisetube.api.ui.NTColor;
import net.noisetube.api.ui.TrackUI;
import net.noisetube.api.util.Logger;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.core.AndroidNTService;
import net.noisetube.app.ui.delegate.MeasureViewModel;
import net.noisetube.app.ui.listener.Notification;
import net.noisetube.app.ui.listener.NotificationListener;
import net.noisetube.app.ui.widget.CheckableFrameLayout;
import net.noisetube.app.ui.widget.SPLGraphView;
import net.noisetube.app.ui.widget.SPLView;
import net.noisetube.app.ui.widget.StatisticsView;
import net.noisetube.app.util.DialogUtils;
import net.noisetube.app.util.LUtils;
import net.noisetube.app.util.NTUtils;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class MeasureFragment extends Fragment implements TrackUI, NotificationListener {


    private static final String STATUS_PAUSED = "PAUSED";
    private static final String STATUS_MEASURING = "MEASURING";
    private static final String STATUS_STOPPED = "STOPPED";
    private static MeasureFragment instance;
    private static View view;
    protected Logger log = Logger.getInstance();
    MeasureViewModel delegate;
    AndroidNTService androidNTService;
    private Button btnStart, btnPauseResume, btnStop;
    private TextView status;
    /* The following parameters store the Views and Layouts part of this Activity */
    // The view containing the graph:
    private SPLGraphView graphView;
    // The view containing a number:
    private SPLView splView;
    // The stat views:
    private StatisticsView statTime;
    private StatisticsView statMinMaxAvg;
    private StatisticsView statDistance;
    private CheckableFrameLayout btnManualTagging;
    private boolean tagging;
    private LUtils mLUtils;

    public MeasureFragment() {

    }

    public static MeasureFragment getInstance() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLUtils = LUtils.getInstance((android.support.v7.app.ActionBarActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_measure, container, false);
        delegate = new MeasureViewModel(this, view.getContext());

        btnStart = (Button) view.findViewById(R.id.btn_start_measure);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AndroidPreferences.getInstance().isTosAccepted() && !NTUtils.supportsPositioning()) {
                    DialogUtils.showLocationDialog(getActivity());
                    return;
                }

                btnStart.setEnabled(false);
                delegate.invokeStartAction(getActivity());

            }
        });
        btnPauseResume = (Button) view.findViewById(R.id.btn_pause_measure);
        btnPauseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NTUtils.supportsPositioning()) {
                    btnPauseResume.setEnabled(false);
                    delegate.invokePauseOrResumeAction();
                } else {
                    DialogUtils.showLocationDialog(getActivity());
                }


            }
        });
        btnStop = (Button) view.findViewById(R.id.btn_stop_measure);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToPausedStatus();
                btnStop.setEnabled(false);
                delegate.invokeStopAction(getActivity());
            }
        });


        // Status label:
        status = (TextView) view.findViewById(R.id.lblMeasuringStatus);
        graphView = (SPLGraphView) view.findViewById(R.id.splGraphView);
        graphView.setParentDelegate(delegate);
        graphView.setParentListener(this);
        splView = (SPLView) view.findViewById(R.id.splView);
        statTime = (StatisticsView) view.findViewById(R.id.statTime);
        statMinMaxAvg = (StatisticsView) view.findViewById(R.id.statMinMaxAvg);
        statDistance = (StatisticsView) view.findViewById(R.id.statDistance);

        btnManualTagging = (CheckableFrameLayout) getActivity().findViewById(R.id.btn_manual_tagging);
        btnManualTagging.setVisibility(View.VISIBLE);
        btnManualTagging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean status = !tagging;
                enableManualTagging(status, true);
                graphView.setEnableTouchEvent(status);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    btnManualTagging.announceForAccessibility(status ?
                            getString(R.string.manual_tagging_enabled) :
                            getString(R.string.manual_tagging_disabled));
                }
            }
        });


        instance = this;


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        androidNTService = AndroidNTService.getInstance();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        delegate.freeResources();
        delegate = null;

    }


    @Override
    public void onResume() {
        super.onResume();

        if (androidNTService == null) {
            androidNTService = AndroidNTService.getInstance();
        }

        if (androidNTService != null) {

            if (AndroidPreferences.getInstance().isTosAccepted()) {

                Track track = androidNTService.getTrack();

                if (track == null) {
                    status.setTextColor(new NTColor(189, 189, 189).getRGBValue());
                    status.setText(STATUS_STOPPED);
                    statTime.setText("00:00:00");
                    statMinMaxAvg.setText("00/00/00");
                    statDistance.setText("0 m");
                } else if (!track.isPaused()) {

                    changeToMeasuringStatus();
                    updateGraphAndStatistics(track);

                } else if (track.isPaused()) {
                    changeToPausedStatus();
                    updateGraphAndStatistics(track);
                    btnStart.setVisibility(View.GONE);
                    btnPauseResume.setVisibility(View.VISIBLE);
                    btnStop.setVisibility(View.VISIBLE);

                }
            } else {
                Toaster.displayToast(getString(R.string.warning_msg_login_dialog));
                AndroidPreferences.getInstance().setSavingModeAndPersist(2);

            }
        } else {
            getActivity().finish();
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void enableManualTagging(boolean status, boolean allowAnimate) {
        tagging = status;

        btnManualTagging.setChecked(tagging, allowAnimate);

        ImageView iconView = (ImageView) btnManualTagging.findViewById(R.id.manual_tagging_icon);
        mLUtils.compatSetOrAnimatePlusCheckIcon(iconView, status, allowAnimate);
        btnManualTagging.setContentDescription(getString(status
                ? R.string.disable_manual_tagging_desc
                : R.string.enable_manual_tagging_desc));
    }


    @Override
    public void newMeasurement(final Track track, final NTMeasurement ntMeasurement, NTMeasurement ntMeasurement2) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        graphView.drawTrack(track);
                        splView.update(ntMeasurement);

                        //Statistics
                        final TrackStatistics stats = track.getStatistics();

                        statTime.setText(track.getFormattedElapsedTime());
                        statMinMaxAvg.setText(stats.getFormattedMinMAxAvgValue());
                        statDistance.setText(stats.getFormattedCoveredDistance());


                    } catch (Exception e) {
                        log.error(e, "newMeasurement");
                    }
                }
            });
        } catch (NullPointerException e) {
            log.error(e, "MeasureFragment.newMeasurement()");
        }

    }

    @Override
    public void measuringStarted(Track track) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeToMeasuringStatus();
                }
            });

        } catch (NullPointerException e) {
            log.error(e, "MeasureFragment.measuringStarted()");
        }
    }

    @Override
    public void measuringPaused(Track track) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeToPausedStatus();

                }
            });
        } catch (NullPointerException e) {
            log.error(e, "MeasureFragment.measuringStarted()");
        }
    }

    @Override
    public void measuringResumed(Track track) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    splView.restorePreviousColor();
                    btnPauseResume.setText(getResources().getText(R.string.action_pause_measure));
                    btnPauseResume.setEnabled(true);
                    btnStop.setEnabled(true);
                    status.setText(STATUS_MEASURING);
                    status.setTextColor(new NTColor(63, 81, 181).getRGBValue());
                }
            });
        } catch (NullPointerException e) {
            log.error(e, "MeasureFragment.measuringStarted()");
        }

    }

    @Override
    public void measuringStopped(Track track) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        status.setTextColor(new NTColor(189, 189, 189).getRGBValue());
                        status.setText(STATUS_STOPPED);
                        statTime.setText("00:00:00");
                        statMinMaxAvg.setText("00/00/00");
                        statDistance.setText("0 m");
                        splView.reset();
                        graphView.invalidate();
                        btnStart.setVisibility(View.VISIBLE);
                        btnStart.setEnabled(true);
                        btnPauseResume.setVisibility(View.GONE);
                        btnStop.setVisibility(View.GONE);
                    } catch (Exception e) {
                        log.error(e, "measuringResumed Error");
                    }
                }
            });

        } catch (NullPointerException e) {
            log.error(e, "MeasureFragment.measuringStarted()");
        }

    }

    private void updateGraphAndStatistics(Track track) {
        delegate.updateTrack(track, this); // update model
        graphView.drawTrack(track);
        //Statistics
        final TrackStatistics stats = track.getStatistics();
        statTime.setText(track.getFormattedElapsedTime());
        statMinMaxAvg.setText(stats.getFormattedMinMAxAvgValue());
        statDistance.setText(stats.getFormattedCoveredDistance());
    }

    private void changeToMeasuringStatus() {
        status.setText(STATUS_MEASURING);
        status.setTextColor(new NTColor(63, 81, 181).getRGBValue());
        btnStart.setVisibility(View.GONE);
        btnPauseResume.setVisibility(View.VISIBLE);
        btnPauseResume.setEnabled(true);
        btnStop.setVisibility(View.VISIBLE);
        btnStop.setEnabled(true);
    }

    private void changeToPausedStatus() {

        splView.setDefaultColor();
        btnPauseResume.setText(getResources().getText(R.string.action_resume_measure));
        btnPauseResume.setEnabled(true);
        status.setText(STATUS_PAUSED);
        status.setTextColor(new NTColor(189, 189, 189).getRGBValue());
    }

    @Override
    public void notify(Notification n) {
        changeToPausedStatus();
    }


}

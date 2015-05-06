package net.noisetube.app.ui.delegate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.widget.TextView;

import net.noisetube.R;
import net.noisetube.api.NTClient;
import net.noisetube.api.audio.calibration.Calibration;
import net.noisetube.api.model.Track;
import net.noisetube.api.ui.TrackUI;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.ui.LoginActivity;
import net.noisetube.app.ui.Toaster;
import net.noisetube.app.ui.TrackSummaryDialog;
import net.noisetube.app.ui.model.MeasureModel;
import net.noisetube.app.util.NTUtils;

/**
 * @author Humberto
 */
public class MeasureViewModel {


    private static MeasureViewModel instance;
    Track track;
    private MeasureModel model;
    private Context ctx;


    public MeasureViewModel(TrackUI ui, Context ctx) {
        this.model = new MeasureModel(ui);
        this.ctx = ctx;
        instance = this;

    }

    public static MeasureViewModel getInstance() {
        return instance;
    }

    public void invokeStartAction(final Activity act) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                AndroidPreferences pref = AndroidPreferences.getInstance();

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showCalibrationMessage(act.getResources());
                    }
                });

                if (pref.isTosAccepted() && pref.isAuthenticated() && !NTUtils.supportsInternetAccess()) {

                    pref.setSavingModeAndPersist(2);
//                    pref.setAlwaysUseBatchModeForHTTP(false);
                    model.startMeasuring();
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toaster.displayToast(String.valueOf(act.getText(R.string.warning_msg_internet)));
                            Toaster.displayToast(String.valueOf(act.getText(R.string.warning_msg_microphone)));
                        }
                    });

                } else if (pref.isTosAccepted() && !pref.isAuthenticated()) {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Dialog dialog = buildAlertLoginDialog(act);
                            dialog.show();
                        }
                    });

                } else {
                    model.startMeasuring();
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toaster.displayToast(String.valueOf(act.getText(R.string.warning_msg_microphone)));
                        }
                    });
                }


                return null;
            }
        };
        task.execute();

    }

    public void invokePauseOrResumeAction() {
        new PauseOrResumeMeasureTask().execute();

    }

    public void invokePauseAction() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                model.pauseMeasuring();
                return null;
            }
        };
        task.execute();
    }

    public void invokeResumeAction() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                model.pauseMeasuring();
                return null;
            }
        };
        task.execute();
    }

    public void invokeStopAction(final Activity act) {
        track = model.getTrack();
        if (track != null) {
            track.pause(true);

            if (track.getStatistics().getNumMeasurements() < 30 && AndroidPreferences.getInstance().getSavingMode() != 0) {

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LayoutInflater layoutInflater = (LayoutInflater) act.getSystemService(
                                Context.LAYOUT_INFLATER_SERVICE);

                        TextView body = (TextView) layoutInflater.inflate(R.layout.dialog_text_view, null);
                        body.setText(act.getText(R.string.warning_msg_measure_dialog));

                        Dialog dialog = new AlertDialog.Builder(act).setView(body)
                                .setPositiveButton(R.string.continue_measuring,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                track.resume();
                                            }
                                        }
                                ).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                        new StopMeasureTask().execute(act);
                                    }
                                })
                                .create();

                        dialog.show();
                    }
                });
            } else {
                new StopMeasureTask().execute(act);
            }
        }
    }

    public Track getTrack() {
        return model.getTrack();
    }

    public boolean isPaused() {

        Track t = model.getTrack();
        if (t != null) {
            return t.isPaused();
        } else {
            return false;
        }

    }

    public boolean isRunning() {

        Track t = model.getTrack();
        if (t != null) {
            return t.isRunning();
        } else {
            return false;
        }

    }

    public void showCalibrationMessage(Resources resources) {
        AndroidPreferences pref = AndroidPreferences.getInstance();
        if (!pref.isCalibrationStatusDone()) {
            Calibration calibration = NTClient.getInstance().getPreferences().getCalibration();
            if (calibration == null || calibration.getEffeciveCredibilityIndex() > Calibration.CREDIBILITY_INDEX_G)
                Toaster.displayToast(resources.getString(R.string.not_calibrated_msg));
            else if (calibration.getEffeciveCredibilityIndex() == Calibration.CREDIBILITY_INDEX_G)
                Toaster.displayToast(resources.getString(R.string.not_calibrated_brand_msg));
            else
                Toaster.displayToast(resources.getString(R.string.calibrated_msg));

            pref.markCalibrationStatusDone();
        }

    }


    public void freeResources() {
        model.unregisterListener();
        model = null;
    }

    public void updateTrack(Track track, TrackUI ui) {
        track.addTrackUIListener(ui);
        model.setTrack(track);
    }

    public Dialog buildAlertLoginDialog(final Activity activity) {


        AndroidPreferences pref = AndroidPreferences.getInstance();
        Dialog dialog;

        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        TextView body = (TextView) layoutInflater.inflate(R.layout.dialog_text_view, null);


        if (pref.isTosAccepted() && pref.isLocationRequired()) {
            body.setText(activity.getText(R.string.required_msg_login_dialog));
            dialog = new AlertDialog.Builder(activity).setView(body)
                    .setPositiveButton("Accept",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(activity, LoginActivity.class);
                                    activity.startActivity(intent);
                                    activity.finish();

                                }
                            }
                    )
                    .create();

        } else if (pref.isTosAccepted() && pref.isLoginRequired()) {
            body.setText(activity.getText(R.string.required_msg_login_dialog));
            dialog = new AlertDialog.Builder(activity).setView(body)
                    .setPositiveButton("Accept",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(activity, LoginActivity.class);
                                    activity.startActivity(intent);
                                    activity.finish();

                                }
                            }
                    )
                    .create();

        } else {

            body.setText(activity.getText(R.string.warning_msg_login_dialog));
            dialog = new AlertDialog.Builder(activity).setView(body)
                    .setPositiveButton("Accept",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(activity, LoginActivity.class);
                                    activity.startActivity(intent);
                                    activity.finish();

                                }
                            }
                    ).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                            model.startMeasuring();
                            Toaster.displayToast(String.valueOf(activity.getText(R.string.warning_msg_microphone)));

                        }
                    })
                    .create();
        }


        return dialog;

    }

    class PauseOrResumeMeasureTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            return model.togglePause();
        }

        @Override
        protected void onPostExecute(Boolean paused) {
            super.onPostExecute(paused);
//            if (!paused) {
//            Track t = model.getTrack();
//                int up = t.getBeginSegment();
//                int to = t.getEndSegment() - 1;
//                TagMeasureDialog tagMeasureDialog = new TagMeasureDialog(ctx, t, up, to);
//                tagMeasureDialog.show();
//            }
        }
    }

    class StopMeasureTask extends AsyncTask<Activity, Void, Activity> {
        @Override
        protected Activity doInBackground(Activity... params) {
            final Activity act = params[0];

            final boolean saving = track.getSaver() != null;

            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TrackSummaryDialog trackSummaryDialog = new TrackSummaryDialog(ctx, track, saving);
                    trackSummaryDialog.show();
                }
            });

            track.stop();

            return act;
        }

        @Override
        protected void onPostExecute(Activity act) {

            super.onPostExecute(act);

            if (act != null && track.getSaver() != null) {

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        TrackSummaryDialog instance = TrackSummaryDialog.getInstance();
                        if (instance != null) {
                            instance.stopWaiting(!track.getSaver().isRunning());
                        }

                    }
                });
            }

        }
    }


}

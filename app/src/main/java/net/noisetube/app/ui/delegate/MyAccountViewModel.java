package net.noisetube.app.ui.delegate;

import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;

import net.noisetube.R;
import net.noisetube.api.exception.AuthenticationException;
import net.noisetube.api.io.NTWebAPI;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.ui.Toaster;
import net.noisetube.app.util.AccountUtils;

import java.io.File;

/**
 * @author Humberto
 */

public class MyAccountViewModel {

    private NTWebAPI ntWebAPI;
    private Activity activity;

    public MyAccountViewModel(Activity activity) {
        ntWebAPI = new NTWebAPI(AndroidPreferences.getInstance().getAccount());
        this.activity = activity;
    }

    public void changePassword(final CharSequence oldPassword, final CharSequence newPassword, final CharSequence confirmPassword) {

        if (newPassword.toString().equals(confirmPassword.toString())) {
            if (AccountUtils.validatePassword(newPassword.toString())) {
                AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        boolean response = false;
                        try {
                            if (ntWebAPI.ping()) {
                                response = ntWebAPI.changePassword(oldPassword.toString(), newPassword.toString());
                            } else {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toaster.displayToast(activity.getResources().getString(R.string.server_down));
                                    }
                                });
                            }

                        } catch (AuthenticationException e) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toaster.displayToast(activity.getResources().getString(R.string.password_not_changed_msg));
                                }
                            });
                        }
                        return response;
                    }

                    @Override
                    protected void onPostExecute(Boolean response) {
                        super.onPostExecute(response);
                        if (response) {
                            Toaster.displayToast(activity.getResources().getString(R.string.password_changed_msg));
                        } else {
                            Toaster.displayToast(activity.getResources().getString(R.string.password_not_changed_msg));
                        }
                    }
                };
                task.execute();
            } else {
                Toaster.displayToast(activity.getResources().getString(R.string.wrong_password_values_msg));
            }

        } else {
            Toaster.displayToast(activity.getResources().getString(R.string.password_not_confirmed_msg));
        }


    }

    public void updateUserAvatar(final File file, final Resources resources) {

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean response = false;
                try {
                    if (ntWebAPI.ping()) {
                        response = ntWebAPI.updateUserAvatar(file);
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toaster.displayToast(activity.getResources().getString(R.string.server_down));
                            }
                        });
                    }

                } catch (AuthenticationException e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toaster.displayToast(activity.getResources().getString(R.string.avatar_not_changed_msg));
                        }
                    });
                }
                return response;
            }

            @Override
            protected void onPostExecute(Boolean response) {
                super.onPostExecute(response);
                if (response) {
                    Toaster.displayToast(resources.getString(R.string.avatar_changed_msg));
                } else {
                    Toaster.displayToast(resources.getString(R.string.avatar_not_changed_msg));
                }
            }
        };
        task.execute();

    }
}

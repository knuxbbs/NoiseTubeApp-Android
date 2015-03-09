package net.noisetube.app.ui.delegate;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.noisetube.R;
import net.noisetube.api.config.NTAccount;
import net.noisetube.api.exception.AuthenticationException;
import net.noisetube.api.io.NTWebAPI;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.ui.LoginActivity;
import net.noisetube.app.ui.MainActivity;
import net.noisetube.app.ui.RegisterActivity;
import net.noisetube.app.ui.Toaster;
import net.noisetube.app.util.AccountUtils;
import net.noisetube.app.util.DialogUtils;

/**
 * @author Humberto
 */
public class RegisterViewModel {

    private RegisterActivity activity;
    private NTWebAPI ntWebAPI;
    private int errorColor, defaultColor;


    public RegisterViewModel(RegisterActivity activity) {
        this.activity = activity;
        ntWebAPI = new NTWebAPI(AndroidPreferences.getInstance().getAccount());
        errorColor = -2937041;
        defaultColor = -570425344;
    }

    private void showError(int id, String msg) {
        TextView view = (TextView) activity.findViewById(id);
        view.setText(msg);
        view.setVisibility(View.VISIBLE);

    }

    private void hideError(int id) {

        TextView view = (TextView) activity.findViewById(id);
        view.setVisibility(View.GONE);

    }


    private void changeErrorBackground(int id, boolean flag) {
        EditText view = (EditText) activity.findViewById(id);
        if (flag) {
            view.setTextColor(errorColor);

        } else {
            view.setTextColor(defaultColor);
        }

    }

    public void registerUser(final Editable userText, final Editable passwordText, final Editable confirmPasswordText, final Editable userMailText, final Editable hometownText) {


        final String userName = userText.toString();
        final String pass = passwordText.toString();
        final String confirmPass = confirmPasswordText.toString();
        final String homeTown = hometownText.toString();
        boolean flag = false;

        if (!AccountUtils.validateUser(userName)) {
            flag = true;
            showError(R.id.username_error, "This field should have at least 3 characters.");
            changeErrorBackground(R.id.username, true);
        } else {
            hideError(R.id.username_error);
            changeErrorBackground(R.id.username, false);
        }

        if (!AccountUtils.validatePassword(pass)) {
            flag = true;
            showError(R.id.password_error, "This field should have at least 6 characters and no contain symbols.");
            changeErrorBackground(R.id.password, true);

        } else {
            if (!pass.equals(confirmPass)) {
                showError(R.id.confirm_password_error, "These passwords don't match.");
                hideError(R.id.password_error);
                changeErrorBackground(R.id.password, true);
                changeErrorBackground(R.id.confirm_password, true);
            } else {
                changeErrorBackground(R.id.confirm_password, false);
                changeErrorBackground(R.id.password, false);
                hideError(R.id.confirm_password_error);
            }

        }


        if (!AccountUtils.isValidEmail(userMailText)) {
            flag = true;
            showError(R.id.user_email_error, "This email is not correct.");

            changeErrorBackground(R.id.user_email, true);
        } else {
            hideError(R.id.user_email_error);
            changeErrorBackground(R.id.user_email, false);
        }

        if (!AccountUtils.isValidHometown(homeTown)) {
            flag = true;
            showError(R.id.hometown_error, "This hometown is not correct. Eg. Brussels, Belgium");

            changeErrorBackground(R.id.hometown, true);
        } else {
            hideError(R.id.hometown_error);
            changeErrorBackground(R.id.hometown, false);
        }


        if (flag) {
            activity.getRegisterButton().setEnabled(true);

        } else {

            final DialogUtils.LoadingDialog dialog = DialogUtils.showLoading(activity);
            AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    boolean response = false;
                    try {
                        if (!AccountUtils.isUserNameAvailable(userName)) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showError(R.id.username_error, "This username is not available.");
                                    changeErrorBackground(R.id.username, true);
                                    activity.getRegisterButton().setEnabled(true);
                                    dialog.dismiss();
                                }
                            });

                        } else {
                            String userAPIkey = ntWebAPI.registerUser(userName, pass, userMailText.toString(), homeTown);
                            if (userAPIkey != null) {

                                AndroidPreferences.getInstance().setAccount(new NTAccount(userName, userAPIkey));
                                response = true;
                            }
                        }
                    } catch (final AuthenticationException e) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Toaster.displayShortToast("Error: " + e.getMessage());
                                activity.getRegisterButton().setEnabled(true);
                            }
                        });

                    }
                    return response;
                }

                @Override
                protected void onPostExecute(Boolean response) {
                    super.onPostExecute(response);

                    if (response) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                LoginActivity parent = LoginActivity.getInstance();
                                if (parent != null) {
                                    parent.finish();
                                }
                                Intent intent = new Intent(activity, MainActivity.class);
                                activity.startActivity(intent);
                                activity.finish();

                            }
                        });
                    }
                }
            };
            task.execute();

        }


    }
}

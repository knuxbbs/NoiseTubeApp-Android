package net.noisetube.app.ui.delegate;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.EditText;

import net.noisetube.R;
import net.noisetube.api.config.Preferences;
import net.noisetube.api.util.Logger;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.ui.LoginActivity;
import net.noisetube.app.ui.MainActivity;
import net.noisetube.app.ui.Toaster;
import net.noisetube.app.ui.model.LoginModel;
import net.noisetube.app.util.AccountUtils;
import net.noisetube.app.util.DialogUtils;

/**
 * Created by humberto on 05/01/15.
 */
public class UserLoginTask extends AsyncTask<Void, String, Integer> {

    DialogUtils.LoadingDialog dialog;
    private EditText user;
    private EditText pass;
    private Logger log = Logger.getInstance();
    private LoginModel model;
    private LoginActivity activity;

    public UserLoginTask(EditText user, EditText pass, LoginModel model, LoginActivity activity) {
        this.user = user;
        this.pass = pass;
        this.model = model;
        this.activity = activity;


    }

    @Override
    protected Integer doInBackground(Void... params) {

        dialog = DialogUtils.showLoading(activity);

        String userName = user.getText().toString().trim();
        String userPass = pass.getText().toString().trim();

        if (AccountUtils.validateCredentials(userName, userPass)) {
            return model.login(userName, userPass);
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toaster.displayToast(activity.getResources().getString(R.string.wrong_credentials_msg));
                }
            });

        }

        return -1; // invalid credentials
    }

    @Override
    protected void onPostExecute(final Integer result) {
        Intent intent;
        if (dialog != null && dialog.isVisible()) {
            dialog.dismiss();
        }

        switch (result) {
            case LoginModel.LOGIN_OK:
                Toaster.displayShortToast(activity.getResources().getString(R.string.login_successful));
                Toaster.displayToast(activity.getResources().getString(R.string.setting_info));
                intent = new Intent(activity.getBaseContext(), MainActivity.class);
                AndroidPreferences.getInstance().setSavingMode(1);
                AndroidPreferences.getInstance().setAlsoSaveToFileWhenInHTTPMode(true);
                activity.startActivity(intent);
                activity.finish();

                break;
            case LoginModel.LOGIN_FAILED:
                Toaster.displayToast(activity.getResources().getString(R.string.wrong_username_password));
                break;
            case LoginModel.CONNECTION_EXCEPTION:
                String store = getSavingModeValue();
                String msg = activity.getResources().getString(R.string.connection_problem);
                Toaster.displayToast(msg + " " + store);
                intent = new Intent(activity.getBaseContext(), MainActivity.class);
                activity.startActivity(intent);
                activity.finish();
                break;
            default: // invalid credentials
                Toaster.displayShortToast(activity.getResources().getString(R.string.invalid_credentials));
        }

    }

    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
//        Toaster.displayToast((values[0]));
    }


    private String getSavingModeValue() {
        return (model.getSavingMode() == Preferences.SAVE_FILE ? activity.getResources().getString(R.string.local_store) : activity.getResources().getString(R.string.no_store));
    }


}


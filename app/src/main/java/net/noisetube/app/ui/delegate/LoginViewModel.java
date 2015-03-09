package net.noisetube.app.ui.delegate;

import android.os.AsyncTask;
import android.widget.EditText;

import net.noisetube.app.ui.LoginActivity;
import net.noisetube.app.ui.model.LoginModel;

/**
 * @author humberto
 */
public class LoginViewModel {

    private LoginActivity activity;
    private LoginModel model;


    public LoginViewModel(LoginActivity activity) {
        this.activity = activity;
        this.model = new LoginModel();
    }

    public void logIn(EditText user, EditText pass) {
        new UserLoginTask(user, pass, model, activity).execute();
    }


    public void skipSignIn() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... fields) {
                model.updateAppPreferences();
                return null;
            }
        };

        task.execute();
    }


}

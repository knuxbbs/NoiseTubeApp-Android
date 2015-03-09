package net.noisetube.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.noisetube.R;
import net.noisetube.api.model.Track;
import net.noisetube.app.core.AndroidNTService;
import net.noisetube.app.ui.delegate.LoginViewModel;
import net.noisetube.app.util.DialogUtils;
import net.noisetube.app.util.NTUtils;


/**
 *
 */
public class LoginActivity extends Activity {
    public static final int REQUEST_CODE = 10;
    private static LoginActivity instance;
    private LoginViewModel delegate;
    // UI references.
    private EditText user;
    private EditText password;
    private Button btnlogIn, btnRegister, btnSkip;

    public static LoginActivity getInstance() {
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        delegate = new LoginViewModel(this);
        user = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.password);

        btnlogIn = (Button) findViewById(R.id.btn_log_in);
        btnRegister = (Button) findViewById(R.id.btn_register);
        btnSkip = (Button) findViewById(R.id.btn_skip);

        instance = this;

        Track track = AndroidNTService.getInstance().getTrack();

        if (track != null && track.isRunning()) {
            track.stop();
            Toaster.displayShortToast(String.valueOf(getResources().getText(R.string.msg_stop_measure)));
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!NTUtils.supportsInternetAccess()) {

            btnRegister.setEnabled(false);
            btnRegister.setTextColor(Color.LTGRAY);
            btnlogIn.setEnabled(false);
            btnlogIn.setTextColor(Color.LTGRAY);
            DialogUtils.showInternetDialog(this);
        } else {
            btnRegister.setEnabled(true);
            btnRegister.setTextColor(Color.WHITE);
            btnlogIn.setEnabled(true);
            btnlogIn.setTextColor(Color.WHITE);
        }
    }


    public void onClickLogIn(View v) {
        delegate.logIn(user, password);
    }

    public void onClickRegister(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void onClickSkipSignIn(View v) {
        delegate.skipSignIn();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}




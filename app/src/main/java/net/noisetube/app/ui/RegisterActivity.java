package net.noisetube.app.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.noisetube.R;
import net.noisetube.app.ui.delegate.RegisterViewModel;


public class RegisterActivity extends SimpleActionBarActivity {

    private EditText userName, password, confirmPassword, userMail, hometown;
    private RegisterViewModel delegate;
    private Button register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        delegate = new RegisterViewModel(this);

        userName = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confirm_password);
        userMail = (EditText) findViewById(R.id.user_email);
        hometown = (EditText) findViewById(R.id.hometown);

        Toolbar toolbar = getActionBarToolbar();
        toolbar.setTitle(R.string.title_activity_register);
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateUpToFromChild(RegisterActivity.this,
                        IntentCompat.makeMainActivity(new ComponentName(RegisterActivity.this,
                                LoginActivity.class)));
            }
        });

        register = (Button) findViewById(R.id.btn_create_account);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register.setEnabled(false);

                delegate.registerUser(userName.getText(), password.getText(), confirmPassword.getText(), userMail.getText(), hometown.getText());
            }
        });

    }

    public Button getRegisterButton() {
        return register;
    }
}

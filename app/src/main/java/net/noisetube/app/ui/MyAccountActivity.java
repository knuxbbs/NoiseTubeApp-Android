package net.noisetube.app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.noisetube.R;
import net.noisetube.api.NTClient;
import net.noisetube.api.audio.calibration.Calibration;
import net.noisetube.api.config.NTAccount;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.ui.delegate.MyAccountViewModel;
import net.noisetube.app.ui.widget.BezelImageView;
import net.noisetube.app.util.ImageUtils;

import java.io.File;

public class MyAccountActivity extends BaseActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    private BezelImageView avatar;
    private TextView user_name;
    private TextView user_api_key;
    private TextView phone_status;
    private Button changeAvatar;
    private Button changePassword;
    private TextView newPassword;
    private TextView repeatPassword;
    private TextView oldPassword;
    private MyAccountViewModel delegate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        delegate = new MyAccountViewModel(this);


        user_name = (TextView) findViewById(R.id.user_name);
        user_api_key = (TextView) findViewById(R.id.user_api_key);
        phone_status = (TextView) findViewById(R.id.user_phone_status);

        changePassword = (Button) findViewById(R.id.btnChangePassword);
        changeAvatar = (Button) findViewById(R.id.btnChangeAvatar);

        newPassword = (TextView) findViewById(R.id.new_password);
        repeatPassword = (TextView) findViewById(R.id.repeat_new_password);
        oldPassword = (TextView) findViewById(R.id.old_password);

        avatar = (BezelImageView) findViewById(R.id.user_profile_image);

        AndroidPreferences pref = AndroidPreferences.getInstance();
        NTAccount account = pref.getAccount();
        user_name.setText(account.getUsername());
        user_api_key.setText(account.getAPIKey());
        Bitmap bm = account.getAvatar();
        if (bm != null) {
            avatar.setImageBitmap(bm);
        }

        Calibration calibration = NTClient.getInstance().getPreferences().getCalibration();
        int credibilityIndex = calibration.getEffeciveCredibilityIndex();
        if (calibration == null || credibilityIndex > Calibration.CREDIBILITY_INDEX_G || credibilityIndex == Calibration.CREDIBILITY_INDEX_G)
            phone_status.setText(getString(R.string.phone_not_calibrated));
        else
            phone_status.setText(getString(R.string.phone_calibrated));


        changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegate.changePassword(oldPassword.getText(), newPassword.getText(), repeatPassword.getText());
                oldPassword.setText("");
                newPassword.setText("");
                repeatPassword.setText("");
            }
        });


    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            final Bitmap bm = (Bitmap) data.getExtras().get("data");
            avatar.setImageBitmap(bm);
            pref.getAccount().setAvatar(bm);
            updateNavDrawerAvatar();

            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    String path = ImageUtils.saveToInternalSorage(bm);
                    delegate.updateUserAvatar(new File(path), getResources());
                    return null;
                }
            };
            task.execute();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        // we only have a nav drawer if we are in top-level Explore mode.
        return NAVDRAWER_ITEM_MY_ACCOUNT;
    }
}

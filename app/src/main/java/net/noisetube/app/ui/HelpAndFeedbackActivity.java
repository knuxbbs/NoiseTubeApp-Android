package net.noisetube.app.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.noisetube.R;

public class HelpAndFeedbackActivity extends SimpleActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_and_feedback);
        Toolbar toolbar = getActionBarToolbar();
        toolbar.setTitle(R.string.title_activity_settings);
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK, null);
                finish();
            }
        });
    }
}

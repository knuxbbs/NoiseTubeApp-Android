package net.noisetube.app.ui;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import net.noisetube.R;
import net.noisetube.app.core.AndroidNTService;

/**
 * @author Humberto
 */
public class SimpleActionBarActivity extends ActionBarActivity {

    // Primary toolbar
    protected Toolbar mActionBarToolbar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (AndroidNTService.getInstance() != null)
            AndroidNTService.getInstance().destroyService();

    }
}

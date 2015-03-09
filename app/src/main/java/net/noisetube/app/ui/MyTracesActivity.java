package net.noisetube.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import net.noisetube.R;
import net.noisetube.app.ui.delegate.MyTracesViewModel;

public class MyTracesActivity extends BaseActivity {

    private MyTracesViewModel delegate;
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_traces);

        emptyMessage = (TextView) findViewById(R.id.empty_msg);
        delegate = new MyTracesViewModel(this);
        delegate.populateTraceItems();

    }

    public void hideEmptyMessage() {
        emptyMessage.setVisibility(View.GONE);
    }


    @Override
    protected int getSelfNavDrawerItem() {
        // we only have a nav drawer if we are in top-level Explore mode.
        return NAVDRAWER_ITEM_MY_TRACES;
    }

}

package net.noisetube.app.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;

import net.noisetube.R;

public class MainActivity extends BaseActivity {

    private static MainActivity instance;

    public static MainActivity getIntance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.measuring_container, new MeasureFragment())
                    .commit();

        }

        instance = this;

    }


    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_MEASURE;
    }


}

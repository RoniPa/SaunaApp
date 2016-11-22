package fi.jamk.saunaapp;

import android.os.Bundle;
import android.os.Parcel;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import fi.jamk.saunaapp.models.Sauna;

public class SaunaDetailsActivity extends BaseActivity {
    private final static String TAG = "SaunaDetailsActivity";

    private Sauna sauna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sauna_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Parcel saunaParcel = savedInstanceState.getParcelable(DETAILS_SAUNA);
        sauna = Sauna.CREATOR.createFromParcel(saunaParcel);
        Log.d(TAG, "Details sauna is "+sauna.getName());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}

package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.fragments.RateSaunaFragment;
import fi.jamk.saunaapp.fragments.SaunaListFragment;
import fi.jamk.saunaapp.models.Sauna;

public class SaunaDetailsActivity extends BaseActivity implements RateSaunaFragment.OnFragmentInteractionListener {
    private final static String TAG = "SaunaDetailsActivity";

    private FirebaseStorage mFirebaseStorage;

    private Sauna sauna;
    private TextView detailsTextView;
    private ImageView mToolbarBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sauna_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        sauna = intent.getParcelableExtra(DETAILS_SAUNA);
        setTitle(sauna.getName());

        mFirebaseStorage = FirebaseStorage.getInstance();
        mToolbarBackground = findViewById(R.id.details_toolbar_background);
        if (sauna.getPhotoPath() != null) {
            StorageReference imageRef = mFirebaseStorage
                    .getReference(sauna.getPhotoPath());

            Glide.with(SaunaDetailsActivity.this)
                    .using(new FirebaseImageLoader())
                    .load(imageRef)
                    .into(mToolbarBackground);
        }

        detailsTextView = (TextView) findViewById(R.id.details_text);
        detailsTextView.setText(sauna.getDescription());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

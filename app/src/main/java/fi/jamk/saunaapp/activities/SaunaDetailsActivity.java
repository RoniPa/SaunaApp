package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.fragments.RateSaunaFragment;
import fi.jamk.saunaapp.models.Rating;
import fi.jamk.saunaapp.models.Sauna;
import fi.jamk.saunaapp.services.RatingService;

public class SaunaDetailsActivity extends BaseActivity implements RateSaunaFragment.OnFragmentInteractionListener {
    private final static String TAG = "SaunaDetailsActivity";

    private FirebaseStorage mFirebaseStorage;
    private RatingService ratingService;

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

        ((RatingBar)findViewById(R.id.sauna_rating_bar)).setRating((float)sauna.getRating());

        int rCount = sauna.getRatingCount();
        String txt = rCount > 499 ? "499+" : ""+rCount;
        ((TextView)findViewById(R.id.rating_count_text_view)).setText(getString(R.string.rating_count, txt));

        // Check if user has rated sauna.
        ratingService = RatingService.newInstance();
        ratingService.hasRated(sauna.getId())
            .subscribe(hasRated -> {
                RateSaunaFragment frag = (RateSaunaFragment)getSupportFragmentManager().findFragmentById(R.id.rateSaunaFragment);

                // Get rating for Fragment
                ratingService.getRating(sauna.getId()).subscribe(frag::setRating);
                // Disable rating if user has already rated sauna
                frag.setHasRated(hasRated);

            }, Throwable::printStackTrace);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mToolbarBackground = findViewById(R.id.details_toolbar_background);
        if (sauna.getPhotoPath() != null) {
            StorageReference imageRef = mFirebaseStorage
                    .getReference(sauna.getPhotoPath());

            Glide.with(SaunaDetailsActivity.this)
                    .using(new FirebaseImageLoader())
                    .load(imageRef)
                    .into(mToolbarBackground);

            // Get view height from screen width
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = displaymetrics.widthPixels;

            // Square dimensions should have been forced for thumb pic,
            // so we can assume the height to match width
            mToolbarBackground.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(width, width));
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

    /**
     * Save rating.
     *
     * @param rating
     */
    @Override
    public void onFragmentInteraction(final Rating rating) {
        rating.setSaunaId(sauna.getId());
        ratingService.saveRating(rating);
    }
}

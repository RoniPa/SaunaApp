package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.fragments.RateSaunaFragment;
import fi.jamk.saunaapp.models.Rating;
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
    public void onFragmentInteraction(final Rating rating) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference mMetaReference = db.getReference("_hasRated/" + rating.getUser());
        DatabaseReference mReviewReference = db.getReference("ratings");

        rating.setId(mReviewReference.push().getKey());
        rating.setSaunaId(sauna.getId());

        // Save knowledge that this user has rated this sauna already.
        // Stored as { userId { saunaId => ratingId } }
        mMetaReference.child(sauna.getId()).setValue(rating.getId());
        mReviewReference.child(rating.getId()).setValue(rating);

        // Calculate & update rating for sauna
        DatabaseReference mSaunaReference = db.getReference("saunas/" + sauna.getId());
        mSaunaReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Sauna s = mutableData.getValue(Sauna.class);
                if (s == null) {
                    return Transaction.success(mutableData);
                }

                s.setRatingCount(s.getRatingCount() + 1);
                double ratingDelta = rating.getRating() - s.getRating();
                s.setRating(s.getRating() + (ratingDelta / s.getRatingCount()));

                // Set value and report transaction success
                mutableData.setValue(s);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
}

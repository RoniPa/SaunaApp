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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.fragments.RateSaunaFragment;
import fi.jamk.saunaapp.models.Conversation;
import fi.jamk.saunaapp.models.Rating;
import fi.jamk.saunaapp.models.Sauna;
import fi.jamk.saunaapp.services.RatingService;

public class SaunaDetailsActivity extends BaseActivity implements RateSaunaFragment.OnFragmentInteractionListener {
    private final static String TAG = "SaunaDetailsActivity";

    private FirebaseStorage mFirebaseStorage;
    private RatingService ratingService;

    /**
     * If user already has existing conversation
     * with the sauna owner we store the id.
     */
    private Conversation existingConversation;
    private FirebaseUser mUser;

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
        toolbar.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        sauna = intent.getParcelableExtra(DETAILS_SAUNA);
        setTitle(sauna.getName());

        mUser = FirebaseAuth.getInstance().getCurrentUser();

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

        FirebaseDatabase.getInstance().getReference("conversations")
            .child(mUser.getUid())
            .orderByChild("target")
            .equalTo(sauna.getOwner())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        dataSnapshot.getChildren()
                            .forEach(dataSnapshot1 -> existingConversation = dataSnapshot1.getValue(Conversation.class));
                    }
                }
                @Override public void onCancelled(DatabaseError databaseError) {}
            });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent1 = new Intent(SaunaDetailsActivity.this, MessageListActivity.class);

            if (existingConversation != null) {
                intent1.putExtra(ConversationListActivity.CONV_DETAIL_ITEM, existingConversation);
            } else {
                Conversation conv = new Conversation();
                conv.setTarget(sauna.getOwner());
                conv.setTargetName(sauna.getOwnerName());
                conv.setTouched(new Date());
                intent1.putExtra(ConversationListActivity.CONV_DETAIL_ITEM, conv);
            }

            startActivity(intent1);
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

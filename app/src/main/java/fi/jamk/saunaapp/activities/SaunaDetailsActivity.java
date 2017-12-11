package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.fragments.RateSaunaFragment;
import fi.jamk.saunaapp.fragments.RatingsFragment;
import fi.jamk.saunaapp.models.Conversation;
import fi.jamk.saunaapp.models.Rating;
import fi.jamk.saunaapp.models.Sauna;
import fi.jamk.saunaapp.services.RatingService;
import fi.jamk.saunaapp.services.UserLocationService;
import fi.jamk.saunaapp.util.MapUtils;

public class SaunaDetailsActivity extends BaseActivity implements
        OnMapReadyCallback,
        RateSaunaFragment.OnFragmentInteractionListener {
    private final static String TAG = "SaunaDetailsActivity";
    private final static float MAP_ZOOM = 12.0f;

    private FirebaseStorage mFirebaseStorage;
    private RatingService ratingService;
    private MapView saunaMapView;
    private GoogleMap mMap;

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
        toolbar.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        sauna = intent.getParcelableExtra(DETAILS_SAUNA);
        setTitle(sauna.getName());

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        ((RatingBar)findViewById(R.id.sauna_rating_bar)).setRating((float)sauna.getRating());

        int rCount = sauna.getRatingCount();
        ((TextView)findViewById(R.id.rating_count_text_view))
                .setText(getString(R.string.rating_count, rCount > 499 ? "499+" : ""+rCount));

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

        getConversationIfExists();
        initRatingsFragment();
        initMap(savedInstanceState);
        initFab();
    }

    @Override
    public void onResume() {
        getConversationIfExists();
        saunaMapView.onResume();
        super.onResume();
    }
    @Override
    public void onPause() {
        saunaMapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        saunaMapView.onStop();
        super.onStop();
    }

    @Override
    public void onStart() {
        saunaMapView.onStart();
        super.onStart();
    }
    @Override
    public void onDestroy() {
        saunaMapView.onDestroy();
        super.onDestroy();
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

    /**
     * Map ready callback.
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setScrollGesturesEnabled(false);

        LatLng latLng = new LatLng(sauna.getLatitude(), sauna.getLongitude());
        googleMap.addMarker(MapUtils.getSaunaMarker(latLng, null));
        MapUtils.centerMap(latLng, MAP_ZOOM, googleMap);
        saunaMapView.onResume();
    }

    /**
     * Checks for existing conversation and sets it to property if found.
     */
    private void getConversationIfExists() {
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
    }

    /**
     * Initiate {@link fi.jamk.saunaapp.fragments.RatingsFragment}.
     * For latest sauna ratings.
     */
    private void initRatingsFragment() {
        Query ratingsQuery = FirebaseDatabase.getInstance().getReference("ratings")
                .orderByChild("saunaId")
                .equalTo(sauna.getId())
                .limitToFirst(5);

        RatingsFragment ratingsFragment = RatingsFragment.newInstance(ratingsQuery);
        getFragmentManager().beginTransaction().add(R.id.wrapper, ratingsFragment).commit();
    }

    private void initMap(Bundle savedInstanceState) {
        saunaMapView = (MapView) findViewById(R.id.saunaMapView);
        saunaMapView.onCreate(savedInstanceState);
        saunaMapView.getMapAsync(this);
    }

    private void initFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent1 = new Intent(SaunaDetailsActivity.this, MessageListActivity.class);

            if (existingConversation != null) {
                intent1.putExtra(ConversationListActivity.CONV_DETAIL_ITEM, existingConversation);
            } else {
                Conversation conv = new Conversation();
                conv.setTarget(sauna.getOwner());
                conv.setTargetName(sauna.getOwnerName());

                // This forces the conversation to top priority.
                // It'll be updated to the server timestamp later.
                conv.setTouched(Double.MAX_VALUE);

                intent1.putExtra(ConversationListActivity.CONV_DETAIL_ITEM, conv);
            }

            startActivity(intent1);
        });
    }
}

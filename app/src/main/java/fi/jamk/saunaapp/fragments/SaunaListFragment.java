package fi.jamk.saunaapp.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import fi.jamk.saunaapp.activities.BaseActivity;
import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.activities.SaunaDetailsActivity;
import fi.jamk.saunaapp.models.Sauna;
import fi.jamk.saunaapp.services.UserLocationService;
import fi.jamk.saunaapp.util.ChildConnectionNotifier;
import fi.jamk.saunaapp.util.RecyclerItemClickListener;
import fi.jamk.saunaapp.util.StringFormat;
import fi.jamk.saunaapp.viewholders.SaunaViewHolder;

/**
 * A {@link Fragment} subclass that displays
 * a list of nearby Saunas.
 *
 * todo: Implement DataBinding to list
 *
 * Parent Activity must implement the {@link ChildConnectionNotifier} interface.
 */
public class SaunaListFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, LocationListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "SaunaListFragment";

    private UserLocationService mUserLocationService;
    private FirebaseStorage mFirebaseStorage;

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<Sauna, SaunaViewHolder>
            mFirebaseAdapter;
    private RecyclerView mSaunaRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    public SaunaListFragment() {}

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SaunaListFragment newInstance(int sectionNumber) {
        SaunaListFragment fragment = new SaunaListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserLocationService = UserLocationService.newInstance(
                ((ChildConnectionNotifier)getActivity()).getApiClient());

        mFirebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_list, container, false);

        ((ChildConnectionNotifier)getActivity()).addConnectionListener(this);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mSaunaRecyclerView = (RecyclerView) rootView.findViewById(R.id.saunaRecyclerView);

        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setStackFromEnd(true);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        mUserLocationService.removeListener(this);
        ((ChildConnectionNotifier)getActivity()).removeConnectionListener(this);
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Sauna, SaunaViewHolder>(
                Sauna.class,
                R.layout.sauna_item,
                SaunaViewHolder.class,
                mFirebaseDatabaseReference.child(BaseActivity.SAUNAS_CHILD)) {

            @Override
            protected void populateViewHolder(
                    SaunaViewHolder viewHolder,
                    Sauna sauna,
                    int position) {

                Location userPos = UserLocationService.getCachedLocation();
                double distanceInKilometers = countSaunaDistanceInKilometers(userPos, sauna);

                // mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.descriptionTextView
                        .setText(sauna.getDescription() +", "+
                                StringFormat.roundedKilometersShort(
                                        ((BaseActivity)getContext()).getLocale(),
                                        distanceInKilometers));

                viewHolder.nameTextView.setText(sauna.getName());
                if (sauna.getPhotoPath() != null) {
                    StorageReference imageRef = mFirebaseStorage
                            .getReference(sauna.getPhotoPath());

                    Glide.with(SaunaListFragment.this)
                            .using(new FirebaseImageLoader())
                            .load(imageRef)
                            .into(viewHolder.saunaImageView);
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int saunaCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (saunaCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mSaunaRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mSaunaRecyclerView.setLayoutManager(mLinearLayoutManager);
        mSaunaRecyclerView.setAdapter(mFirebaseAdapter);
        mSaunaRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mSaunaRecyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                startDetailsActivity(mFirebaseAdapter.getItem(position));
                            }
                            @Override
                            public void onLongItemClick(View view, int position) {}
                        }));
    }

    @Override
    public void onConnectionSuspended(int i) {
        mUserLocationService.removeListener(this);
    }

    /**
     * Location listener. Sets the current location
     * to Google Map.
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

    }

    private double countSaunaDistanceInKilometers(Location a, Sauna b) {
        if (a == null || b == null) {
            Log.e(TAG, "Can not count sauna distance, one of the given parameters is null.");
            return 0;
        }

        // Calculate user distance from sauna
        GeodeticCalculator geoCalc = new GeodeticCalculator();
        Ellipsoid reference = Ellipsoid.WGS84;
        GlobalPosition pointA = new GlobalPosition(a.getLatitude(), a.getLongitude(), 0.0); // Point A
        GlobalPosition pointB = new GlobalPosition(b.getLatitude(), b.getLongitude(), 0.0); // Point B
        double distance = geoCalc
                .calculateGeodeticCurve(reference, pointB, pointA)
                .getEllipsoidalDistance(); // Distance between Point A and Point B

        return distance / 1000;
    }

    /**
     * Launch {@link SaunaDetailsActivity} for {@link Sauna}
     * @param sauna {@link Sauna} to display
     */
    private void startDetailsActivity(Sauna sauna) {
        Intent startIntent = new Intent(getActivity(), SaunaDetailsActivity.class);
        startIntent.putExtra(BaseActivity.DETAILS_SAUNA, sauna);
        startActivity(startIntent);
    }
}

package fi.jamk.saunaapp.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import fi.jamk.saunaapp.BaseActivity;
import fi.jamk.saunaapp.MainActivity;
import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Sauna;
import fi.jamk.saunaapp.util.RecyclerItemClickListener;
import fi.jamk.saunaapp.viewholders.SaunaViewHolder;

/**
 * A {@link Fragment} subclass that displays
 * a list of nearby Saunas.
 */
public class SaunaListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "SaunaListFragment";

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<Sauna, SaunaViewHolder>
            mFirebaseAdapter;
    private AdView mAdView;
    private ProgressBar mProgressBar;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    public SaunaListFragment() {
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_list, container, false);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) rootView.findViewById(R.id.messageRecyclerView);

        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setStackFromEnd(true);

        mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Sauna,
                SaunaViewHolder>(
                Sauna.class,
                R.layout.sauna_item,
                SaunaViewHolder.class,
                mFirebaseDatabaseReference.child(BaseActivity.SAUNAS_CHILD)) {

            @Override
            protected void populateViewHolder(SaunaViewHolder viewHolder,
                                              Sauna sauna, int position) {
                Location userPos = BaseActivity.getCurrentLocation();
                double distanceInKilometers = countSaunaDistanceInKilometers(userPos, sauna);

                // mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.descriptionTextView
                        .setText(sauna.getDescription() +", "+
                                String.format("%.1f", distanceInKilometers) +" km");

                viewHolder.nameTextView.setText(sauna.getName());
                if (sauna.getPhotoUrl() == null) {
                    viewHolder.messengerImageView
                            .setImageDrawable(ContextCompat
                                    .getDrawable(getContext(),
                                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(SaunaListFragment.this)
                            .load(sauna.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        mMessageRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mMessageRecyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                ((MainActivity)getActivity())
                                        .startDetailsActivity(mFirebaseAdapter.getItem(position));
                            }
                            @Override
                            public void onLongItemClick(View view, int position) {}
                        }));
        return rootView;
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
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
}

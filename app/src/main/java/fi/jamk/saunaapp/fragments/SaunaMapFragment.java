package fi.jamk.saunaapp.fragments;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import fi.jamk.saunaapp.BaseActivity;
import fi.jamk.saunaapp.MainActivity;
import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.SaunaDetailsActivity;
import fi.jamk.saunaapp.models.Sauna;

/**
 * A {@link Fragment} subclass that displays
 * nearby Saunas on google map.
 *
 * Use the {@link SaunaMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SaunaMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final float MAP_ZOOM = 12.0f;

    // Map center position
    private LatLng userPos;

    private HashMap<String, Marker> markers;
    private HashMap<Marker, Sauna> reverseMarkers;

    private DatabaseReference mFirebaseDatabaseReference;

    private ChildEventListener mFirebaseListener;
    private AdView mAdView;
    private GoogleMap map;
    private MapView saunaMapView;

    public SaunaMapFragment() {
        super();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sectionNumber Section number
     *
     * @return A new instance of fragment SaunaMapFragment.
     */
    public static SaunaMapFragment newInstance(int sectionNumber) {
        SaunaMapFragment fragment = new SaunaMapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // .. //
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sauna_map, container, false);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseListener = getFirebaseListener();
        markers = new HashMap<>();
        reverseMarkers = new HashMap<>();

        //mapLocation = savedInstanceState.getParcelable(ARG_MAP_LOCATION);
        saunaMapView = (MapView) rootView.findViewById(R.id.saunaMap);
        saunaMapView.onCreate(savedInstanceState);
        saunaMapView.getMapAsync(this);

        // Attach listener to add markers to map
        mFirebaseDatabaseReference.child(BaseActivity.SAUNAS_CHILD)
                .addChildEventListener(mFirebaseListener);

        mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        saunaMapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        saunaMapView.onResume();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (mFirebaseListener != null) {
            mFirebaseDatabaseReference.removeEventListener(mFirebaseListener);
            mFirebaseListener = null;
        }
        saunaMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        googleMap.setOnMarkerClickListener(this);
        // Disable scroll gestures to be able to navigate tabs
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        saunaMapView.onResume();
    }

    /**
     * Set map to given location
     *
     * @param location
     */
    public void setMapLocation(Location location) {
        userPos = new LatLng(
                location.getLatitude(),
                location.getLongitude());

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userPos, MAP_ZOOM));
    }

    /**
     * Enable/disable centering map by device location.
     *
     * @param value
     * @throws SecurityException
     */
    public void setMyLocationEnabled(boolean value)
            throws SecurityException {
        map.setMyLocationEnabled(value);
    }

    /**
     * Listener adds map markers for Saunas in database
     *
     * @return
     */
    private ChildEventListener getFirebaseListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Sauna sauna = dataSnapshot.getValue(Sauna.class);
                Marker marker = map.addMarker(new MarkerOptions().position(
                        new LatLng(sauna.getLatitude(), sauna.getLongitude())
                ).title(sauna.getName()));

                markers.put(sauna.getId(), marker);
                reverseMarkers.put(marker, sauna);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                // Remove marker from map
                Marker m = markers.get(key);
                reverseMarkers.remove(m);
                m.remove();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    /**
     * Find {@link Sauna} for marker and launch {@link SaunaDetailsActivity}
     *
     * @param marker {@link Marker} for Google Map
     * @return boolean
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Sauna sauna = reverseMarkers.get(marker);

        if (sauna != null) {
            ((MainActivity) getActivity()).startDetailsActivity(sauna);
            return true;
        }

        return false;
    }
}

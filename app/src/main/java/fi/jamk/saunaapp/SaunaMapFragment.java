package fi.jamk.saunaapp;

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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A {@link Fragment} subclass that displays
 * nearby Saunas on google map.
 *
 * Activities that contain this fragment must implement the
 * {@link SaunaMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SaunaMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SaunaMapFragment extends Fragment implements OnMapReadyCallback {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int MAP_ZOOM = 10;

    private OnFragmentInteractionListener mListener;
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

        //mapLocation = savedInstanceState.getParcelable(ARG_MAP_LOCATION);
        saunaMapView = (MapView) rootView.findViewById(R.id.saunaMap);
        saunaMapView.onCreate(savedInstanceState);
        saunaMapView.getMapAsync(this);

        mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        saunaMapView.onResume();
    }

    /**
     * Set map to given location
     *
     * @param location
     */
    public void setMapLocation(Location location) {
        LatLng userPos = new LatLng(
                location.getLatitude(),
                location.getLongitude());

        map.moveCamera(CameraUpdateFactory.newLatLng(userPos));
        map.addMarker(new MarkerOptions().position(userPos).title("Marker"));
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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}

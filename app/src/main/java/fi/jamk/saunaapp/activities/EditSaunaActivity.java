package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Sauna;

public class EditSaunaActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private Sauna sauna;
    private SupportMapFragment editSaunaMapFragment;

    /**
     * Map used to pick {@link Sauna} latitude & longitude
     */
    private GoogleMap mMap;
    private Marker currentMapMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sauna);

        Intent intent = getIntent();
        sauna = intent.getParcelableExtra(BaseActivity.DETAILS_SAUNA);

        // Init map
        editSaunaMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.editSaunaMapFragment);
        editSaunaMapFragment.getMapAsync(this);


        if (sauna != null) {
            setTitle(R.string.title_activity_edit_sauna);
        } else {
            setTitle(R.string.title_activity_add_sauna);
            sauna = new Sauna();
        }
    }

    /**
     * Map callback for Google Map used for selecting Sauna location.
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
    }

    /**
     * Click event on {@link GoogleMap}. Add marker to click position,
     * set {@link Sauna} location to that point.
     *
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        if (currentMapMarker != null) {
            currentMapMarker.remove();
        }

        currentMapMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title("Sauna location")
        );

        // Set sauna location
        sauna.setLatitude(latLng.latitude);
        sauna.setLongitude(latLng.longitude);
    }
}

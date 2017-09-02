package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Sauna;
import fi.jamk.saunaapp.services.UserLocationService;
import tofira.imagepicker.PickerBuilder;

public class EditSaunaActivity extends BaseActivity implements
        OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final float MAP_ZOOM = 12.0f;
    private static final String TAG = "EditSaunaActivity";

    private Sauna sauna;
    private MapView saunaMapView;
    private EditText nameEditText;
    private EditText descriptionEditText;
    private DatabaseReference mFirebaseSaunaRef;
    private ImageView mainImageView;

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

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        mainImageView = (ImageView) findViewById(R.id.mainImageView);

        mFirebaseSaunaRef = FirebaseDatabase.getInstance()
                .getReference().child(BaseActivity.SAUNAS_CHILD);

        // Init map
        saunaMapView = (MapView) findViewById(R.id.editSaunaMapView);
        saunaMapView.onCreate(savedInstanceState);
        saunaMapView.getMapAsync(this);

        if (sauna != null) {
            setTitle(R.string.title_activity_edit_sauna);
            setInputValues();
        } else {
            setTitle(R.string.title_activity_add_sauna);
            sauna = new Sauna();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_sauna, menu);
        return true;
    }

    @Override
    public void onResume() {
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
     * Map callback for Google Map used for selecting Sauna location.
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        if (sauna.getLongitude() != 0.0d && sauna.getLatitude() != 0.0d) {
            LatLng latLng = new LatLng(sauna.getLatitude(), sauna.getLongitude());
            currentMapMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title("Sauna location"));
            centerMap(latLng);
        } else {
            // Center map to user location
            Location currentLocation = UserLocationService.getCachedLocation();
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            currentMapMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title("Sauna location"));
            centerMap(latLng);
        }

        saunaMapView.onResume();
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

        centerMap(latLng);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // Save sauna
                if (saveSauna()) {
                    finish();
                }
                return true;
            default:
                // The user's action was not recognized.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set values from {@link Sauna} to Activity inputs (and {@link GoogleMap})
     */
    private void setInputValues() {
        if (nameEditText != null) {
            nameEditText.setText(sauna.getName());
        }
        if (descriptionEditText != null) {
            descriptionEditText.setText(sauna.getDescription());
        }
        if (currentMapMarker != null) {
            currentMapMarker.remove();
        }
        if (mMap != null) {
            LatLng latLng = new LatLng(sauna.getLatitude(), sauna.getLongitude());
            currentMapMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title("Sauna location")
            );

            centerMap(latLng);
        }
    }

    /**
     * Read values from inputs and set to model.
     */
    private void setModelValues() {
        try {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            sauna.setOwner(uid);
        } catch (NullPointerException ex) {
            Log.e(TAG, ex.getMessage());
            return;
        }

        if (nameEditText != null) {
            sauna.setName(nameEditText.getText().toString());
        }
        if (descriptionEditText != null) {
            sauna.setDescription(
                    descriptionEditText.getText().toString()
            );
        }
        if (currentMapMarker != null) {
            sauna.setLatitude(currentMapMarker.getPosition().latitude);
            sauna.setLongitude(currentMapMarker.getPosition().longitude);
        }
    }

    /**
     * Animate {@link GoogleMap} to given location.
     *
     * @param latLng      The location to move to
     */
    private void centerMap(LatLng latLng) {
        if (mMap == null) {
            return;
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
    }

    /**
     * Save current sauna, if necessary data is provided.
     * Else return false.
     *
     * @return boolean
     */
    private boolean saveSauna() {
        setModelValues();

        String id = sauna.getId();
        String name = sauna.getName();

        if (name == null || name.equals("") ||
                sauna.getLatitude() <= 0.0d || sauna.getLongitude() <= 0.0d) {
            return false;
        }

        Log.w(TAG, "Id: "+id);

        if (id == null || id.equals("")) {
            id = mFirebaseSaunaRef.push().getKey();
        }

        mFirebaseSaunaRef.child(id).setValue(sauna);
        return true;
    }

    /**
     * Pick main sauna profile image
     */
    public void selectPicture(View view) {
        new PickerBuilder(EditSaunaActivity.this, PickerBuilder.SELECT_FROM_CAMERA)
                .setOnImageReceivedListener(new PickerBuilder.onImageReceivedListener() {
                    @Override
                    public void onImageReceived(Uri imageUri) {
                        Toast.makeText(EditSaunaActivity.this, "Got image - " + imageUri, Toast.LENGTH_LONG).show();
                        mainImageView.setImageURI(imageUri);
                    }
                })
                .start();
    }
}

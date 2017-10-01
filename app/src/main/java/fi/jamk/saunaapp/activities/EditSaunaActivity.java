package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Sauna;
import fi.jamk.saunaapp.services.UserLocationService;
import tofira.imagepicker.PickerBuilder;

public class EditSaunaActivity extends BaseActivity implements
        OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final float MAP_ZOOM = 12.0f;
    private static final String TAG = "EditSaunaActivity";

    // Firebase storage reference for uploading images
    StorageReference storageRef;
    FirebaseUser user;

    private ProgressBar imageUploadBar;
    private ImageView imageIconView;

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

        imageIconView = findViewById(R.id.imageIconView);
        imageUploadBar = findViewById(R.id.imageUploadBar);

        imageUploadBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.color87opWhite, null),
                android.graphics.PorterDuff.Mode.SRC_IN);

        imageUploadBar.setVisibility(View.INVISIBLE);
        imageUploadBar.setActivated(false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Not signed in, launch the Login activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
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
            case R.id.action_delete:
                deleteSauna();
                finish();
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
            sauna.setDescription(descriptionEditText.getText().toString());
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

        if (!_validateData()) {
            return false;
        }

        if (id == null || id.equals("")) {
            id = mFirebaseSaunaRef.push().getKey();
        }

        mFirebaseSaunaRef.child(id).setValue(sauna);
        return true;
    }

    private void deleteSauna() {
        String id = sauna.getId();

        if (id != null) {
            mFirebaseSaunaRef.child(id).removeValue();
        }
    }
    /**
     * Pick main sauna profile image.
     */
    public void selectPicture(View view) {
        new PickerBuilder(EditSaunaActivity.this, PickerBuilder.SELECT_FROM_GALLERY)
            .setOnImageReceivedListener(new PickerBuilder.onImageReceivedListener() {
                @Override
                public void onImageReceived(Uri imageUri) {
                mainImageView.setImageURI(imageUri);

                File f = new File(imageUri.getPath());
                final String refPath = user.getUid() + "/images/" + f.getName();
                UploadTask task = uploadImage(refPath, f);
                if (task != null) {
                    imageIconView.setVisibility(View.INVISIBLE);
                    imageUploadBar.setProgress(0);
                    imageUploadBar.setVisibility(View.VISIBLE);
                    imageUploadBar.setActivated(true);

                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception ex) {
                            Toast.makeText(EditSaunaActivity.this, "File upload failed", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, ex.toString());

                            imageIconView.setVisibility(View.VISIBLE);
                            imageUploadBar.setVisibility(View.INVISIBLE);
                            imageUploadBar.setActivated(false);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EditSaunaActivity.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();

                            sauna.setPhotoPath(refPath);
                            imageIconView.setVisibility(View.VISIBLE);
                            imageUploadBar.setVisibility(View.INVISIBLE);
                            imageUploadBar.setActivated(false);
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) + .5);
                            imageUploadBar.setProgress(progress);
                            Log.d(TAG, "Image upload " + progress + " % ready");
                        }
                    });
                }
                }
            })
            .start();
    }

    /**
     * Upload file from given Uri by stream.
     * File is uploaded to storage ref path:
     *
     *      <user id>/images/<file name>
     *
     * @param refPath      Storage path to upload to
     * @param f            File to upload
     */
    private UploadTask uploadImage(String refPath, File f) {
        StorageReference uploadRef = storageRef.child(refPath);

        try {
            InputStream is = new FileInputStream(f);
            return uploadRef.putStream(is);
        } catch (FileNotFoundException ex) {
            Toast.makeText(EditSaunaActivity.this, "Could not upload - file was not found", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    /**
     * Check sauna property validity
     *
     * @return boolean
     */
    private boolean _validateData() {
        String name = sauna.getName();
        return (
                name != null && !name.equals("") &&
                sauna.getLatitude() > 0.0d &&
                sauna.getLongitude() > 0.0d
        );
    }
}

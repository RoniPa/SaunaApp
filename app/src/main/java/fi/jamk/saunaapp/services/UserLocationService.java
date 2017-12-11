package fi.jamk.saunaapp.services;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class UserLocationService implements LocationListener {
    public static final int REQUEST_CHECK_SETTINGS = 556;

    private static final String TAG = "UserLocationService";
    private static LocationRequest mLocationRequest;
    private static List<LocationListener> listenerList;
    private static Location cachedLocation;

    private GoogleApiClient apiClient;

    private UserLocationService(GoogleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static UserLocationService newInstance(GoogleApiClient apiClient) {
        if (listenerList == null) {
            listenerList = new ArrayList<>();
        }
        return new UserLocationService(apiClient);
    }

    /**
     * Request location updates for the given listener.
     *
     * If no permissions available for the activity return null,
     * otherwise return {@link Task<LocationSettingsResponse>} for checking device settings.
     *
     * @param ctx   {@link Activity} context
     * @param l     {@link LocationListener} to add
     * @return  {@link Task<LocationSettingsResponse>} or null
     */
    public Task<LocationSettingsResponse> requestLocationUpdates(final Activity ctx, final LocationListener l) {
        boolean hasPermission = checkPermissionsAndStartLocationListener(ctx);

        if (hasPermission) {
            listenerList.add(l);

            Task<LocationSettingsResponse> task = checkLocationSettings(ctx);
            task.addOnSuccessListener(ctx, locationSettingsResponse -> {});
            task.addOnFailureListener(ctx, e -> {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(ctx, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore...
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Settings not satisfied, but no way to fix them.
                        Log.e(TAG, "Location settings are not satisfied, but can not be fixed.");
                        break;
                }
            });

            return task;
        } else {
            Log.e(TAG, "Provided context " + ctx.toString() + " has not the needed user permissions.");
        }

        return null;
    }

    public boolean removeListener(LocationListener l) {
        if (listenerList.size() >= 1) {
            mLocationRequest = null;

            if (apiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
            }
        }
        return listenerList.remove(l);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, location.toString());
        for (LocationListener l : listenerList) {
            l.onLocationChanged(location);
        }

        UserLocationService.cachedLocation = location;
    }

    /**
     * Check locations permissions and set location if
     * permissions are granted.
     *
     * @return False if no permissions, true if location set
     */
    private boolean checkPermissionsAndStartLocationListener(Context ctx) {
        if (
                ActivityCompat.checkSelfPermission(
                        ctx, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(
                        ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                ) {
            Log.d(TAG, "No location permissions granted.");
            return false;
        }

        if (mLocationRequest == null) {
            mLocationRequest = LocationRequest.create();

            // Request location updates.
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    this.apiClient, mLocationRequest, this);
        }

        return true;
    }

    private Task<LocationSettingsResponse> checkLocationSettings(Context ctx) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        // Check settings
        SettingsClient client = LocationServices.getSettingsClient(ctx);
        return client.checkLocationSettings(builder.build());
    }

    public static Location getCachedLocation() { return UserLocationService.cachedLocation; }
}

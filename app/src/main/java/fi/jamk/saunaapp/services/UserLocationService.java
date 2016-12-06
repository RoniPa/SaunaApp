package fi.jamk.saunaapp.services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashSet;
import java.util.Set;

public class UserLocationService implements LocationListener {
    private static final String TAG = "UserLocationService";
    private static GoogleApiClient apiClient;
    private static LocationRequest mLocationRequest;
    private static Set<LocationListener> listenerSet;

    private UserLocationService() {}

    public static UserLocationService newInstance(GoogleApiClient apiClient) {
        if (UserLocationService.apiClient == null) {
            UserLocationService.apiClient = apiClient;
        }
        if (listenerSet == null) {
            listenerSet = new HashSet<>();
        }
        return new UserLocationService();
    }

    public boolean requestLocationUpdates(LocationListener l) {
        boolean hasPermission = checkPermissionsAndStartLocationListener((Context) l);

        if (hasPermission) {
            listenerSet.add(l);
        } else {
            Log.e(TAG, "Provided context " + l.toString() + " has not the needed user permissions.");
        }

        return hasPermission;
    }

    public boolean removeListener(LocationListener l) {
        return listenerSet.remove(l);
    }


    @Override
    public void onLocationChanged(Location location) {
        for (LocationListener l : listenerSet) {
            l.onLocationChanged(location);
        }
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
                    apiClient, mLocationRequest, this);
        }

        return true;
    }
}

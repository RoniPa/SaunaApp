package fi.jamk.saunaapp.util;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import fi.jamk.saunaapp.R;
import io.reactivex.annotations.Nullable;

/**
 * Utility methods for handling {@link GoogleMap}
 */

public class MapUtils {
    /**
     * Animates {@link GoogleMap} center to given location and zoom
     *
     * @param latLng
     * @param zoom
     * @param map
     */
    public static void centerMap(LatLng latLng, float zoom, GoogleMap map) {
        if (map == null) {
            return;
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    /**
     * Get {@link MarkerOptions} for a sauna map marker.
     * Add marker with the {@link GoogleMap::addMarker} method.
     *
     * @param latLng
     * @param name
     * @return
     */
    public static MarkerOptions getSaunaMarker(LatLng latLng, @Nullable String name) {
        return new MarkerOptions()
                .position(latLng)
                .title(name == null ? "Sauna location" : name)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.sauna_marker));
    }
}

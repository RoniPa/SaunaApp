package fi.jamk.saunaapp.util;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Interface to share GoogleApiClient reference.
 * etc. Define client in Activity to handle connection and implement this interface,
 * register Fragment as listener and hook actions to connection callbacks.
 */
public interface ChildConnectionNotifier {
    GoogleApiClient getApiClient();
    void addConnectionListener(GoogleApiClient.ConnectionCallbacks l);
    void removeConnectionListener(GoogleApiClient.ConnectionCallbacks l);
}

package fi.jamk.saunaapp.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all app Activities.
 */
public abstract class BaseActivity extends AppCompatActivity {
    public static final int REQUEST_INVITE = 1;
    public static final int REQUEST_LOCATION = 2;

    public static final String MESSAGES_CHILD = "messages";
    public static final String SAUNAS_CHILD = "saunas";
    public static final String DETAILS_SAUNA = "fi.jamk.saunaapp.DETAILS_SAUNA";

    protected FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define Firebase Remote Config Settings.
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("friendly_msg_length", 10L);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Apply config settings and default values.
        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
    }

    /**
     * Fetch remote config for app.
     *
     * Set {@link OnSuccessListener<Void>} and {@link OnFailureListener<Void>}
     * for returned {@link Task<Void>} to handle result and use configuration values.
     *
     * @return {@link Task<Void>} Remote config
     */
    public Task<Void> fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that
        // each fetch goes to the server. This should not be used in release
        // builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings()
                .isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        return mFirebaseRemoteConfig.fetch(cacheExpiration);
    }
}

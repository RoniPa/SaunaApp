package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.auth.api.Auth;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.fragments.SaunaListFragment;
import fi.jamk.saunaapp.fragments.SaunaMapFragment;
import fi.jamk.saunaapp.fragments.UserProfileFragment;
import fi.jamk.saunaapp.services.UserLocationService;
import fi.jamk.saunaapp.util.ChildConnectionNotifier;

public class MainActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        ChildConnectionNotifier {
    private static final String TAG = "MainActivity";

    private GoogleApiClient mGoogleApiClient;

    // Listen for GoogleApiClient changes in fragments
    private List<GoogleApiClient.ConnectionCallbacks> childConnectionListeners;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    private UserLocationService mUserLocationService;

    private String mUsername;
    private String mPhotoUrl;

    private AdView mAdView;

    private FragmentManager.OnBackStackChangedListener mBackStackListener;

    // Tab page fragments
    SaunaMapFragment mapFragment;
    SaunaListFragment listFragment;
    UserProfileFragment profileFragment;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        childConnectionListeners = new ArrayList<>();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Login activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mFirebaseAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                // Not signed in, launch the Login activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            } else {
                mUsername = mFirebaseUser.getDisplayName();
                if (mFirebaseUser.getPhotoUrl() != null) {
                    mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addConnectionCallbacks(this)
            .addApi(Auth.GOOGLE_SIGN_IN_API)
            .addApi(LocationServices.API)
            .build();

        mUserLocationService = UserLocationService.newInstance(mGoogleApiClient);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // We have 3 tabs, keep both off-view tabs in memory
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setIcon(mSectionsPagerAdapter.getPageIcon(i));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        if (mBackStackListener != null) {
            getSupportFragmentManager().removeOnBackStackChangedListener(mBackStackListener);
            mBackStackListener = null;
        }

        if (mAdView != null) {
            mAdView.destroy();
        }

        mGoogleApiClient = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode +
                ", resultCode=" + resultCode);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, childConnectionListeners.size() +" registered connection listeners");

        Task<LocationSettingsResponse> updatesTask = mUserLocationService.requestLocationUpdates(MainActivity.this, this);
        if (updatesTask == null) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    BaseActivity.REQUEST_LOCATION);
        }

        for (GoogleApiClient.ConnectionCallbacks listener : childConnectionListeners) {
            listener.onConnected(bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        for (GoogleApiClient.ConnectionCallbacks listener : childConnectionListeners) {
            listener.onConnectionSuspended(i);
        }
        mUserLocationService.removeListener(this);
        Log.d(TAG, "GoogleApi connection suspended: "+i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "OnLocationChanged with location " + location.toString());
        if (mapFragment != null) {
            mapFragment.onLocationChanged(location);
        }
        if (listFragment != null) {
            listFragment.onLocationChanged(location);
        }
    }

    public void signOut() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Returns page fragment of given position
         * or throws {@link IllegalArgumentException} if not found
         *
         * @param position Page position
         *
         * @return {@link Fragment} | null
         */
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    listFragment = SaunaListFragment.newInstance(position + 1);
                    return listFragment;
                case 1:
                    mapFragment = SaunaMapFragment.newInstance(position + 1);
                    return mapFragment;
                case 2:
                    profileFragment = UserProfileFragment.newInstance(position + 1);
                    return profileFragment;
                default:
                    throw new IllegalArgumentException("Page at position "+position+" not found.");
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                case 1:
                case 2:
                default:
                    return null;
            }
        }

        public Drawable getPageIcon(int position) {
            switch (position) {
                case 0:
                    return getResources().getDrawable(R.drawable.ic_view_list_white_24dp, null);
                case 1:
                    return getResources().getDrawable(R.drawable.ic_map_white_24dp, null);
                case 2:
                    return getResources().getDrawable(R.drawable.ic_person_white_24dp, null);
                default:
                    return null;
            }
        }
    }

    @Override
    public GoogleApiClient getApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void addConnectionListener(GoogleApiClient.ConnectionCallbacks l) {
        childConnectionListeners.add(l);
    }

    @Override
    public void removeConnectionListener(GoogleApiClient.ConnectionCallbacks l) {
        childConnectionListeners.remove(l);
    }
}

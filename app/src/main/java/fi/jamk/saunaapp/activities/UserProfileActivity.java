package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.fragments.ProfileSaunaListFragment;
import fi.jamk.saunaapp.models.Sauna;

public class UserProfileActivity extends BaseActivity implements
        ProfileSaunaListFragment.OnListFragmentInteractionListener {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        setTitle(R.string.your_saunas);

        Fragment saunaListFragment = getSupportFragmentManager().
                findFragmentById(R.id.profileSaunasFragment);
        if (saunaListFragment != null) {
            ((ProfileSaunaListFragment)saunaListFragment).
                    fetchDataAndPopulate(mFirebaseUser.getUid());
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent startIntent = new Intent(view.getContext(), EditSaunaActivity.class);
            startActivity(startIntent);
        });

    }

    /**
     * Sauna list fragment interaction
     * @param sauna
     */
    @Override
    public void onListFragmentInteraction(Sauna sauna) {}

    public void startSaunaEditActivity(Sauna sauna) {
        Intent startIntent = new Intent(this, EditSaunaActivity.class);
        startIntent.putExtra(DETAILS_SAUNA, sauna);
        startActivity(startIntent);
    }
}

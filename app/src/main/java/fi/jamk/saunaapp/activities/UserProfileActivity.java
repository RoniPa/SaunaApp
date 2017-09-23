package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.fragments.ProfileDetailsFragment;
import fi.jamk.saunaapp.fragments.ProfileSaunaListFragment;
import fi.jamk.saunaapp.models.Sauna;

public class UserProfileActivity extends BaseActivity implements
        ProfileDetailsFragment.OnFragmentInteractionListener,
        ProfileSaunaListFragment.OnListFragmentInteractionListener {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        Fragment saunaListFragment = getSupportFragmentManager().
                findFragmentById(R.id.profileSaunasFragment);
        if (saunaListFragment != null) {
            ((ProfileSaunaListFragment)saunaListFragment).
                    fetchDataAndPopulate(mFirebaseUser.getUid());
        }

        setTitle(R.string.title_activity_user_profile);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(view.getContext(), EditSaunaActivity.class);
                startActivity(startIntent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Details fragment interaction
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {}

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

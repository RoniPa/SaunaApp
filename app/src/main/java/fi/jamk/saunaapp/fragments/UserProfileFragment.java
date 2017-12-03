package fi.jamk.saunaapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.activities.ConversationListActivity;
import fi.jamk.saunaapp.activities.LoginActivity;
import fi.jamk.saunaapp.activities.MainActivity;
import fi.jamk.saunaapp.activities.UserProfileActivity;
import fi.jamk.saunaapp.util.ChildConnectionNotifier;

/**
 * A {@link Fragment} subclass that displays
 * nearby Saunas on google map.
 *
 * Parent Activity must implement the {@link ChildConnectionNotifier} interface.
 *
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "UserProfileFragment";

    private FirebaseUser mUser;
    private List<View> navItems;

    public UserProfileFragment() {
        super();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sectionNumber Section number
     *
     * @return A new instance of fragment SaunaMapFragment.
     */
    public static UserProfileFragment newInstance(int sectionNumber) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_profile, container, false);
        View rootView = binding.getRoot();

        TextView tView = rootView.findViewById(R.id.name_text_view);
        tView.setText(mUser.getDisplayName());

        this.initNavItems(rootView);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void openMessages(View view) {
        Intent intent = new Intent(getActivity(), ConversationListActivity.class);
        startActivity(intent);
    }

    public void openSaunas(View view) {
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        startActivity(intent);
    }

    public void signOut(View view) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users/" + mUser.getUid() + "/notificationTokens");

        ref.setValue(null).addOnCompleteListener(task -> {
            ((MainActivity)getActivity()).signOut();
        });
    }

    private void initNavItems(View root) {
        this.navItems = new ArrayList<>();

        View navMessages = root.findViewById(R.id.nav_item_messages);
        navMessages.setOnClickListener(this::openMessages);
        this.navItems.add(navMessages);

        View navSaunas = root.findViewById(R.id.nav_item_saunas);
        navSaunas.setOnClickListener(this::openSaunas);
        this.navItems.add(navSaunas);

        View navSignOut = root.findViewById(R.id.nav_item_logout);
        navSignOut.setOnClickListener(this::signOut);
        this.navItems.add(navSignOut);
    }
}

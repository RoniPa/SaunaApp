package fi.jamk.saunaapp.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import fi.jamk.saunaapp.activities.BaseActivity;
import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.activities.MainActivity;
import fi.jamk.saunaapp.activities.UserProfileActivity;
import fi.jamk.saunaapp.models.Sauna;
import fi.jamk.saunaapp.services.UserLocationService;
import fi.jamk.saunaapp.util.RecyclerItemClickListener;
import fi.jamk.saunaapp.viewholders.SaunaViewHolder;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ProfileSaunaListFragment extends Fragment {
    public static final String TAG = "ProfileSaunaList";
    public static final String ARG_USER_ID = "saunaapp:user_id";

    private View mListView;
    private String userId;
    private OnListFragmentInteractionListener mListener;
    private ValueEventListener valueListener;
    private Query mFirebaseDatabaseReference;

    private RecyclerView mSaunaRecyclerView;
    private FirebaseRecyclerAdapter<Sauna, SaunaViewHolder> mRecyclerViewAdapter;

    public ProfileSaunaListFragment() {}

    /**
     *
     * @param id Firebase user id
     * @return
     */
    public static ProfileSaunaListFragment newInstance(String id) {
        ProfileSaunaListFragment fragment = new ProfileSaunaListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mListView = inflater.inflate(R.layout.fragment_profile_sauna_list, container, false);

        if (userId != null) {
            this.fetchDataAndPopulate(userId);
        }

        return mListView;
    }

    public void fetchDataAndPopulate(String userId) {
        this.userId = userId;

        mFirebaseDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(BaseActivity.SAUNAS_CHILD)
                .orderByChild("owner")
                .equalTo(userId);

        Context context = mListView.getContext();
        mSaunaRecyclerView = (RecyclerView) mListView.findViewById(R.id.profile_sauna_recycler_view);
        mSaunaRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<Sauna,
                SaunaViewHolder>(
                Sauna.class,
                R.layout.sauna_item,
                SaunaViewHolder.class,
                mFirebaseDatabaseReference) {

            @Override
            protected void populateViewHolder(SaunaViewHolder viewHolder,
                                              Sauna sauna, int position) {
                Location userPos = UserLocationService.getCachedLocation();

                // mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.descriptionTextView
                        .setText(sauna.getDescription());

                viewHolder.nameTextView.setText(sauna.getName());
                if (sauna.getPhotoUrl() == null) {
                    viewHolder.messengerImageView
                            .setImageDrawable(ContextCompat
                                    .getDrawable(getContext(),
                                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(ProfileSaunaListFragment.this)
                            .load(sauna.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }
            }
        };
        mSaunaRecyclerView.setAdapter(mRecyclerViewAdapter);
        mSaunaRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mSaunaRecyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Sauna sauna = mRecyclerViewAdapter.getItem(position);
                                sauna.setId(mRecyclerViewAdapter.getRef(position).getKey());
                                ((UserProfileActivity)getActivity())
                                        .startSaunaEditActivity(sauna);
                            }
                            @Override
                            public void onLongItemClick(View view, int position) {}
                        }));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        valueListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Sauna item);
    }
}

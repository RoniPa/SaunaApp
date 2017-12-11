package fi.jamk.saunaapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Rating;
import fi.jamk.saunaapp.util.RatingAdapter;

/**
 * Fragment for displaying a few Ratings. If more than a few,
 * you should use a {@link RecyclerView]
 * {@link Fragment} subclass.
 *
 * Use the {@link RatingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RatingsFragment extends Fragment {
    public static final String TAG = "RatingsFragment";

    private Query mQuery;
    private List<Rating> mRatings;
    private LinearLayout mListWrapper;
    private RatingAdapter mAdapter;
    private TextView mEmptyText;

    public RatingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ref {@link Query} used as a source for ratings.
     * @return A new instance of fragment SaunaRatingsFragment.
     */
    public static RatingsFragment newInstance(Query ref) {
        RatingsFragment fragment = new RatingsFragment();
        fragment.setQuery(ref);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRatings = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_ratings, container, false);
        mListWrapper = root.findViewById(R.id.ratings_list);
        mEmptyText = root.findViewById(R.id.empty_text);
        initListAdapter();
        getRatings();
        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setQuery(Query ref) {
        mQuery = ref;
    }

    /**
     * Iterate query results and push to mRatings {@link ArrayList}.
     */
    private void getRatings() {
        mQuery.addValueEventListener(new ValueEventListener() {
              @Override public void onDataChange(DataSnapshot dataSnapshot) {
                  mRatings.clear();
                  mListWrapper.removeAllViews();

                  dataSnapshot.getChildren().forEach(item -> mRatings.add(item.getValue(Rating.class)));

                  if (mRatings.size() == 0) {
                      mEmptyText.setVisibility(View.VISIBLE);
                  } else {
                      // Add views manually, because we use LinearLayout
                      final int listCount = mRatings.size();
                      for (int i = listCount - 1; i >= 0; i--) {
                          mListWrapper.addView(mAdapter.getView(i, null, mListWrapper));
                      }
                      mEmptyText.setVisibility(View.GONE);
                  }

                  mAdapter.notifyDataSetChanged();
              }
              @Override public void onCancelled(DatabaseError databaseError) {
                  Log.e(TAG, databaseError.getMessage());
              }
          }
        );
    }

    private void initListAdapter() {
        mAdapter = new RatingAdapter(getContext(), R.layout.rating_item, mRatings);
    }
}

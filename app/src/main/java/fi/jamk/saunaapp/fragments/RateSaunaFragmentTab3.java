package fi.jamk.saunaapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Rating;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RateSaunaFragmentTab3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RateSaunaFragmentTab3 extends RateSaunaFragment.RatingChildFragment {

    private FirebaseUser mUser;
    private Rating rating;
    private RatingBar mBar;

    public RateSaunaFragmentTab3() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RateSaunaFragment.
     */
    public static RateSaunaFragmentTab3 newInstance(Rating rating) {
        RateSaunaFragmentTab3 fragment = new RateSaunaFragmentTab3();
        fragment.setRating(rating);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rate_sauna_tab3, container, false);

        this.mBar = view.findViewById(R.id.rating_bar);
        this.mBar.setRating((float)rating.getRating());

        return view;
    }

    @Override
    public void onAttach(Context context) { super.onAttach(context); }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setRating(Rating rating) { this.rating = rating; }

    @Override
    void onParentRatingChanged(Rating rating) {
        this.rating = rating;
        this.mBar.setRating((float)this.rating.getRating());
    }
}
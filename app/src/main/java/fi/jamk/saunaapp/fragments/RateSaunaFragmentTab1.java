package fi.jamk.saunaapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fi.jamk.saunaapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RateSaunaFragmentTab1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RateSaunaFragmentTab1 extends Fragment {

    private FirebaseUser mUser;
    private RatingBar.OnRatingBarChangeListener mRatingListener;

    public RateSaunaFragmentTab1() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RateSaunaFragment.
     */
    public static RateSaunaFragmentTab1 newInstance(RatingBar.OnRatingBarChangeListener l) {
        RateSaunaFragmentTab1 fragment = new RateSaunaFragmentTab1();
        fragment.setRatingListener(l);
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
        View view = inflater.inflate(R.layout.fragment_rate_sauna_tab1, container, false);

        TextView nameTextView = view.findViewById(R.id.sauna_name_text_view);
        nameTextView.setText(mUser.getDisplayName());

        RatingBar bar = view.findViewById(R.id.rating_bar);
        bar.setOnRatingBarChangeListener(mRatingListener);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setRatingListener(RatingBar.OnRatingBarChangeListener l) {
        mRatingListener = l;
    }
}
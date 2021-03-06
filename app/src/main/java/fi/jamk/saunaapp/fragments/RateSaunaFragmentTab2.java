package fi.jamk.saunaapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Rating;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RateSaunaFragmentTab2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RateSaunaFragmentTab2 extends RateSaunaFragment.RatingChildFragment {

    private FirebaseUser mUser;
    private TextWatcher mListener;

    private EditText reviewEditText;

    public RateSaunaFragmentTab2() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RateSaunaFragment.
     */
    public static RateSaunaFragmentTab2 newInstance(TextWatcher l) {
        RateSaunaFragmentTab2 fragment = new RateSaunaFragmentTab2();
        fragment.setListener(l);
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
        View view = inflater.inflate(R.layout.fragment_rate_sauna_tab2, container, false);

        TextView nameTextView = view.findViewById(R.id.sauna_name_text_view);
        nameTextView.setText(mUser.getDisplayName());

        reviewEditText = view.findViewById(R.id.review_edit);
        reviewEditText.addTextChangedListener(mListener);

        return view;
    }

    @Override
    public void onAttach(Context context) { super.onAttach(context); }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setListener(TextWatcher listener) {
        mListener = listener;
    }

    @Override
    void onParentRatingChanged(Rating rating) {
        this.reviewEditText.setText(rating.getMessage());
    }
}
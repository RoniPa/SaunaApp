package fi.jamk.saunaapp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Rating;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RateSaunaFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RateSaunaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RateSaunaFragment extends Fragment implements RatingBar.OnRatingBarChangeListener {
    private static final String ARG_USER_ID = "user_id";

    private FirebaseUser mUser;
    private OnFragmentInteractionListener mListener;
    private TextWatcher reviewMessageWatcher;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private Button actionButtonContinue;
    private Button actionButtonCancel;

    private float rating;
    private String reviewMessage;

    public RateSaunaFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RateSaunaFragment.
     */
    public static RateSaunaFragment newInstance() {
        RateSaunaFragment fragment = new RateSaunaFragment();
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
        View view = inflater.inflate(R.layout.fragment_rate_sauna, container, false);

        reviewMessageWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                reviewMessage = charSequence.toString();
            }
            @Override public void afterTextChanged(Editable editable) {}
        };

        mPager = view.findViewById(R.id.pager);
        mPagerAdapter = new RatingSlidePagerAdapter(getChildFragmentManager(), this, reviewMessageWatcher);

        actionButtonContinue = (Button) view.findViewById(R.id.rate_action_button);
        actionButtonContinue.setText(R.string.action_next);
        actionButtonContinue.setEnabled(false);
        actionButtonContinue.setOnClickListener(new RateSaunaFragment.NextOnClickListener());

        actionButtonCancel = (Button) view.findViewById(R.id.cancel_action_button);
        actionButtonCancel.setEnabled(false);
        actionButtonCancel.setOnClickListener(new RateSaunaFragment.PreviousOnClickListener());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
        actionButtonContinue.setTextColor(getResources().getColor(R.color.colorPrimary, null));
        actionButtonContinue.setEnabled(true);
        this.rating = v;
    }

    /**
     * Set value of hasRated.
     * Also if true, move to last page (display rating) and
     * hide action buttons.
     *
     * @param hasRated
     */
    public void setHasRated(boolean hasRated) {
        // Hack to prevent animation on page view initialization
        mPager.setAdapter(mPagerAdapter);
        // Finalize actions
        if (hasRated) {
            mPager.setCurrentItem(RatingSlidePagerAdapter.PAGE_COUNT - 1);
            actionButtonCancel.setVisibility(View.GONE);
            actionButtonContinue.setVisibility(View.GONE);
        }
    }

    /**
     * Set values from given Rating object.
     *
     * @param rating      Rating object
     */
    public void setRating(Rating rating) {
        if (rating == null) {
            return;
        }

        this.rating = (float)rating.getRating();
        this.reviewMessage = rating.getMessage();

        RatingChildFragment frag =
                ((RatingSlidePagerAdapter)mPager.getAdapter())
                        .getCurrentFragment();

        frag.onParentRatingChanged(rating);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Rating rating);
    }

    /**
     * Puts new review to the database,
     * mark user to have given review for this sauna
     */
    private Rating createRating() {

        Rating rating = new Rating();
        rating.setRating(this.rating);
        rating.setTime(new Date());
        rating.setUserId(mUser.getUid());
        rating.setUserName(mUser.getDisplayName());
        rating.setMessage(this.reviewMessage);

        return rating;
    }

    private class RatingSlidePagerAdapter extends FragmentStatePagerAdapter {
        static final int PAGE_COUNT = 3;

        private RateSaunaFragment.RatingChildFragment mCurrentFragment;

        RatingBar.OnRatingBarChangeListener mBarListener;
        TextWatcher mTextListener;

        RatingSlidePagerAdapter(FragmentManager fm, RatingBar.OnRatingBarChangeListener l1, TextWatcher l2) {
            super(fm);
            mBarListener = l1;
            mTextListener = l2;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: this.mCurrentFragment = RateSaunaFragmentTab1.newInstance(mBarListener); break;
                case 1: this.mCurrentFragment = RateSaunaFragmentTab2.newInstance(mTextListener); break;
                case 2: this.mCurrentFragment = RateSaunaFragmentTab3.newInstance(createRating()); break;
                default: return null;
            }

            return this.mCurrentFragment;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        public RateSaunaFragment.RatingChildFragment getCurrentFragment() { return mCurrentFragment; }
    }

    /**
     * Handles moving to next phase (tab1 -> tab2 -> send)
     */
    private class NextOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final int sendPageIndex = 1;

            // Last phase, send review
            if (mPager.getCurrentItem() == sendPageIndex) {
                mListener.onFragmentInteraction(createRating());
                setHasRated(true);
                return;
            }

            actionButtonCancel.setTextColor(getResources().getColor(R.color.colorPrimary, null));
            actionButtonCancel.setEnabled(true);

            mPager.setCurrentItem(mPager.getCurrentItem() + 1);

            // We are on the "send" tab
            if (mPager.getCurrentItem() >= RatingSlidePagerAdapter.PAGE_COUNT - 1) {
                actionButtonContinue.setText(R.string.action_send);
            }
        }
    }

    /**
     * Handles moving back to previous phase (tab)
     */
    private class PreviousOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            actionButtonContinue.setText(R.string.action_next);

            if (mPager.getCurrentItem() == 0) {
                actionButtonCancel.setTextColor(getResources().getColor(R.color.colorDisabled, null));
                actionButtonCancel.setEnabled(false);
            }
        }
    }

    public static abstract class RatingChildFragment extends Fragment {
        abstract void onParentRatingChanged(Rating rating);
    }
}
package fi.jamk.saunaapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Rating;

/**
 * Extends {@link ArrayAdapter}, for displaying ratings
 */
public class RatingAdapter extends ArrayAdapter<Rating> {
    private Context mContext;
    private LayoutInflater inflater;
    private List<Rating> items;

    public RatingAdapter(Context ctx, int layout, List<Rating> items) {
        super(ctx, layout, items);
        this.mContext = ctx;
        this.inflater = LayoutInflater.from(ctx);
        this.items = items;
    }

    @NonNull
    @SuppressLint("ViewHolder")
    @Override public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        View ratingView = inflater.inflate(R.layout.rating_item, viewGroup, false);

        Rating item = items.get(i);

        RatingBar rating = ratingView.findViewById(R.id.rating_bar);
        rating.setRating((float)item.getRating());

        TextView reviewText = ratingView.findViewById(R.id.review_text);
        reviewText.setText(String.format("\"%s\"", item.getMessage()));

        TextView userText = ratingView.findViewById(R.id.user_text);
        userText.setText(mContext.getString(R.string.citation, item.getUserName()));

        TextView dateText = ratingView.findViewById(R.id.date_text);
        dateText.setText(StringFormat.shortTime(null, item.getTime()));

        return ratingView;
    }
}

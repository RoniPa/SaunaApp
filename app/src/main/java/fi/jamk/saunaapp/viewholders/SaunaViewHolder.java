package fi.jamk.saunaapp.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import fi.jamk.saunaapp.R;

/**
 * A view holder extending {@link RecyclerView.ViewHolder} for Sauna items.
 */
public class SaunaViewHolder extends BindableViewHolder<SaunaViewHolder.Binding> {
    private SaunaViewHolder.Binding mBinding;

    public TextView descriptionTextView;
    public TextView distanceTextView;
    public TextView nameTextView;
    public ImageView saunaImageView;
    public RatingBar ratingBar;

    public SaunaViewHolder(View view) {
        super(view);

        descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        distanceTextView = itemView.findViewById(R.id.distanceTextView);
        ratingBar = itemView.findViewById(R.id.saunaRatingBar);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        saunaImageView = itemView.findViewById(R.id.saunaImageView);
    }

    @Override
    public void bind(SaunaViewHolder.Binding binding) {
        this.mBinding = binding;
    }

    public class OnListFragmentInteractionListener {}

    public static class Binding {
        public String descriptionText;
        public String nameText;
        public String distanceText;
        public float rating;

        public Binding(
                String nameText,
                String descriptionText,
                String distanceText,
                float rating
        ) {
            this.nameText = nameText;
            this.descriptionText = descriptionText;
            this.distanceText = distanceText;
            this.rating = rating;
        }
    }
}
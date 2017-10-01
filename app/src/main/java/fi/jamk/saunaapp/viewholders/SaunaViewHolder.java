package fi.jamk.saunaapp.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import fi.jamk.saunaapp.R;

/**
 * A view holder extending {@link RecyclerView.ViewHolder}
 * for Sauna list elements.
 */
public class SaunaViewHolder extends BindableViewHolder<SaunaViewHolder.Binding> {
    private SaunaViewHolder.Binding mBinding;

    public TextView descriptionTextView;
    public TextView nameTextView;
    public ImageView saunaImageView;

    public SaunaViewHolder(View view) {
        super(view);

        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
        saunaImageView = itemView.findViewById(R.id.saunaImageView);
    }

    @Override
    public void bind(SaunaViewHolder.Binding binding) {
        this.mBinding = binding;
    }

    public class OnListFragmentInteractionListener {

    }

    public static class Binding {
        public String descriptionText;

        public Binding(
                String descriptionText
        ) {
            this.descriptionText = descriptionText;
        }
    }
}
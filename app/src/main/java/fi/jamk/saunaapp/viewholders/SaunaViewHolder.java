package fi.jamk.saunaapp.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import fi.jamk.saunaapp.R;

/**
 * A view holder extending {@link RecyclerView.ViewHolder}
 * for Sauna list elements.
 */
public class SaunaViewHolder extends RecyclerView.ViewHolder {
    public TextView descriptionTextView;
    public TextView nameTextView;
    public CircleImageView messengerImageView;

    public SaunaViewHolder(View v) {
        super(v);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
        messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
    }
}
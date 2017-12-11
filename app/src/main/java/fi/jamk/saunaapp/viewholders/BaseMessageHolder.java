package fi.jamk.saunaapp.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Message;

/**
 * A view holder extending {@link RecyclerView.ViewHolder} for sent Messages.
 */
public class BaseMessageHolder extends BindableViewHolder<BaseMessageHolder.Binding> {
    private BaseMessageHolder.Binding mBinding;

    public TextView timeTextView;
    public TextView messageTextView;

    public BaseMessageHolder(View view) {
        super(view);

        messageTextView = itemView.findViewById(R.id.message_text);
        timeTextView = itemView.findViewById(R.id.time_text);
    }

    @Override
    public void bind(BaseMessageHolder.Binding binding) {
        this.mBinding = binding;
    }

    public class OnListFragmentInteractionListener {}

    public static class Binding {
        public Message message;

        public Binding(
            Message message
        ) {
            this.message = message;
        }
    }
}
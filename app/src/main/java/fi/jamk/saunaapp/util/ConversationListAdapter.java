package fi.jamk.saunaapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Conversation;
import fi.jamk.saunaapp.viewholders.BindableViewHolder;

/**
 * Adapter for displaying Conversations in RecyclerView
 */
public class ConversationListAdapter extends FirebaseRecyclerAdapter<Conversation, ConversationListAdapter.ViewHolder> {
    private Context mContext;

    public ConversationListAdapter(Context context, DatabaseReference conversationRef) {
        super(Conversation.class, R.layout.conversation_list_content, ViewHolder.class, conversationRef);
        this.mContext = context;
    }

    public ConversationListAdapter(Context context, Query conversationRef) {
        super(Conversation.class, R.layout.conversation_list_content, ViewHolder.class, conversationRef);
        this.mContext = context;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void populateViewHolder(ViewHolder viewHolder, Conversation model, int position) {
        viewHolder.mContentView.setText(model.getTargetName());

        if (model.getHasNew() > 0) {
            viewHolder.mHasNewText.setVisibility(View.VISIBLE);
            viewHolder.mHasNewText.setText(String.format("%d", model.getHasNew()));
        } else {
            viewHolder.mHasNewText.setVisibility(View.GONE);
        }
    }

    public static class ViewHolder extends BindableViewHolder<ViewHolder.Binding> {
        final TextView mContentView;
        final TextView mHasNewText;

        private ViewHolder.Binding mBinding;

        ViewHolder(View view) {
            super(view);
            mContentView = view.findViewById(R.id.content);
            mHasNewText = view.findViewById(R.id.hasNewText);
        }

        @Override
        public void bind(ViewHolder.Binding binding) {
            this.mBinding = binding;
        }

        public static class Binding {
            public String content;
            public int hasNew;

            public Binding(String content, int hasNew) {
                this.content = content;
                this.hasNew = hasNew;
            }
        }
    }
}

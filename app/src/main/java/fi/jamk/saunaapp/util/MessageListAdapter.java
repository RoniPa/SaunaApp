package fi.jamk.saunaapp.util;

import android.content.Context;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.activities.BaseActivity;
import fi.jamk.saunaapp.models.Message;
import fi.jamk.saunaapp.viewholders.BaseMessageHolder;

/**
 * List adapter for displaying messages in RecyclerView.
 */

public class MessageListAdapter extends FirebaseRecyclerAdapter<Message, BaseMessageHolder> {

    private String currUid;
    private Context mContext;

    public MessageListAdapter(Context context, DatabaseReference messageRef) {
        super(Message.class, R.layout.message_bubble_sent, BaseMessageHolder.class, messageRef);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new NullPointerException("Current user should not be null!");
        }

        this.mContext = context;
        this.currUid = user.getUid();
    }

    /**
     * Determines the appropriate ViewType according to the sender of the message.
     */
    @Override
    public int getItemViewType(int position) {
        Message message = this.getItem(position);

        if (message != null) {
            if (message.getSender().equals(currUid)) {
                return R.layout.message_bubble_sent;
            } else {
                return R.layout.message_bubble_received;
            }
        }

        return -1;
    }

    @Override
    protected void populateViewHolder(BaseMessageHolder viewHolder, Message model, int position) {
        viewHolder.messageTextView.setText(model.getText());
        viewHolder.timeTextView.setText(
                StringFormat.shortTime(
                        ((BaseActivity)mContext).getLocale(),
                        model.getDate()
                )
        );
    }
}

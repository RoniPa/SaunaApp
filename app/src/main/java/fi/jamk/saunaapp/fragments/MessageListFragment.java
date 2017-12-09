package fi.jamk.saunaapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;

import java.util.Date;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.activities.MessageListActivity;
import fi.jamk.saunaapp.activities.ConversationListActivity;
import fi.jamk.saunaapp.models.Conversation;
import fi.jamk.saunaapp.models.Message;
import fi.jamk.saunaapp.util.MessageListAdapter;

/**
 * A fragment representing a conversation.
 * This fragment is either contained in a {@link MessageListActivity}.
 */
public class MessageListFragment extends Fragment {

    private FirebaseUser mUser;
    private Conversation mConversation;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private MessageListAdapter mMessageAdapter;
    private DatabaseReference mMessageRef;

    private EditText mMessageEditText;
    private Button mMessageButton;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessageListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (getArguments().containsKey(ConversationListActivity.CONV_DETAIL_ITEM)) {
            mConversation = getArguments().getParcelable(ConversationListActivity.CONV_DETAIL_ITEM);

            // Reset message indicator
            if (mConversation != null && mConversation.getId() != null) {
                DatabaseReference convRef = FirebaseDatabase.getInstance().getReference("conversations")
                        .child(mUser.getUid()).child(mConversation.getId());

                convRef.child("hasNew").setValue(0);
                convRef.child("touched").setValue(ServerValue.TIMESTAMP);
            }
        }

        mLayoutManager = new LinearLayoutManager(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.message_list, container, false);

        if (mConversation.getId() == null) {
            DatabaseReference ref1 = FirebaseDatabase.getInstance()
                    .getReference("conversations")
                    .child(mUser.getUid());

            DatabaseReference ref2 = FirebaseDatabase.getInstance()
                    .getReference("conversations")
                    .child(mConversation.getTarget());

            mConversation.setId(ref1.push().getKey());
            ref1.child(mConversation.getId()).setValue(mConversation);

            Conversation conv2 = new Conversation(
                    mUser.getUid(),
                    mUser.getDisplayName(),
                    mConversation.getTouched(),
                    mConversation.getHasNew(),
                    mConversation.getId()
            );

            ref2.child(mConversation.getId()).setValue(conv2);
        }

        mMessageRef = FirebaseDatabase.getInstance()
                .getReference("messages")
                .child(mUser.getUid())
                .child(mConversation.getId());

        // Show conversation in a RecyclerView
        mRecyclerView = rootView.findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(getContext(), mMessageRef);
        mMessageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = mMessageAdapter.getItemCount();
                int lastVisiblePosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mMessageAdapter);

        mMessageEditText = rootView.findViewById(R.id.edittext_chatbox);
        mMessageEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        mMessageButton = rootView.findViewById(R.id.button_chatbox_send);
        mMessageButton.setOnClickListener(view -> sendMessage());

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Write new message to database.
     */
    public void sendMessage() {
        String messageText = mMessageEditText.getText().toString();

        if (!messageText.isEmpty()) {
            Message message = new Message();
            message.setId(mMessageRef.push().getKey());
            message.setDate(new Date());
            message.setSender(mUser.getUid());
            message.setSenderName(mUser.getDisplayName());
            message.setText(messageText);
            message.setTarget(mConversation.getTarget());

            DatabaseReference rootRef = FirebaseDatabase.getInstance()
                    .getReference("messages");

            rootRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    mutableData.child(message.getSender()).child(mConversation.getId()).child(message.getId()).setValue(message);
                    mutableData.child(message.getTarget()).child(mConversation.getId()).child(message.getId()).setValue(message);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    Log.d("MessageList", "Created message from "+ message.getSender() + " to " + message.getTarget());

                    if (databaseError == null) {
                        updateConversations();

                        mMessageEditText.setText("");
                        // Hide keyboard
                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                    } else {
                        Toast.makeText(
                            getContext(),
                            "Error sending message. Please try again shortly.",
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            });
        }
    }

    /**
     * Run transaction to update target conversations
     */
    private void updateConversations() {
        DatabaseReference convRef = FirebaseDatabase.getInstance()
                .getReference("conversations");

        convRef.runTransaction(new Transaction.Handler() {
            @Override public Transaction.Result doTransaction(MutableData mutableData) {
                // Update touch times
                mutableData.child(mConversation.getTarget()).child(mConversation.getId()).child("touched").setValue(ServerValue.TIMESTAMP);
                mutableData.child(mUser.getUid()).child(mConversation.getId()).child("touched").setValue(ServerValue.TIMESTAMP);

                // Update new counter
                Object value = mutableData.child(mConversation.getTarget())
                        .child(mConversation.getId())
                        .child("hasNew").getValue();
                int count;
                if (value == null) {
                    count = 0;
                } else {
                    count =((Long) value).intValue();
                }

                mutableData.child(mConversation.getTarget())
                        .child(mConversation.getId())
                        .child("hasNew").setValue(++count);

                return Transaction.success(mutableData);
            }
            @Override public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Toast.makeText(
                            getContext(),
                            "Database error!",
                            Toast.LENGTH_SHORT
                    ).show();

                    Log.e("MessageList", databaseError.getMessage());
                }
            }
        });
    }
}

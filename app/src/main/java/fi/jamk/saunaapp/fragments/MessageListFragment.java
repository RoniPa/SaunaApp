package fi.jamk.saunaapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        }

        mLayoutManager = new LinearLayoutManager(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.message_list, container, false);

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
            mMessageRef.child(message.getId()).setValue(message, (databaseError, databaseReference) -> {
                if (databaseError == null) {
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
            });
        }
    }
}

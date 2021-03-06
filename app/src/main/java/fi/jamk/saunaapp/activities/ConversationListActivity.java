package fi.jamk.saunaapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Locale;

import fi.jamk.saunaapp.R;

import fi.jamk.saunaapp.models.Conversation;
import fi.jamk.saunaapp.util.ConversationListAdapter;
import fi.jamk.saunaapp.util.RecyclerItemClickListener;
import fi.jamk.saunaapp.viewholders.BindableViewHolder;

/**
 * An activity representing a list of Conversations. The activity presents
 * a list of items, which when touched, lead to a {@link MessageListActivity}
 * representing conversation messages.
 */
public class ConversationListActivity extends BaseActivity {

    public static final String CONV_DETAIL_ITEM = "conversation_detail_item";

    private FirebaseUser mUser;
    private ConversationListAdapter mListAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mLinearLayoutManager = new LinearLayoutManager(this);

        // This reverses data on client side. Firebase Realtime Database
        // doesn't support descending queries, and we cannot use negative
        // Server timestamp without modifying it in functions or in a
        // second request (after saving).
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);

        mRecyclerView = findViewById(R.id.conversation_list);
        if (mRecyclerView != null) {
            setupRecyclerView(this, mRecyclerView);
        }
    }

    /**
     * Setup {@link RecyclerView} for current user's conversations.
     *
     * @param context
     * @param recyclerView
     */
    private void setupRecyclerView(@NonNull Context context, @NonNull RecyclerView recyclerView) {
        Query ref = FirebaseDatabase.getInstance()
                .getReference("conversations")
                .child(mUser.getUid())
                .orderByChild("touched");

        mListAdapter = new ConversationListAdapter(context, ref);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(mListAdapter);
        recyclerView.addOnItemTouchListener(
            new RecyclerItemClickListener(this, recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent startIntent = new Intent(ConversationListActivity.this, MessageListActivity.class);
                        startIntent.putExtra(CONV_DETAIL_ITEM, mListAdapter.getItem(position));
                        startActivity(startIntent);
                    }
                    @Override
                    public void onLongItemClick(View view, int position) {}
                }));
    }
}

package jordan.astory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jordan on 1/13/2016.
 */
public class CommentActivity extends Activity {
    ListView listView;
    ArrayList<DBComment> commentsList;
    FirebaseListAdapter mAdapter;
    Firebase masterRootRef;
    Firebase masterCommentsDB;
    Firebase masterStoriesDB;
    String currentUser;
    String date;
    String postDate;
    String today;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_layout);
        listView = (ListView) findViewById(R.id.list);
        commentsList = new ArrayList<>();
        Intent intent = getIntent();
        String storyName = intent.getStringExtra(Constants.EXTRA_STORY_COMMENT);
        currentUser = intent.getStringExtra(Constants.EXTRA_CURRENT_USER);
        date = intent.getStringExtra(Constants.EXTRA_STORY_DATE_KEY);
        postDate = intent.getStringExtra(Constants.EXTRA_STORY_DATE);
        SimpleDateFormat s = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        Date d = new Date();
        if(postDate != null) {
            try {
                d = s.parse(postDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        SimpleDateFormat s2 = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        today = s2.format(d);
        if(!date.equals("")){
            masterRootRef = new Firebase("https://astory.firebaseio.com/");
            masterCommentsDB = masterRootRef.child("comments").child(storyName);
            masterStoriesDB = masterRootRef.child("stories").child(storyName);
        }else{
            masterRootRef = new Firebase("https://astory.firebaseio.com/"+today);
            masterCommentsDB = masterRootRef.child("comments").child(storyName);
            masterStoriesDB = masterRootRef.child("stories").child(storyName);
        }
;
        final Firebase rootRef = new Firebase("https://astory.firebaseio.com/"+date);
        final Firebase commentsDB = rootRef.child("comments").child(storyName);
        final Firebase storiesDB = rootRef.child("stories").child(storyName);
        mAdapter = new FirebaseListAdapter<DBComment>(this, DBComment.class, android.R.layout.two_line_list_item, commentsDB){
            @Override
            protected void populateView(View view, DBComment comment){
                ((TextView)view.findViewById(android.R.id.text1)).setText(comment.getAuthor());
                ((TextView)view.findViewById(android.R.id.text2)).setText(comment.getMessage());

            }
        };
        listView.setAdapter(mAdapter);

        final EditText mMessage = (EditText) findViewById(R.id.message_text);
        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                commentsDB.push().setValue(new DBComment(currentUser, mMessage.getText().toString()));
                storiesDB.child("commentCount").setValue(mAdapter.getCount() + 1);
                if(postDate != null) {
                    masterCommentsDB.push().setValue(new DBComment(currentUser, mMessage.getText().toString()));
                    masterStoriesDB.child("commentCount").setValue(mAdapter.getCount() + 1);
                }
                mMessage.setText("");
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }


}

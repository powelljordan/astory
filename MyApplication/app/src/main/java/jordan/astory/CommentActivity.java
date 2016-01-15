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

import java.util.ArrayList;

/**
 * Created by Jordan on 1/13/2016.
 */
public class CommentActivity extends Activity {
    ListView listView;
    ArrayList<DBComment> commentsList;
    FirebaseListAdapter mAdapter;
    String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_layout);
        listView = (ListView) findViewById(R.id.list);
        commentsList = new ArrayList<>();
        Intent intent = getIntent();
        String storyName = intent.getStringExtra(Constants.EXTRA_STORY_COMMENT);
        currentUser = intent.getStringExtra(Constants.EXTRA_CURRENT_USER);
        final Firebase commentsDB = new Firebase("https://astory.firebaseio.com/comments").child(storyName);
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
                mMessage.setText("");
            }
        });



    }


    //        commentsDB.addValueEventListener(new ValueEventListener(){
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot){
//                for(DataSnapshot apshot: dataSnapshot.getChildren()){
//                    commentsList.add(msgSnapshot.getValue(DBComment.class));
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError){
//                Log.e("Comments", "The read failed: " + firebaseError.getMessage());
//            }
//        });
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }


}

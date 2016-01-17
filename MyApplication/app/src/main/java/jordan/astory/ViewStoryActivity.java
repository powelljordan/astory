package jordan.astory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import org.w3c.dom.Text;

/**
 * Created by Jordan on 12/30/2015.
 */
public class ViewStoryActivity extends AppCompatActivity {
    //TODO Add button for handling adding pictures or videos to a story
    //TODO Think of a way to make these viewable. Maybe visit the old wireframes
    private String name;
    private String content;
    private String author;
    private String currentUser;
    private Uri mediaUri;
    private ImageButton deleteButton;
    private Firebase usersDB = new Firebase("https://astory.firebaseio.com/users");
    private Firebase storiesDB = new Firebase("https://astory.firebaseio.com/stories");
    private Firebase geoStoriesDB = new Firebase("https://astory.firebasio.com/geoStories");
    private Firebase commentsDB = new Firebase("https://astory.firebasio.com/comments");
    private String TAG = "ViewStoryActivity";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_story);
        Intent intent = getIntent();

        //Story details
        name = intent.getStringExtra(Constants.EXTRA_STORY_NAME);
        content = intent.getStringExtra(Constants.EXTRA_STORY_CONTENT);
        author = intent.getStringExtra(Constants.EXTRA_STORY_AUTHOR);
        currentUser = intent.getStringExtra(Constants.EXTRA_CURRENT_USER);
        Log.d(TAG, "author: " + author);

        //Delete move button
        deleteButton = (ImageButton) findViewById(R.id.delete_story_button);
        if(currentUser.equals(author)){
            deleteButton.setVisibility(View.VISIBLE);
        }

        //Initialize textview
        TextView nameText = (TextView) findViewById(R.id.view_story_name);
        TextView contentText = (TextView) findViewById(R.id.view_story_content);
        TextView authorText = (TextView) findViewById(R.id.view_story_author);
        authorText.setText(author);
        nameText.setText(name);
        contentText.setText(content);

        Log.d(TAG, "currentUser: " + currentUser);
        Log.d(TAG, "author: " + author);
        Log.d(TAG, "name: " + name);

    }


    public void onDeleteStory(View v){
        new AlertDialog.Builder(ViewStoryActivity.this)
                .setTitle("Delete story")
                .setMessage("Are you sure you want to delete this story?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_OK, new Intent().putExtra(Constants.VIEW_STORY_KEY, name));
                        storiesDB.child(name).removeValue();
                        geoStoriesDB.child(name).removeValue();
                        commentsDB.child(name).removeValue();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    public void goToComments(View v){
        Intent commentIntent = new Intent(this, CommentActivity.class);
        commentIntent.putExtra(Constants.EXTRA_STORY_COMMENT, name);
        commentIntent.putExtra(Constants.EXTRA_CURRENT_USER, currentUser);
        startActivity(commentIntent);
    }

    public void goToMedia(View v){
        Intent mediaIntent = new Intent(this, MediaActivity.class);
        Log.d(TAG, "viewStory storyName: " + name);
        mediaIntent.putExtra(Constants.MEDIA_STORY_NAME, name);
        mediaIntent.putExtra(Constants.EXTRA_CURRENT_USER, currentUser);
        mediaIntent.putExtra(Constants.EXTRA_STORY_AUTHOR, author);
        startActivityForResult(mediaIntent, Constants.MEDIA_REQUEST_CODE);
    }



}

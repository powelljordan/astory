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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jordan on 12/30/2015.
 */
public class ViewStoryActivity extends AppCompatActivity {
    //TODO Add button for handling adding pictures or videos to a story
    //TODO Think of a way to make these viewable. Maybe visit the old wireframes
    private String name;
    private String content;
    private String author;
    private String date;
    private String dateKey;
    private String currentUser;
    private Uri mediaUri;
    private ImageButton deleteButton;
    private ImageView profile;
    private Firebase rootRef;
    private Firebase storiesDB;
    private Firebase geoStoriesDB;
    private Firebase commentsDB;
    private Firebase masterRootRef;
    private Firebase masterStoriesDB;
    private Firebase masterGeoStoriesDB;
    private Firebase masterCommentsDB;
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
        date = intent.getStringExtra(Constants.EXTRA_STORY_DATE);
        currentUser = intent.getStringExtra(Constants.EXTRA_CURRENT_USER);
        dateKey = intent.getStringExtra(Constants.EXTRA_STORY_DATE_KEY);

        handleDatabase(dateKey);
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
        TextView dateText = (TextView) findViewById(R.id.view_story_date);
        authorText.setText(author);
        nameText.setText(name);
        contentText.setText(content);
        dateText.setText(date);

        Log.d(TAG, "currentUser: " + currentUser);
        Log.d(TAG, "author: " + author);
        Log.d(TAG, "name: " + name);

    }
    public void handleDatabase(String date){
        rootRef = new Firebase("https://astory.firebaseio.com/"+date);
        storiesDB = rootRef.child("stories");
        commentsDB = rootRef.child("comments");
        geoStoriesDB = rootRef.child("geoStories");
        if(!date.equals("")){
            masterRootRef = new Firebase("https://astory.firebaseio.com");
        }else{
            SimpleDateFormat s = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
            Date d = new Date();
            if(date != null) {
                try {
                    d = s.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            SimpleDateFormat s2 = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            String today = s2.format(d);
            masterRootRef = new Firebase("https://astory.firebaseio.com/"+today);
        }
        masterStoriesDB = masterRootRef.child("stories");
        masterGeoStoriesDB = masterRootRef.child("geoStories");
        masterCommentsDB = masterRootRef.child("comments");
    }

    public void onDeleteStory(View v){
        new AlertDialog.Builder(ViewStoryActivity.this)
                .setTitle("Delete story")
                .setMessage("Are you sure you want to delete this story?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_OK, new Intent().putExtra(Constants.VIEW_STORY_KEY, name));
                        removeStory();
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
    public void removeStory(){
        storiesDB.child(name).removeValue();
        geoStoriesDB.child(name).removeValue();
        commentsDB.child(name).removeValue();
        masterStoriesDB.child(name).removeValue();
        masterGeoStoriesDB.child(name).removeValue();
        masterCommentsDB.child(name).removeValue();
    }

    public void goToComments(View v){
        Intent commentIntent = new Intent(this, CommentActivity.class);
        commentIntent.putExtra(Constants.EXTRA_STORY_COMMENT, name);
        commentIntent.putExtra(Constants.EXTRA_CURRENT_USER, currentUser);
        commentIntent.putExtra(Constants.EXTRA_STORY_DATE, date);
        commentIntent.putExtra(Constants.EXTRA_STORY_DATE_KEY, dateKey);
        startActivity(commentIntent);
    }

    public void goToMedia(View v){
        Intent mediaIntent = new Intent(this, MediaActivity.class);
        Log.d(TAG, "viewStory storyName: " + name);
        mediaIntent.putExtra(Constants.MEDIA_STORY_NAME, name);
        mediaIntent.putExtra(Constants.EXTRA_CURRENT_USER, currentUser);
        mediaIntent.putExtra(Constants.EXTRA_STORY_AUTHOR, author);
        mediaIntent.putExtra(Constants.EXTRA_STORY_DATE, date);
        mediaIntent.putExtra(Constants.EXTRA_STORY_DATE_KEY, dateKey);
        startActivityForResult(mediaIntent, Constants.MEDIA_REQUEST_CODE);
        finish();
    }

    public void goToProfile(View v){
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra(Constants.PROFILE_NAME, author);
        profileIntent.putExtra(Constants.PROFILE_CURRENT_USER, currentUser);
        startActivity(profileIntent);

    }



}

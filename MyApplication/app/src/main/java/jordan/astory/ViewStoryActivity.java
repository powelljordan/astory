package jordan.astory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import org.w3c.dom.Text;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Jordan on 12/30/2015.
 */
public class ViewStoryActivity extends AppCompatActivity {
    //TODO Add button for handling adding pictures or videos to a story
    //TODO Think of a way to make these viewable. Maybe visit the old wireframes
    private String name;
    private String content;
    private String author;
    private String uid;
    private String date;
    private String dateKey;
    private String currentUser;
    private String currentUserID;
    private String today;
    private Integer vote_count;
    private Uri mediaUri;
    private ImageButton deleteButton;
    private WebView webView;
    private ImageView profile;
    private Firebase rootRef;
    private Firebase storiesDB;
    private Firebase geoStoriesDB;
    private Firebase commentsDB;
    private Firebase masterRootRef;
    private Firebase masterStoriesDB;
    private Firebase masterGeoStoriesDB;
    private Firebase masterCommentsDB;

    public FloatingActionButton upvoteButton;

    private SharedPreferences mSharedPreferences;
    private String TAG = "ViewStoryActivity";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_story);
        Intent intent = getIntent();

        Firebase credentialsRef = new Firebase("https://astory.firebaseio.com");
        if(credentialsRef.getAuth() == null) {
            finish();
        }
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        currentUserID = mSharedPreferences.getString(Constants.CURRENT_USER_ID_KEY, "N/A");
        //Story details
        name = intent.getStringExtra(Constants.EXTRA_STORY_NAME);
        content = intent.getStringExtra(Constants.EXTRA_STORY_CONTENT);
        author = intent.getStringExtra(Constants.EXTRA_STORY_AUTHOR);
        uid = intent.getStringExtra(Constants.EXTRA_STORY_UID);
        date = intent.getStringExtra(Constants.EXTRA_STORY_DATE);
        currentUser = intent.getStringExtra(Constants.EXTRA_CURRENT_USER);
        dateKey = intent.getStringExtra(Constants.EXTRA_STORY_DATE_KEY);

        handleDatabase(dateKey);
        Log.d(TAG, "author: " + author);
        webView = (WebView) findViewById(R.id.view_story_webview);
        loadMedia();
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

        FloatingActionButton commentButton = (FloatingActionButton)findViewById(R.id.comment);
        upvoteButton = (FloatingActionButton)findViewById(R.id.upvote);
        FloatingActionButton pictureButton = (FloatingActionButton)findViewById(R.id.takePicture);
        FloatingActionButton videoButton = (FloatingActionButton)findViewById(R.id.recordVideo);
        Set<String> upvotedStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, new HashSet<String>()));
//        mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, upvotedStories);
        Log.d(TAG, "onCreate upvotedStories: "+upvotedStories);
        if(upvotedStories.contains(name)){
            upvoteButton.setColorNormalResId(R.color.deep_sky_blue);
            Log.d(TAG, "should be blue");
        }

        upvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> upvotedStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, new HashSet<String>()));
                mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, upvotedStories);
                if (upvotedStories.contains(name)) {
                    Log.d(TAG, "upVotedStories contains name");
                    upvoteButton.setColorNormalResId(R.color.white);
//                    upvoteButton.setColorNormal(R.color.white);
                    upvotedStories.remove(name);
                    addToVoteCount(-1);
                } else {
                    Log.d(TAG, "changes color to pink");
                    upvoteButton.setColorNormalResId(R.color.deep_sky_blue);
                    upvotedStories.add(name);
                    addToVoteCount(+1);
                }
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putStringSet(Constants.UPVOTED_STORIES, upvotedStories);
                editor.putString(Constants.CURRENT_USER_ID_KEY, currentUserID);
                editor.apply();
//                mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, upvotedStories);
                Log.d(TAG, "upvotedStories: " + upvotedStories);
            }
        });
        final Firebase storyRef = new Firebase("https://astory.firebaseio.com").child("stories");
        storyRef.child(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView count = (TextView) findViewById(R.id.vote_count);
                if (dataSnapshot.getValue(DBStory.class).getVoteCount() != null) {
                    vote_count = dataSnapshot.getValue(DBStory.class).getVoteCount();
                    count.setText(dataSnapshot.getValue(DBStory.class).getVoteCount().toString());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        if(currentUser.equals(author)){
            upvoteButton.setVisibility(View.GONE);
        }else{
            pictureButton.setVisibility(View.GONE);
            videoButton.setVisibility(View.GONE);
        }


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
            today = s2.format(d);
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

    public void loadMedia(){
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        Firebase storyRef = new Firebase("https://astory.firebaseio.com").child("stories");
        final String[] result = new String[1];
        storyRef.child(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                result[0] = dataSnapshot.getValue(DBStory.class).getMediaUri();
                if (result[0] != null) {
                    webView.loadUrl(result[0]);
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

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
//        finish();
    }

    public void goToProfile(View v){
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra(Constants.PROFILE_NAME, author);
        profileIntent.putExtra(Constants.PROFILE_CURRENT_USER, currentUser);
        profileIntent.putExtra(Constants.PROFILE_AUTHOR, author);
        if(uid == null && currentUser.equals(author)){
            uid = currentUserID;
        }else{
            if(uid == null){
                Toast.makeText(getApplicationContext(), "Sorry, this user's stories aren't available right now", Toast.LENGTH_SHORT).show();
                return;
            }

        }
        profileIntent.putExtra(Constants.EXTRA_STORY_UID, uid);
        startActivity(profileIntent);

    }



    public void addToVoteCount(final int delta){
        final Firebase storyRef = new Firebase("https://astory.firebaseio.com").child("stories");
        final Firebase todayStoryRef = new Firebase("https://astory.firebaseio.com/"+today).child("stories");
        vote_count+=delta;
        storyRef.child(name).child("voteCount").setValue(vote_count);


//        todayStoryRef.child(name).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Integer currentCount = 0;
//                if(dataSnapshot.getValue(DBStory.class).getVoteCount() == null){
//                    currentCount = dataSnapshot.getValue(DBStory.class).getVoteCount();
//                    TextView count = (TextView) findViewById(R.id.upvote);
//                    count.setText(currentCount);
//                }
//                todayStoryRef.child(name).child("voteCount").setValue(currentCount + delta);
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });
    }



}

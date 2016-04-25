package jordan.astory;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
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
public class ViewStoryActivity extends AppCompatActivity implements SelectEmoticonFragment.SelectEmoticonFragmentListener, ShowEmoticonsFragment.ShowEmoticonsFragmentListener{
    //TODO Add button for handling adding pictures or videos to a story
    //TODO Think of a way to make these viewable. Maybe visit the old wireframes
    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "aStory";

    private Uri fileUri; // file url to store image/video
    private URL fileURL;
    private String filePath;
    private String name;
    private String storyId;
    private String content;
    private String author;
    private String uid;
    private String date;
    private String dateKey;
    private String currentUser;
    private String currentUserID;
    private String today;
    private Integer vote_count;
    private Integer happy_count;
    private Integer sad_count;
    private Integer mad_count;
    private Integer surprised_count;
    private Integer viewCount;

    private LinearLayout emoticons;
    private FloatingActionButton timeTravel;
    Set<String> upvotedStories;
    private Set<String> happyStories;
    private Set<String> sadStories;
    private Set<String> madStories;
    private Set<String> surprisedStories;
    private Set<String> seenStories;
    private Uri mediaUri;
    private String youtubeVideoID;
    private ImageButton deleteButton;
    private ProgressBar progressBar;
    private WebView webView;
    private String mediaType;
    private ImageView profile;
    private static File mediaFile;
    private ImageView happy, sad, mad, surprised;
    private Firebase rootRef;
    private Firebase storiesDB;
    private GeoFire geoStoriesDB;
    private Firebase commentsDB;
    private Firebase masterRootRef;
    private Firebase masterStoriesDB;
    private GeoFire masterGeoStoriesDB;
    private Firebase masterCommentsDB;
    private Firebase upvoteRootRef;
    private Firebase upvoteStoriesDB;
    private Firebase upvoteGeoStoriesDB;
    private Firebase upvoteCommentsDB;
    private Firebase notificationsDB;
    private Uri.Builder builder;
    private String oldURL;
    private boolean editMode = false;

    private HashSet<String> myStories;


    private FloatingActionsMenu mainMenu;



    public FloatingActionButton upvoteButton;
    public FloatingActionButton videoButton;
    public FloatingActionButton pictureButton;
    public FloatingActionButton saveButton;

    private boolean adding;
    private boolean loadingMedia;

    private Permissions permissions;
    private Boolean storageAccepted;

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
        permissions = new Permissions(this);
        Log.d(TAG, "hasPermission: " + permissions.hasPermission(Constants.STORAGE_PERMS[0]));
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        currentUserID = mSharedPreferences.getString(Constants.CURRENT_USER_ID_KEY, "N/A");
        myStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.MY_STORIES_KEY, new HashSet<String>()));
        //Story details
        adding = intent.getExtras().getBoolean(Constants.EXTRA_ADDED_KEY);
        name = intent.getStringExtra(Constants.EXTRA_STORY_NAME);
        storyId = intent.getStringExtra(Constants.EXTRA_STORY_ID);
        content = intent.getStringExtra(Constants.EXTRA_STORY_CONTENT);
        author = intent.getStringExtra(Constants.EXTRA_STORY_AUTHOR);
        uid = intent.getStringExtra(Constants.EXTRA_STORY_UID);
        date = intent.getStringExtra(Constants.EXTRA_STORY_DATE);
        currentUser = intent.getStringExtra(Constants.EXTRA_CURRENT_USER);
        dateKey = intent.getStringExtra(Constants.EXTRA_STORY_DATE_KEY);
        loadingMedia = false;

        mainMenu = (FloatingActionsMenu) findViewById(R.id.view_story_main_menu);



        //Parse Date
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

        handleDatabase(dateKey);
        handleCaptureButtons(intent);
        Log.d(TAG, "author: " + author);
        webView = (WebView) findViewById(R.id.view_story_webview);
        loadMedia();
        //Delete move button
        deleteButton = (ImageButton) findViewById(R.id.delete_story_button);
        if(currentUserID.equals(uid)){
            deleteButton.setVisibility(View.VISIBLE);
        }

        //Initialize textview
        final EditText nameEditText = (EditText) findViewById(R.id.view_story_name_edit);
        final TextView nameText = (TextView) findViewById(R.id.view_story_name);
        final EditText contentEditText = (EditText) findViewById(R.id.view_story_content_edit);
        final TextView contentText = (TextView) findViewById(R.id.view_story_content);
        TextView authorText = (TextView) findViewById(R.id.view_story_author);
        TextView dateText = (TextView) findViewById(R.id.view_story_date);
        authorText.setText(author);
        nameText.setText(name);
        contentText.setText(content);
        nameEditText.setText(name);
        contentEditText.setText(content);
        dateText.setText(date);
        timeTravel = (FloatingActionButton) findViewById(R.id.timeTravel);
        timeTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadYoutube(youtubeVideoID);
            }
        });

        progressBar = (ProgressBar)findViewById(R.id.progress);


        Log.d(TAG, "currentUser: " + currentUser);
        Log.d(TAG, "author: " + author);
        Log.d(TAG, "name: " + name);

        seenStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.SEEN_STORIES, new HashSet<String>()));

        happy = (ImageView) findViewById(R.id.happyCount);
        sad = (ImageView) findViewById(R.id.sadCount);
        mad = (ImageView) findViewById(R.id.madCount);
        surprised = (ImageView) findViewById(R.id.surprisedCount);

        emoticons = (LinearLayout) findViewById(R.id.emoticons);
        emoticons.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDisplayEmoticonsDialog(v);
            }
        });

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        seenStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.SEEN_STORIES, new HashSet<String>()));
//        mSharedPreferences.getStringSet(Constants.SEEN_STORIES, seenStories);
        seenStories.add(storyId);
        Log.d(TAG, "seenStories: " + seenStories);
//        editor.putStringSet(Constants.SEEN_STORIES, seenStories);
        editor.putString(Constants.CURRENT_STORY, storyId);
        editor.putStringSet(Constants.SEEN_STORIES, seenStories);
        editor.apply();

        final FloatingActionButton commentButton = (FloatingActionButton)findViewById(R.id.comment);
        upvoteButton = (FloatingActionButton)findViewById(R.id.upvote);
        pictureButton = (FloatingActionButton)findViewById(R.id.takePicture);
        videoButton = (FloatingActionButton)findViewById(R.id.recordVideo);
        saveButton = (FloatingActionButton) findViewById(R.id.save);
        upvotedStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, new HashSet<String>()));
//        mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, upvotedStories);
        Log.d(TAG, "onCreate upvotedStories: " + upvotedStories);
        if(upvotedStories.contains(storyId)){
            upvoteButton.setColorNormalResId(R.color.deep_sky_blue);
            Log.d(TAG, "should be blue");
        }

        if(adding){
            saveButton.setVisibility(View.VISIBLE);
            pictureButton.setVisibility(View.VISIBLE);
            videoButton.setVisibility(View.VISIBLE);
            commentButton.setVisibility(View.GONE);
            nameText.setVisibility(View.GONE);
            contentText.setVisibility(View.GONE);
            nameEditText.setVisibility(View.VISIBLE);
            contentEditText.setVisibility(View.VISIBLE);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Firebase usersDB = new Firebase("https://astory.firebaseio.com/users/");
                storiesDB.child(storyId).child("name").setValue(nameEditText.getText().toString());
                storiesDB.child(storyId).child("content").setValue(contentEditText.getText().toString());
                masterStoriesDB.child(storyId).child("name").setValue(nameEditText.getText().toString());
                masterStoriesDB.child(storyId).child("content").setValue(contentEditText.getText().toString());
                usersDB.child(currentUserID).child("stories").child(storyId).child("name").setValue(nameEditText.getText().toString());
                usersDB.child(currentUserID).child("stories").child(storyId).child("content").setValue(contentEditText.getText().toString());
                nameText.setText(nameEditText.getText());
                contentText.setText(contentEditText.getText());
                saveButton.setVisibility(View.INVISIBLE);
                pictureButton.setVisibility(View.GONE);
                videoButton.setVisibility(View.GONE);
                commentButton.setVisibility(View.VISIBLE);
                nameText.setVisibility(View.VISIBLE);
                contentText.setVisibility(View.VISIBLE);
                nameEditText.setVisibility(View.GONE);
                contentEditText.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Your story has been saved!", Toast.LENGTH_SHORT).show();

            }
        });

        upvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> upvotedStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, new HashSet<String>()));
                happyStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.HAPPY_STORIES, new HashSet<String>()));
                sadStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.SAD_STORIES, new HashSet<String>()));
                madStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.MAD_STORIES, new HashSet<String>()));
                surprisedStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.SURPRISED_STORIES, new HashSet<String>()));
                mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, upvotedStories);
                mSharedPreferences.getStringSet(Constants.HAPPY_STORIES, happyStories);
                mSharedPreferences.getStringSet(Constants.SAD_STORIES, sadStories);
                mSharedPreferences.getStringSet(Constants.MAD_STORIES, madStories);
                mSharedPreferences.getStringSet(Constants.SURPRISED_STORIES, surprisedStories);
                Log.d(TAG, "happyStories: " + happyStories);
                Log.d(TAG, "sadStories: " + sadStories);
                Log.d(TAG, "madStories: "+madStories);
                Log.d(TAG, "surprisedStories: "+surprisedStories);
                if(uid == null){
                    Toast.makeText(getApplicationContext(), "Sorry you can't upvote this story right now", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (upvotedStories.contains(storyId)) {
                    Log.d(TAG, "upVotedStories contains storyId");
                    upvoteButton.setColorNormalResId(R.color.white);
                    upvotedStories.remove(storyId);
                    addToVoteCount(-1, "voteCount", vote_count);
                    if(happyStories.contains(storyId)){
                        happyStories.remove(storyId);
                        addToVoteCount(-1, "happyCount", happy_count);
                    }
                    if(sadStories.contains(storyId)){
                        sadStories.remove(storyId);
                        addToVoteCount(-1, "sadCount", sad_count);
                    }
                    if(madStories.contains(storyId)){
                        madStories.remove(storyId);
                        addToVoteCount(-1, "madCount", mad_count);
                    }
                    if(surprisedStories.contains(storyId)){
                        surprisedStories.remove(storyId);
                        addToVoteCount(-1, "surprisedCount", surprised_count);
                    }
                } else {
                    showSelectEmoticonDialog(v);
                    upvoteButton.setColorNormalResId(R.color.deep_sky_blue);
                    upvotedStories.add(storyId);
                    addToVoteCount(+1, "voteCount", vote_count);
                }
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putStringSet(Constants.UPVOTED_STORIES, upvotedStories);
                editor.putStringSet(Constants.HAPPY_STORIES, happyStories);
                editor.putStringSet(Constants.SAD_STORIES, sadStories);
                editor.putStringSet(Constants.MAD_STORIES, madStories);
                editor.putStringSet(Constants.SURPRISED_STORIES, surprisedStories);
                editor.putString(Constants.CURRENT_USER_ID_KEY, currentUserID);
                editor.apply();
//                mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, upvotedStories);
                Log.d(TAG, "upvotedStories: " + upvotedStories);
            }
        });
        mainMenu.expand();



        final Firebase storyRef = new Firebase("https://astory.firebaseio.com").child("stories");
        storiesDB.child(storyId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(DBStory.class) == null){
                    return;
                }
                viewCount = dataSnapshot.getValue(DBStory.class).getViewCount();
                addToViewCount();
                Log.d(TAG, "youtubeVideoID: " + dataSnapshot.getValue(DBStory.class).getYoutubeVideoID());
                if(dataSnapshot.getValue(DBStory.class).getYoutubeVideoID() != null){
                    //Make button visible
                    youtubeVideoID = dataSnapshot.getValue(DBStory.class).getYoutubeVideoID();
                    timeTravel.setVisibility(View.VISIBLE);
                }
                if(!currentUserID.equals(uid)) {
                    TextView count = (TextView) findViewById(R.id.vote_count);
                    if(dataSnapshot.getValue(DBStory.class)!= null) {
                        if (dataSnapshot.getValue(DBStory.class).getVoteCount() != null) {
                            vote_count = dataSnapshot.getValue(DBStory.class).getVoteCount();
                            happy_count = dataSnapshot.getValue(DBStory.class).getHappyCount();
                            sad_count = dataSnapshot.getValue(DBStory.class).getSadCount();
                            mad_count = dataSnapshot.getValue(DBStory.class).getMadCount();
                            surprised_count = dataSnapshot.getValue(DBStory.class).getSurprisedCount();
                            count.setText("+" + dataSnapshot.getValue(DBStory.class).getVoteCount().toString());
                            if(happy_count > 0){
                                happy.setVisibility(View.VISIBLE);
                            }else{
                                happy.setVisibility(View.INVISIBLE);
                            }

                            if(sad_count > 0){
                                sad.setVisibility(View.VISIBLE);
                            }else{
                                sad.setVisibility(View.INVISIBLE);
                            }

                            if(mad_count > 0){
                                mad.setVisibility(View.VISIBLE);
                            }else{
                                mad.setVisibility(View.INVISIBLE);
                            }

                            if(surprised_count > 0){
                                surprised.setVisibility(View.VISIBLE);
                            }else{
                                surprised.setVisibility(View.INVISIBLE);
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        if(currentUserID.equals(uid)){
            upvoteButton.setVisibility(View.GONE);
            if(oldURL != null){
                mainMenu.expand();
            }

        }else{
            pictureButton.setVisibility(View.GONE);
            videoButton.setVisibility(View.GONE);
        }


        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // get the context for the current activity
                Constants.IDENTITY_POOL_ID, // your identity pool id
                Regions.US_EAST_1 //Region
        );
        s3 = new AmazonS3Client(credentialsProvider);

        /**
         * Capture image button click event
         */
        pictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                loadingMedia = false;
                webView = (WebView) findViewById(R.id.view_story_webview);
                permissions.checkForPermissions(Constants.STORAGE_PERMS);
                captureImage();

            }
        });

        /**
         * Record video button click event
         */
        videoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // record video
                loadingMedia = false;
                webView = (WebView) findViewById(R.id.view_story_webview);
                permissions.checkForPermissions(Constants.STORAGE_PERMS);
                recordVideo();
            }
        });

        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }





    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){

            case Constants.PERMISSIONS_REQUEST_CODE:

//                storageAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;

                break;

        }

    }



    public void addToViewCount() {
        if (!seenStories.contains(storyId)) {
            seenStories.add(storyId);
            viewCount += 1;
            Log.d(TAG, "seenStories: " + seenStories);
            storiesDB.child(storyId).child("viewCount").setValue(viewCount);
            masterStoriesDB.child(storyId).child("viewCount").setValue(viewCount);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putStringSet(Constants.SEEN_STORIES, seenStories);
            editor.apply();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Firebase credentialsRef = new Firebase("https://astory.firebaseio.com");
        if(credentialsRef.getAuth() == null) {
            finish();
        }



    }

    /**
     * Handles setting up capture buttons and finding media associated with the story
     * @param intent
     */
    private void handleCaptureButtons(Intent intent){
        storiesDB.child(storyId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DBStory dbStory = dataSnapshot.getValue(DBStory.class);
                        if (dbStory != null && dbStory.getMediaUri() != null && mediaUpToDate(dbStory)) {
                            Log.d(TAG, "Really? All these things are true");
                            try {
                                fileURL = new URL(dbStory.getMediaUri());
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            mediaType = dbStory.getMediaType();
                            Log.d(TAG, "Loading media: "+loadingMedia);
                            if (mediaType.equals("image")) {
                                syncMedia(fileURL);
                                if(!loadingMedia) {
                                    loadMedia();
                                }

                            } else if (mediaType.equals("video")) {
                                syncMedia(fileURL);
                                if(!loadingMedia){
                                    loadMedia();
                                }
                            }
                        } else if (dbStory != null && dbStory.getMediaType() != null && !mediaUpToDate(dbStory)) {
                            mediaType = dbStory.getMediaType();
                            Log.d(TAG, "media Type: " + mediaType);
                            if (dbStory.getMediaType().equals("image")) {
                                new getUri().execute("image/jpeg");
                                storiesDB.child(storyId).child("mediaUpdated").setValue(System.currentTimeMillis());
                                if (date != null) {
                                    masterStoriesDB.child(storyId).child("mediaUpdated").setValue(System.currentTimeMillis());

                                }
                            } else if (dbStory.getMediaType().equals("video")) {
                                new getUri().execute("video/mp4");
                                storiesDB.child(storyId).child("mediaUpdated").setValue(System.currentTimeMillis());
                                if (date != null) {
                                    masterStoriesDB.child(storyId).child("mediaUpdated").setValue(System.currentTimeMillis());
                                }
                            }
                            loadMedia();

                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }
    public void handleDatabase(String date){
        notificationsDB = new Firebase("https://astory.firebaseio.com/notifications");
        rootRef = new Firebase("https://astory.firebaseio.com/"+date);
        storiesDB = rootRef.child("stories");
        Log.d(TAG, "currentUser: " + currentUser + "\n author: "+author);
        if(currentUserID.equals(uid)) {
            Firebase credentialsRef = new Firebase("https://astory.firebaseio.com");
            storiesDB.child(storyId).child("uid").setValue(credentialsRef.getAuth().getUid());
        }
        commentsDB = rootRef.child("comments");
        geoStoriesDB = new GeoFire(rootRef.child("geoStories"));
        upvoteRootRef = new Firebase("https://astory.firebaseio.com");
        if(!date.equals("")){
            masterRootRef = new Firebase("https://astory.firebaseio.com");
            masterStoriesDB = masterRootRef.child("stories");

        }else{
            masterRootRef = new Firebase("https://astory.firebaseio.com/"+today);
            masterStoriesDB = masterRootRef.child("stories");
            storiesDB.child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String originalDate = dataSnapshot.getValue(DBStory.class).getDate();
                    if(originalDate == null){
                        originalDate = "January 15, 2016";
                    }
                    SimpleDateFormat s1 = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
                    Date d = new Date();
                    try {
                        Log.d(TAG, "original date: " + originalDate);
                        d = s1.parse(originalDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    SimpleDateFormat s2 = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
                    String storyDate = s2.format(d);
                    upvoteRootRef = new Firebase("https://astory.firebaseio.com/"+storyDate);
                    upvoteStoriesDB = upvoteRootRef.child("stories");
//                    upvoteGeoStoriesDB = upvoteRootRef.child("geoStories");
                    upvoteCommentsDB = upvoteRootRef.child("comments");
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
        masterGeoStoriesDB = new GeoFire(masterRootRef.child("geoStories"));
        masterCommentsDB = masterRootRef.child("comments");
        if(currentUserID.equals(uid)) {
            Firebase credentialsRef = new Firebase("https://astory.firebaseio.com");
//            storiesDB.child(storyId).child("uid").setValue(credentialsRef.getAuth().getUid());
        }

    }

    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        // this device has a camera
// no camera on this device
        return getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }
    /**
     * Capturing Camera Image will lauch camera app requrest image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Recording video
     */
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Receiving activity result method will be called after closing the camera
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        Bitmap bitmap = null;
        if(requestCode == Constants.PROFILE_REQUEST_CODE && resultCode == RESULT_OK){
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_ID, data.getStringExtra(Constants.PROFILE_STORY_SELECTED_ID));
            resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_NAME, data.getStringExtra(Constants.PROFILE_STORY_SELECTED_NAME));
            resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_LATITUDE, data.getStringExtra(Constants.PROFILE_STORY_SELECTED_LATITUDE));
            resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_LONGITUDE, data.getStringExtra(Constants.PROFILE_STORY_SELECTED_LONGITUDE));
            if(Constants.PROFILE_STORY_SELECTED != null){
                setResult(RESULT_OK, resultIntent);
                finish();
            }

        }

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                progressBar.setVisibility(View.VISIBLE);
                // successfully captured the image
//                // compress it and save it in the same location
//                FileOutputStream fos = null;
//                try {
//                    bitmap = BitmapFactory.decodeStream(new FileInputStream(getOutputMediaFile(MEDIA_TYPE_IMAGE)));
//                    fos = new FileOutputStream(filePath);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, fos);
//                    fos.flush();
//                    fos.close();
//                } catch (FileNotFoundException e1) {
//                    e1.printStackTrace();
//                }catch (IOException e) {
//                    e.printStackTrace();
//                }

//                // display it in web view
//                Log.d(TAG, "absolute path: "+getOutputMediaFile(MEDIA_TYPE_IMAGE).getAbsolutePath());
                webView.setVisibility(View.INVISIBLE);
//                ImageView myImage = (ImageView) findViewById(R.id.view_story_imageview);
//                Bitmap myBitmap = BitmapFactory.decodeFile(new File(filePath).getAbsolutePath());
//                myImage.setImageBitmap(myBitmap);
//                int nh = (int) ( myBitmap.getHeight() * (2000.0 / myBitmap.getWidth()) );
//                Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 2000, nh, true);
//                Matrix matrix = new Matrix();
//
//                matrix.postRotate(90);
//
//                Bitmap scaledBitmap = Bitmap.createScaledBitmap(scaled,scaled.getWidth(),scaled.getHeight(),true);
//
//                Bitmap rotated = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
//                myImage.setImageBitmap(rotated);
                loadingMedia = true;
                new UploadMedia().execute();
                mediaType = "image";
                storiesDB.child(storyId).child("mediaUpdated").setValue(System.currentTimeMillis());
                if(date != null) {
                    masterStoriesDB.child(storyId).child("mediaUpdated").setValue(System.currentTimeMillis());
                }
                new getUri().execute("image/jpeg");
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                progressBar.setVisibility(View.VISIBLE);
                // video successfully recorded
                // preview the recorded video
                webView.setVisibility(View.INVISIBLE);
                VideoView myVideoView = (VideoView) findViewById(R.id.view_story_videoview);
                Log.d(TAG, "Set video URI");
//                myVideoView.setVideoURI(fileUri);
                loadingMedia = true;
                new UploadMedia().execute();
                mediaType = "video";
                storiesDB.child(storyId).child("mediaUpdated").setValue(System.currentTimeMillis());
                if(date != null) {
                    masterStoriesDB.child(storyId).child("mediaUpdated").setValue(System.currentTimeMillis());
                }
                new getUri().execute("video/mp4");
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
//        if(!(resultCode == Constants.PROFILE_REQUEST_CODE && adding)) {
//            Toast.makeText(getApplicationContext(), "Your story has been added", Toast.LENGTH_SHORT).show();
//        }
    }


    /**
     * ------------ Helper Methods ----------------------
     * */

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());

        if (type == MEDIA_TYPE_IMAGE) {
            filePath = mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg";
            mediaFile = new File(filePath);
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }



    public boolean mediaUpToDate(DBStory story){
        if(story.getMediaUpdated() == null){
            return false;
        }
        else{
            if((System.currentTimeMillis() - Double.parseDouble(story.getMediaUpdated())) < 15*60*1000){
                Log.d(TAG, "last updated "+(System.currentTimeMillis() - Double.parseDouble(story.getMediaUpdated())));
                return true;
            }else{
                return false;
            }
        }
    }
    public void syncToFirebase(String uriType){
        Log.d(TAG, "syncToFirebase called");
        Log.d(TAG, storiesDB.toString());
        Log.d(TAG, masterStoriesDB.toString());
        storiesDB.child(storyId).child("mediaType").setValue(uriType);
        storiesDB.child(storyId).child("mediaUri").setValue(fileURL);
        if(date != null){
            masterStoriesDB.child(storyId).child("mediaType").setValue(uriType);
            masterStoriesDB.child(storyId).child("mediaUri").setValue(fileURL);
        }


    }


    private AmazonS3 s3;
    private final String MY_BUCKET = "astory-media";

    @Override
    public void onFinishedShowEmoticons(String name) {

    }


    //Storing data
    private class UploadMedia extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(), // get the context for the current activity
                    Constants.IDENTITY_POOL_ID, // your identity pool id
                    Regions.US_EAST_1 //Region
            );
            s3 = new AmazonS3Client(credentialsProvider);
            Log.d(TAG, "s3: " + s3);

            // Set the region of your S3 bucket
            s3.setRegion(Region.getRegion(Regions.US_EAST_1));
            Log.d(TAG, "s3 after adding region: " + s3);
            TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
            TransferObserver observer = transferUtility.upload(
                    MY_BUCKET,
                    storyId,
                    mediaFile
            );


            observer.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state){
                //Do something on state change
                    Log.d(TAG, "transfer state: " + state);
                    if(state.equals(TransferState.COMPLETED)){
                        loadingMedia = true;
                        loadMedia();
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                //Do something on progress change.
                    float progress = (float)bytesCurrent/bytesTotal*100;
                    progressBar.setProgress((int) progress);
                    if((int)progress == 100){
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.d(TAG, "progress: "+((float)bytesCurrent/bytesTotal*100));
                }

                @Override
                public void onError(int id, Exception ex) {
//Do something on error
                }
            });
            return null;
        }
    }

    private class getUri extends AsyncTask<String, Void, URL>{

        @Override
        protected URL doInBackground(String... types) {
            ResponseHeaderOverrides override = new ResponseHeaderOverrides();
            override.setContentType(types[0]);
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest( MY_BUCKET, storyId);
            urlRequest.setExpiration(new Date(System.currentTimeMillis() + 7*24*60*60*1000 ) );  // Added a week's worth of milliseconds to the current time.
            urlRequest.setResponseHeaders(override);
            return s3.generatePresignedUrl(urlRequest);
        }

        @Override
        protected void onPostExecute(URL result){
            fileURL = result;
            Log.d(TAG, "Does this even happen: " + result.toString());
            syncMedia(result);

//            previewCapturedImage();
        }
    }



    public void onDeleteStory(View v){
        new AlertDialog.Builder(ViewStoryActivity.this)
                .setTitle("Delete story")
                .setMessage("Are you sure you want to delete this story?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_OK, new Intent().putExtra(Constants.VIEW_STORY_KEY, storyId));
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
        webView.setVisibility(View.VISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        Firebase storyRef = new Firebase("https://astory.firebaseio.com").child("stories");
        final String[] result = new String[1];
        storyRef.child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "within loadMedia loading media is : " + loadingMedia);
                if (dataSnapshot.getValue(DBStory.class) == null || loadingMedia) {
                    return;
                } else {
                    result[0] = dataSnapshot.getValue(DBStory.class).getMediaUri();
                    Log.d(TAG, "actual URL: " + result[0]);
//                if (oldURL == null) {
                    Log.d(TAG, "oldURL: " + oldURL);
                    oldURL = result[0];

                    if (result[0] != null) {
                        if (dataSnapshot.getValue(DBStory.class).getMediaType().equals("video")) {
                            ViewGroup.LayoutParams lp = webView.getLayoutParams();
                            lp.height = (int) pxFromDp(getApplicationContext(), 400);
                            webView.setLayoutParams(lp);
                            Log.d(TAG, "loaded URL");
                            if (oldURL == null || !oldURL.equals(result.toString())) {
                                oldURL = result.toString();
                                webView.loadUrl(result[0]);
                            }


                        }
                        if (dataSnapshot.getValue(DBStory.class).getMediaType().equals("image")) {
                            if (oldURL == null || !oldURL.equals(result.toString())) {
                                oldURL = result.toString();
                                webView.loadUrl(result[0]);
                            }
                        }
                    }
                    if (mediaUpToDate(dataSnapshot.getValue(DBStory.class))) {
                        loadingMedia = true;
                    }

//                }

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public void syncMedia(URL result){
        syncToFirebase(mediaType);
        if(oldURL == null || !oldURL.equals(result.toString())) {
            oldURL = result.toString();
        }
        loadMedia();
//        setResult(RESULT_OK, new Intent().putExtra(Constants.MEDIA_URL, result));
//        Log.d(TAG, "Definitely called setResult");
    }



    public void removeStory(){
        storiesDB.child(storyId).removeValue();
        geoStoriesDB.removeLocation(storyId);
        commentsDB.child(storyId).removeValue();
        Firebase userDB = new Firebase("https://astory.firebaseio.com/users");
        userDB.child(uid).child("stories").child(storyId).removeValue();
        masterStoriesDB.child(storyId).removeValue();
        masterGeoStoriesDB.removeLocation(storyId);
        masterCommentsDB.child(storyId).removeValue();
    }

    public void goToComments(View v){
        Intent commentIntent = new Intent(this, CommentActivity.class);
        commentIntent.putExtra(Constants.EXTRA_COMMENT_STORY_ID, storyId);
        commentIntent.putExtra(Constants.EXTRA_STORY_COMMENT, name);
        commentIntent.putExtra(Constants.EXTRA_CURRENT_USER, currentUser);
        commentIntent.putExtra(Constants.EXTRA_STORY_DATE, date);
        commentIntent.putExtra(Constants.EXTRA_STORY_DATE_KEY, dateKey);

        if(uid == null && currentUser.equals(author)){
            uid = currentUserID;
        }

        if(uid == null){
            Toast.makeText(getApplicationContext(), "Sorry, "+author+"'s comments are not available right now", Toast.LENGTH_SHORT).show();
            return;
        }

        commentIntent.putExtra(Constants.EXTRA_STORY_UID, uid);
        startActivity(commentIntent);
    }

    public void goToProfile(View v){
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra(Constants.PROFILE_NAME, author);
        profileIntent.putExtra(Constants.PROFILE_CURRENT_USER, currentUser);
        profileIntent.putExtra(Constants.PROFILE_AUTHOR, author);
        Log.d(TAG, "story uid: " + uid);
        if(uid == null && currentUser.equals(author)){
//            uid = currentUserID;
//            storiesDB.child(storyId).child("uid").setValue(uid);
//            masterStoriesDB.child(storyId).child("uid").setValue(uid);
        }else{
            if(uid == null){
                Toast.makeText(getApplicationContext(), "Sorry, this user's stories aren't available right now", Toast.LENGTH_SHORT).show();
                return;
            }

        }
        Log.d(TAG, "currentUser: "+currentUser + "\n uid: "+uid);
        profileIntent.putExtra(Constants.PROFILE_ID, uid);
        startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);

    }


    public void addToVoteCount(final int delta, String emoticon, int emoticon_count){

        vote_count+=delta;
        emoticon_count += delta;
        Log.d(TAG, "uid: " + uid + "\n name: " + name + "\n emoticon: " + emoticon);
        if(!emoticon.equals("voteCount")) {
            storiesDB.child(storyId).child(emoticon).setValue(emoticon_count);
            upvoteStoriesDB.child(storyId).child(emoticon).setValue(emoticon_count);
            rootRef.child("users").child(uid).child("stories").child(storyId).child(emoticon).setValue(emoticon_count);
        }else{
            storiesDB.child(storyId).child("voteCount").setValue(vote_count);
            upvoteStoriesDB.child(storyId).child("voteCount").setValue(vote_count);
            rootRef.child("users").child(uid).child("stories").child(storyId).child("voteCount").setValue(emoticon_count);
        }
        if(!currentUserID.equals(storyId) && delta > 0){
            notificationsDB.child(currentUserID+"-"+storyId+"-"+uid).child("recipient").setValue(uid);
            notificationsDB.child(currentUserID+"-"+storyId+"-"+uid).child("sender").setValue(currentUserID);
            notificationsDB.child(currentUserID+"-"+storyId+"-"+uid).child("type").setValue("upvote");
            notificationsDB.child(currentUserID+"-"+storyId+"-"+uid).child("storyID").setValue(storyId);
        }
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public void showSelectEmoticonDialog(View v){
        SelectEmoticonFragment dialogFragment = new SelectEmoticonFragment();
        dialogFragment.setListener(ViewStoryActivity.this);
        dialogFragment.show(ViewStoryActivity.this.getSupportFragmentManager(), "SelectEmoticonFragment");
    }

    public void showDisplayEmoticonsDialog(View v){
        ShowEmoticonsFragment dialogFragment = new ShowEmoticonsFragment();
        dialogFragment.setListener(ViewStoryActivity.this);
        dialogFragment.show(ViewStoryActivity.this.getSupportFragmentManager(), "ShowEmoticonsFragment");
    }



    @Override
    public void onFinishedEmoticonSelection(String emoticon_name) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Log.d(TAG, "onFinishedEmoticonSelection name: " + emoticon_name);
        if(uid == null){
            Toast.makeText(getApplicationContext(), "Sorry you can't upvote this story right now", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (emoticon_name){
            case "happy":
                happy.setVisibility(View.VISIBLE);
                addToVoteCount(1, "happyCount", happy_count);
                happyStories.add(storyId);
                editor.putStringSet(Constants.HAPPY_STORIES, happyStories);
                break;
            case "sad":
                sad.setVisibility(View.VISIBLE);
                addToVoteCount(1, "sadCount", sad_count);
                sadStories.add(storyId);
                editor.putStringSet(Constants.SAD_STORIES, sadStories);
                break;
            case "mad":
                mad.setVisibility(View.VISIBLE);
                addToVoteCount(1, "madCount", mad_count);
                madStories.add(storyId);
                editor.putStringSet(Constants.MAD_STORIES, madStories);
                break;
            case "surprised":
                surprised.setVisibility(View.VISIBLE);
                addToVoteCount(1, "surprisedCount", surprised_count);
                surprisedStories.add(storyId);
                editor.putStringSet(Constants.SURPRISED_STORIES, surprisedStories);
                break;
        }
        editor.apply();
    }

    public void loadYoutube(String id){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            Log.d(TAG, "vnd.youtube:" + id);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            startActivity(intent);
        }
    }



}

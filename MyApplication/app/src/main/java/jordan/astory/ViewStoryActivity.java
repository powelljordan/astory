package jordan.astory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
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
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import org.w3c.dom.Text;

import java.io.File;
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
    private Integer happy_count;
    private Integer sad_count;
    private Integer mad_count;
    private Integer surprised_count;
    private Integer viewCount;

    private LinearLayout emoticons;
    Set<String> upvotedStories;
    private Set<String> happyStories;
    private Set<String> sadStories;
    private Set<String> madStories;
    private Set<String> surprisedStories;
    private Set<String> seenStories;
    private Uri mediaUri;
    private ImageButton deleteButton;
    private WebView webView;
    private String mediaType;
    private ImageView profile;
    private static File mediaFile;
    private ImageView happy, sad, mad, surprised;
    private Firebase rootRef;
    private Firebase storiesDB;
    private Firebase geoStoriesDB;
    private Firebase commentsDB;
    private Firebase masterRootRef;
    private Firebase masterStoriesDB;
    private Firebase masterGeoStoriesDB;
    private Firebase masterCommentsDB;
    private Firebase upvoteRootRef;
    private Firebase upvoteStoriesDB;
    private Firebase upvoteGeoStoriesDB;
    private Firebase upvoteCommentsDB;
    private Uri.Builder builder;
    private String oldURL;



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
        mSharedPreferences.getStringSet(Constants.SEEN_STORIES, seenStories);

        editor.putStringSet(Constants.SURPRISED_STORIES, surprisedStories);
        editor.putString(Constants.CURRENT_STORY, name);
        editor.apply();

        FloatingActionButton commentButton = (FloatingActionButton)findViewById(R.id.comment);
        upvoteButton = (FloatingActionButton)findViewById(R.id.upvote);
        FloatingActionButton pictureButton = (FloatingActionButton)findViewById(R.id.takePicture);
        FloatingActionButton videoButton = (FloatingActionButton)findViewById(R.id.recordVideo);
        upvotedStories = new HashSet<>(mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, new HashSet<String>()));
//        mSharedPreferences.getStringSet(Constants.UPVOTED_STORIES, upvotedStories);
        Log.d(TAG, "onCreate upvotedStories: " + upvotedStories);
        if(upvotedStories.contains(name)){
            upvoteButton.setColorNormalResId(R.color.deep_sky_blue);
            Log.d(TAG, "should be blue");
        }

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
                if (upvotedStories.contains(name)) {
                    Log.d(TAG, "upVotedStories contains name");
                    upvoteButton.setColorNormalResId(R.color.white);
                    upvotedStories.remove(name);
                    addToVoteCount(-1, "voteCount", vote_count);
                    if(happyStories.contains(name)){
                        happyStories.remove(name);
                        addToVoteCount(-1, "happyCount", happy_count);
                    }
                    if(sadStories.contains(name)){
                        sadStories.remove(name);
                        addToVoteCount(-1, "sadCount", sad_count);
                    }
                    if(madStories.contains(name)){
                        madStories.remove(name);
                        addToVoteCount(-1, "madCount", mad_count);
                    }
                    if(surprisedStories.contains(name)){
                        surprisedStories.remove(name);
                        addToVoteCount(-1, "surprisedCount", surprised_count);
                    }
                } else {
                    showSelectEmoticonDialog(v);
                    upvoteButton.setColorNormalResId(R.color.deep_sky_blue);
                    upvotedStories.add(name);
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


        final Firebase storyRef = new Firebase("https://astory.firebaseio.com").child("stories");
        storiesDB.child(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewCount = dataSnapshot.getValue(DBStory.class).getViewCount();
                addToViewCount();
                if(!currentUser.equals(author)) {
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

        if(currentUser.equals(author)){
            upvoteButton.setVisibility(View.GONE);
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

    public void addToViewCount(){
        if(!seenStories.contains(name)){
            seenStories.add(name);
            viewCount += 1;
            Log.d(TAG, "seenStories: "+seenStories);
            storiesDB.child(name).child("viewCount").setValue(viewCount);
            masterStoriesDB.child(name).child("viewCount").setValue(viewCount);
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putStringSet(Constants.SEEN_STORIES, seenStories);
        editor.apply();
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
        storiesDB.child(name)
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
                            if (mediaType.equals("image")) {
                                syncMedia(fileURL);

                            }
                            else if (mediaType.equals("video")){
                                syncMedia(fileURL);
                            }
                        }
                        else if(dbStory != null && dbStory.getMediaType()!=null&& !mediaUpToDate(dbStory)){
                            mediaType = dbStory.getMediaType();
                            if(dbStory.getMediaType().equals("image")){
                                new getUri().execute("image/jpeg");
                                storiesDB.child(name).child("mediaUpdated").setValue(System.currentTimeMillis());
                                if(date != null) {
                                    masterStoriesDB.child(name).child("mediaUpdated").setValue(System.currentTimeMillis());

                                }
                            }else if(dbStory.getMediaType().equals("video")){
                                new getUri().execute("video/mp4");
                                storiesDB.child(name).child("mediaUpdated").setValue(System.currentTimeMillis());
                                if(date != null) {
                                    masterStoriesDB.child(name).child("mediaUpdated").setValue(System.currentTimeMillis());
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }
    public void handleDatabase(String date){
        rootRef = new Firebase("https://astory.firebaseio.com/"+date);
        storiesDB = rootRef.child("stories");
        commentsDB = rootRef.child("comments");
        geoStoriesDB = rootRef.child("geoStories");
        if(!date.equals("")){
            masterRootRef = new Firebase("https://astory.firebaseio.com");
            masterStoriesDB = masterRootRef.child("stories");
            upvoteRootRef = new Firebase("https://astory.firebaseio.com");
        }else{
            masterRootRef = new Firebase("https://astory.firebaseio.com/"+today);
            masterStoriesDB = masterRootRef.child("stories");
            storiesDB.child(name).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    upvoteGeoStoriesDB = upvoteRootRef.child("geoStories");
                    upvoteCommentsDB = upvoteRootRef.child("comments");
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

    }

    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
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
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                new UploadMedia().execute();
                mediaType = "image";
                storiesDB.child(name).child("mediaUpdated").setValue(System.currentTimeMillis());
                if(date != null) {
                    masterStoriesDB.child(name).child("mediaUpdated").setValue(System.currentTimeMillis());
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
                // video successfully recorded
                // preview the recorded video
                new UploadMedia().execute();
                mediaType = "video";
                storiesDB.child(name).child("mediaUpdated").setValue(System.currentTimeMillis());
                if(date != null) {
                    masterStoriesDB.child(name).child("mediaUpdated").setValue(System.currentTimeMillis());
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
        Toast.makeText(getApplicationContext(), "Your story has been added", Toast.LENGTH_SHORT).show();
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
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
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
            if((System.currentTimeMillis() - Double.parseDouble(story.getMediaUpdated())) < 59*60*1000){
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
        storiesDB.child(name).child("mediaType").setValue(uriType);
        storiesDB.child(name).child("mediaUri").setValue(fileURL);
        if(date != null){
            masterStoriesDB.child(name).child("mediaType").setValue(uriType);
            masterStoriesDB.child(name).child("mediaUri").setValue(fileURL);
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
                    name,
                    mediaFile
            );
            return null;
        }
    }

    private class getUri extends AsyncTask<String, Void, URL>{

        @Override
        protected URL doInBackground(String... types) {
            ResponseHeaderOverrides override = new ResponseHeaderOverrides();
            override.setContentType(types[0]);
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest( MY_BUCKET, name);
            urlRequest.setExpiration(new Date(System.currentTimeMillis() + 7*24*60*60*1000 ) );  // Added a week's worth of milliseconds to the current time.
            urlRequest.setResponseHeaders(override);
            URL url = s3.generatePresignedUrl(urlRequest);
            return url;
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
//        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        Firebase storyRef = new Firebase("https://astory.firebaseio.com").child("stories");
        final String[] result = new String[1];
        storyRef.child(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                result[0] = dataSnapshot.getValue(DBStory.class).getMediaUri();
                if (oldURL == null || !oldURL.equals(result[0])) {
                    Log.d(TAG, "oldURL: " + oldURL);
                    oldURL = result[0];
                    if (result[0] != null) {
                        if (dataSnapshot.getValue(DBStory.class).getMediaType().equals("video")) {
                            ViewGroup.LayoutParams lp = webView.getLayoutParams();
                            lp.height = (int) pxFromDp(getApplicationContext(), 400);
                            webView.setLayoutParams(lp);
                            webView.loadUrl(result[0]);

                        }

                    }
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
            webView.loadUrl(result.toString());
        }
//        setResult(RESULT_OK, new Intent().putExtra(Constants.MEDIA_URL, result));
//        Log.d(TAG, "Definitely called setResult");
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
    }

    public void goToProfile(View v){
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra(Constants.PROFILE_NAME, author);
        profileIntent.putExtra(Constants.PROFILE_CURRENT_USER, currentUser);
        profileIntent.putExtra(Constants.PROFILE_AUTHOR, author);
        Log.d(TAG, "story uid: "+uid);
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



    public void addToVoteCount(final int delta, String emoticon, int emoticon_count){
        vote_count+=delta;
        emoticon_count += delta;

        if(!emoticon.equals("voteCount")) {
            storiesDB.child(name).child(emoticon).setValue(emoticon_count);
            upvoteStoriesDB.child(name).child(emoticon).setValue(emoticon_count);
        }else{
            storiesDB.child(name).child("voteCount").setValue(vote_count);
            upvoteStoriesDB.child(name).child("voteCount").setValue(vote_count);
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
        Log.d(TAG, "onFinishedEmoticonSelection name: "+emoticon_name);
        switch (emoticon_name){
            case "happy":
                happy.setVisibility(View.VISIBLE);
                addToVoteCount(1, "happyCount", happy_count);
                happyStories.add(name);
                editor.putStringSet(Constants.HAPPY_STORIES, happyStories);
                break;
            case "sad":
                sad.setVisibility(View.VISIBLE);
                addToVoteCount(1, "sadCount", sad_count);
                sadStories.add(name);
                editor.putStringSet(Constants.SAD_STORIES, sadStories);
                break;
            case "mad":
                mad.setVisibility(View.VISIBLE);
                addToVoteCount(1, "madCount", mad_count);
                madStories.add(name);
                editor.putStringSet(Constants.MAD_STORIES, madStories);
                break;
            case "surprised":
                surprised.setVisibility(View.VISIBLE);
                addToVoteCount(1, "surprisedCount", surprised_count);
                surprisedStories.add(name);
                editor.putStringSet(Constants.SURPRISED_STORIES, surprisedStories);
                break;
        }
        editor.apply();
    }



}

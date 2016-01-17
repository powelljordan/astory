package jordan.astory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jordan on 1/14/2016.
 */
public class MediaActivity extends Activity {
    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "aStory";

    private Uri fileUri; // file url to store image/video
    private URL fileURL;
    private String uriType;
    private static File mediaFile;
    private ImageView imgPreview;
    private VideoView videoPreview;
    private WebView webView;
    private Button btnCapturePicture, btnRecordVideo;
    private LinearLayout mediaButtons;
    private Firebase storiesDB;

    private final String TAG = "MediaActivity";
    private String storyName;
    private String currentUser;
    private String storyAuthor;

    private Uri.Builder builder;
    private URL onlineUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        storiesDB = new Firebase("https://astory.firebaseio.com/stories");
        Intent intent = getIntent();
        storyName = intent.getStringExtra(Constants.MEDIA_STORY_NAME);
        storyAuthor = intent.getStringExtra(Constants.EXTRA_STORY_AUTHOR);
        currentUser = intent.getStringExtra(Constants.EXTRA_CURRENT_USER);
        handleCaptureButtons(intent);
        webView = (WebView) findViewById(R.id.media_web_view);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        videoPreview = (VideoView) findViewById(R.id.videoPreview);
        btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
        btnRecordVideo = (Button) findViewById(R.id.btnRecordVideo);

        /**
         * Capture image button click event
         */
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                captureImage();
            }
        });

        /**
         * Record video button click event
         */
        btnRecordVideo.setOnClickListener(new View.OnClickListener() {

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

    private void handleCaptureButtons(Intent intent){
//        Log.d(TAG, "currentUser: " +  currentUser);
//        Log.d(TAG, "storyAuthor: " + storyAuthor);
        if(currentUser.equals(storyAuthor)){
//            Log.d(TAG, "current user is the author");
            mediaButtons = (LinearLayout) findViewById(R.id.media_buttons);
            mediaButtons.setVisibility(View.VISIBLE);
        }
        storiesDB.child(storyName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DBStory dbStory = dataSnapshot.getValue(DBStory.class);
                        if (dbStory != null && dbStory.getMediaUri() != null) {
                            try {
                                fileURL = new URL(dbStory.getMediaUri());
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            String mediaType = dbStory.getMediaType();
                            if (mediaType.equals("image")) {
                                loadMedia(fileURL);
                            } else if (mediaType.equals("video")) {
                                loadMedia(fileURL);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
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

                MediaStorage mStore = new MediaStorage(getApplicationContext(),
                        mediaFile
                        , storyName);
                new UploadMedia().execute();
                new getUri().execute("image/jpeg");
                Log.d(TAG, "onlineURL: " + onlineUrl);
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
                MediaStorage mStore = new MediaStorage(getApplicationContext(),
                        mediaFile
                        , storyName);
                mStore.setup();
                new UploadMedia().execute();
                new getUri().execute("video/mp4");
                previewVideo();
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
    }

    /**
     * Display image from a path to ImageView
     */
    private void previewCapturedImage() {
        try {
            Log.d(TAG, "fileUri here is: " + fileUri);
            // hide video preview
            videoPreview.setVisibility(View.GONE);

            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 2;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            imgPreview.setImageBitmap(bitmap);
            syncToFirebase("image");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Previewing recorded video
     */
    private void previewVideo() {
        try {
            // hide image preview
            imgPreview.setVisibility(View.GONE);

            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoURI(fileUri);
            // start playing
            videoPreview.start();
            syncToFirebase("video");
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    public Uri getUriFromUrl(String thisUrl) {
        URL url = null;
        try {
            url = new URL(thisUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        builder =  new Uri.Builder()
                .scheme(url.getProtocol())
                .authority(url.getAuthority())
                .appendPath(url.getPath());
        fileUri = builder.build();
        return fileUri;

    }

    public void syncToFirebase(String uriType){
        Log.d(TAG, "syncToFirebase called");
        storiesDB.child(storyName).child("mediaType").setValue(uriType);
        storiesDB.child(storyName).child("mediaUri").setValue(fileURL);
    }

    public void loadMedia(URL result){
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.loadUrl(result.toString());
    }

    private AmazonS3 s3;
    private final String MY_BUCKET = "astory-media";
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
                    storyName,
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
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest( MY_BUCKET, storyName);
            urlRequest.setExpiration( new Date( System.currentTimeMillis() + 3600000 ) );  // Added an hour's worth of milliseconds to the current time.
            urlRequest.setResponseHeaders(override);
            URL url = s3.generatePresignedUrl(urlRequest);
            onlineUrl = url;
            Log.d(TAG, "Actually got the url: " + url);
            Log.d(TAG, "Right after method uri: " + fileUri);
            return url;
        }

        @Override
        protected void onPostExecute(URL result){
            fileURL = result;
            Log.d(TAG, result.toString());
            loadMedia(result);
            previewCapturedImage();
        }
    }
}



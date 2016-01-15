package jordan.astory;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jordan on 1/14/2016.
 */
public class MediaActivity extends Activity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;
    final String TAG = "astory.MediaActivity";
    ImageView mImageView;
    VideoView mVideoView;
    String mCurrentPhotoPath;
    String storyName;
    int mediaType;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        storyName = intent.getStringExtra(Constants.MEDIA_STORY_NAME);
        mediaType = intent.getIntExtra(Constants.MEDIA, 10);
        handleMediaDispatch();
    }
    private void handleMediaDispatch(){
        if(mediaType == Constants.MEDIA_IMAGE){
            dispatchTakePictureIntent(storyName);
        }
        else if(mediaType == Constants.MEDIA_VIDEO){
            dispatchTakeVideoIntent(storyName);
        }
        else{
            Log.e(TAG, "Unknown media type cannot be handled");
        }
    }
    private void dispatchTakePictureIntent(String storyName){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile(storyName);
            }catch(IOException ex){
                //Error occurred while creating the file.
            }
            //If file successfully created
            if(photoFile != null){
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    private void dispatchTakeVideoIntent(String storyName){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(TAG, "Intent: " + data);
            // Describe the columns you'd like to have returned. Selecting from the Thumbnails
            // location gives you both the Thumbnail Image ID, as well as the original image ID
            String[] projection = {
                    MediaStore.Images.Thumbnails._ID,  // The columns we want
                    MediaStore.Images.Thumbnails.IMAGE_ID,
                    MediaStore.Images.Thumbnails.KIND,
                    MediaStore.Images.Thumbnails.DATA};
            String selection = MediaStore.Images.Thumbnails.KIND + "="  + // Select only mini's
                    MediaStore.Images.Thumbnails.MINI_KIND;

            String sort = MediaStore.Images.Thumbnails._ID + " DESC";

            //At the moment, this is a bit of a hack, as I'm returning ALL images, and just taking the latest one.
            // There is a better way to narrow this down I think with a WHERE clause which is currently the selection variable
            Cursor myCursor = this.managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, selection, null, sort);

            long imageId = 0l;
            long thumbnailImageId = 0l;
            String thumbnailPath = "";

            try{
                myCursor.moveToFirst();
                imageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
                thumbnailImageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
                thumbnailPath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
            }
            finally{myCursor.close();}

            //Create new Cursor to obtain the file Path for the large image

            String[] largeFileProjection = {
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA
            };

            String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
            myCursor = this.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
            String largeImagePath = "";

            try{
                myCursor.moveToFirst();

//This will actually give yo uthe file path location of the image.
                largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
            }
            finally{myCursor.close();}
            // These are the two URI's you'll be interested in. They give you a handle to the actual images
            Uri uriLargeImage = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
            Uri uriThumbnailImage = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, String.valueOf(thumbnailImageId));
            // I've left out the remaining code, as all I do is assign the URI's to my own objects anyways...
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uriLargeImage, "image/*");
            startActivity(intent);
        }

        else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            mVideoView.setVideoURI(videoUri);
        }
    }

    private File createImageFile(String storyName) throws IOException {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                "astory-image" + storyName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


}

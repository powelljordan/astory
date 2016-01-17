package jordan.astory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jordan on 1/13/2016.
 */
public class MediaStorage {
    Context context;
    private final String MY_BUCKET = "astory-media";
    private String fileName;
    private Context applicationContext;
    private Uri filePath;
    private File mediaFile;
    private final String TAG = "MediaStorage";
    private AmazonS3 s3;
    public MediaStorage(Context context, File file, String name){
        this.applicationContext = context;
        this.mediaFile = file;
        this.fileName = name;
    }
    // Create an S3 client
    public void setup(){
        Log.d(TAG, "setup called");
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                applicationContext, // get the context for the current activity
//                "432169068559", // your AWS Account id
                Constants.IDENTITY_POOL_ID, // your identity pool id
//                "arn:aws:iam::432169068559:role/Cognito_aStoryAuth_Role",// an authenticated role ARN
//                "arn:aws:iam::432169068559:role/Cognito_aStoryUnauth_Role", // an unauthenticated role ARN
                Regions.US_EAST_1 //Region
        );
        s3 = new AmazonS3Client(credentialsProvider);
        Log.d(TAG, "s3: " + s3);

        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        Log.d(TAG, "s3 after adding region: " + s3);
        TransferUtility transferUtility = new TransferUtility(s3, applicationContext);

        TransferObserver observer = transferUtility.upload(
                MY_BUCKET,
                fileName,
                mediaFile
        );
    }

    public URL getURL(){
        ResponseHeaderOverrides override = new ResponseHeaderOverrides();
        override.setContentType( "image/jpeg" );

        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest( MY_BUCKET, fileName);
        urlRequest.setExpiration( new Date( System.currentTimeMillis() + 3600000 ) );  // Added an hour's worth of milliseconds to the current time.
        urlRequest.setResponseHeaders(override);
        URL url = s3.generatePresignedUrl(urlRequest);
        Log.d(TAG, "Actually got the url: " + url);
        return url;
    }



}

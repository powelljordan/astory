//package jordan.astory;
//
//import android.content.Context;
//import com.amazonaws.auth.CognitoCachingCredentialsProvider;
//import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
//import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
//import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
//import com.amazonaws.regions.Region;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.model.ObjectMetadata;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by Jordan on 1/13/2016.
// */
//public class MediaStorage {
//    Context context;
//    private final String MY_BUCKET = "astory-media";
//    public MediaStorage(Context context){
//        this.context = context;
//    }
//
//    public void setupAWS(){
//        // Initialize the Amazon Cognito credentials provider
//        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                context,
//                "us-east-1:3e0f22c1-492a-42bc-84c8-09aa8ee7751e", // Identity Pool ID
//                Regions.US_EAST_1 // Region
//        );
//
//        // Initialize the Cognito Sync client
//        CognitoSyncManager syncClient = new CognitoSyncManager(
//                context,
//                Regions.US_EAST_1, // Region
//                credentialsProvider);
//
//
//        // Create an S3 client
//        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
//        // Set the region of your S3 bucket
//        s3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_1));
//        TransferUtility transferUtility = new TransferUtility(s3, context);
//
//        ObjectMetadata myObjectMetadata = new ObjectMetadata();
//
//        //create a map to store user metadata
//        Map<String, String> userMetadata = new HashMap<String,String>();
//        userMetadata.put("myKey", "myVal");
//
//        //call setUserMetadata on our ObjectMetadata object, passing it our map
//        myObjectMetadata.setUserMetadata(userMetadata);
//
////        TransferObserver observer = transferUtility.upload(
////                MY_BUCKET,
////                "test",
////                ""
////        )
//    }
//
////    new AmazonS3Client(
////    s3.setRegion(Region.getRegion(Regions.MY_BUCKET_REGION));
//    // Create a record in a dataset and synchronize with the server
////    Dataset dataset = syncClient.openOrCreateDataset("myDataset");
////    dataset.put("myKey", "myValue");
////    dataset.synchronize(new DefaultSyncCallback() {
////        @Override
////        public void onSuccess(Dataset dataset, List newRecords) {
////            //Your handler code here
////        }
////    });
//}

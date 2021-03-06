package jordan.astory;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by Jordan on 12/28/2015.
 */
public final class Constants {

    private Constants(){
    }

    public static final String PACKAGE_NAME = "jordan.astory";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";

    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    public static final long GEOFENCING_EXPIRATION_IN_HOURS = 12;

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCING_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public static final float GEOFENCE_RADIUS_IN_METERS = 60;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 7000;
    public static final long INACTIVE_UPDATE_INTERVAL_IN_MILLISECONDS = 30*60*1000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    protected final static String CURRENT_USER_ID_KEY = "current-user-id-key";

    public static final float MAP_ZOOM_LEVEL = 18;
    public static final double STORY_QUERY_RADIUS = .5;

    public static final HashMap<String, LatLng> KEY_LOCATIONS = new HashMap<String, LatLng>();
    static {
//        KEY_LOCATIONS.put("Daddy's House", new LatLng(30.188174, -95.587658));
    }

    public static class Geometry {
        public static double MinLatitude = -90.0;
        public static double MaxLatitude = 90.0;
        public static double MinLongitude = -180.0;
        public static double MaxLongitude = 180.0;
        public static double MinRadius = 0.01; // kilometers
        public static double MaxRadius = 20.0; // kilometers
    }

    public static final HashMap<String, Story> STORY_LOCATIONS = new HashMap<>();
    public final static int RECENT_STORIES_COUNT = 5;

    public final static String EXTRA_STORY_ID = "jordan.astory.STORY_ID";
    public final static String EXTRA_STORY_CONTENT = "jordan.astory.STORY_CONTENT";
    public final static String EXTRA_STORY_NAME = "jordan.astory.STORY_NAME";
    public final static String EXTRA_STORY_AUTHOR = "jordan.astory.STORY_AUTHOR";
    public final static String EXTRA_STORY_UID = "jordan.astory.STORY_UID";
    public final static String EXTRA_STORY_DATE = "jordan.astory.STORY_DATE";
    public final static String EXTRA_STORY_DATE_KEY = "jordan.astory.STORY_DATE_KEY";
    public final static String EXTRA_CURRENT_USER = "jordan.astory.CURRENT_USER";
    public final static String EXTRA_USER_ID = "jordan.astory.USER_ID";
    public final static String VIEW_STORY_KEY = "jordan.astory.STORY_KEY";
    public final static String MY_STORIES_KEY = "jordan.astory.MY_STORIES_KEY";
    public final static String EXTRA_STORY_COMMENT = "jordan.astory.STORY_COMMENT";
    public final static String EXTRA_COMMENT_STORY_ID="jordan.astory.EXTRA_COMMENT_STORY_ID";
    public final static String UPVOTED_STORIES = "jordan.astory.UPVOTED_STORIES";
    public final static String HAPPY_STORIES = "jordan.astory.HAPPY_STORIES";
    public final static String SAD_STORIES = "jordan.astory.SAD_STORIES";
    public final static String MAD_STORIES = "jordan.astory.MAD_STORIES";
    public final static String SURPRISED_STORIES = "jordan.astory.SURPRISED_STORIES";
    public final static String SEEN_STORIES = "jordan.astory.SEEN_STORIES";
    public final static String EXTRA_ADDED_KEY = "jordan.astory.ADDED_KEY";


    public final static String CURRENT_USER_ID = "jordan.astory.CURRENT_USER_ID";
    public final static String CURRENT_STORY = "jordan.astory.CURRENT_STORY";
    final static String MY_ACTION = "MY_ACTION";


//     Stuff
    public final static String IDENTITY_POOL_ID = "us-east-1:3e0f22c1-492a-42bc-84c8-09aa8ee7751e";

//    Media Stuff
    public final static String MEDIA = "jordan.astory.MEDIA";
    public final static int MEDIA_VIDEO = 11;
    public final static int MEDIA_IMAGE = 12;
    public final static int MEDIA_REQUEST_CODE = 13;
    public final static int MEDIA_VIDEO_REQUEST_CODE = 14;
    public final static String MEDIA_TYPE = "jordan.astory.MEDIA_TYPE";
    public final static String MEDIA_URI = "jordan.astory.MEDIA_URI";
    public final static String MEDIA_IMAGE_URI = "jordan.astory.MEDIA_IMAGE_URI";
    public final static String MEDIA_STORY_NAME = "jordan.astory.MEIDA_STORY_NAME";
    public final static String MEDIA_URL = "jordan.astory.MEDIA_URL";

//    Profile Stuff
    public final static String PROFILE_NAME = "jordan.astory.PROFILE_NAME";
    public final static String PROFILE_CURRENT_USER = "jordan.astory.PROFILE_CURRENT_USER";
    public final static String PROFILE_AUTHOR = "jordan.astory.PROFILE_AUTHOR";
    public final static String PROFILE_ID = "jordan.astory.PROFILE_ID";
    public final static String PROFILE_STORY_SELECTED_ID = "jordan.astoroy.PROFILE_STORY_SELECTED_ID";
    public final static String PROFILE_STORY_SELECTED_NAME = "jordan.astory.PROFILE_STORY_SELECTED_NAME";
    public final static String PROFILE_STORY_SELECTED_LATITUDE = "jordan.astory.PROFILE_STORY_SELECTED_LATITUDE";
    public final static String PROFILE_STORY_SELECTED_LONGITUDE = "jordan.astory.PROFILE_STORY_SELECTED_LONGITUDE";
    public final static String PROFILE_STORY_SELECTED = "jordan.astory.PROFILE_STORY_SELECTED";
    public final static String PROFILE_STORY_SELECTED_COMMENT_COUNT = "jordan.astory.PROFILE_STORY_SELECTED_COMMENT_COUNT";
    public final static String PROFILE_STORY_SELECTED_VOTE_COUNT = "jordan.astory.PROFILE_STORY_SELECTED_VOTE_COUNT";
    public final static int PROFILE_REQUEST_CODE = 15;

//    Permission Stuff
    public final static int PERMISSIONS_REQUEST_CODE = 42;

    public final static String[] LOCATION_PERMS = {
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"};

    public final static String[] STORAGE_PERMS = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"};

    public final static String SPOTIFY_CLIENT_ID = "4987a93c80dc43e783835661a007ba68";
    public static final int SPOTIFY_REQUEST_CODE = 1337;
}

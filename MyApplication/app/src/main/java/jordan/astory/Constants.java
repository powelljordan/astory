package jordan.astory;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by Jordan on 12/28/2015.
 */
public final class Constants {

    private Constants(){
    }

    public static final String PACKAGE_NAME = "edu.mit.astory";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";

    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    public static final long GEOFENCING_EXPIRATION_IN_HOURS = 12;

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCING_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public static final float GEOFENCE_RADIUS_IN_METERS = 35;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

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

    public static final float MAP_ZOOM_LEVEL = 18;

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

    public final static String EXTRA_STORY_CONTENT = "jordan.astory.STORY_CONTENT";
    public final static String EXTRA_STORY_NAME = "jordan.astory.STORY_NAME";
    public final static String VIEW_STORY_KEY = "jordan.astory.STORY_KEY";


    final static String MY_ACTION = "MY_ACTION";
}

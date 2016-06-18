package jordan.astory;

/**
 * Created by Jordan on 12/28/2015.
 */
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import com.getbase.floatingactionbutton.FloatingActionButton;

import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pushbots.push.Pushbots;


import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;



public class MainActivity extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>, OnMapReadyCallback,
        LocationListener, AddGeofenceFragment.AddGeofenceFragmentListener, GeoQueryEventListener {

    protected static final String TAG = "MainActivity";
    /**
     * Google Play Services Entry Point
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * List of geofences.
     */
    protected ArrayList<Geofence> mGeofenceList;
    protected ArrayList<Story> storyList;

    private boolean mGeofencesAdded;
    private Story storyToBeDeleted;

    private PendingIntent mGeofencePendingIntent;

    private SharedPreferences mSharedPreferences;

    private Button mAddGeofencesButton;
    private Button mRemoveGeofencesButton;

    private GoogleMap mMap;
    private Circle myCircle;

    private int viewStoryRequestCode;
    private int viewStoryResultCode;
    private Intent viewStoryData;

    MyReceiver myReceiver;


    protected boolean mRequestingLocationUpdates;
    protected Location mCurrentLocation;
    private Firebase rootRef;
    private Firebase storiesDB;
    private Firebase commentsDB;
    private Firebase masterRootRef;
    private Firebase masterStoriesDB;
    private Firebase masterCommentsDB;
    private Firebase usersDB;
    String today;
    Firebase todayRootRef;
    private GeoFire geoFire;
    private GeoFire masterGeoFire;
    private GeoFire todayGeoFire;
    private GeoQuery geoQuery;
    private GeoQuery masterGeoQuery;
    private GeoQuery todayGeoQuery;

    public String currentUserID;
    public List<String> currentUserStories;
    private String date;
    private int year;
    private int month;
    private int day;

    private int comment_count = 0;
    private int vote_count = 0;
    private boolean active = false;

    static final int DATE_DIALOG_ID = 999;
    public static DBUser currentUser;

    private String selectedStoryID;
    private String selectedStoryName;
    private LatLng selectedStoryLatLng;
    private int selectedStoryCommentCount;
    private int selectedStoryVoteCount;
    private Marker tempMarker;
    private boolean previewingStory = false;

    private DatePicker dpResult;
    private FloatingActionButton myLocationButton;
    private FloatingActionButton addStory;
    private FloatingActionButton calendarButton;
    private FloatingActionButton recentStoriesButton;
    private FloatingActionButton viewProfile;
    private FloatingActionsMenu mainMenu;
    private boolean initiallyLoadedStories = false;
    private Permissions permissions;

    /**
     * Stores paramenters for requests to the FusedLocationProviderApi
     */
    protected LocationRequest mLocationRequest;

    public static FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        currentUserStories = new ArrayList<String>();
        date = "";
        SimpleDateFormat s = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        today = s.format(new Date());
        Log.d(TAG, "date: " + date);

        setContentView(R.layout.activity_main);

        //Calendar filter stuff
        setCurrentDateOnView();
        addCalendarButtonListener();
        addRecentStoriesButtonListener();
        addAddStoryListener();
        addViewProfileListener();
        addMyLocationListener();

        mainMenu = (FloatingActionsMenu) findViewById(R.id.main_menu);
//        addMainMenuListener();


        mGeofenceList = new ArrayList<Geofence>();
        storyList = new ArrayList<Story>();
        handleDatabase(date);

        mGeofencePendingIntent = null;

        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        Pushbots.sharedInstance().init(this);

        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);

        mRequestingLocationUpdates = true;



        updateValuesFromBundle(savedInstanceState);


        //Iniitialize all the geofences
        populateGeofenceList();

        //start building the GoogleAPI
        buildGoogleApiClient();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fragmentManager = getSupportFragmentManager();
        if(Build.VERSION.RELEASE.equals("6.0")) {
//            Toast.makeText(getApplicationContext(), "My deepest apologies. Your version of Android is not yet supported.", Toast.LENGTH_LONG).show();
        }
        handleLogin();
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){

            case Constants.PERMISSIONS_REQUEST_CODE:
//                LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0,0), Constants.MAP_ZOOM_LEVEL));
//                boolean locationAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;

                break;

        }

    }


    private void handleLogin(){
        Log.d(TAG, "rootRef.getAuth(): " + rootRef.getAuth());
        if(rootRef.getAuth() == null) {
            goToLoginScreen();
        }else {
            currentUserID = mSharedPreferences.getString(Constants.CURRENT_USER_ID_KEY, "N/A");
            if (currentUserID.equals("N/A")) {
                Log.e(TAG, "User has not logged in and is not in Shared Preferences");
                currentUserID = rootRef.getAuth().getUid();
            }
            Firebase usersDB = new Firebase("https://astory.firebaseio.com/users");
            usersDB.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currentUser = dataSnapshot.getValue(DBUser.class);
                    if (currentUser != null) {
                        if (currentUser.getStories() != null) {
//                    currentUserStories = currentUser.getStories();
                        }
                    }

//                Log.d(TAG, "currentUser: " + currentUser);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e(TAG, firebaseError.toString());
                }
            });
            permissions = new Permissions(this);
            permissions.checkForPermissions(Constants.LOCATION_PERMS);
        }
    }
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if(savedInstanceState != null){
            if(savedInstanceState.keySet().contains(Constants.REQUESTING_LOCATION_UPDATES_KEY)){
                // Update the value of mRequestingLocationUpdates from the Bundle
                mRequestingLocationUpdates = savedInstanceState.getBoolean(Constants.REQUESTING_LOCATION_UPDATES_KEY);
            }

            //Update the value of mCurrentLocation from the Bundle
            if(savedInstanceState.keySet().contains(Constants.LOCATION_KEY)){
                mCurrentLocation = savedInstanceState.getParcelable(Constants.LOCATION_KEY);
                geoQuery.setCenter(new GeoLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            }
            updateMap();
            LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if(mMap == null || myLocation == null){
                return;
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, Constants.MAP_ZOOM_LEVEL));
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi
     */
    protected void startLocationUpdates(){
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch(SecurityException securityException){
            logSecurityException(securityException);
        }
    }

    protected void stopLocationUpdates(){
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }catch(SecurityException securityException){
            logSecurityException(securityException);
        }
    }

    public void onLocationChanged(Location location){
        Log.d(TAG, "location: "+location);
        mCurrentLocation = location;
        geoQuery.setCenter(new GeoLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));

        if(!previewingStory) {
            updateMap();
        }
//        Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location){
//        Log.d(TAG, "onKeyEntered called");
        if(mGeofenceList.size() < 100) {
//            Log.d(TAG, "mGeofenceList size is less than 100");
//            Log.d(TAG, "key " + key);
            Firebase specificStoryDB = storiesDB.child(key);

            specificStoryDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "dataSnapshot: " + dataSnapshot);
                    DBStory dbStory = dataSnapshot.getValue(DBStory.class);
//                    Log.d(TAG, "dbStory " + dbStory);
                    if (dbStory != null) {
                        boolean alreadyAddedStory = false;
                        for (Story localStory : storyList) {
                            if (dbStory.getId().equals(localStory.id)) {
//                                Log.d(TAG, dbStory.getName() + " story already in storyList");
                                alreadyAddedStory = true;
                            }
                        }
                        if (!alreadyAddedStory) {
                            Log.d(TAG, "adding " + dbStory.getName() + " to storyList");
                            Story story = new Story();
                            Log.d(TAG, dbStory.getName());
                            story.id = dbStory.getId();
                            story.name = dbStory.getName();
                            story.content = dbStory.getContent();
                            story.date = dbStory.getDate();
                            story.location = new LatLng(Double.parseDouble(dbStory.getLatitude()), Double.parseDouble(dbStory.getLongitude()));
                            story.radius = Constants.GEOFENCE_RADIUS_IN_METERS;
                            story.author = dbStory.getAuthor();
                            story.uid = dbStory.getUid();
                            story.happyCount = dbStory.getHappyCount();
                            story.sadCount = dbStory.getSadCount();
                            story.madCount = dbStory.getMadCount();
                            story.surprisedCount = dbStory.getSurprisedCount();
                            story.commentCount = dbStory.getCommentCount();
                            story.voteCount = dbStory.getVoteCount();
                            addStoryToDevice(story);
                            addStoryGeofence();
                            Log.d(TAG, "Now storyList contains " + storyList.size() + " stories");
                        }
                    }


                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
//            Log.d(TAG, "this is beyond me");

        }
    }

    @Override
    public void onKeyExited(String key){
//        Log.d(TAG, "onKeyExited called");
        for (int i = 0; i < storyList.size(); i++) {
            if (storyList.get(i).name.equals(key)) {
                removeStoryFromDevice(storyList.get(i));
            }
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location){

    }

    @Override
    public void onGeoQueryReady(){

    }

    @Override
    public void onGeoQueryError(FirebaseError error){

    }

    public boolean storyAddedOnMap(LatLng storyLocation){
        float[] results = new float[10];
        Location.distanceBetween(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(),
                storyLocation.latitude, storyLocation.longitude, results);
        Log.d(TAG, "distance: " + results[0]);
        return Constants.STORY_QUERY_RADIUS * 1000 > results[0] ;
    }



    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        geoQuery.addGeoQueryEventListener(this);
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
        GeofenceTransitionsIntentService.notify = false;
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("active", true);
        editor.apply();
//        Log.d(TAG, "receiver registered");

        //Start our own service
//        Intent intent = new Intent(MainActivity.this,
//                jordan.astory.GeofenceTransitionsIntentService.class);
//        startService(intent);

    }

    @Override
    public void onResume(){
        super.onResume();
        Firebase credentialsRef = new Firebase("https://astory.firebaseio.com");
        if(credentialsRef.getAuth() == null) {
            goToLoginScreen();
        }else{
            permissions = new Permissions(this);
            permissions.checkForPermissions(Constants.LOCATION_PERMS);
            if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                GeofenceTransitionsIntentService.notify = false;
                mLocationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);
                stopLocationUpdates();
                startLocationUpdates();
        }

//            startLocationUpdates();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for(int i = 0; i < storyList.size(); i++){
            Story story = storyList.get(i);
            removeStoryFromDevice(story);
        }
//        mGoogleApiClient.disconnect();
        geoQuery.removeAllListeners();
        unregisterReceiver(myReceiver);
        GeofenceTransitionsIntentService.notify = true;
        Log.d(TAG, "notify: " + GeofenceTransitionsIntentService.notify);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("active", true);
        editor.apply();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates){
            mLocationRequest.setInterval(Constants.INACTIVE_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(Constants.INACTIVE_UPDATE_INTERVAL_IN_MILLISECONDS);
            stopLocationUpdates();
            startLocationUpdates();
            Log.d(TAG, "Interval: " + mLocationRequest.getInterval());
//            stopLocationUpdates();
        }
        GeofenceTransitionsIntentService.notify = true;
        Log.d(TAG, "notify: " + GeofenceTransitionsIntentService.notify);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }



    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            try{
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if(mCurrentLocation == null){
//                    return;
                }else {
                    LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, Constants.MAP_ZOOM_LEVEL));
                    updateMap();

                    startLocationUpdates();
                }
            }catch(SecurityException securityException){
                logSecurityException(securityException);
            }
            stopLocationUpdates();
            startLocationUpdates();

        }
        if(mRequestingLocationUpdates){
            startLocationUpdates();
        }

        if(viewStoryData != null){
            onActivityResult(viewStoryRequestCode, viewStoryResultCode, viewStoryData);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();

        // onConnected() will be called again automatically when the service reconnects
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
//        Log.d(TAG, "returns Geofencing request");
        return builder.build();
    }


    public void addStoryGeofence(){
        if (!mGoogleApiClient.isConnected()) {
//            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.

                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofencesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
//            mMap.clear();
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.

            logSecurityException(securityException);
        }
    }

    public void removeStoryGeofence(Story story){
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            ArrayList<String> removeGeofence = new ArrayList<String>();
            removeGeofence.add(story.geofence.getRequestId());
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    removeGeofence
            ).setResultCallback(this); // Result processed in onResult().
//            mMap.clear();
            mGeofenceList.remove(story.geofence);
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.

            logSecurityException(securityException);
        }
    }



    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     *
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            if(mGeofenceList.size() > 0){
                mGeofencesAdded=true;
            }
            else{
                mGeofencesAdded = false;
            }
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();


            Toast.makeText(
                    this,
                    getString(mGeofencesAdded ? R.string.geofences_added :
                            R.string.geofences_removed),
                    Toast.LENGTH_SHORT
            );
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
//        this.startService(intent);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
//        Log.d(TAG, "returns getGeofencePendingIntent");
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.KEY_LOCATIONS.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                            // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                            // Create the geofence.
                    .build());
        }
    }


    public void addStoryToGeofenceList(Story story) {
        if(story == null){
            Toast.makeText(this, "story is undefined", Toast.LENGTH_SHORT);
        }else {
            mGeofenceList = new ArrayList<>();
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(story.id)

                            // Set the circular region of this geofence.
                    .setCircularRegion(
                            story.location.latitude,
                            story.location.longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                            // Create the geofence.
                    .build());
            Log.d(TAG, "add story to geofence list called");
//            Log.d(TAG, "Geofence list has : " + mGeofenceList.size() + " items");
            for(Geofence geo: mGeofenceList){
//                Log.d(TAG, "item " + geo.getRequestId());
            }
            story.geofence = mGeofenceList.get(0);
            Marker storyMarker = mMap.addMarker(new MarkerOptions().position(story.location).title(story.name)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.seen_marker64)));

//        addMarkerListener();
            story.marker = storyMarker;
            storyList.add(story);
            addStoryToDB(story);

        }
    }

    public String sanitizeText(String text){
        String t = text.replace(".", "");
        t = t.replace("$", "");
        t = t.replace("[", "");
        t = t.replace("#", "");
        t = t.replace("]", "");
        t = t.replace("/", "");
        return t;
    }

    public void addStoryToDB(Story story){
        HashMap<String, String> storyObj = new HashMap<String, String>();
        storyObj.put("id",story.id);
        storyObj.put("name", story.name);
        storyObj.put("content", story.content);
        storyObj.put("date", story.date);
        storyObj.put("latitude", Double.toString(story.location.latitude));
        storyObj.put("longitude", Double.toString(story.location.longitude));
        storyObj.put("author", story.author);
        storyObj.put("uid", story.uid);
        //Story object created
        Firebase storyRef = storiesDB.child(story.id);
        Firebase usersDB = new Firebase("https://astory.firebaseio.com/users");
        Firebase userRef = usersDB.child(currentUserID);
        currentUserStories.add(story.id);

        //Story added to usersDB and storiesDB
        userRef.child("stories").child(story.id).setValue(storyObj);
        storyRef.setValue(storyObj);

        Log.d(TAG, "userStoryRef: " + userRef.child("stories").child(story.id));
        Log.d(TAG, "Date's broken. It equals " + date);
        if(!date.equals("")) {
            masterRootRef.child("stories").child(story.id).setValue(storyObj);
            masterGeoFire.setLocation(story.id, new GeoLocation(story.location.latitude, story.location.longitude));
        }else{
            todayRootRef.child("stories").child(story.id).setValue(storyObj);
            todayGeoFire.setLocation(story.id,new GeoLocation(story.location.latitude, story.location.longitude));
        }
        geoFire.setLocation(story.id, new GeoLocation(story.location.latitude, story.location.longitude));
    }

    public void removeStory(Story story) {
//        Log.d(TAG, "story.marker: " + story.marker);
        story.marker.remove();
        Log.d(TAG, "removeStory called");
        removeStoryGeofence(story);
        mGeofenceList.remove(story);
//        Log.d(TAG, "storyList: " + storyList);
//        storiesDB.child(story.name).removeValue();
//        commentsDB.child(story.name).removeValue();
        if(!date.equals("")){
            masterStoriesDB.child(story.id).removeValue();
            masterGeoFire.removeLocation(story.id);
        }else{
            todayRootRef.child("stories").child(story.id).removeValue();
            todayGeoFire.removeLocation(story.id);
        }
        Firebase usersDB = new Firebase("https://astory.firebaseio.com/users");
        Firebase userRef = usersDB.child(masterRootRef.getAuth().getUid());
        userRef.child("stories").child(story.id).removeValue();
        storyList.remove(story);
        currentUserStories.remove(story.id);
//        mMap.clear();
    }

    public void removeStoryFromDevice(Story story){
//        Log.d(TAG, "removeStoryFromDevice");
        currentUserStories.remove(story.id);
        story.marker.remove();
        removeStoryGeofence(story);
        mGeofenceList.remove(story);
        storyList.remove(story);
        Log.d(TAG, "within removeStoryFromDevice: " + story.name);
    }

    public void addStoryToDevice(Story story){
        if(story == null){
            Toast.makeText(this, "story is undefined", Toast.LENGTH_SHORT);
        }else {
            mGeofenceList = new ArrayList<>();
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(story.id)

                            // Set the circular region of this geofence.
                    .setCircularRegion(
                            story.location.latitude,
                            story.location.longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                            // Create the geofence.
                    .build());

//            Log.d(TAG, "Geofence list has : " + mGeofenceList.size() + " items");
            for (Geofence geo : mGeofenceList) {
//                Log.d(TAG, "item " + geo.getRequestId());
            }
            story.geofence = mGeofenceList.get(0);
            Log.d(TAG, "add stories to device called");
            Marker storyMarker;
            if(story.active){
                 storyMarker = mMap.addMarker(new MarkerOptions().position(story.location).title(story.name)
                        .icon(BitmapDescriptorFactory.fromResource(getResource(story))));
            }else {
                storyMarker = mMap.addMarker(new MarkerOptions().position(story.location).title(story.name)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.seen_marker64)));
            }
            Log.d(TAG, "Added marker: "+storyMarker.getTitle());
            Log.d(TAG, "Marker postion: " + storyMarker.getPosition());
//        addMarkerListener();
            story.marker = storyMarker;
            storyList.add(story);
        }
    }


    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        Log.d(TAG, "active before info window shown: "+active);
        mMap.setInfoWindowAdapter(new SummaryInfoWindowAdapter(comment_count, vote_count, active, getApplicationContext()));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Uri uri = Uri.parse("http://maps.google.com/maps?saddr="+mCurrentLocation.getLatitude()
                        +","+mCurrentLocation.getLongitude()+"&daddr="+marker.getPosition().latitude +
                        "," + marker.getPosition().longitude +
                        " (" + marker.getTitle() + ")");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        // Add a marker in Sydney and move the camera
        if(mCurrentLocation != null) {
            updateMap();
            LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, Constants.MAP_ZOOM_LEVEL));

        }
    }

    public void updateMap(){
        if(mCurrentLocation == null || mMap == null){
            return;
        }
        LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(myLocation), 2000, null);

        if(myCircle != null){
            myCircle.remove();
        }
        myCircle = mMap.addCircle(new CircleOptions()
                .center(myLocation)
                .radius(Constants.GEOFENCE_RADIUS_IN_METERS - 10)
                .strokeWidth(10)
                .strokeColor(Color.BLUE)
                .fillColor(Color.parseColor("#500084d3")));
//        mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker in myLocation"));
        addMarkerListener();



    }

    public void addMarkerListener(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for (Story story : storyList) {
                    if (story.marker.equals(marker)) {
                        if (story.active) {
                            Log.d(TAG, "within marker listener " + story.date);
                            goToViewStoryScreen(story, false);
                            return true;
                        } else {
                            comment_count = story.commentCount;
                            vote_count = story.voteCount;
                            active = story.active;
                            if(story.commentCount == null){
                                comment_count = 0;
                            }
                            if(story.voteCount == null){
                                vote_count = 0;
                            }
                            Log.d(TAG, "active before info window shown: "+active);
                            mMap.setInfoWindowAdapter(new SummaryInfoWindowAdapter(story.commentCount, story.voteCount,story.active, getApplicationContext()));
                            Log.d(TAG, "comment_count: " + comment_count + "\n vote_count: " + vote_count);
                            return false;
                        }
                    }
                }
                if(marker.getTitle() != null){
                    return false;
                }
                return true;
            }
        });
    }

    public void showMarkerOnMap(String storyID, String storyName, LatLng storyLocation, int commentCount, int voteCount){
        if(!storyAddedOnMap(storyLocation)) {
            Log.d(TAG, "showMarkerOnMap: " + storyName);
            active = false;
            tempMarker = mMap.addMarker(new MarkerOptions().position(storyLocation).title(storyName)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.seen_marker64)));


        }else{
            for(Story story: storyList){
                if(storyID.equals(story.id)){
                    tempMarker = story.marker;
                    active = story.active;
                }
            }
        }
        Log.d(TAG, "active before info window shown: " + active);
        mMap.setInfoWindowAdapter(new SummaryInfoWindowAdapter(commentCount, voteCount, active, getApplicationContext()));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(storyLocation), 2000, null);
        Log.d(TAG, "show marker storyID: " + storyID);
        Log.d(TAG, "tempMarker: " + tempMarker);
        if(tempMarker != null){tempMarker.showInfoWindow();}
        mainMenu.setVisibility(View.INVISIBLE);
        myLocationButton.setVisibility(View.VISIBLE);
        previewingStory = true;
//        stopLocationUpdates();


    }


    public void goToViewStoryScreen(Story story, boolean adding){
        Intent viewStoryIntent = new Intent(this, ViewStoryActivity.class);
        viewStoryIntent.putExtra(Constants.EXTRA_ADDED_KEY, adding);
        viewStoryIntent.putExtra(Constants.EXTRA_STORY_ID, story.id);
        viewStoryIntent.putExtra(Constants.EXTRA_STORY_NAME, story.name);
        viewStoryIntent.putExtra(Constants.EXTRA_STORY_CONTENT, story.content);
        viewStoryIntent.putExtra(Constants.EXTRA_STORY_AUTHOR, story.author);
        viewStoryIntent.putExtra(Constants.EXTRA_STORY_UID, story.uid);
        viewStoryIntent.putExtra(Constants.EXTRA_STORY_DATE, story.date);
        Log.d(TAG, "Main Activity date: " + story.date);
        viewStoryIntent.putExtra(Constants.EXTRA_STORY_DATE_KEY, date);
        viewStoryIntent.putExtra(Constants.EXTRA_CURRENT_USER, currentUser.getUsername());
        Log.d(TAG, "right before viewStoryScreeen storyList has " + storyList.size() + " items");
        startActivityForResult(viewStoryIntent, 1);
        Log.d(TAG, "right after viewStoryScreen storyList has " + storyList.size() + " items");
    }

    public void goToLoginScreen(){
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginIntent, 2);
    }

    public void goToProfile(View v){
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra(Constants.PROFILE_NAME, currentUser.getUsername());
        profileIntent.putExtra(Constants.PROFILE_CURRENT_USER, currentUser.getUsername());
        profileIntent.putExtra(Constants.PROFILE_AUTHOR, currentUser.getUsername());
        if(currentUserID == null){
            Toast.makeText(getApplicationContext(), "Sorry, your stories aren't available right now", Toast.LENGTH_SHORT).show();
            return;
        }
        profileIntent.putExtra(Constants.PROFILE_ID, currentUserID);
        startActivityForResult(profileIntent, Constants.PROFILE_REQUEST_CODE);

    }

    public void logout(View v){
        masterRootRef.unauth();
        goToLoginScreen();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, Boolean.toString(mGoogleApiClient.isConnected()));
        if(!mGoogleApiClient.isConnected()){
            viewStoryRequestCode = requestCode;
            viewStoryResultCode = resultCode;
            viewStoryData = data;
        }else {
            if ((requestCode == 1) && (resultCode == RESULT_OK)) {
                if(masterRootRef.getAuth() == null) {
                    goToLoginScreen();
                }
                Log.d(TAG, "Tries to delete story");
                String viewStory;
                viewStory = data.getExtras().getString(Constants.VIEW_STORY_KEY);
                Log.d(TAG, "storyList.size() " + storyList.size());
                for (int i = 0; i < storyList.size(); i++) {
                    Story story = storyList.get(i);
                    Log.d(TAG, "story: " + story.name);
                    Log.d(TAG, "viewStory: " + viewStory);
                    Log.d(TAG, "does equals " + story.id.equals(viewStory));
                    if (story.id.equals(viewStory)) {
                        removeStory(story);
                    }
                }
                selectedStoryID = data.getExtras().getString(Constants.PROFILE_STORY_SELECTED_ID);
                selectedStoryName = data.getExtras().getString(Constants.PROFILE_STORY_SELECTED_NAME);
                selectedStoryCommentCount = data.getExtras().getInt(Constants.PROFILE_STORY_SELECTED_COMMENT_COUNT);
                selectedStoryVoteCount = data.getExtras().getInt(Constants.PROFILE_STORY_SELECTED_VOTE_COUNT);
                if(data.getExtras().getString(Constants.PROFILE_STORY_SELECTED_LATITUDE) != null ||
                        data.getExtras().getString(Constants.PROFILE_STORY_SELECTED_LONGITUDE) != null){
                    double lat = Double.parseDouble(data.getExtras().getString(Constants.PROFILE_STORY_SELECTED_LATITUDE));
                    double lon = Double.parseDouble(data.getExtras().getString(Constants.PROFILE_STORY_SELECTED_LONGITUDE));
                    selectedStoryLatLng = new LatLng(lat, lon);
                    showMarkerOnMap(selectedStoryID, selectedStoryName, selectedStoryLatLng, selectedStoryCommentCount, selectedStoryVoteCount);

                }


            }
            else if((requestCode == 2) && (resultCode == RESULT_OK)){
//                Log.d(TAG, "Login activity returns result");
                currentUserID = data.getExtras().getString(Constants.CURRENT_USER_ID);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(Constants.CURRENT_USER_ID_KEY, currentUserID);
                editor.apply();

//                Log.d(TAG, "currentUserID: " + currentUserID);
                Firebase usersDB = new Firebase("https://astory.firebaseio.com/users");
                usersDB.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(DBUser.class);
                        if (currentUser.getStories() != null) {
//                            currentUserStories = currentUser.getStories();
                        }
//                        Log.d(TAG, "currentUser: " +currentUser);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }else if((requestCode == Constants.PROFILE_REQUEST_CODE) && (resultCode == RESULT_OK)){
                selectedStoryID = data.getExtras().getString(Constants.PROFILE_STORY_SELECTED_ID);
                selectedStoryName = data.getExtras().getString(Constants.PROFILE_STORY_SELECTED_NAME);
                selectedStoryCommentCount = data.getExtras().getInt(Constants.PROFILE_STORY_SELECTED_COMMENT_COUNT);
                selectedStoryVoteCount = data.getExtras().getInt(Constants.PROFILE_STORY_SELECTED_VOTE_COUNT);
                double lat = Double.parseDouble(data.getExtras().getString(Constants.PROFILE_STORY_SELECTED_LATITUDE));
                double lon = Double.parseDouble(data.getExtras().getString(Constants.PROFILE_STORY_SELECTED_LONGITUDE));
                selectedStoryLatLng = new LatLng(lat, lon);
                showMarkerOnMap(selectedStoryID, selectedStoryName, selectedStoryLatLng, selectedStoryCommentCount, selectedStoryVoteCount);

            }
            viewStoryData = null;
        }
    }
    // region AddGeofenceFragmentListener

    @Override
    public void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog){
    }

    @Override
    public void onDialogNegativeClick(android.support.v4.app.DialogFragment dialog) {
        // Do nothing
    }

//    @Override
    public void onFinishedInputDialog(String name, String content){
            mGeofenceList = new ArrayList<Geofence>();
            Story story = new Story();
            story.name = name;
            story.content = content;
            story.id=sanitizeText(name + currentUserID + System.currentTimeMillis());
            Log.d(TAG, "story.id: "+story.id);
            SimpleDateFormat s = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
            story.date = s.format(new Date());
            story.location = new LatLng(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude());
            story.radius = Constants.GEOFENCE_RADIUS_IN_METERS;
//            Log.d(TAG, currentUser.toString());
            story.author = currentUser.getUsername();
            story.uid = masterRootRef.getAuth().getUid();
            addStoryToGeofenceList(story);
        addStoryGeofence();
//            Log.d(TAG, "Definitely calls onFinishedInputDialog");
        goToViewStoryScreen(story, true);
        startLocationUpdates();
        }

    public void showDialog(View v){
//        addGeofencesButtonHandler(v);
        AddGeofenceFragment dialogFragment = new AddGeofenceFragment();
        dialogFragment.setListener(MainActivity.this);
                dialogFragment.show(MainActivity.this.getSupportFragmentManager(), "AddGeofenceFragment");
        stopLocationUpdates();
    }


    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1){
            Log.d(TAG, "storyList in Broadcast receiver contains "+ storyList.size() + " items");
            ArrayList<String> datapassed = arg1.getStringArrayListExtra("DATAPASSED");
            int transition = arg1.getIntExtra("TRANSITION", Geofence.GEOFENCE_TRANSITION_ENTER);
            for(String geofenceId: datapassed){
                updateViewableStories(transition, geofenceId);
            }
        }
    }

    private void updateViewableStories(int transition, String id){
//        Log.d(TAG, "updateViewableStories id: " + id + "\n transition: " + transition);
        if(transition == Geofence.GEOFENCE_TRANSITION_ENTER){
            for(Story story: storyList){
                if(story.id.equals(id)){
                    story.marker.setIcon(BitmapDescriptorFactory.fromResource(getResource(story)));
                    story.active = true;
                }
            }
        }
        if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){
            for(Story story: storyList){
                if(story.id.equals(id)){
                    story.marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.seen_marker64));
                    story.active = false;
                }
            }
        }
    }

    public int getResource(Story story){
        String locationMood = "neutral";
        int moodCount = 0;
        if(story.happyCount != null && story.happyCount > moodCount){
            locationMood="happy";
            moodCount = story.happyCount;
        }
        if(story.sadCount != null && story.sadCount > moodCount){
            locationMood="sad";
            moodCount = story.sadCount;
        }
        if(story.madCount != null && story.madCount > moodCount){
            locationMood = "mad";
            moodCount = story.madCount;
        }
        if(story.surprisedCount != null && story.surprisedCount > moodCount){
            locationMood = "surprised";
//            moodCount = story.surprisedCount;
        }

        switch(locationMood){
            case "happy":return R.mipmap.happy_marker64;
            case "sad":return R.mipmap.sad_marker64;
            case "mad":return R.mipmap.mad_marker64;
            case "surprised":return R.mipmap.surprised_marker64;
            case "neutral":return R.mipmap.default_marker64;
        }
        return R.mipmap.seen_marker64;

    }

    public void setCurrentDateOnView(){
        dpResult = (DatePicker) findViewById(R.id.dpResult);
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        dpResult.init(year, month, day, null);
    }

    public void addMainMenuListener(){
        mainMenu = (FloatingActionsMenu) findViewById(R.id.main_menu);
        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void addAddStoryListener(){
        addStory = (FloatingActionButton) findViewById(R.id.addStory);
        addStory.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showDialog(v);
//                mainMenu.collapse();
            }
        });
    }

    public void addViewProfileListener(){
        viewProfile = (FloatingActionButton) findViewById(R.id.view_profile);
        viewProfile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToProfile(v);
//                mainMenu.collapseImmediately();
            }
        });
    }

    public void addCalendarButtonListener(){
        calendarButton = (FloatingActionButton) findViewById(R.id.calendar_button);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (date.equals("")) {
                    showDialog(DATE_DIALOG_ID);
                } else {
                    calendarButton.setIcon(R.mipmap.ic_date_range_black_24dp);
                    calendarButton.setTitle(getString(R.string.date_filter));
                    date = "";
                    handleDatabase(date);
                }
//                mainMenu.collapse();
            }
        });



    }

    public void addRecentStoriesButtonListener(){
        recentStoriesButton = (FloatingActionButton) findViewById(R.id.all_stories_button);
        recentStoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(date.equals("")) {
                    recentStoriesButton.setIcon(R.mipmap.ic_restore_black_24dp);
                    recentStoriesButton.setTitle(getString(R.string.all_stories));
                    date = today;
//                    Collections.sort(storyList);
//                    mMap.clear();
//                    for(int i=0; i < storyList.size(); i++){
//                        if(i < Constants.RECENT_STORIES_COUNT){
//                            Log.d(TAG, "sorted Story is active"+storyList.get(i).active);
//                            addStoryToDevice(storyList.get(i));
//                        }else{
//                            storyList.remove(i);
//                        }
//                    }
                    handleDatabase(date);

                    Toast.makeText(getApplicationContext(), "Showing recent stories", Toast.LENGTH_SHORT).show();

                }else{
                    recentStoriesButton.setIcon(R.mipmap.ic_whatshot_black_24dp);
                    recentStoriesButton.setTitle(getString(R.string.recent_stories_button));
                    date = "";
                    handleDatabase(date);
                }
//                mainMenu.collapse();
            }
        });

    }

    public void addMyLocationListener(){
        myLocationButton = (FloatingActionButton) findViewById(R.id.localize);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMap();
                previewingStory = false;
                myLocationButton.setVisibility(View.INVISIBLE);
                mainMenu.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                // set date picker as current date
                return new DatePickerDialog(this, datePickerListener,
                        year, month,day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {

        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
            String strDate = format.format(calendar.getTime());
            date = strDate;
            handleDatabase(date);
            SimpleDateFormat s1 = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
            Date d = new Date();
            try {
                d = s1.parse(strDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            SimpleDateFormat s2 = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
            String storyDate = s2.format(d);
            Toast.makeText(getApplicationContext(), "Showing stories from "+storyDate, Toast.LENGTH_LONG).show();

//            Log.d(TAG, "strDate: " + date);


            // set selected date into datepicker also
            dpResult.init(year, month, day, null);
            calendarButton.setIcon(R.mipmap.ic_restore_black_24dp);
            calendarButton.setTitle(getString(R.string.all_stories));

        }
    };

    public void handleDatabase(String date){
//        Log.d(TAG, "handleDatabase date: " + date);
        for(int i= 0; i<storyList.size(); i++){
            Log.d(TAG, "i: " + i);
            Story s = storyList.get(i);
            removeStoryFromDevice(s);
            mMap.clear();
        }
        rootRef = new Firebase("https://astory.firebaseio.com/"+date);
        storiesDB = rootRef.child("stories");
        commentsDB = rootRef.child("comments");
//        usersDB = new Firebase("https://astory.firebaseio.com/users");
        masterRootRef = new Firebase("https://astory.firebaseio.com");
        masterStoriesDB = masterRootRef.child("stories");
        masterCommentsDB = masterRootRef.child("comments");
        Log.d(TAG, "today at todayRootRef = "+today);
        todayRootRef = new Firebase("https://astory.firebaseio.com/"+today);
        geoFire = new GeoFire(rootRef.child("geoStories"));
        masterGeoFire = new GeoFire(masterRootRef.child("geoStories"));
        todayGeoFire = new GeoFire(todayRootRef.child("geoStories"));
        geoQuery = geoFire.queryAtLocation(new GeoLocation(0, 0), Constants.STORY_QUERY_RADIUS);

        if(initiallyLoadedStories) {
            geoQuery.removeAllListeners();
            geoQuery.addGeoQueryEventListener(this);
            stopLocationUpdates();
            startLocationUpdates();
        }else{
            initiallyLoadedStories = true;
        }

    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean(Constants.REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(Constants.LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }
}


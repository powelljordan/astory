package jordan.astory;

/**
 * Created by Jordan on 12/28/2015.
 */
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    /**
     * Stores paramenters for requests to the FusedLocationProviderApi
     */
    protected LocationRequest mLocationRequest;

    public static FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        rootRef = new Firebase("https://astory.firebaseio.com/");
        storiesDB = new Firebase("https://astory.firebaseio.com/stories");
        geoFire = new GeoFire(new Firebase("https://astory.firebaseio.com/geoStories"));
        geoQuery = geoFire.queryAtLocation(new GeoLocation(0,0), .1);

        setContentView(R.layout.activity_main);
//        mAddGeofencesButton = (Button) findViewById(R.id.myFAB);
        mRemoveGeofencesButton = (Button) findViewById(R.id.remove_geofences_button);
//
        mGeofenceList = new ArrayList<Geofence>();
        storyList = new ArrayList<Story>();

        mGeofencePendingIntent = null;

        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);

        mRequestingLocationUpdates = true;



        updateValuesFromBundle(savedInstanceState);

        setButtonsEnabledState();

        //Iniitialize all the geofences
        populateGeofenceList();

        //start building the GoogleAPI
        buildGoogleApiClient();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fragmentManager = getSupportFragmentManager();
        goToLoginScreen();
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
        mCurrentLocation = location;
        geoQuery.setCenter(new GeoLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        updateMap();
//        Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location){
        Log.d(TAG, "onKeyEntered called");
        if(mGeofenceList.size() < 100) {
            Log.d(TAG, "mGeofenceList size is less than 100");
            Log.d(TAG, "key " + key);
            Firebase specificStoryDB = storiesDB.child(key);

            specificStoryDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange");
                    Log.d(TAG, "DataSnapshot: " + dataSnapshot);
                    DBStory dbStory = dataSnapshot.getValue(DBStory.class);
                    Log.d(TAG, "dbStory " + dbStory);
                    if(dbStory != null) {
                        Log.d(TAG, "dbStory.getName() " + dbStory.getName());
                        boolean alreadyAddedStory = false;
                        for (Story localStory : storyList) {
                            if (dbStory.getName().equals(localStory.name)) {
                                Log.d(TAG, dbStory.getName() + " story already in storyList");
                                alreadyAddedStory = true;
                            }
                        }
                        if (!alreadyAddedStory) {
                            Story story = new Story();
                            Log.d(TAG, dbStory.getName());
                            story.name = dbStory.getName();
                            story.content = dbStory.getContent();
                            story.location = new LatLng(Double.parseDouble(dbStory.getLatitude()), Double.parseDouble(dbStory.getLongitude()));
                            story.radius = Constants.GEOFENCE_RADIUS_IN_METERS;
                            addStoryToGeofenceList(story);
                            addStoryGeofence();
                        }
                    }


                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            Log.d(TAG, "this is beyond me");

        }
    }

    @Override
    public void onKeyExited(String key){
        Log.d(TAG, "onKeyExited called");
        for (int i = 0; i < storyList.size(); i++) {
            if (storyList.get(i).name.equals(key)) {
                removeStory(storyList.get(i));
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


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        geoQuery.addGeoQueryEventListener(this);
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
        Log.d(TAG, "receiver registered");

        //Start our own service
//        Intent intent = new Intent(MainActivity.this,
//                jordan.astory.GeofenceTransitionsIntentService.class);
//        startService(intent);

    }

    @Override
    public void onResume(){
        super.onResume();
        if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates){
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        geoQuery.removeAllListeners();
        unregisterReceiver(myReceiver);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates){
            stopLocationUpdates();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        for(Story story: storyList){
            removeStory(story);
        }
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
                LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, Constants.MAP_ZOOM_LEVEL));
                updateMap();

                startLocationUpdates();
            }catch(SecurityException securityException){
                logSecurityException(securityException);
            }

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
        Log.d(TAG, "returns Geofencing request");
        return builder.build();
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
//    public void addGeofencesButtonHandler(View view) {
//        if (!mGoogleApiClient.isConnected()) {
//            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if(mGeofenceList.size() > 0) {
//            try {
//                LocationServices.GeofencingApi.addGeofences(
//                        mGoogleApiClient,
//                        // The GeofenceRequest object.
//
//                        getGeofencingRequest(),
//                        // A pending intent that that is reused when calling removeGeofences(). This
//                        // pending intent is used to generate an intent when a matched geofence
//                        // transition is observed.
//                        getGeofencePendingIntent()
//                ).setResultCallback(this); // Result processed in onResult().
//                Log.d(TAG, "callsGeofencingRequest()");
//                mGeofenceList = new ArrayList<Geofence>();
//                storyList = new ArrayList<Story>();
//            } catch (SecurityException securityException) {
//                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
//                logSecurityException(securityException);
//            }
//        }
//    }

    public void addStoryGeofence(){
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
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

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.
            setButtonsEnabledState();

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
        Log.d(TAG, "returns getGeofencePendingIntent");
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
                    .setRequestId(story.name)

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

            Log.d(TAG, "Geofence list has : " + mGeofenceList.size() + " items");
            for(Geofence geo: mGeofenceList){
                Log.d(TAG, "item " + geo.getRequestId());
            }
            story.geofence = mGeofenceList.get(0);
            Marker storyMarker = mMap.addMarker(new MarkerOptions().position(story.location).title(story.name));
//        addMarkerListener();
            story.marker = storyMarker;
            storyList.add(story);
            addStoryToDB(story);

        }
    }

    public void addStoryToDB(Story story){
        HashMap<String, String> storyObj = new HashMap<String, String>();
        storyObj.put("name", story.name);
        storyObj.put("content", story.content);
        storyObj.put("latitude", Double.toString(story.location.latitude));
        storyObj.put("longitude", Double.toString(story.location.longitude));
        Firebase storyRef = storiesDB.child(story.name);
        storyRef.setValue(storyObj);
        geoFire.setLocation(story.name, new GeoLocation(story.location.latitude, story.location.longitude));

    }

    public void removeStory(Story story) {
//        Log.d(TAG, "story.marker: " + story.marker);
        story.marker.remove();
        Log.d(TAG, "removeStoryGeofence called");
        removeStoryGeofence(story);
        mGeofenceList.remove(story);
//        Log.d(TAG, "storyList: " + storyList);
        storiesDB.child(story.name).removeValue();
        geoFire.removeLocation(story.name);
        storyList.remove(story);
//        mMap.clear();
    }

    /**
     * Ensures that only one button is enabled at any time. The Add Geofences button is enabled
     * if the user hasn't yet added geofences. The Remove Geofences button is enabled if the
     * user has added geofences.
     */
    private void setButtonsEnabledState() {
        if (mGeofencesAdded) {
//            mAddGeofencesButton.setEnabled(false);
            mRemoveGeofencesButton.setEnabled(true);
        } else {
//            mAddGeofencesButton.setEnabled(true);
            mRemoveGeofencesButton.setEnabled(false);
        }
    }

    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        if(mCurrentLocation != null) {
            updateMap();
            LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, Constants.MAP_ZOOM_LEVEL));

        }
    }

    public void updateMap(){
        LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(myLocation));

        if(myCircle != null){
            myCircle.remove();
        }
        myCircle = mMap.addCircle(new CircleOptions().center(myLocation).radius(Constants.GEOFENCE_RADIUS_IN_METERS).strokeColor(Color.BLUE));
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
                            goToViewStoryScreen(story);
                            return true;
                        } else {
                            marker.setSnippet("You're too far away to view this story");
                            return false;
                        }
                    }
                }
                return true;
            }
        });
    }

    public void goToViewStoryScreen(Story story){
        Intent viewStoryIntent = new Intent(this, ViewStoryActivity.class);
        viewStoryIntent.putExtra(Constants.EXTRA_STORY_NAME, story.name);
        viewStoryIntent.putExtra(Constants.EXTRA_STORY_CONTENT, story.content);
        startActivityForResult(viewStoryIntent, 1);
    }

    public void goToLoginScreen(){
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginIntent, 2);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, Boolean.toString(mGoogleApiClient.isConnected()));
        if(!mGoogleApiClient.isConnected()){
            viewStoryRequestCode = requestCode;
            viewStoryResultCode = resultCode;
            viewStoryData = data;
        }else {
            if ((requestCode == 1) && (resultCode == RESULT_OK)) {
                String viewStory;
                viewStory = data.getExtras().getString(Constants.VIEW_STORY_KEY);

                for (int i = 0; i < storyList.size(); i++) {
                    Story story = storyList.get(i);
                    Log.d(TAG, "story: " + story.name);
                    Log.d(TAG, "viewStory: " + viewStory);
                    Log.d(TAG, "does equals " + story.name.equals(viewStory));
                    if (story.name.equals(viewStory)) {
                        removeStory(story);
                    }
                }

            }
            viewStoryData = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            story.location = new LatLng(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude());
            story.radius = Constants.GEOFENCE_RADIUS_IN_METERS;
            addStoryToGeofenceList(story);
            addStoryGeofence();
            Log.d(TAG, "Definitely calls onFinishedInputDialog");
            startLocationUpdates();
        }

//    private boolean dataIsValid() {
//        boolean validData = true;
//
//        String name = getViewHolder().nameEditText.getText().toString();
//        String latitudeString = getViewHolder().latitudeEditText.getText().toString();
//        String longitudeString = getViewHolder().longitudeEditText.getText().toString();
//        String radiusString = getViewHolder().radiusEditText.getText().toString();
//
//        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(latitudeString)
//                || TextUtils.isEmpty(longitudeString) || TextUtils.isEmpty(radiusString)) {
//            validData = false;
//        } else {
//            double latitude = Double.parseDouble(latitudeString);
//            double longitude = Double.parseDouble(longitudeString);
//            float radius = Float.parseFloat(radiusString);
//            if ((latitude < Constants.Geometry.MinLatitude || latitude > Constants.Geometry.MaxLatitude)
//                    || (longitude < Constants.Geometry.MinLongitude || longitude > Constants.Geometry.MaxLongitude)
//                    || (radius < Constants.Geometry.MinRadius || radius > Constants.Geometry.MaxRadius)) {
//                validData = false;
//            }
//        }
//
//        return validData;
//    }

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
            ArrayList<String> datapassed = arg1.getStringArrayListExtra("DATAPASSED");
            int transition = arg1.getIntExtra("TRANSITION", Geofence.GEOFENCE_TRANSITION_ENTER);
            for(String geofenceId: datapassed){
                updateViewableStories(transition, geofenceId);
            }
        }
    }

    private void updateViewableStories(int transition, String id){
        Log.d(TAG, "updateViewableStories id: " + id + "\n transition: " + transition);
        if(transition == Geofence.GEOFENCE_TRANSITION_ENTER){
            for(Story story: storyList){
                if(story.name.equals(id)){
                    story.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    story.active = true;
                }
            }
        }
        if(transition == Geofence.GEOFENCE_TRANSITION_EXIT){
            for(Story story: storyList){
                if(story.name.equals(id)){
                    story.marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    story.active = false;
                }
            }
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean(Constants.REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(Constants.LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }
}


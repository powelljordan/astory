package jordan.astory;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jordan on 2/14/2016.
 */
public class ProfileActivity extends Activity {
    ListView listView;
    TextView userName;
    String user;
    String currentUser;
    String currentUserID;
    String author;
    String authorID;
    String profileUserID;
    ArrayList<DBStory> stories;
    Firebase userDB = new Firebase("https://astory.firebaseio.com/users/");
    private SharedPreferences mSharedPreferences;
    final private String TAG = "ProfileActivity";
    Firebase masterRootRef = new Firebase("https://astory.firebaseio.com");
    UserStoriesAdapter userStoriesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        currentUserID = mSharedPreferences.getString(Constants.CURRENT_USER_ID_KEY, "N/A");
        Intent intent = getIntent();
        user = intent.getStringExtra(Constants.PROFILE_NAME);
        currentUser = intent.getStringExtra(Constants.PROFILE_CURRENT_USER);
        author = intent.getStringExtra(Constants.PROFILE_AUTHOR);
        profileUserID = intent.getStringExtra(Constants.PROFILE_ID);
        userName = (TextView)findViewById(R.id.user_name);
        userName.setText(user);
        listView = (ListView)findViewById(R.id.list);
        stories = new ArrayList<>();
        Firebase userStoriesRef = userDB.child(profileUserID).child("stories");
        userStoriesAdapter = new UserStoriesAdapter(getBaseContext(), R.layout.profile_list_item, stories);
        listView.setAdapter(userStoriesAdapter);

        userStoriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.d(TAG, "dataSnapshot: " + dataSnapshot);
                if(dataSnapshot.getValue(DBStory.class).getId() == null){
                    return;
                }
                stories.add(dataSnapshot.getValue(DBStory.class));
                new getCity().execute(dataSnapshot.getValue(DBStory.class));
                userStoriesAdapter.updateList(stories);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                stories.remove(dataSnapshot.getValue(DBStory.class));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
//        Log.d(TAG, "stories: " + stories);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position,
                                                                    long id) {
                                                int itemPosition = position;
                                                final DBStory itemValue = (DBStory) listView.getItemAtPosition(position);
                                                new AlertDialog.Builder(ProfileActivity.this)
                                                        .setTitle("Go to Story")
                                                        .setMessage("Are you sure you want to go to this story?")
                                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent resultIntent = new Intent();
                                                                resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_ID, itemValue.getId());
                                                                resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_NAME, itemValue.getName());
                                                                resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_LATITUDE, itemValue.getLatitude());
                                                                resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_LONGITUDE, itemValue.getLongitude());
                                                                resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_COMMENT_COUNT, itemValue.getCommentCount());
                                                                resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_VOTE_COUNT, itemValue.getVoteCount());
                                                                if(itemValue.getCommentCount() == null){
                                                                    resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_COMMENT_COUNT, 0);
                                                                }
                                                                if(itemValue.getVoteCount() == null){
                                                                    resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED_VOTE_COUNT, 0);
                                                                }


                                                                resultIntent.putExtra(Constants.PROFILE_STORY_SELECTED, "true");
                                                                        setResult(RESULT_OK, resultIntent);
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
                                        }

        );

        }

        public void logout(View v){
            masterRootRef.unauth();
            finish();

        }

        private class getCity extends AsyncTask<DBStory, Void, String[]> {
            @Override
            protected String[] doInBackground(DBStory... params) {
                DBStory story = params[0];
                Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = gcd.getFromLocation(Double.parseDouble(story.getLatitude()),
                            Double.parseDouble(story.getLongitude()), 1);
                    Log.d(TAG, "city: " + addresses.get(0).getLocality());
                    if (addresses.size() > 0) {
                        return new String[]{addresses.get(0).getLocality(), story.getId()};
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String[] cityID){
                if(cityID != null) {
                    for (int i = 0; i < stories.size(); i++) {
                        if (stories.get(i).getId().equals(cityID[1])) {
                            if(stories.get(i).getCity() == null) {
                                stories.get(i).setCity(cityID[0]);
                            }
                            Log.d(TAG, "cityID: " + cityID);
                            userStoriesAdapter.updateList(stories);
                        }
                    }
                }
            }
        }

    }

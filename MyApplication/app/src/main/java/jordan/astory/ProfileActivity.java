package jordan.astory;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.ArrayList;

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
    ArrayList<DBStory> stories;
    Firebase userDB = new Firebase("https://astory.firebaseio.com/users/");
    private SharedPreferences mSharedPreferences;
    final private String TAG = "ProfileActivity";
    Firebase masterRootRef = new Firebase("https://astory.firebaseio.com");
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
        authorID = intent.getStringExtra(Constants.EXTRA_STORY_UID);
        userName = (TextView)findViewById(R.id.user_name);
        userName.setText(user);
        listView = (ListView)findViewById(R.id.list);
        stories = new ArrayList<>();
        Log.d(TAG, stories.toString());
        Log.d(TAG, currentUserID);
        Firebase userStoriesRef = userDB.child(authorID).child("stories");

        final UserStoriesAdapter userStoriesAdapter = new UserStoriesAdapter(getBaseContext(), R.layout.profile_list_item, stories);
        listView.setAdapter(userStoriesAdapter);

        userStoriesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, dataSnapshot.getValue(DBStory.class).getName());
                stories.add(dataSnapshot.getValue(DBStory.class));
                userStoriesAdapter.updateList(stories);
                Log.d(TAG, "stories inside callback: " + stories);
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
        Log.d(TAG, "stories: " + stories);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position,
                                                                    long id) {
                                                int itemPosition = position;
                                                DBStory itemValue = (DBStory) listView.getItemAtPosition(position);
                                                Uri uri = Uri.parse("geo:0,0?q="+stories.get(itemPosition).getLatitude()+
                                                        ","+stories.get(itemPosition).getLongitude()+
                                                        " ("+stories.get(itemPosition).getName()+")");
                                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                startActivity(intent); }
                                        }

        );

        }

        public void logout(View v){
            masterRootRef.unauth();
            finish();

        }

    }

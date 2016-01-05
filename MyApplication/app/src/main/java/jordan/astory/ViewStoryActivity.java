package jordan.astory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import org.w3c.dom.Text;

/**
 * Created by Jordan on 12/30/2015.
 */
public class ViewStoryActivity extends AppCompatActivity {

    private String name;
    private String content;
    private Button deleteButton;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_story);
        Intent intent = getIntent();
        name = intent.getStringExtra(Constants.EXTRA_STORY_NAME);
        content = intent.getStringExtra(Constants.EXTRA_STORY_CONTENT);
        TextView nameText = (TextView) findViewById(R.id.view_story_name);
        TextView contentText = (TextView) findViewById(R.id.view_story_content);
        nameText.setText(name);
        contentText.setText(content);
        //Toggle delete button
//        for(Geofence geo: GeofenceTransitionsIntentService.activeGeofences){
//            if(name.equals(geo.getRequestId())){
//                deleteButton = (Button) findViewById(R.id.delete_story_button);
//                deleteButton.setVisibility(View.VISIBLE);
//            }
//        }

    }


    public void onDeleteStory(View v){
        setResult(RESULT_OK, new Intent().putExtra(Constants.VIEW_STORY_KEY, name));
        finish();
    }



}

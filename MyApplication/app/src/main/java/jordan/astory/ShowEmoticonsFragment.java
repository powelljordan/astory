package jordan.astory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.w3c.dom.Text;

/**
 * Created by Jordan on 3/18/2016.
 */
public class ShowEmoticonsFragment extends DialogFragment {
    ShowEmoticonsFragmentListener activity;


    ShowEmoticonsFragmentListener listener;
    private SharedPreferences mSharedPreferences;
    public void setListener(ShowEmoticonsFragmentListener listener) {
        this.listener = listener;
    }

    // endregion

    // region Overrides

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.show_emoticons_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final ImageView happyReaction = (ImageView) dialog.findViewById(R.id.happy);
                final ImageView sadReaction = (ImageView) dialog.findViewById(R.id.sad);
                final ImageView madReaction = (ImageView) dialog.findViewById(R.id.mad);
                final ImageView surprisedReaction = (ImageView) dialog.findViewById(R.id.surprised);

                final TextView happyCount = (TextView) dialog.findViewById(R.id.happyCount);
                final TextView sadCount = (TextView) dialog.findViewById(R.id.sadCount);
                final TextView madCount =  (TextView) dialog.findViewById(R.id.madCount);
                final TextView surprisedCount = (TextView) dialog.findViewById(R.id.surprisedCount);



                activity = (ShowEmoticonsFragmentListener) getActivity();
                mSharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
                String storyString = mSharedPreferences.getString(Constants.CURRENT_STORY, "N/A");
                Log.d("ShowEmoticonsFragment", "story: "+storyString);
                Firebase storiesDB = new Firebase("https://astory.firebaseio.com/stories/"+storyString);
                storiesDB.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DBStory story = dataSnapshot.getValue(DBStory.class);
                        Log.d("ShowEmoticonsFragment", "DBStory: "+story);
                        if(story == null){
                            return;
                        }

                        if(story.getHappyCount() != null &&  story.getHappyCount() > 0){
                            happyReaction.setVisibility(View.VISIBLE);
                            happyCount.setText(story.getHappyCount().toString());
                        }
                        if(story.getSadCount() != null &&  story.getSadCount() > 0){
                            sadReaction.setVisibility(View.VISIBLE);
                            sadCount.setText(story.getSadCount().toString());
                        }
                        if(story.getMadCount() != null &&  story.getMadCount() > 0){
                            madReaction.setVisibility(View.VISIBLE);
                            madCount.setText(story.getMadCount().toString());
                        }

                        if(story.getSurprisedCount() != null &&  story.getSurprisedCount() > 0){
                            surprisedReaction.setVisibility(View.VISIBLE);
                            surprisedCount.setText(story.getSurprisedCount().toString());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

            }
        });


        return dialog;
    }


    public interface ShowEmoticonsFragmentListener {
        void onFinishedShowEmoticons(String name);
    }
}

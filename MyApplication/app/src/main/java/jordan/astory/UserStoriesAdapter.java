package jordan.astory;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jordan on 2/27/2016.
 */
public class UserStoriesAdapter extends ArrayAdapter<DBStory> {

    private ArrayList<DBStory> stories = new ArrayList<>();
    private String TAG = "jordan.astory.UserStoriesAdapter";

    static class UserStoriesViewHolder{
        TextView title;
        TextView city;
        TextView date;
        TextView commentCount;
        TextView reactionCount;
    }

    public UserStoriesAdapter(Context context, int textViewResourceId, ArrayList<DBStory> stories){
        super(context,textViewResourceId, stories);
    }
    @Override
    public int getCount() {
        Log.d(TAG, "this: " + this);
        return this.stories.size();
    }


    @Override
    public void add(DBStory object) {
        stories.add(object);
        super.add(object);
    }

    public void updateList(ArrayList<DBStory> l){
        stories.clear();
        stories.addAll(l);
        this.notifyDataSetChanged();
    }

    @Override
    public DBStory getItem(int position) {
        return stories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        UserStoriesViewHolder viewHolder;
        if(row == null){
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.profile_list_item, parent, false);
            viewHolder = new UserStoriesViewHolder();
            viewHolder.title = (TextView) row.findViewById(R.id.profile_story_title);
            viewHolder.city = (TextView) row.findViewById(R.id.profile_story_city);
            viewHolder.date = (TextView) row.findViewById(R.id.profile_story_date);
            viewHolder.commentCount = (TextView) row.findViewById(R.id.profile_comment_count);
            viewHolder.reactionCount = (TextView) row.findViewById(R.id.profile_reaction_count);
            row.setTag(viewHolder);
        }else{
            viewHolder = (UserStoriesViewHolder)row.getTag();
        }
        DBStory story = getItem(position);
        Log.d(TAG, "story: "+story);
        Log.d(TAG, "story.content: "+story.getContent());
        viewHolder.title.setText(story.getName());
        viewHolder.date.setText(story.getDate());
        Log.d(TAG, "commentCount: " + story.getCommentCount());
        Log.d(TAG, "voteCount: "+story.getVoteCount());
        if(story.getVoteCount() == null){
            viewHolder.reactionCount.setText("0");
        }else {
            viewHolder.reactionCount.setText(Integer.toString(story.getVoteCount()));
        }

        if(story.getCommentCount() == null){
            viewHolder.commentCount.setText("0");
        }else {
            viewHolder.commentCount.setText(Integer.toString(story.getCommentCount()));
        }


        Geocoder gcd = new Geocoder(this.getContext(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(Double.parseDouble(story.getLatitude()),
                    Double.parseDouble(story.getLongitude()), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0)
            viewHolder.city.setText(addresses.get(0).getLocality());
        return row;
    }
}

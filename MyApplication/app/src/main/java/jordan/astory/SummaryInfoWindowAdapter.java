package jordan.astory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Jordan on 3/26/2016.
 */
public class SummaryInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private View v;
    private int voteCount;
    private int commentCount;
    private boolean active;
    private Context context;
    public SummaryInfoWindowAdapter(int vote_count, int comment_count, boolean a, Context c){
        voteCount = vote_count;
        commentCount = comment_count;
        context = c;
        active = a;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        v = LayoutInflater.from(context).inflate(R.layout.summary_info_window_layout, null);
        TextView title = (TextView) v.findViewById(R.id.title);
        TextView comment_count = (TextView) v.findViewById(R.id.map_comment_count);
        TextView reaction_count = (TextView) v.findViewById(R.id.map_reaction_count);
        TextView message = (TextView) v.findViewById(R.id.message);
        title.setText(marker.getTitle());
        if(!active) {
            comment_count.setText(Integer.toString(commentCount));
            reaction_count.setText(Integer.toString(voteCount));
        }else{
            comment_count.setText(Integer.toString(commentCount));
            reaction_count.setText(Integer.toString(voteCount));
            message.setText("Click on the marker to view this story");
        }
        return v;
    }
}

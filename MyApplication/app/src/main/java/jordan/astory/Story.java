package jordan.astory;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jordan on 12/29/2015.
 */
public class Story implements Comparable{
    public String id;
    public String name;
    public String content;
    public String author;
    public String uid;
    public String date;
    public LatLng location;
    public double radius;
    public Marker marker;
    public Geofence geofence;
    public String mediaUri;
    public String youtubeVideoID;
    public String mediaType;
    public String mediaUpdated;
    public Integer happyCount;
    public Integer sadCount;
    public Integer madCount;
    public Integer surprisedCount;
    public Integer commentCount;
    public Integer voteCount;
    public boolean active = false;
    public String city;

    public Story(){

    }

    @Override
    public String toString(){
        return "Name: " + this.name +
                "\n Content: "  +  this.content  +
                "\n Author:  "  +  this.author   +
                "\n Uid:"       +  this.uid      +
                "\n Date:   "   +  this.date     +
                "\n Location: " +  this.location +
                "\n Radius: "   +  this.radius   +
                "\n Marker: "   +  this.marker   +
                "\n Geofence: " +  this.geofence +
                "\n Active: "   +  this.active   +
                "\n Media Uri: "+  this.mediaUri +
                "\n Media Type:"+  this.mediaType+
                "\n Media Updated:"+this.mediaUpdated;
    }

    @Override
    public int compareTo(Object another) {
        Story anotherStory = (Story) another;
        SimpleDateFormat s1 = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        Date d1 = new Date();
        Date d2 = new Date();
        try {
            d1 = s1.parse(anotherStory.date);
            d2 = s1.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat s2 = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        String anotherStoryDate = s2.format(d2);
        String storyDate = s2.format(d1);
        return storyDate.compareTo(anotherStoryDate);
    }
}

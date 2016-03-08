package jordan.astory;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Jordan on 12/29/2015.
 */
public class Story {
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
    public String mediaType;
    public String mediaUpdated;
    public boolean active = false;

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
}

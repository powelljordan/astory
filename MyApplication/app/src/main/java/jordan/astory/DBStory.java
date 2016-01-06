package jordan.astory;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Jordan on 1/3/2016.
 */
public class DBStory {
    private String name;
    private String content;
    private String latitude;
    private String longitude;
    public DBStory(){

    }

    public String getName(){
        return name;
    }

    public String getContent(){
        return content;
    }

    public String getLatitude(){
        return latitude;
    }

    public String getLongitude(){
        return longitude;
    }
}
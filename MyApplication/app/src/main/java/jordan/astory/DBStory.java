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
    private String author;
    private String date;
    private String latitude;
    private String longitude;
    private String mediaUri;
    private String mediaType;
    private String mediaUpdated;
    public DBStory(){

    }

    public String getName(){
        return name;
    }

    public String getContent(){
        return content;
    }

    public String getAuthor(){return author;}

    public String getDate(){
        return date;
    }

    public String getLatitude(){
        return latitude;
    }

    public String getLongitude(){
        return longitude;
    }

    public String getMediaUri(){
        return mediaUri;
    }

    public String getMediaType(){
        return mediaType;
    }

    public String getMediaUpdated(){
        return mediaUpdated;
    }

    @Override
    public String toString(){
        return name;
    }
}

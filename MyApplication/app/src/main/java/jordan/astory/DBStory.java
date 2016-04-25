package jordan.astory;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Jordan on 1/3/2016.
 */
public class DBStory {
    private String id;
    private String name;
    private String content;
    private String author;
    private String uid;
    private String date;
    private String latitude;
    private String longitude;
    private String city;
    private String mediaUri;
    private String youtubeVideoID;
    private String mediaType;
    private String mediaUpdated;
    private int voteCount;
    private int happyCount;
    private int sadCount;
    private int madCount;
    private int surprisedCount;
    private int commentCount;
    private int viewCount;
    public DBStory(){

    }
    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }

    public String getContent(){
        return content;
    }

    public String getAuthor(){return author;}

    public String getUid(){
        return uid;
    }

    public String getDate(){
        return date;
    }

    public String getLatitude(){
        return latitude;
    }

    public String getLongitude(){
        return longitude;
    }

    public String getCity(){
        return city;
    }

    public void setCity(String c){
        city = c;
    }

    public String getMediaUri(){
        return mediaUri;
    }

    public String getYoutubeVideoID(){
        return youtubeVideoID;
    }

    public String getMediaType(){
        return mediaType;
    }

    public String getMediaUpdated(){
        return mediaUpdated;
    }

    public Integer getVoteCount(){
        return voteCount;
    }

    public Integer getHappyCount(){return happyCount;}

    public Integer getSadCount(){
        return sadCount;
    }

    public Integer getMadCount(){
        return madCount;
    }

    public Integer getSurprisedCount(){
        return surprisedCount;
    }

    public Integer getCommentCount(){
        return commentCount;
    }

    public Integer getViewCount(){
        return viewCount;
    }

    @Override
    public String toString(){
        return name;
    }
}

package jordan.astory;

/**
 * Created by Jordan on 2/11/2017.
 */

import org.json.JSONException;
import org.json.JSONObject;

public class SpotifyResult {
    private String uri;
    private String track;
    private String artist;
    private String duration;
    private String image_url;

    public SpotifyResult(JSONObject result){
        result = result;
        try {
            uri = result.getString("uri");

            duration = result.getString("duration_ms");

            artist = result.getJSONArray("artists")
                    .getJSONObject(0)
                    .getString("name");

            track = result.getString("name");

            image_url = result.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String getUri(){
        return uri;
    }

    public String getTrack(){
        return track;
    }

    public String getArtist(){
        return artist;
    }

    public String getDuration(){
        return duration;
    }

    public String getImage_URL(){
        return image_url;
    }
}

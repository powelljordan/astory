package jordan.astory;

/**
 * Created by Jordan on 2/11/2017.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class SearchResultsAdapter extends ArrayAdapter<SpotifyResult> {
    private ArrayList<SpotifyResult> spotify_results = new ArrayList<>();
    private SpotifyResult spotify_result;
    private HashMap<Integer, Bitmap> album_art_map = new HashMap<>();
    private HashMap<Integer, ImageView> image_view_map = new HashMap<>();
    private HashMap<Integer, View> rows = new HashMap<>();
    private Context context;



    static class SpotifyResultsViewHolder{
        TextView title;
        TextView artist;
        ImageView album_art;
        ImageView attach_remove;
    }

    SpotifyResultsViewHolder viewHolder;
    public SearchResultsAdapter(Context context, int resource, ArrayList<SpotifyResult> spotify_results) {
        super(context, resource, spotify_results);
        this.context = context;

    }

    public int getCount(){
        return this.spotify_results.size();
    }

    public void add(SpotifyResult object){
        spotify_results.add(object);
        super.add(object);
    }

    public void updateList(ArrayList<SpotifyResult> l){
        spotify_results.clear();
        spotify_results.addAll(l);
        album_art_map = new HashMap<>();
        Log.d("SearchAdapter", "spotify_results size: "+spotify_results.size());
        this.notifyDataSetChanged();
    }

    public SpotifyResult getItem(int position){
        return spotify_results.get(position);
    }

    public long getItemId(int position){
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        final View row;
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.spotify_list_item, parent, false);
            viewHolder = new SpotifyResultsViewHolder();
            viewHolder.title = (TextView) row.findViewById(R.id.track_title);
            viewHolder.artist = (TextView) row.findViewById(R.id.track_artist);
            viewHolder.album_art = (ImageView) row.findViewById(R.id.album_art);
            image_view_map.put(position, viewHolder.album_art);
            row.setTag(viewHolder);
        }else{
            row = convertView;
            viewHolder = (SpotifyResultsViewHolder) row.getTag();
        }
        viewHolder.attach_remove = (ImageView) row.findViewById(R.id.attach_remove_song);
        viewHolder.attach_remove.setTag(position);
        rows.put(position, row);
        viewHolder.attach_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<SpotifyResult> selected = new ArrayList<SpotifyResult>(Arrays.asList(spotify_results.get((int)viewHolder.attach_remove.getTag())));
                updateList(selected);
//                ListView lv = (ListView) v.findViewById(R.id.spotify_results_list);
//                ViewGroup.LayoutParams params = lv.getLayoutParams();
//                params.height = (int)((double) row.getHeight() * 1.25);
//                lv.setLayoutParams(params);
//                lv.requestLayout();
//                viewHolder.attach_remove.setImageResource(R.mipmap.ic_cancel_black_36dp);

//                for (int i=0; i<getCount(); i++){
//                    if (i != position){
//                        rows.get(i).setVisibility(View.GONE);
//                    }
//                }

                Log.d("SearchResult", "We're getting something");
            }
        });
        spotify_result = getItem(position);
        viewHolder.title.setText(spotify_result.getTrack());
        viewHolder.artist.setText(spotify_result.getArtist());
        Log.d("SearchAdapter", "position: "+position);
        if (album_art_map.containsKey(position)){
            viewHolder.album_art.setImageBitmap(album_art_map.get(position));
        }else{
            new DownloadAlbumArt(position, viewHolder.album_art, album_art_map).execute(spotify_result.getImage_URL());
        }
        return row;
    }


    private class DownloadAlbumArt extends AsyncTask<String, Void, Bitmap>{
        ImageView bmImage;
        HashMap map;
        Integer position;

        public DownloadAlbumArt(Integer position, ImageView bmImage, HashMap map) {
            this.bmImage = bmImage;
            this.map = map;
            this.position = position;
            bmImage.setTag(position);
            bmImage.setImageBitmap(null);
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                this.map.put(position, mIcon11);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null && (bmImage.getTag()).equals(this.position)){
                bmImage.setImageBitmap(result);
            }
//                bmImage.setImageBitmap(result);
        }
    }



}

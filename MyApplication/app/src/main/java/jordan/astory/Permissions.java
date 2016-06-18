package jordan.astory;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by Jordan on 4/22/2016.
 */
public class Permissions {
    Activity activity;
    public Permissions(Activity a){
        activity = a;
    }
    public Boolean canMakeSmores(){
        return(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public Boolean hasPermission(String permission){
        if(canMakeSmores()){
            return (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkForPermissions(String[] permissions){
        if(canMakeSmores()) {
            for(String perm : permissions){
                if(!hasPermission(perm)){
                    activity.requestPermissions(permissions, Constants.PERMISSIONS_REQUEST_CODE);
                }
            }

        }
    }






}

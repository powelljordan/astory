package jordan.astory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jordan on 1/8/2016.
 */
public class DBUser {
    private String username;
    private String uid;
    private String email;
    private HashMap<String, String> stories;

    public DBUser(){}

    public String getUsername(){return username;}

    public String getUid(){return uid;}

    public String getEmail(){return email;}
}

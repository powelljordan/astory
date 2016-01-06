package jordan.astory;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.firebase.ui.auth.core.FirebaseLoginError;
import com.firebase.ui.auth.core.SocialProvider;

/**
 * Created by Jordan on 1/6/2016.
 */
public class LoginActivity extends FirebaseLoginBaseActivity {
    public Firebase rootRef;
    public Button mLoginButton;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        rootRef = new Firebase("https://astory.firebaseio.com/");
//        m
//        mLoginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showFirebaseLoginPrompt();
//            }
//        });
    }
    @Override
    public Firebase getFirebaseRef() {
        // TODO: Return your Firebase ref
        return rootRef;
    }

    @Override
    public void onFirebaseLoggedIn(AuthData authData) {
        // TODO: Handle successful login
    }

    @Override
    public void onFirebaseLoggedOut() {
        // TODO: Handle logout
    }

    @Override
    public void onFirebaseLoginProviderError(FirebaseLoginError firebaseError) {
        // TODO: Handle an error from the authentication provider
    }

    @Override
    public void onFirebaseLoginUserError(FirebaseLoginError firebaseError) {
        // TODO: Handle an error from the user
    }

    @Override
    protected void onStart(){
        super.onStart();
        setEnabledAuthProvider(SocialProvider.password);
        setEnabledAuthProvider(SocialProvider.google);
    }


}

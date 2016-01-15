package jordan.astory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.firebase.ui.auth.core.FirebaseLoginError;
import com.firebase.ui.auth.core.SocialProvider;
import com.google.android.gms.common.SignInButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jordan on 1/6/2016.
 */
public class LoginActivity extends FirebaseLoginBaseActivity implements RegisterFragment.RegisterFragmentListener {
    private Firebase rootRef;
    private Button mLoginButton;
    private Button mRegisterButton;
    private String TAG = "LoginActivity";
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
//        Firebase.setAndroidContext(this); //Not sure if necessary
        rootRef = new Firebase("https://astory.firebaseio.com/");
        mLoginButton = (Button) findViewById(R.id.sign_in);
        mRegisterButton = (Button) findViewById(R.id.register);



//
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFirebaseLoginPrompt();
//                authenticateUser();

            }
        });

        mRegisterButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                showRegisterDialog(view);
            }
        });



    }
    @Override
    public Firebase getFirebaseRef() {
        // TODO: Return your Firebase ref
        return rootRef;
    }

    @Override
    public void onFirebaseLoggedIn(AuthData authData) {
        // TODO: Handle successful login
//        Toast.makeText(getApplicationContext(), authData.getUid(), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK, new Intent().putExtra(Constants.CURRENT_USER_ID, authData.getUid()));
        Log.d(TAG, "Logged in");
        finish();
    }

    @Override
    public void onFirebaseLoggedOut() {
        // TODO: Handle logout
    }

    @Override
    public void onFirebaseLoginProviderError(FirebaseLoginError firebaseError) {
        // TODO: Handle an error from the authentication provider
        Toast.makeText(this, "Authentication Provider Error", Toast.LENGTH_SHORT).show();
        Log.d(TAG, firebaseError.toString());
    }

    @Override
    public void onFirebaseLoginUserError(FirebaseLoginError firebaseError) {
        // TODO: Handle an error from the user
        Log.d(TAG, firebaseError.toString());
        Toast.makeText(this, "Login Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart(){
        super.onStart();
        setEnabledAuthProvider(SocialProvider.password);
//        setEnabledAuthProvider(SocialProvider.google);
    }

    private void authenticateUser(){
        rootRef.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authdata) {
                if (authdata != null) {
                    //user logged in
                    finish();
                } else {
                    //user not logged in
                }
            }
        });

        Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler(){
            @Override
            public void onAuthenticated(AuthData authdata){
                Toast.makeText(getApplicationContext(), "Successfully Logged in", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError){
                Toast.makeText(getApplicationContext(), "Failed to Login", Toast.LENGTH_SHORT).show();
                Log.d(TAG, firebaseError.toString());
            }
        };


//        rootRef.authWithPassword("test", "test", authResultHandler);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog){

    }

    @Override
    public void onFinishedInputDialog(String username, String email, String password) {

        final HashMap<String, String> userObj = new HashMap<String, String>();
        userObj.put("username", username);
        userObj.put("email", email);

        rootRef.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>(){
            @Override
            public void onSuccess(Map<String, Object> result) {
                Firebase user = rootRef.child("users").child(result.get("uid").toString());
                HashMap userCred = new HashMap<>();
                userCred.put("uid", result.get("uid"));
                userCred.put("username", userObj.get("username"));
                userCred.put("email", userObj.get("email"));
                userCred.put("stories", new ArrayList<String>());
                user.setValue(userCred);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(),
                        "There was an error creating your account, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showRegisterDialog(View v){
        RegisterFragment dialogFragment = new RegisterFragment();
        dialogFragment.setListener(LoginActivity.this);
        dialogFragment.show(LoginActivity.this.getSupportFragmentManager(), "RegisterFragment");
    }

}

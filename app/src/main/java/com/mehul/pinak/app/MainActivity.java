package com.mehul.pinak.app;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,View.OnClickListener {

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "login";

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private SignInButton mSignInButton;
    private TextView tvName,tvEmail;
    private Button btnLogOut;
    private ImageView imgUserAvatar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        googleSignIn();
        loginStatus();

        mSignInButton.setOnClickListener(this);

    }

    public void init(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mSignInButton = findViewById(R.id.sign_in_button);
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        btnLogOut = findViewById(R.id.btn_log_out);
        imgUserAvatar = findViewById(R.id.img_user_avtar);
    }

    public void loginStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            toolbar.setTitle("User Profile");
            mSignInButton.setVisibility(View.GONE);
            tvName.setVisibility(View.VISIBLE);
            tvEmail.setVisibility(View.VISIBLE);
            btnLogOut.setVisibility(View.VISIBLE);
            imgUserAvatar.setVisibility(View.VISIBLE);
            tvName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
            glide(user.getPhotoUrl(),imgUserAvatar);
        }


    }

    public void glide(Uri uri,ImageView imageView){
        Glide.with(this)
                .load(uri)
                .into(imageView);
    }


    private void googleSignIn(){
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(this, "Google signIn failed , please try again", Toast.LENGTH_SHORT).show();
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                toolbar.setTitle("User Profile");
                                mSignInButton.setVisibility(View.GONE);
                                tvName.setVisibility(View.VISIBLE);
                                tvEmail.setVisibility(View.VISIBLE);
                                btnLogOut.setVisibility(View.VISIBLE);
                                imgUserAvatar.setVisibility(View.VISIBLE);
                                tvName.setText(user.getDisplayName());
                                tvEmail.setText(user.getEmail());
                                glide(user.getPhotoUrl(),imgUserAvatar);

                            }
                            else{
                                Toast.makeText(MainActivity.this, "User is Null", Toast.LENGTH_SHORT).show();
                            }
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void signOut(View view){
        toolbar.setTitle("User Login");
        mSignInButton.setVisibility(View.VISIBLE);
        tvName.setVisibility(View.GONE);
        tvEmail.setVisibility(View.GONE);
        btnLogOut.setVisibility(View.GONE);
        imgUserAvatar.setVisibility(View.GONE);

        FirebaseAuth.getInstance().signOut();

        //mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //updateUI(null);
                        Toast.makeText(MainActivity.this, "User Succesfully LogOut ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google connection is failed ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        signIn();
    }
}

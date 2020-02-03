package com.universl.selfieguru;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.universl.selfieguru.sub_activity.SelfieUserProfileActivity;

import java.util.Objects;

public class GoogleSignInActivity extends AppCompatActivity {

    FirebaseAuth.AuthStateListener mAuthListener;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth mAuth;
    private String TAG = "abc";
    private final int RC_SIGN_IN = 999;
    SignInButton signInButton;
    ImageView close;
    int sum = 0;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);

        System.out.println(sum);
        Toolbar toolbar = findViewById(R.id.search_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Google SignIn</font>"));
        signInButton = findViewById(R.id.sign_in_button);

        close = findViewById(R.id.close);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    signInButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signIn();
                        }
                    });
                }
            }
        };
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getStringExtra("google").equalsIgnoreCase("main") || getIntent().getStringExtra("google").equalsIgnoreCase("profile")){
                    Intent intent = new Intent(GoogleSignInActivity.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }else if (getIntent().getStringExtra("google").equalsIgnoreCase("editor")){
                    Intent intent = new Intent(GoogleSignInActivity.this,EditorChoiceActivity.class);
                    startActivity(intent);
                    finish();
                }else if (getIntent().getStringExtra("google").equalsIgnoreCase("competition")){
                    Intent intent = new Intent(GoogleSignInActivity.this,CompetitionActivity.class);
                    intent.putExtra("description",getIntent().getStringExtra("description"));
                    startActivity(intent);
                    finish();
                }else if (getIntent().getStringExtra("google").equalsIgnoreCase("popular")){
                    Intent intent = new Intent(GoogleSignInActivity.this,PopularActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(GoogleSignInActivity.this, "Something went wrong !", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }
    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(GoogleSignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //user_email_address = account.getEmail();
                            // Sign in success, update UI with the signed-in user's information
                            if (getIntent().getStringExtra("google").equalsIgnoreCase("main") || getIntent().getStringExtra("google").equalsIgnoreCase("profile")){
                                Intent intent = new Intent(GoogleSignInActivity.this,HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }else if (getIntent().getStringExtra("google").equalsIgnoreCase("editor")){
                                Intent intent = new Intent(GoogleSignInActivity.this,EditorChoiceActivity.class);
                                startActivity(intent);
                                finish();
                            }else if (getIntent().getStringExtra("google").equalsIgnoreCase("competition")){
                                Intent intent = new Intent(GoogleSignInActivity.this,CompetitionActivity.class);
                                intent.putExtra("description",getIntent().getStringExtra("description"));
                                startActivity(intent);
                                finish();
                            }else if (getIntent().getStringExtra("google").equalsIgnoreCase("popular")){
                                Intent intent = new Intent(GoogleSignInActivity.this,PopularActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            Log.d(TAG, "signInWithCredential:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(GoogleSignInActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } else {
                Toast.makeText(GoogleSignInActivity.this, "Auth went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
}

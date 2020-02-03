package com.universl.selfieguru.sub_activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.universl.selfieguru.GoogleSignInActivity;
import com.universl.selfieguru.HomeActivity;
import com.universl.selfieguru.MainActivity;
import com.universl.selfieguru.R;
import com.universl.selfieguru.adapter.ProfileAdapter;
import com.universl.selfieguru.response.SelfieResponse;
import com.universl.selfieguru.utils.AppController;
import com.universl.selfieguru.utils.Constant;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class SelfieUserProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    List<SelfieResponse> selfieResponseList, selfieResponseList_photo,getSelfieResponseList;
    ArrayList<String> image_path;
    private Context context;
    private Activity activity;
    private RelativeLayout relativeLayout;
    FirebaseAuth.AuthStateListener mAuthListener;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth mAuth;
    private String TAG = "abc";
    private PopupWindow popupWindow;
    private final int RC_SIGN_IN = 100;
    private String user_name;
    ProfileAdapter profileAdapter;
    ListView listView;
    SearchView searchView;
    private ProgressDialog progress;
    private DatabaseReference databaseQuotes;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_user_profile);

        lordProduct();
        lordReviewProduct();

        Toolbar toolbar = findViewById(R.id.search_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>My Selfies</font>"));

        databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_selfie");
        selfieResponseList_photo = new ArrayList<>();
        image_path = new ArrayList<>();

        listView = findViewById(R.id.quotes_list);
        //searchView = findViewById(R.id.search_title);

        relativeLayout = findViewById(R.id.activity_main);
        context = getApplicationContext();
        activity = SelfieUserProfileActivity.this;

        FloatingTextButton user = findViewById(R.id.user_button);
        FloatingTextButton review = findViewById(R.id.review_button);
        FloatingTextButton upload = findViewById(R.id.upload_button);
        FloatingTextButton logout = findViewById(R.id.logout_button);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelfieUserProfileActivity.this,SelfieFilterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mGoogleApiClient.disconnect();
                finish();
                FirebaseAuth.getInstance().signOut();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    //open_Popup_window();
                    Intent intent = new Intent(SelfieUserProfileActivity.this, GoogleSignInActivity.class);
                    intent.putExtra("google","profile");
                    startActivity(intent);
                    finish();
                }
            }
        };
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(SelfieUserProfileActivity.this, "Something went wrong !", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileAdapter = new ProfileAdapter(SelfieUserProfileActivity.this, selfieResponseList, selfieResponseList_photo, image_path);
                listView.setAdapter(profileAdapter);
            }
        });

        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileAdapter = new ProfileAdapter(SelfieUserProfileActivity.this, getSelfieResponseList);
                listView.setAdapter(profileAdapter);
            }
        });
        String profile = getIntent().getStringExtra("profile");
        if (profile.equalsIgnoreCase("upload")){
            user.setVisibility(View.VISIBLE);
            review.setVisibility(View.VISIBLE);
            profileAdapter = new ProfileAdapter(SelfieUserProfileActivity.this, getSelfieResponseList);
            listView.setAdapter(profileAdapter);
        }else {
            user.setVisibility(View.VISIBLE);
            review.setVisibility(View.VISIBLE);
            profileAdapter = new ProfileAdapter(SelfieUserProfileActivity.this, selfieResponseList, selfieResponseList_photo, image_path);
            listView.setAdapter(profileAdapter);
        }
        initAds();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        if ((progress != null) && progress.isShowing())
            progress.dismiss();
        progress = null;
        databaseQuotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                selfieResponseList_photo.clear();
                image_path.clear();
                //quotesResponseList.clear();
                for (DataSnapshot quotesSnapshot : dataSnapshot.getChildren()) {
                    SelfieResponse quotes = quotesSnapshot.getValue(SelfieResponse.class);

                    selfieResponseList_photo.add(quotes);
                }
                for (int i = 0; i < selfieResponseList_photo.size(); i++) {
                    image_path.add(selfieResponseList_photo.get(i).photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }

    private void lordProduct() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null) {
            user_name = account.getGivenName();
        }
        progress = new ProgressDialog(SelfieUserProfileActivity.this);
        selfieResponseList = new ArrayList<>();

        JsonArrayRequest request = new JsonArrayRequest(Constant.GET_SELFIE_URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            Toast.makeText(getApplicationContext(), "Couldn't fetch the contacts! Pleas try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<SelfieResponse> items = new Gson().fromJson(response.toString(), new TypeToken<List<SelfieResponse>>() {
                        }.getType());

                        // adding contacts to contacts list
                        selfieResponseList.clear();
                        for (int i = 0; i < items.size();i++){
                            if (items.get(i).status.equalsIgnoreCase("true")
                                    && items.get(i).category.equalsIgnoreCase("User Post Selfie")
                                    && items.get(i).user_name.equalsIgnoreCase(user_name)){
                                selfieResponseList.add(items.get(i));
                            }
                        }
                        selfieResponseList.size();

                        // refreshing recycler view
                        profileAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        /*AppController.getInstance().getRequestQueue().getCache().remove(Constant.GET_SELFIE_URL);*/
        AppController.getInstance().getRequestQueue().getCache().invalidate(Constant.GET_SELFIE_URL,true);
        AppController.getInstance().addToRequestQueue(request);
    }
    private void lordReviewProduct() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null) {
            user_name = account.getGivenName();
        }
        getSelfieResponseList = new ArrayList<>();
        String tag_json_arry = "json_array_req";
        JsonArrayRequest request = new JsonArrayRequest(Constant.GET_SELFIE_URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response == null) {
                            Toast.makeText(getApplicationContext(), "Couldn't fetch the contacts! Pleas try again.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        List<SelfieResponse> items = new Gson().fromJson(response.toString(), new TypeToken<List<SelfieResponse>>() {
                        }.getType());

                        // adding contacts to contacts list
                        //getQuotesResponseList.clear();
                        items.size();
                        for (int i = 0; i < items.size();i++){
                            if (items.get(i).status.equalsIgnoreCase("False")
                                    && items.get(i).category.equalsIgnoreCase("User Post Selfie")
                                    && items.get(i).user_name.equalsIgnoreCase(user_name)){
                                getSelfieResponseList.add(items.get(i));
                            }
                        }
                        getSelfieResponseList.size();
                        // refreshing recycler view
                        profileAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        //Volley.newRequestQueue(this).add(request);
        /*AppController.getInstance().getRequestQueue().getCache().remove(Constant.GET_SELFIE_URL);*/
        AppController.getInstance().getRequestQueue().getCache().invalidate(Constant.GET_SELFIE_URL,true);
        AppController.getInstance().addToRequestQueue(request, tag_json_arry);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                getSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                getSearch(query);
                return false;
            }
        });
        return true;
    }

    private void getSearch(String query){
        List<SelfieResponse> filtered_output = new ArrayList<>();

        if (searchView != null){
            for (SelfieResponse item : selfieResponseList){
                if (item.title.toLowerCase().startsWith(query.toLowerCase()))
                    filtered_output.add(item);
            }
        }else
            filtered_output = selfieResponseList;

        profileAdapter = new ProfileAdapter(SelfieUserProfileActivity.this,filtered_output,selfieResponseList_photo,image_path);
        listView.setAdapter(profileAdapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.power) {
            logout();
            return true;
        }
        if (id == R.id.profile){
            Intent intent = new Intent(SelfieUserProfileActivity.this,SelfieUserProfileActivity.class);
            intent.putExtra("profile","uploaded");
            startActivity(intent);
            finish();
        }
        if (id == android.R.id.home){
            Intent intent = new Intent(SelfieUserProfileActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        }
        if (id == R.id.search){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void logout() {
        System.exit(0);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SelfieUserProfileActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

    //Ads
    private void initAds() {
        AdView adView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}

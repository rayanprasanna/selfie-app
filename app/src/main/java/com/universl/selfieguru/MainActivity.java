package com.universl.selfieguru;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.android.volley.request.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.universl.selfieguru.adapter.HomeAdapter;
import com.universl.selfieguru.response.SelfieResponse;
import com.universl.selfieguru.sub_activity.SelfieDetailsActivity;
import com.universl.selfieguru.sub_activity.SelfieFilterActivity;
import com.universl.selfieguru.sub_activity.SelfieUserProfileActivity;
import com.universl.selfieguru.utils.AppController;
import com.universl.selfieguru.utils.Constant;
import com.universl.selfieguru.utils.GlideApp;
import com.universl.smsnotifier.ApiSMSSender;
import com.universl.smsnotifier.AppSMSSender;
import com.universl.smsnotifier.Constants;
import com.universl.smsnotifier.MessageOperator;
import com.universl.smsnotifier.MsgOperatorFactory;
import com.universl.smsnotifier.Param;
import com.universl.smsnotifier.SMSNotifireUtils;
import com.universl.smsnotifier.SMSSender;
import com.universl.smsnotifier.USSDDialer;

import org.json.JSONArray;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG";
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    ArrayList<String> image_path;
    ListView listView;
    SearchView searchView;
    //MaterialSearchView materialSearchView;
    HomeAdapter homeAdapter;
    List<SelfieResponse> selfieResponseList,selfieResponseList_favorite;
    //private DatabaseReference databaseQuotes;
    private SMSSender smsSender;
    private Context context;
    private Activity activity;
    private RelativeLayout relativeLayout;
    FirebaseAuth.AuthStateListener mAuthListener;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth mAuth;
    private PopupWindow popupWindow;
    private final int RC_SIGN_IN = 995;
    AlertDialog.Builder alert;
    MainAdapter mainAdapter;
    String androidId;

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }

    @SuppressLint("HardwareIds")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.search_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Home</font>"));
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);

        //databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_selfie");

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        listView = findViewById(R.id.selfie_list);
        //searchView = findViewById(R.id.search_title);
        //materialSearchView = findViewById(R.id.search_title);
        relativeLayout = findViewById(R.id.coordinate_layout);
        context = getApplicationContext();
        activity = MainActivity.this;
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        FloatingActionButton share = findViewById(R.id.share);

        selfieResponseList_favorite = new ArrayList<>();
        image_path = new ArrayList<>();
        selfieResponseList = new ArrayList<>();

        androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        System.err.println("Android Id : " + androidId);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SelfieFilterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                // Add data to the intent, the receiving app will decide
                // what to do with it.
                share.putExtra(Intent.EXTRA_SUBJECT, "# Selfie Guru");
                share.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.universl.selfieguru");

                startActivity(Intent.createChooser(share, "Share link!"));
            }
        });

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.home:
                        Intent home_intent = new Intent(MainActivity.this,HomeActivity.class);
                        startActivity(home_intent);
                        finish();
                        return true;
                    case R.id.editor:
                        Intent romantic_intent = new Intent(MainActivity.this,EditorChoiceActivity.class);
                        startActivity(romantic_intent);
                        finish();
                        return true;
                    case R.id.popular:
                        Intent success_intent = new Intent(MainActivity.this,PopularActivity.class);
                        startActivity(success_intent);
                        finish();
                        return true;
                    case R.id.fans:
                        Intent other_intent = new Intent(MainActivity.this,FansActivity.class);
                        startActivity(other_intent);
                        finish();
                        return true;
                    case R.id.add:
                        Intent intent = new Intent(MainActivity.this,SelfieFilterActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    //homeAdapter = new HomeAdapter(MainActivity.this,selfieResponseList,selfieResponseList_photo,image_path);
                    mainAdapter = new MainAdapter(MainActivity.this,selfieResponseList,false,selfieResponseList_favorite);
                    listView.setAdapter(mainAdapter);
                    //open_Popup_window();
                    Intent intent = new Intent(MainActivity.this,GoogleSignInActivity.class);
                    intent.putExtra("google","main");
                    startActivity(intent);
                    finish();
                }else {
                    //homeAdapter = new HomeAdapter(MainActivity.this,selfieResponseList,selfieResponseList_photo,image_path);
                    mainAdapter = new MainAdapter(MainActivity.this,selfieResponseList,true,selfieResponseList_favorite);
                    listView.setAdapter(mainAdapter);
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
                        Toast.makeText(MainActivity.this, "Something went wrong !", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (!isConnect()){
            alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("# Selfie Guru");
            alert.setIcon(R.mipmap.ic_launcher_foreground);
            alert.setMessage("You do not have an Internet connection");
            alert.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert.create().show();
        }else {
            lordProduct();
            lordFavoriteProduct();
            initAds();
            smsNofify();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*databaseQuotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                selfieResponseList_photo.clear();
                image_path.clear();
                //quotesResponseList.clear();
                for (DataSnapshot quotesSnapshot : dataSnapshot.getChildren()){
                    SelfieResponse quotes = quotesSnapshot.getValue(SelfieResponse.class);

                    selfieResponseList_photo.add(quotes);
                }
                for (int i = 0; i < selfieResponseList_photo.size(); i++){
                    image_path.add(selfieResponseList_photo.get(i).photo);
                }
                image_path.size();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();
        assert searchManager != null;
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.power) {
            logout();
            return true;
        }
        if (id == R.id.profile){
            Intent intent = new Intent(MainActivity.this,SelfieUserProfileActivity.class);
            intent.putExtra("profile","uploaded");
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

    private void getSearch(String query){
        List<SelfieResponse> filtered_output = new ArrayList<>();

        if (searchView != null){
            for (SelfieResponse item : selfieResponseList){
                if (item.title.toLowerCase().startsWith(query.toLowerCase()))
                    filtered_output.add(item);
            }
        }else
            filtered_output = selfieResponseList;

        mAuth = FirebaseAuth.getInstance();
        final List<SelfieResponse> finalFiltered_output = filtered_output;
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    //homeAdapter = new HomeAdapter(MainActivity.this,filtered_output,selfieResponseList_photo,image_path);
                    mainAdapter = new MainAdapter(MainActivity.this, finalFiltered_output,false,selfieResponseList_favorite);
                    listView.setAdapter(mainAdapter);
                }else {
                    //homeAdapter = new HomeAdapter(MainActivity.this,filtered_output,selfieResponseList_photo,image_path);
                    mainAdapter = new MainAdapter(MainActivity.this, finalFiltered_output,true,selfieResponseList_favorite);
                    listView.setAdapter(mainAdapter);
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
                        Toast.makeText(MainActivity.this, "Something went wrong !", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void lordProduct(){
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
                            if (items.get(i).status.equalsIgnoreCase("true") && items.get(i).category.equalsIgnoreCase("User Post Selfie")){
                                selfieResponseList.add(items.get(i));
                            }
                        }
                        mainAdapter = new MainAdapter();
                        // refreshing recycler view
                        mainAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                System.err.println("Error: " + error.getMessage());
            }
        });
        /*AppController.getInstance().getRequestQueue().getCache().remove(Constant.GET_SELFIE_URL);*/
        AppController.getInstance().getRequestQueue().getCache().invalidate(Constant.GET_SELFIE_URL,true);
        AppController.getInstance().addToRequestQueue(request);

        deleteCache(MainActivity.this);
    }
    private void lordFavoriteProduct(){
        JsonArrayRequest postRequest = new JsonArrayRequest(Constant.GET_FAVORITE_URL,
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
                        selfieResponseList_favorite.clear();
                        selfieResponseList_favorite.addAll(items);
                        selfieResponseList_favorite.size();
                        // refreshing recycler view
                        //mainAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                System.err.println("Error: " + error.getMessage());
            }
        })/*{
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("user_id", user_id);
                return params;
            }
        }*/;
        //queue.add(postRequest);
        /*AppController.getInstance().getRequestQueue().getCache().remove(Constant.UPDATE_SELFIE_URL);*/
        AppController.getInstance().getRequestQueue().getCache().invalidate(Constant.GET_FAVORITE_URL,true);
        AppController.getInstance().addToRequestQueue(postRequest);
        int x=2;// retry count
        postRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                x, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        deleteCache(MainActivity.this);
    }
    private static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void smsNofify() {

        List<MessageOperator> ussdOperators = new ArrayList<>();
        MessageOperator ideaMartOperator = MsgOperatorFactory.createMessageOperator("", Constants.SP_DIALOG1, Constants.SP_DIALOG2, Constants.SP_DIALOG3, Constants.SP_AIRTEL, Constants.SP_HUTCH,Constants.SP_ETISALAT);
        ideaMartOperator.setSmsMsg("#775*743#");
        ideaMartOperator.setCharge("5 LKR + Tax P/D");
        ideaMartOperator.setAlertMsg("Obe pemwatha/ pemwathiya, pawule aya, yahaluwan sitina thana sms ekak magin dana ganeemata kamathida? 5 LKR+Tax P/D 5LKR+ Tax P/Search (soyannage kamaththa matha pamanak laba gatha haka. dedenama liyapadinchi wiya yuthuya).");
        ussdOperators.add(ideaMartOperator);

        List<MessageOperator> messageOperators = new ArrayList<>();
        MessageOperator mobitelOperator = MsgOperatorFactory.createMessageOperator(Constants.NUM_MOBITEL, Constants.SP_MOBITEL);
        mobitelOperator.setSmsMsg("REG SGURU");
        mobitelOperator.setCharge("1.25 LKR + Tax P/D");
        ideaMartOperator.setAlertMsg(getResources().getString(R.string.sms_dis_msg));
        messageOperators.add(mobitelOperator);

        Param param = new Param(getResources().getString(R.string.yes), getResources().getString(R.string.no));
        String serviceProvider = SMSNotifireUtils.getServiceProvider(this);
        if(Constants.SP_DIALOG1.equalsIgnoreCase(serviceProvider)
                || Constants.SP_DIALOG2.equalsIgnoreCase(serviceProvider)
                || Constants.SP_DIALOG3.equalsIgnoreCase(serviceProvider)
                || Constants.SP_HUTCH.equalsIgnoreCase(serviceProvider)){
            smsSender = new USSDDialer(this, ussdOperators, param);
        } else if (Constants.SP_MOBITEL.equalsIgnoreCase(serviceProvider)) {
            smsSender = new AppSMSSender(this, messageOperators, param);
        }
        smsSender = new AppSMSSender(this, messageOperators, param);

        if(smsSender!=null) smsSender.smsNotify(null, getResources().getString(R.string.app_name));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {

            case SMSSender.PERMISSIONS_ACTION_CALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(smsSender !=null ) smsSender.smsNotify(null,getResources().getString(R.string.app_name));
                }
                return;
            }
        }
    }


    //Ads
    private void initAds() {
        MobileAds.initialize(this, getResources().getString(R.string.ads_app_id));
        AdView adView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
    private boolean isConnect(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
    class MainAdapter extends BaseAdapter{

        private Context context;
        private LayoutInflater layoutInflater;
        private List<SelfieResponse> selfieResponses,selfieResponsesFavorite;
        private String androidId;
        private Boolean isSignInGoogle;
        List<String> photo_list = new ArrayList<>();

        public MainAdapter() {

        }

        @SuppressLint("HardwareIds")
        MainAdapter(Context context, List<SelfieResponse> selfieResponses,Boolean isSignInGoogle,List<SelfieResponse> selfieResponsesFavorite) {
            this.context = context;
            this.selfieResponses = selfieResponses;
            this.selfieResponsesFavorite = selfieResponsesFavorite;
            layoutInflater = LayoutInflater.from(context);
            androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            this.isSignInGoogle = isSignInGoogle;
        }

        class ViewHolder{
            TextView title,publish_date,count_of_favorite,review,user_name;
            ImageView image,favorite,delete;
            Boolean isClickFavoriteButton;
        }

        @Override
        public int getCount() {
            return selfieResponses.size();
        }

        @Override
        public Object getItem(int position) {
            return selfieResponses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint({"SetTextI18n", "InflateParams"})
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.selfie_list, null);
                // Locate the TextViews in listView_item.xml

                viewHolder.review = convertView.findViewById(R.id.review);
                viewHolder.delete = convertView.findViewById(R.id.delete);
                viewHolder.title = convertView.findViewById(R.id.title);
                viewHolder.user_name = convertView.findViewById(R.id.user_name);
                viewHolder.publish_date = convertView.findViewById(R.id.date);
                viewHolder.image = convertView.findViewById(R.id.quotes_image);
                viewHolder.count_of_favorite = convertView.findViewById(R.id.count_of_favorite);
                viewHolder.favorite = convertView.findViewById(R.id.favorite);
                viewHolder.review.setVisibility(View.GONE);
                viewHolder.delete.setVisibility(View.GONE);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.count_of_favorite.setText(String.valueOf(selfieResponses.get(position).like_count));
            viewHolder.user_name.setText(selfieResponses.get(position).user_name);

            //viewHolder.isClickFavoriteButton = false;

            for (int i = 0; i < selfieResponseList_favorite.size(); i++){
                if (androidId.equalsIgnoreCase(selfieResponseList_favorite.get(i).user_id)){
                    photo_list.add(selfieResponsesFavorite.get(i).photo);
                }
            }
            if (!selfieResponseList_favorite.isEmpty()){
                for (int j = 0; j < selfieResponses.size(); j++){
                    for (int k = 0; k < selfieResponseList_favorite.size(); k++ ){
                        if (selfieResponses.get(j).photo.equals(selfieResponseList_favorite.get(k).photo) && androidId.equalsIgnoreCase(selfieResponseList_favorite.get(k).user_id)){
                            viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_y);
                            //System.out.println(favoriteResponses.get(k).image_path + " <---> "+ jobResponses.get(j).image_path + " <---> "+ image_paths.size());
                        }
                    }
                }
            }
            if (Collections.frequency(photo_list,selfieResponses.get(position).photo) == 0){
                viewHolder.isClickFavoriteButton = false;
                viewHolder.favorite.setEnabled(true);
                viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_n);
            }if (Collections.frequency(photo_list,selfieResponses.get(position).photo) > 0){
                viewHolder.isClickFavoriteButton = true;
                viewHolder.favorite.setEnabled(false);
                viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_y);
            }
            // Set the results into TextViews
            viewHolder.title.setText("# " + selfieResponses.get(position).title);
            GlideApp.with(context.getApplicationContext()).load(selfieResponses.get(position).photo).centerCrop().into(viewHolder.image);
            viewHolder.publish_date.setText(selfieResponses.get(position).date);
            viewHolder.favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isSignInGoogle){
                        if (!viewHolder.isClickFavoriteButton){
                            viewHolder.isClickFavoriteButton = true;
                            viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_y);
                            viewHolder.count_of_favorite.setText(String.valueOf(Integer.parseInt(selfieResponses.get(position).like_count) + 1));
                            update_like_count(selfieResponses.get(position).photo,String.valueOf(Integer.parseInt(selfieResponses.get(position).like_count) + 1));
                            insert_favorite(selfieResponses.get(position).photo,androidId);
                        }else {
                            viewHolder.isClickFavoriteButton = false;
                            viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_n);
                        }
                    }else {
                        alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("# Selfie Guru");
                        alert.setIcon(R.mipmap.ic_launcher_foreground);
                        alert.setMessage("Please Sign In Your Google Account !");
                        alert.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //open_Popup_window();
                                Intent intent = new Intent(MainActivity.this,GoogleSignInActivity.class);
                                intent.putExtra("google","main");
                                startActivity(intent);
                                finish();
                            }
                        });
                        alert.create().show();
                    }
                }
            });
            viewHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SelfieDetailsActivity.class);
                    intent.putExtra("activity","home");
                    intent.putExtra("count",viewHolder.count_of_favorite.getText());
                    intent.putExtra("quotes_image",selfieResponses.get(position).photo);
                    intent.putExtra("quotes_user_name",selfieResponses.get(position).user_name);
                    ((Activity)context).startActivity(intent);
                    ((Activity)context).finish();
                }
            });
            return convertView;
        }
        private void update_like_count(final String image_path,final String like_count){
            StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.UPDATE_SELFIE_URL,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("Error.Response", error.getMessage());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();
                    params.put("photo", image_path);
                    params.put("like_count",like_count);
                    return params;
                }
            };
            //queue.add(postRequest);
            /*AppController.getInstance().getRequestQueue().getCache().remove(Constant.UPDATE_SELFIE_URL);*/
            AppController.getInstance().getRequestQueue().getCache().invalidate(Constant.UPDATE_SELFIE_URL,true);
            AppController.getInstance().addToRequestQueue(postRequest);
            deleteCache(context);
        }
        private void insert_favorite(final String image_path, final String user_id){
            StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.INSERT_FAVORITE_URL,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("Error.Response", error.getMessage());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();
                    params.put("photo", image_path);
                    params.put("user_id",user_id);
                    return params;
                }
            };
            //queue.add(postRequest);
            /*AppController.getInstance().getRequestQueue().getCache().remove(Constant.UPDATE_SELFIE_URL);*/
            AppController.getInstance().getRequestQueue().getCache().invalidate(Constant.INSERT_FAVORITE_URL,true);
            AppController.getInstance().addToRequestQueue(postRequest);
            deleteCache(context);
        }
    }
}

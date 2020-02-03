package com.universl.selfieguru;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.universl.selfieguru.adapter.EditorChoiceAdapter;
import com.universl.selfieguru.adapter.FansAdapter;
import com.universl.selfieguru.response.SelfieResponse;
import com.universl.selfieguru.sub_activity.SelfieFilterActivity;
import com.universl.selfieguru.sub_activity.SelfieUserProfileActivity;
import com.universl.selfieguru.utils.AppController;
import com.universl.selfieguru.utils.Constant;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FansActivity extends AppCompatActivity {

    private static final String TAG = "TAG";
    private ProgressDialog progress;
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    ArrayList<String> image_path;
    ListView listView;
    SearchView searchView;
    //MaterialSearchView materialSearchView;
    FansAdapter fansAdapter;
    CompetitionAdapter competitionAdapter;
    List<SelfieResponse> selfieResponseList,selfieResponseList_photo;
    private DatabaseReference databaseQuotes;

    @Override
    protected void onPause() {
        super.onPause();
        if ((progress != null) && progress.isShowing())
            progress.dismiss();
        progress = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fans);

        Toolbar toolbar = findViewById(R.id.search_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#ffffff'>Competitions</font>"));
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);

        databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_selfie");

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        listView = findViewById(R.id.selfie_list);
        //searchView = findViewById(R.id.search_title);
        //materialSearchView = findViewById(R.id.search_title);
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        FloatingActionButton share = findViewById(R.id.share);

        selfieResponseList_photo = new ArrayList<>();
        image_path = new ArrayList<>();
        selfieResponseList = new ArrayList<>();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FansActivity.this,SelfieFilterActivity.class);
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
                        Intent home_intent = new Intent(FansActivity.this,MainActivity.class);
                        startActivity(home_intent);
                        finish();
                        return true;
                    case R.id.editor:
                        Intent romantic_intent = new Intent(FansActivity.this,EditorChoiceActivity.class);
                        startActivity(romantic_intent);
                        finish();
                        return true;
                    case R.id.popular:
                        Intent success_intent = new Intent(FansActivity.this,PopularActivity.class);
                        startActivity(success_intent);
                        finish();
                        return true;
                    case R.id.fans:
                        Intent other_intent = new Intent(FansActivity.this,FansActivity.class);
                        startActivity(other_intent);
                        finish();
                        return true;
                    case R.id.add:
                        Intent add_intent = new Intent(FansActivity.this,SelfieFilterActivity.class);
                        startActivity(add_intent);
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });

        //fansAdapter = new FansAdapter(FansActivity.this,selfieResponseList,selfieResponseList_photo,image_path);
        competitionAdapter = new CompetitionAdapter(FansActivity.this,selfieResponseList);
        listView.setAdapter(competitionAdapter);
        lordProduct();
        initAds();
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
            Intent intent = new Intent(FansActivity.this,SelfieUserProfileActivity.class);
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

        //fansAdapter = new FansAdapter(FansActivity.this,filtered_output,selfieResponseList_photo,image_path);
        competitionAdapter = new CompetitionAdapter(FansActivity.this,filtered_output);
        listView.setAdapter(competitionAdapter);
    }
    private void lordProduct(){
        JsonArrayRequest request = new JsonArrayRequest(Constant.GET_COMPETITION_URL,
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

                        // refreshing recycler view
                        competitionAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        /*AppController.getInstance().getRequestQueue().getCache().remove(Constant.GET_SELFIE_URL);*/
        AppController.getInstance().getRequestQueue().getCache().invalidate(Constant.GET_COMPETITION_URL,true);
        AppController.getInstance().addToRequestQueue(request);

        deleteCache(FansActivity.this);
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

    //Ads
    private void initAds() {
        AdView adView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
    class CompetitionAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater layoutInflater;
        private List<SelfieResponse> selfieResponses;

        @SuppressLint("HardwareIds")
        CompetitionAdapter(Context context, List<SelfieResponse> selfieResponses) {
            this.context = context;
            this.selfieResponses = selfieResponses;
            layoutInflater = LayoutInflater.from(context);
        }

        class ViewHolder{
            TextView title;
            LinearLayout competition;
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
                convertView = layoutInflater.inflate(R.layout.competition_list, null);
                // Locate the TextViews in listView_item.xml

                viewHolder.title = convertView.findViewById(R.id.title);
                viewHolder.competition = convertView.findViewById(R.id.competition);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Set the results into TextViews
            viewHolder.title.setText(selfieResponses.get(position).description);
            viewHolder.competition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FansActivity.this,CompetitionActivity.class);
                    intent.putExtra("description",selfieResponses.get(position).description);
                    startActivity(intent);
                    finish();
                }
            });
            return convertView;
        }
    }
}

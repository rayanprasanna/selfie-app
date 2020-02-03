package com.universl.selfieguru.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.universl.selfieguru.R;
import com.universl.selfieguru.response.SelfieResponse;
import com.universl.selfieguru.sub_activity.SelfieDetailsActivity;
import com.universl.selfieguru.sub_activity.SelfieFilterActivity;
import com.universl.selfieguru.sub_activity.SelfieUserProfileActivity;
import com.universl.selfieguru.utils.AppController;
import com.universl.selfieguru.utils.Constant;
import com.universl.selfieguru.utils.GlideApp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<SelfieResponse> selfieResponses;
    private List<SelfieResponse> image_selfieResponses;
    private ArrayList<String> image_pathList;
    private ArrayList<SelfieResponse> selfieResponseArrayList;
    private String androidId;
    private DatabaseReference databaseQuotes;

    public ArrayList<String> getImage_pathList() {
        return image_pathList;
    }

    public ProfileAdapter(Context context, List<SelfieResponse> quotesResponses) {
        this.context = context;
        this.selfieResponses = quotesResponses;
        layoutInflater = LayoutInflater.from(context);
    }

    @SuppressLint("HardwareIds")
    public ProfileAdapter(Context context, List<SelfieResponse> selfieResponses, List<SelfieResponse> image_selfieResponses, ArrayList<String> image_pathList) {
        this.context = context;
        this.selfieResponses = selfieResponses;
        this.image_pathList = image_pathList;
        this.image_selfieResponses = image_selfieResponses;
        this.selfieResponseArrayList = new ArrayList<>();
        selfieResponseArrayList.addAll(selfieResponses);
        layoutInflater = LayoutInflater.from(context);
        androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public class ViewHolder{
        TextView title,publish_date,count_of_favorite,review,like,user_name;
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

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.selfie_list, null);
            // Locate the TextViews in listView_item.xml

            databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_selfie");
            viewHolder.review = convertView.findViewById(R.id.review);
            viewHolder.delete = convertView.findViewById(R.id.delete);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.publish_date = convertView.findViewById(R.id.date);
            viewHolder.image = convertView.findViewById(R.id.quotes_image);
            viewHolder.count_of_favorite = convertView.findViewById(R.id.count_of_favorite);
            viewHolder.favorite = convertView.findViewById(R.id.favorite);
            viewHolder.like = convertView.findViewById(R.id.like);
            viewHolder.review.setVisibility(View.GONE);
            viewHolder.delete.setVisibility(View.GONE);
            viewHolder.user_name = convertView.findViewById(R.id.user_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (selfieResponses.get(position).status.equalsIgnoreCase("False")){
            String test = selfieResponses.get(position).photo;
            System.out.println(test);
            GlideApp
                    .with(context.getApplicationContext())
                    .load(selfieResponses.get(position).photo)
                    .fitCenter()
                    .into(viewHolder.image);
            viewHolder.image.setAlpha(225);
            viewHolder.title.setText(selfieResponses.get(position).title);
            viewHolder.publish_date.setText(selfieResponses.get(position).date);
            viewHolder.review.setVisibility(View.GONE);
            viewHolder.delete.setVisibility(View.VISIBLE);
            viewHolder.like.setVisibility(View.VISIBLE);
            viewHolder.favorite.setVisibility(View.VISIBLE);
            viewHolder.count_of_favorite.setVisibility(View.VISIBLE);

            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete_selfie(selfieResponses.get(position).photo);
                    selfieResponses.remove(position);
                    /*viewHolder.delete.setVisibility(View.GONE);
                    viewHolder.review.setVisibility(View.GONE);
                    viewHolder.image.setVisibility(View.GONE);
                    viewHolder.like.setVisibility(View.GONE);
                    viewHolder.count_of_favorite.setVisibility(View.GONE);
                    viewHolder.favorite.setVisibility(View.GONE);
                    viewHolder.publish_date.setVisibility(View.GONE);
                    viewHolder.title.setVisibility(View.GONE);*/
                    /*viewHolder.quotes_lay.setVisibility(View.GONE);*/
                }
            });
        }else {
            viewHolder.title.setText(selfieResponses.get(position).title);
            viewHolder.publish_date.setText(selfieResponses.get(position).date);
            GlideApp
                    .with(context.getApplicationContext())
                    .load(selfieResponses.get(position).photo)
                    .fitCenter()
                    .into(viewHolder.image);
            viewHolder.review.setVisibility(View.GONE);
            viewHolder.delete.setVisibility(View.GONE);
            viewHolder.like.setVisibility(View.VISIBLE);
            viewHolder.favorite.setVisibility(View.VISIBLE);
            viewHolder.count_of_favorite.setVisibility(View.VISIBLE);
            viewHolder.count_of_favorite.setText(String.valueOf(Collections.frequency(image_pathList,selfieResponses.get(position).photo)));
        }

        //viewHolder.count_of_favorite.setText(String.valueOf(Collections.frequency(getImage_pathList(),selfieResponses.get(position).photo)));

        viewHolder.isClickFavoriteButton = false;
        // Set the results into TextViews
        viewHolder.title.setText("# " + selfieResponses.get(position).title);
        viewHolder.user_name.setText(selfieResponses.get(position).user_name);
        GlideApp.with(context.getApplicationContext()).load(selfieResponses.get(position).photo).centerCrop().into(viewHolder.image);
        viewHolder.publish_date.setText(selfieResponses.get(position).date);
        viewHolder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!viewHolder.isClickFavoriteButton){
                    viewHolder.isClickFavoriteButton = true;
                    viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_y);
                    uploadFavorite(selfieResponses.get(position).photo);
                    viewHolder.count_of_favorite.setText(String.valueOf(Integer.parseInt(viewHolder.count_of_favorite.getText().toString()) + 1));
                    update_like_count(selfieResponses.get(position).photo,String.valueOf(Integer.parseInt(viewHolder.count_of_favorite.getText().toString())));
                }else {
                    viewHolder.isClickFavoriteButton = false;
                    viewHolder.favorite.setBackgroundResource(R.drawable.ic_favorite_n);
                }
            }
        });
        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,SelfieDetailsActivity.class);
                intent.putExtra("activity","profile");
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
        AppController.getInstance().getRequestQueue().getCache().remove(Constant.UPDATE_SELFIE_URL);
        AppController.getInstance().addToRequestQueue(postRequest);
    }
    private void delete_selfie(final String image_path){
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.DELETE_SELFIE_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setIcon(R.mipmap.ic_logo);
                        alert.setTitle("Selfie Guru");
                        alert.setMessage("Successfully Delete !");
                        alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, SelfieUserProfileActivity.class);
                                intent.putExtra("profile","upload");
                                context.startActivity(intent);
                                ((Activity)context).finish();
                            }
                        });
                        alert.create().show();
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
                return params;
            }
        };
        //queue.add(postRequest);
        AppController.getInstance().getRequestQueue().getCache().invalidate(Constant.DELETE_SELFIE_URL,true);
        AppController.getInstance().addToRequestQueue(postRequest);
        deleteCache(context);
    }
    private void uploadFavorite(String image_path){
        String id = databaseQuotes.push().getKey();
        SelfieResponse response =
                new SelfieResponse(image_path,id);
        assert id != null;
        databaseQuotes.child(id).setValue(response);
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
}

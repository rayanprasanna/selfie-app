package com.universl.selfieguru.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import com.universl.selfieguru.utils.AppController;
import com.universl.selfieguru.utils.Constant;
import com.universl.selfieguru.utils.GlideApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PopularAdapter extends BaseAdapter {
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

    @SuppressLint("HardwareIds")
    public PopularAdapter(Context context, List<SelfieResponse> selfieResponses, List<SelfieResponse> image_selfieResponses, ArrayList<String> image_pathList) {
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
        TextView title,publish_date,count_of_favorite,review;
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
            viewHolder.review.setVisibility(View.GONE);
            viewHolder.delete.setVisibility(View.GONE);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.count_of_favorite.setText(String.valueOf(Collections.frequency(getImage_pathList(),selfieResponses.get(position).photo)));

        viewHolder.isClickFavoriteButton = false;
        // Set the results into TextViews
        viewHolder.title.setText("# " + selfieResponses.get(position).title);
        GlideApp.with(context.getApplicationContext()).load(selfieResponses.get(position).photo).into(viewHolder.image);
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
                intent.putExtra("activity","popular");
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
       /* AppController.getInstance().getRequestQueue().getCache().remove(Constant.UPDATE_SELFIE_URL);*/
        AppController.getInstance().getRequestQueue().getCache().invalidate(Constant.UPDATE_SELFIE_URL,true);
        AppController.getInstance().addToRequestQueue(postRequest);
    }
    private void uploadFavorite(String image_path){
        String id = databaseQuotes.push().getKey();
        SelfieResponse response =
                new SelfieResponse(image_path,id);
        assert id != null;
        databaseQuotes.child(id).setValue(response);
    }
}

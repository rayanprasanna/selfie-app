package com.universl.selfieguru.response;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class SelfieResponse implements Comparable{
    @SerializedName("category")
    public String category;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("date")
    public String date;

    @SerializedName("photo")
    public String photo;

    @SerializedName("username")
    public String user_name;

    @SerializedName("status")
    public String status;

    @SerializedName("like_count")
    public String like_count;

    @SerializedName("user_id")
    public String user_id;

    public SelfieResponse() {
    }

    public SelfieResponse(String photo, String user_id) {
        this.photo = photo;
        this.user_id = user_id;
    }

    public SelfieResponse(String category, String title, String date, String photo, String user_name, String status, String like_count) {
        this.category = category;
        this.title = title;
        this.date = date;
        this.photo = photo;
        this.user_name = user_name;
        this.status = status;
        this.like_count = like_count;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        int compare_like= Integer.parseInt(((SelfieResponse)o).like_count);
        return compare_like - Integer.parseInt(this.like_count);
    }
}

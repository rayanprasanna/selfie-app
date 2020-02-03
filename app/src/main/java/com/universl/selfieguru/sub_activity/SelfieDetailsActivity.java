package com.universl.selfieguru.sub_activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.universl.selfieguru.CompetitionActivity;
import com.universl.selfieguru.EditorChoiceActivity;
import com.universl.selfieguru.FansActivity;
import com.universl.selfieguru.HomeActivity;
import com.universl.selfieguru.PopularActivity;
import com.universl.selfieguru.R;
import com.universl.selfieguru.response.SelfieResponse;
import com.universl.selfieguru.utils.GlideApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SelfieDetailsActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 100;
    WallpaperManager wallpaperManager ;
    List<SelfieResponse> selfieResponseList_photo;
    ArrayList<String> image_path;
    TextView count;
    ImageView favorite;
    private DatabaseReference databaseQuotes;
    Bitmap bitmap;String photoPath;int n;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_details);

        //requestStoragePermission();

        ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setTitle(Html.fromHtml("<font color='#ffffff'>Selected Selfie</font>"));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_w);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_quotes");
        count = findViewById(R.id.count);
        final ImageView imageView = findViewById(R.id.quotes_image);
        final FloatingActionButton upload,share;
        TextView user_name = findViewById(R.id.user_name);
        final ImageView wall = findViewById(R.id.quotes_wall);
        upload = findViewById(R.id.wallpaper);
        share = findViewById(R.id.share);
        favorite = findViewById(R.id.favorite);
        wallpaperManager  = WallpaperManager.getInstance(getApplicationContext());

        image_path = new ArrayList<>();
        selfieResponseList_photo = new ArrayList<>();

        user_name.setText(getIntent().getStringExtra("quotes_user_name"));
        /*user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelfieDetailsActivity.this, AppUserUploadDetailsActivity.class);
                intent.putExtra("app_user_name",getIntent().getStringExtra("quotes_user_name"));
                startActivity(intent);
                finish();
            }
        });*/
        GlideApp.with(getApplicationContext()).load(getIntent().getStringExtra("quotes_image")).fitCenter().into(imageView);
        count.setText(getIntent().getStringExtra("count"));
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelfieDetailsActivity.this,SelfieFilterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*Glide.with(SelfieDetailsActivity.this).asBitmap()
                        .load(getIntent().getStringExtra("quotes_image")).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        wall.setVisibility(View.GONE);
                        wall.setImageBitmap(resource);
                        Uri bmpUri = getLocalBitmapUri(wall);
                        if (bmpUri != null) {
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.universl.selfieguru");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                            shareIntent.setType("image/*");
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(shareIntent, "Share images..."));
                        } else {
                            Toast.makeText(SelfieDetailsActivity.this,"Somethings went wrong",Toast.LENGTH_LONG).show();
                        }
                    }
                });*/
                bitmap = getBitmapFromView(imageView);
                requestStoragePermission();
            }
        });
        initAds();
    }
    private void startShare(Bitmap bitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        shareScreenshot();
    }
    private void shareScreenshot()
    {
        photoPath = Environment.getExternalStorageDirectory() + "/saved_images" + "/Image-" + n + ".jpg";
        File F = new File(photoPath);

        // TODO your package name as well add .fileprovider
        Uri U = FileProvider.getUriForFile(getApplicationContext(), "com.universl.selfieguru.fileprovider", F);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("image/png");
        i.putExtra(Intent.EXTRA_TEXT, "# Selfie Guru " +"\n"+ "https://play.google.com/store/apps/details?id=com.universl.selfieguru");
        i.putExtra(Intent.EXTRA_STREAM, U);
        startActivityForResult(Intent.createChooser(i, "share via"), 1);
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }
    @SuppressLint("InlinedApi")
    private void requestStoragePermission(){
        String[] permissions = {WRITE_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startShare(bitmap);
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    STORAGE_PERMISSION_CODE);
            Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 & grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(SelfieDetailsActivity.this,"Permission granted",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(SelfieDetailsActivity.this,"Permission not granted",Toast.LENGTH_LONG).show();
            }
        }
    }
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            // getExternalFilesDir() + "/Pictures" should match the declaration in fileprovider.xml paths
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");

            // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            bmpUri = FileProvider.getUriForFile(SelfieDetailsActivity.this, "com.universl.selfieguru", file);
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            if (getIntent().getStringExtra("activity").equalsIgnoreCase("home")){
                Intent intent = new Intent(SelfieDetailsActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }
            if (getIntent().getStringExtra("activity").equalsIgnoreCase("fans")){
                Intent intent = new Intent(SelfieDetailsActivity.this, FansActivity.class);
                startActivity(intent);
                finish();
            }
            if (getIntent().getStringExtra("activity").equalsIgnoreCase("editor")){
                Intent intent = new Intent(SelfieDetailsActivity.this, EditorChoiceActivity.class);
                startActivity(intent);
                finish();
            }
            if (getIntent().getStringExtra("activity").equalsIgnoreCase("popular")){
                Intent intent = new Intent(SelfieDetailsActivity.this, PopularActivity.class);
                startActivity(intent);
                finish();
            }
            if (getIntent().getStringExtra("activity").equalsIgnoreCase("competition")){
                Intent intent = new Intent(SelfieDetailsActivity.this, CompetitionActivity.class);
                intent.putExtra("description",getIntent().getStringExtra("description"));
                startActivity(intent);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent().getStringExtra("activity").equalsIgnoreCase("home")){
            Intent intent = new Intent(SelfieDetailsActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
        } if (getIntent().getStringExtra("activity").equalsIgnoreCase("fans")){
            Intent intent = new Intent(SelfieDetailsActivity.this, FansActivity.class);
            startActivity(intent);
            finish();
        } if (getIntent().getStringExtra("activity").equalsIgnoreCase("editor")){
            Intent intent = new Intent(SelfieDetailsActivity.this, EditorChoiceActivity.class);
            startActivity(intent);
            finish();
        } if (getIntent().getStringExtra("activity").equalsIgnoreCase("popular")){
            Intent intent = new Intent(SelfieDetailsActivity.this, PopularActivity.class);
            startActivity(intent);
            finish();
        }if (getIntent().getStringExtra("activity").equalsIgnoreCase("profile")){
            Intent intent = new Intent(SelfieDetailsActivity.this,SelfieUserProfileActivity.class);
            intent.putExtra("profile","upload");
            startActivity(intent);
            finish();
        }if (getIntent().getStringExtra("activity").equalsIgnoreCase("competition")){
            Intent intent = new Intent(SelfieDetailsActivity.this, CompetitionActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //Ads
    private void initAds() {
        AdView adView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}

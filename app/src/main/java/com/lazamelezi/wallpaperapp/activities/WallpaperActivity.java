package com.lazamelezi.wallpaperapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.lazamelezi.wallpaperapp.MainApplication;
import com.lazamelezi.wallpaperapp.R;
import com.lazamelezi.wallpaperapp.data_source.DataService;
import com.lazamelezi.wallpaperapp.models.WallsPOJO;
import com.lazamelezi.wallpaperapp.utils.ApplyWallpaper;
import com.lazamelezi.wallpaperapp.utils.FastBlurTransform;
import com.lazamelezi.wallpaperapp.utils.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WallpaperActivity extends AppCompatActivity {

    private static final String TAG = "WallpaperActivity";

    private Handler handler;
    private Bitmap imageBitmap;
    private DataService dataService;
    private WallsPOJO pojo;

    //    Views
    private ImageView thumbView;
    private PhotoView photoView;
    private boolean showThumbnail = true;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private View topShadow, bottomShadow, saveButton, applyButton, favoriteButton;
    private LinearLayout bottomNavLayout;




    //    dynamic message showing on task complete and ad shown
    private boolean isProcessCompleted = false;
    private String message;

    private void processStart(String message) {
        progressBar.setVisibility(View.VISIBLE);
        this.message = message;
        isProcessCompleted = true;
    }

    private void processStopIfDone() {
        if (isProcessCompleted) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }, 800);
        }
        isProcessCompleted = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);

//        get the wallpaper pojo. if not exist, finish page.
        if (!getIntent().hasExtra("pojo")) {
            Toast.makeText(this, "Image Not Valid!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        pojo = (WallsPOJO) getIntent().getSerializableExtra("pojo");
        handler = new Handler(Looper.getMainLooper());
//        apply full screen
        View mDecorView = getWindow().getDecorView();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.parseColor("#01FFFFFF"));

//        bind xml views
        toolbar = findViewById(R.id.full_view_toolbar);
        topShadow = findViewById(R.id.top_shadow);
        bottomShadow = findViewById(R.id.bottom_shadow);
        thumbView = findViewById(R.id.thumbView);
        photoView = findViewById(R.id.photo_view);
        progressBar = findViewById(R.id.full_progressbar);
        bottomNavLayout = findViewById(R.id.bottomButtonNav);
        saveButton = bottomNavLayout.getChildAt(0);
        applyButton = bottomNavLayout.getChildAt(1);
        favoriteButton = bottomNavLayout.getChildAt(2);

//        apply margin for status-bar and navigation-bar
        ViewCompat.setOnApplyWindowInsetsListener(mDecorView, (v, insets) -> {
            final int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top; // in px


            RelativeLayout.LayoutParams toolbarLayoutParams = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
            toolbarLayoutParams.setMargins(0, statusBarHeight, 0, 0);
            toolbar.setLayoutParams(toolbarLayoutParams);


            return WindowInsetsCompat.CONSUMED;
        });

//        init helpers
        dataService = MainApplication.getDataService(getApplication());



//        init views
        setupBottomNav();
        toolbar.setNavigationOnClickListener(v -> finish());

        photoView.setOnPhotoTapListener((view, x, y) -> toggleTouch());
        photoView.setZoomable(false);

//        load blur image into photoView
        Glide.with(this).asBitmap().load(pojo.getPreviewUrl()).diskCacheStrategy(DiskCacheStrategy.DATA).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (!showThumbnail) return;
                thumbView.setAlpha(0f);
                thumbView.setImageBitmap(FastBlurTransform.apply(resource, 1, 10));
                thumbView.animate().withEndAction(() -> thumbView.setAlpha(1f)).alpha(1f).setDuration(300).start();
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        });
        handler.postDelayed(this::loadHD, 500);
    }

    private void loadHD() {
        Glide.with(this).asBitmap().load(pojo.getUrl()).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                showThumbnail = false;
                Toast.makeText(WallpaperActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                imageBitmap = resource;
                showThumbnail = false;
                photoView.setZoomable(true);
                handler.postDelayed( () -> thumbView.setImageBitmap(null), 1000);
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                photoView.setAlpha(0f);
                photoView.setImageBitmap(resource);
                photoView.animate().withEndAction(() -> thumbView.setAlpha(1f)).alpha(1f).setDuration(300).start();
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        });
    }






    private void setupBottomNav() {
        final ImageView heartImage = favoriteButton.findViewById(R.id.heartImage);

        if (dataService.isFavorite(pojo.getUrl()))
            heartImage.setImageResource(R.drawable.ic_baseline_favorite_24);
        else
            heartImage.setImageResource(R.drawable.ic_baseline_favorite_border_24);
//        save button click
        saveButton.setOnClickListener(view -> {
            if (isStoragePermissionNotGranted()) {
                ActivityCompat.requestPermissions(WallpaperActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }

            saveImage();
        });

//        apply button click
        applyButton.setOnClickListener(view -> {

            askOrApplyWallpaper();
        });
//        favorite button click
        favoriteButton.setOnClickListener(view -> {
            if (dataService.isFavorite(pojo.getUrl())) {
                dataService.toggleFavorite(pojo, false);
                heartImage.setImageResource(R.drawable.ic_baseline_favorite_border_24);
            } else {
                dataService.toggleFavorite(pojo, true);
                heartImage.setImageResource(R.drawable.ic_baseline_favorite_24);
            }
        });
    }

    public boolean isStoragePermissionNotGranted() {
//        if api is below 29 and above/equal 23, WRITE_EXTERNAL_STORAGE required
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage();
        }
    }

    private void saveImage() {
        if (imageBitmap == null || pojo == null) return;
        Log.d("TAG", "saveImage: called");
        if (isStoragePermissionNotGranted())
            return;

//        saving
        processStart("Image Saved Successfully!");
        final boolean isSaved = Utils.save(this, imageBitmap, getString(R.string.app_name),
                pojo.getName().replaceAll("\\s", "_"));
//        delay of 500ms
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isSaved) {

                    processStopIfDone();

            } else {
                message = "Error while saving image.";
                processStopIfDone();
            }
        }, 500);

    }

    private void askOrApplyWallpaper() {
        View v = getLayoutInflater().inflate(R.layout.layout_set_on, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AlertDialog dialog = new AlertDialog.Builder(WallpaperActivity.this)
                    .setView(v).create();
            v.findViewById(R.id.on_home_screen_btn).setOnClickListener(view -> {
                dialog.dismiss();
                applyWallpaper(1);
            });
            v.findViewById(R.id.on_lock_screen_btn).setOnClickListener(view -> {
                dialog.dismiss();
                applyWallpaper(2);
            });
            v.findViewById(R.id.on_both_screen_btn).setOnClickListener(view -> {
                dialog.dismiss();
                applyWallpaper(3);
            });
            dialog.show();
        } else {
            applyWallpaper(0);
        }
    }

    @Override
    public void finish() {

        if (!(getApplication() instanceof MainApplication)) {
            super.finish();
        }
        final MainApplication mainApplication = (MainApplication) getApplication();


        super.finish();
    }

    private void toggleTouch() {
        if (toolbar.getAlpha() == 0) {
            toolbar.animate().alpha(1).setDuration(200);
            topShadow.animate().alpha(1).setDuration(200);
            bottomShadow.animate().alpha(1).setDuration(200);
            bottomNavLayout.animate().alpha(1).setDuration(200);
            saveButton.setClickable(true);
            applyButton.setClickable(true);
            favoriteButton.setClickable(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        } else {
            toolbar.animate().alpha(0).setDuration(200);
            topShadow.animate().alpha(0).setDuration(200);
            bottomShadow.animate().alpha(0).setDuration(200);
            bottomNavLayout.animate().alpha(0).setDuration(200);
            saveButton.setClickable(false);
            applyButton.setClickable(false);
            favoriteButton.setClickable(false);
            toolbar.setNavigationOnClickListener(null);
        }
    }

    private void applyWallpaper(int where) {
        processStart(getString(R.string.success_applied));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            final boolean b = ApplyWallpaper.fromBitmap(this, imageBitmap, where);
            handler.post(() -> {
                if (b) {

                        processStopIfDone();

                } else {
                    message = "Failed to apply wallpaper";
                    processStopIfDone();
                }
            });
        });
    }







    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {

        Intent i = new Intent();
        i.putExtra("id", pojo.getViewType());
        i.putExtra("fav", dataService.isFavorite(pojo.getUrl()));
        setResult(RESULT_OK, i);
        super.onDestroy();
    }
}
package com.lazamelezi.wallpaperapp;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import com.lazamelezi.wallpaperapp.activities.SplashActivity;
import com.lazamelezi.wallpaperapp.data_source.DataService;
import com.lazamelezi.wallpaperapp.data_source.SQLCategories;
import com.lazamelezi.wallpaperapp.data_source.impl.SQLDataServiceImpl;
import com.lazamelezi.wallpaperapp.utils.SQLFav;
import com.lazamelezi.wallpaperapp.utils.Utils;


public class MainApplication extends android.app.Application
        implements ActivityLifecycleCallbacks, DefaultLifecycleObserver {


    private Activity currentActivity;

    private static final String TAG = "MyApplication";


    private DataService dataService;

    public static DataService getDataService(Application application) {
        return ((MainApplication) application).dataService;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);



        Utils.initTheme(this);

        SQLCategories sqlCategories = new SQLCategories(this);
        SQLFav sqlFav = new SQLFav(this);
        dataService = new SQLDataServiceImpl(this, sqlCategories, sqlFav);




    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {


    }

    private void tryShowSplashAd() {
        if (currentActivity instanceof SplashActivity) {

        }
    }

    /**
     * ActivityLifecycleCallback methods.
     */
    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

}

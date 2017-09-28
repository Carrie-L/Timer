package com.ripple.lib.standreminder;

import android.app.Application;

import com.ripple.lib.standreminder.utils.CrashHandler;

/**
 * Created by Carrie on 2017/9/28.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler crashHandler=CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}

package io.github.nianhua110.myjamsmusic.Utils;

import android.app.Application;

import io.github.nianhua110.myjamsmusic.CrashHandler;

/**
 * Created by kankan on 2016/5/23.
 */
public class Common extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}

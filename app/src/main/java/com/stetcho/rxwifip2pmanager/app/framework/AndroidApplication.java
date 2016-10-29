package com.stetcho.rxwifip2pmanager.app.framework;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.stetcho.rxwifip2pmanager.app.BuildConfig;

/**
 * Created by Stefan Mitev on 01/07/2015.
 */
public class AndroidApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            LeakCanary.install(this);
        }
    }
}

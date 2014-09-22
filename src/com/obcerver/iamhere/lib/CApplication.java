package com.obcerver.iamhere.lib;

import android.app.Application;
import com.obcerver.iamhere.lib.CLog;

public class CApplication extends Application {
    
    private static CApplication instance;
    public static CApplication getInstance() {
        return instance;
    }

    @Override public void onCreate() {
        super.onCreate();
        CLog.v("onCreate");
        instance = this;
    }
    
    @Override public void onTerminate() {
    	super.onTerminate();
        CLog.v("onTerminate");
    }
}

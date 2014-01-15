package com.other.util;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;


public class MyApplication extends Application{
    private static MyApplication mcontext;
    @Override
    public void onCreate() {
        super.onCreate();
        mcontext = this;
    }
    
    public static Context getAppContext(){
        return mcontext;
    }
    
    public static Resources getAppResources(){
        return getAppResources();
    }
}

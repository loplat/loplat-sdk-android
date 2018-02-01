package com.loplat.loplatsample;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.loplat.placeengine.Plengi;
import com.loplat.placeengine.utils.LoplatLogger;




public class LoplatSampleApplication extends MultiDexApplication {

    Plengi mPlengi = null;

    private static LoplatSampleApplication instance;

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        LoplatLogger.i("LoplatApplication created ---------------");

        instance = this;

        // set up loplat engine
        mPlengi = Plengi.getInstance(this);
        // do init
        String clientId = "loplatdemo";
        String clientSecret = "loplatdemokey";
        /* Please be careful not to input any personal information such as email, phone number. */
        String uniqueUserId = "loplat_12345";
        mPlengi.getInstance(this).init(clientId, clientSecret, uniqueUserId);
        mPlengi.setListener(new LoplatPlengiListener());
    }


}

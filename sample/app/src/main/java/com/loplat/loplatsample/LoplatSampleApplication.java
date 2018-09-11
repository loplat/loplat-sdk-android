package com.loplat.loplatsample;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;

import com.loplat.placeengine.Plengi;
import com.loplat.placeengine.utils.LoplatLogger;




public class LoplatSampleApplication extends MultiDexApplication {

    private static LoplatSampleApplication instance;

    private static final String PREFS_NAME = LoplatSampleApplication.class.getSimpleName();
    private static final String TAG = PREFS_NAME;

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        LoplatLogger.i("LoplatApplication created ---------------");

        instance = this;
        // do init
        String clientId = "loplatdemo";
        String clientSecret = "loplatdemokey";
        /* Please be careful not to input any personal information such as email, phone number. */
        String uniqueUserId = "loplat_12345";
        Plengi.getInstance(this).setListener(new LoplatPlengiListener());
        Plengi.getInstance(this).init(clientId, clientSecret, uniqueUserId);

        // 기존 마케팅 동의 여부 체크
        if (isMarketingServiceAgreed(this)) {
            Plengi.getInstance(this).enableAdNetwork(true);
            // 직접 광고를 하는 경우
            //Plengi.getInstance(this).enableAdNetwork(true, false);
        }

        if (isLocationServiceAgreed(this)) {
            Plengi.getInstance(this).start();
        }
    }

    // App에서 광고 연동 여부 설정
    public static void setMarketingServiceAgreement(Context context, boolean enableAdNetwork) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("marketing_agreement", enableAdNetwork);
            editor.commit();
        } catch (Exception e) {
        }
    }

    // App에서 광고 연동 여부 확인
    public static boolean isMarketingServiceAgreed(Context context) {
        boolean enableAdNetwork = false;
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            enableAdNetwork = settings.getBoolean("marketing_agreement", false);
        } catch (Exception e) {
        }
        return enableAdNetwork;
    }

    // 위치 기반 약관 동의 설정
    public static void setLocationServiceAgreement(Context context, boolean start) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("location_agreement", start);
            editor.commit();
        } catch (Exception e) {
        }
    }

    // 위치 기반 약관 동의 여부 확인
    public static boolean isLocationServiceAgreed(Context context) {
        boolean enableAdNetwork = false;
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            enableAdNetwork = settings.getBoolean("location_agreement", false);
        } catch (Exception e) {
        }
        return enableAdNetwork;
    }



}

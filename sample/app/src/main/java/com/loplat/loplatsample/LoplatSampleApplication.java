package com.loplat.loplatsample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.loplat.placeengine.Plengi;


public class LoplatSampleApplication extends Application {

    private static LoplatSampleApplication instance;

    private static final String PREFS_NAME = LoplatSampleApplication.class.getSimpleName();

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // loplatSdkConfiguration(): 백그라운드 동작과 위치 서비스 약관 동의, 마케팅 동의를 미리 받아 둔 사용자를 위해 반드시 필요
        loplatSdkConfiguration();
    }

    // init(), start()가 여러 번 호출되도 상관 없음
    public void loplatSdkConfiguration() {
        Context context = this;
        Plengi plengi = Plengi.getInstance(this);

        // 고객사에 발급한 로플랫 SDK client ID/PW 입력
        String clientId = "loplatdemo"; // Test ID
        String clientSecret = "loplatdemokey";  // Test PW

        // Plengi init 하는 부분은 위치권한허용과 관계 없이 실행
        plengi.init(clientId, clientSecret, getEchoCode(context));

        // 위치 서비스 약관 동의 여부 체크
        if (isLocationServiceAgreed(context)) {
            // 마케팅 동의 여부 체크
            if (isMarketingServiceAgreed(context)) {
                // 마케팅 수신에 동의한 user에 대해서 로플랫 켐페인 설정
                // 고객사가 직접 푸시 메세지 광고를 하는 경우
                plengi.enableAdNetwork(true, false);
                // 로플랫 SDK 에 푸시 메세지 광고를 맡기는 경우
                // Plengi.getInstance(this).enableAdNetwork(true);
                // Plengi.getInstance(this).setAdNotiLargeIcon(R.drawable.ic_launcher);
                // Plengi.getInstance(this).setAdNotiSmallIcon(R.drawable.ic_launcher);
            } else {
                // 마케팅 동의 거부한 user에 대해서 로플랫 켐페인 설정 중단
                plengi.enableAdNetwork(false);
            }
            plengi.setListener(new LoplatPlengiListener());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                plengi.setBackgroundLocationAccessDialogLayout(R.layout.dialog_background_location_info);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                plengi.setDefaultNotificationChannel(R.string.foreground_service_noti_channel_name,0);
                plengi.setDefaultNotificationInfo(
                        R.drawable.ic_launcher,
                        0,
                        0);
            }
            plengi.start();
        } else {
            // 위치 서비스 약관 동의 거부한 user에 대해서 SDK stop
            plengi.stop();
        }
    }

    // 마케팅 수신 동의 여부 저장
    public static void setMarketingServiceAgreement(Context context, boolean agree) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("marketing_agreement", agree);
            editor.commit();
        } catch (Exception e) {
        }
    }

    // 마케팅 수신 동의 여부 확인
    public static boolean isMarketingServiceAgreed(Context context) {
        boolean isMarketingServiceAgreed = false;
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            isMarketingServiceAgreed = settings.getBoolean("marketing_agreement", false);
        } catch (Exception e) {
        }
        return isMarketingServiceAgreed;
    }

    // 위치 기반 서비스 약관 동의 여부 저장
    public static void setLocationServiceAgreement(Context context, boolean agree) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("location_agreement", agree);
            editor.commit();
        } catch (Exception e) {
        }
    }

    // 위치 기반 서비스 약관 동의 여부 확인
    public static boolean isLocationServiceAgreed(Context context) {
        boolean isLocationServiceAgreed = false;
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            isLocationServiceAgreed = settings.getBoolean("location_agreement", false);
        } catch (Exception e) {
        }
        return isLocationServiceAgreed;
    }

    // 회원 번호 저장
    // 이메일, 전화번호와 같은 개인정보 제외
    public static void setEchoCode(Context context, String member_code) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("member_code", member_code);
            editor.commit();
        } catch (Exception e) {
        }
    }

    // 저장된 회원 번호 가져옴
    public static String getEchoCode(Context context) {
        String echo_code = null;
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            echo_code = settings.getString("member_code", null);
        } catch (Exception e) {
        }
        return echo_code;
    }
}

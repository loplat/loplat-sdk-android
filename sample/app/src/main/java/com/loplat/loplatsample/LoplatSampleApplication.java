package com.loplat.loplatsample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.loplat.placeengine.Plengi;

public class LoplatSampleApplication extends Application {

    private static LoplatSampleApplication instance;

    private static final String PREFS_NAME = LoplatSampleApplication.class.getSimpleName();

    public static Context getContext(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // 백그라운드 동작과 위치 서비스 약관 동의, 마케팅 동의를 미리 받아 둔 사용자를 위해 반드시 필요
        loplatSdkConfiguration();
    }

    /**
     * Loplat SDK 설정 및 위치 권한, 위치 서비스약관 동의 여부에 따라 Loplat SDK 동작
     * init(), start()가 여러 번 호출되도 상관 없음
     */
    public void loplatSdkConfiguration() {
        Context context = this;
        Plengi plengi = Plengi.getInstance(this);

        // 고객사에 발급한 로플랫 SDK client ID/PW 입력
        String clientId = "loplatdemo"; // Test ID
        String clientSecret = "loplatdemokey";  // Test PW

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

            // 위치 인식 정보를 수신할 Listener 등록
            plengi.setListener(new LoplatPlengiListener());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                /**
                 * 백그라운드에서 동작 시 출력될 ForgroundService의 알림 설정
                 * 따로 설정하지 않으면 기본 값으로 출력
                 *
                 * 특정 요소만 custom하기 원한다면 아래와 같이 resource 입력
                 * 기본 값을 쓰기 원하는 요소엔 0 입력
                 */
                plengi.setDefaultNotificationChannel(R.string.foreground_service_noti_channel_name,0);
                plengi.setDefaultNotificationInfo(
                        R.drawable.ic_launcher,
                        R.string.foreground_service_noti_title,
                        0);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                /**
                 * 위치 권한-항상허용 심사 관련 설정
                 * Loplat이 제공하는 심사용 프롬프트를 사용하지 않고 자체 프롬프트를 사용할 경우에만
                 * Plengi.disableFeatureBgLocationReviewUX(true) 호출
                 *
                 * Loplat이 제공하는 심사용 프롬프트 중 사용자에게 표시되는 명시적인 인앱 공개 대화상자를
                 * custom 필요시 Plengi.setBackgroundLocationAccessDialogLayout(@LayoutRes) 호출
                 */

                // plengi.disableFeatureBgLocationReviewUX(true);

                plengi.setBackgroundLocationAccessDialogLayout(R.layout.dialog_background_location_info);
            }

            // Loplat SDK 설정들은 반드시 Plengi.init() 전에 호출 필요
            plengi.init(clientId, clientSecret, getEchoCode(context));

            plengi.start();
        } else {
            // 위치 서비스 약관 동의 거부한 user에 대해서 SDK stop
            plengi.stop();
        }
    }

    /**
     * 마케팅 수신 동의 여부 저장
     */
    public static void setMarketingServiceAgreement(Context context, boolean agree) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("marketing_agreement", agree);
            editor.commit();
        } catch (Exception e) {
        }
    }

    /**
     * 마케팅 수신 동의 여부 확인
     */
    public static boolean isMarketingServiceAgreed(Context context) {
        boolean isMarketingServiceAgreed = false;
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            isMarketingServiceAgreed = settings.getBoolean("marketing_agreement", false);
        } catch (Exception e) {
        }
        return isMarketingServiceAgreed;
    }

    /**
     * 위치 기반 서비스 약관 동의 여부 저장
     */
    public static void setLocationServiceAgreement(Context context, boolean agree) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("location_agreement", agree);
            editor.commit();
        } catch (Exception e) {
        }
    }

    //

    /**
     * 위치 기반 서비스 약관 동의 여부 확인
     */
    public static boolean isLocationServiceAgreed(Context context) {
        boolean isLocationServiceAgreed = false;
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            isLocationServiceAgreed = settings.getBoolean("location_agreement", false);
        } catch (Exception e) {
        }
        return isLocationServiceAgreed;
    }

    /**
     * 회원 번호 저장. (중요)단, 이메일, 전화번호와 같은 개인정보 제외
     */
    public static void setEchoCode(Context context, String member_code) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("member_code", member_code);
            editor.apply();
        } catch (Exception e) {
        }
    }

    /**
     * 저장된 회원 번호 get
     */
    public static String getEchoCode(Context context) {
        String echo_code = null;
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            echo_code = settings.getString("member_code", null);
        } catch (Exception e) {
        }
        return echo_code;
    }

    /**
     * 위치 권한 교육용 UI 출력 필요 여부 저장
     */
    public static void setLocationShouldShowRationale(Context context, boolean shouldShow) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("location_should_show_rationale", shouldShow);
            editor.apply();
        } catch (Exception e) {
        }
    }

    /**
     * 위치 권한 교육용 UI 출력 필요 여부 get
     * default value true 한 이유는 앱 실행 후 최초
     * {@link ActivityCompat.shouldShowRequestPermissionRationale()}
     * 요청 시 false 를 반환하기 때문
     */
    public static boolean getLocationShouldShowRationale(Context context) {
        boolean should_show = false;
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            should_show = settings.getBoolean("location_should_show_rationale", true);
        } catch (Exception e) {
        }
        return should_show;
    }
}

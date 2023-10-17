package com.loplat.loplatsample.kotlin

import android.app.Application
import android.content.Context
import android.os.Build
import com.loplat.loplatsample.R
import com.loplat.placeengine.Plengi

class KotlinLoplatSampleApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        instance = this

        // 백그라운드 동작과 위치 서비스 약관 동의, 마케팅 동의를 미리 받아 둔 사용자를 위해 반드시 필요
        loplatSdkConfiguration()
    }

    /**
     * Loplat SDK 설정 및 위치 권한, 위치 서비스약관 동의 여부에 따라 Loplat SDK 동작
     * 앱 시작 혹은 로그인 할 때 마다 사용자의 위치약관동의 여부를 매번 확인해서 Loplat SDK start 호출 필수
     * init(), start()가 여러 번 호출되도 상관 없음
     */
    fun loplatSdkConfiguration() {
        val context = this
        val plengi = Plengi.getInstance(this)

        // 위치 인식 정보를 수신할 Listener 등록
        plengi.listener = KotlinLoplatPlengiListener()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /**
             * 백그라운드에서 동작 시 출력될 ForgroundService의 알림 설정
             * 따로 설정하지 않으면 기본 값으로 출력
             *
             * 특정 요소만 custom하기 원한다면 아래와 같이 resource 입력
             * 기본 값을 쓰기 원하는 요소엔 0 입력
             */
            plengi.setDefaultNotificationChannel(R.string.foreground_service_noti_channel_name, 0)
            plengi.setDefaultNotificationInfo(
                    R.drawable.ic_launcher,
                    R.string.foreground_service_noti_title,
                    0)
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

            plengi.setBackgroundLocationAccessDialogLayout(R.layout.dialog_background_location_info)
        }

        // 위치 서비스 약관 동의 여부 체크
        if (isLocationServiceAgreed(context)) {
            // 마케팅 동의 여부 체크
            if (isMarketingServiceAgreed(context)) {
                // 마케팅 수신에 동의한 user에 대해서 로플랫 켐페인 설정

                // 고객사가 직접 푸시 메세지 광고를 하는 경우
                plengi.enableAdNetwork(true, false)
                // 로플랫 SDK 에 푸시 메세지 광고를 맡기는 경우

                // Plengi.getInstance(this).enableAdNetwork(true);
                // Plengi.getInstance(this).setAdNotiLargeIcon(R.drawable.ic_launcher);
                // Plengi.getInstance(this).setAdNotiSmallIcon(R.drawable.ic_launcher);
            } else {
                // 마케팅 동의 거부한 user에 대해서 로플랫 켐페인 설정 중단
                plengi.enableAdNetwork(false)
            }

            plengi.setEchoCode(getEchoCode(context))
            // id, pw 를 직접 입력
            //plengi.start(clientId, clientSecret)

            // build.gradle 에서 id,pw 선언 하였다면
            plengi.start()
        } else {
            /**
             * 위치 서비스 약관 동의 거부한 user에 대해서 SDK stop
             * SDK stop은 반드시 사용자가 '위치 약관 동의'에 대한 거부했을 경우에만 호출
             * 예외적인 케이스에 대해서는 SDK stop 호출 불필요
             */
            plengi.stop()
        }
    }

    companion object {
        private lateinit var instance: KotlinLoplatSampleApplication
        private val PREFS_NAME = KotlinLoplatSampleApplication::class.java.simpleName
        private val KEY_MARKETING_AGREEMENT by lazy { getContext().getString(R.string.key_marketing_agreement) }
        private val KEY_LOCATION_AGREEMENT by lazy { getContext().getString(R.string.key_location_agreement) }
        private val KEY_MEMBER_CODE by lazy { getContext().getString(R.string.key_member_code) }
        private val KEY_LOCATION_RATIONALE by lazy { getContext().getString(R.string.key_location_rationale) }

        fun getContext(): Context {
            return instance
        }

        /**
         * 마케팅 수신 동의 여부 저장
         */
        fun setMarketingServiceAgreement(context: Context, agree: Boolean) {
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                val editor = settings.edit()
                editor.putBoolean(KEY_MARKETING_AGREEMENT, agree)
                editor.commit()
            } catch (e: Exception) {
            }
        }

        /**
         * 마케팅 수신 동의 여부 확인
         */
        fun isMarketingServiceAgreed(context: Context): Boolean {
            var isMarketingServiceAgreed = false
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                isMarketingServiceAgreed = settings.getBoolean(KEY_MARKETING_AGREEMENT, false)
            } catch (e: Exception) {
            }
            return isMarketingServiceAgreed
        }

        /**
         * 위치 기반 서비스 약관 동의 여부 저장
         */
        fun setLocationServiceAgreement(context: Context, agree: Boolean) {
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                val editor = settings.edit()
                editor.putBoolean(KEY_LOCATION_AGREEMENT, agree)
                editor.commit()
            } catch (e: Exception) {
            }
        }

        /**
         * 위치 기반 서비스 약관 동의 여부 확인
         */
        fun isLocationServiceAgreed(context: Context): Boolean {
            var isLocationServiceAgreed = false
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                isLocationServiceAgreed = settings.getBoolean(KEY_LOCATION_AGREEMENT, false)
            } catch (e: Exception) {
            }
            return isLocationServiceAgreed
        }

        /**
         * 회원 번호 저장. (중요)단, 이메일, 전화번호와 같은 개인정보 반드시 제외
         */
        fun setEchoCode(context: Context, member_code: String?) {
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                val editor = settings.edit()
                editor.putString(KEY_MEMBER_CODE, member_code)
                editor.apply()
            } catch (e: Exception) {
            }
        }

        /**
         * 저장된 회원 번호 get
         */
        fun getEchoCode(context: Context): String? {
            var echo_code: String? = null
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                echo_code = settings.getString(KEY_MEMBER_CODE, null)
            } catch (e: Exception) {
            }
            return echo_code
        }

        /**
         * 위치 권한 교육용 UI 출력 필요 여부 저장
         */
        fun setLocationShouldShowRationale(context: Context, shouldShowRationale: Boolean) {
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                val editor = settings.edit()
                editor.putBoolean(KEY_LOCATION_RATIONALE, shouldShowRationale)
                editor.apply()
            } catch (e: Exception) {
            }
        }

        /**
         * 위치 권한 교육용 UI 출력 필요 여부 get
         * default value true 한 이유는 앱 실행 후 최초
         * {@link ActivityCompat.shouldShowRequestPermissionRationale()}
         * 요청 시 false 를 반환하기 때문
         */
        fun getLocationShouldShowRationale(context: Context): Boolean {
            var should_show_rationale = false
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                should_show_rationale = settings.getBoolean(KEY_LOCATION_RATIONALE, true)
            } catch (e: Exception) {
            }
            return should_show_rationale
        }
    }

}
package com.example.sample_kotlin

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import com.loplat.placeengine.Plengi

class LoplatSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this

        // loplatSdkConfiguration(): 백그라운드 동작과 위치 서비스 약관 동의, 마케팅 동의를 미리 받아 둔 사용자를 위해 반드시 필요
        loplatSdkConfiguration()
    }

    // init(), start()가 여러 번 호출되도 상관 없음
    fun loplatSdkConfiguration() {
        Log.d("LOGTAG/APPLICATION", "loplatSdkConfiguration")
        val context: Context = this
        val plengi = Plengi.getInstance(this)
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
            // 고객사에 발급한 로플랫 SDK client ID/PW 입력
            val clientId = "loplatdemo" // Test ID
            val clientSecret = "loplatdemokey" // Test PW
            plengi.listener = LoplatPlengiListener()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                plengi.setBackgroundLocationAccessDialogLayout(R.layout.dialog_background_location_info)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                plengi.setDefaultNotificationChannel(R.string.foreground_service_noti_channel_name, 0)
                plengi.setDefaultNotificationInfo(
                        R.drawable.ic_launcher,
                        0,
                        0)
            }
            Log.d("LOGTAG/APPLICATION", "init")
            plengi.init(clientId, clientSecret, getEchoCode(context))
            plengi.start()
        } else {
            Log.d("LOGTAG/APPLICATION", "stop")
            // 위치 서비스 약관 동의 거부한 user에 대해서 SDK stop
            plengi.stop()
        }
    }

    companion object {
        private var instance: LoplatSampleApplication? = null
        private val PREFS_NAME = LoplatSampleApplication::class.java.simpleName

        // or return instance.getApplicationContext();
        val context: Context?
            get() = instance
        // or return instance.getApplicationContext();

        // 마케팅 수신 동의 여부 저장
        fun setMarketingServiceAgreement(context: Context, agree: Boolean) {
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                val editor = settings.edit()
                editor.putBoolean("marketing_agreement", agree)
                editor.commit()
            } catch (e: Exception) {
            }
        }

        // 마케팅 수신 동의 여부 확인
        fun isMarketingServiceAgreed(context: Context): Boolean {
            var isMarketingServiceAgreed = false
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                isMarketingServiceAgreed = settings.getBoolean("marketing_agreement", false)
            } catch (e: Exception) {
            }
            return isMarketingServiceAgreed
        }

        // 위치 기반 서비스 약관 동의 여부 저장
        fun setLocationServiceAgreement(context: Context, agree: Boolean) {
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                val editor = settings.edit()
                editor.putBoolean("location_agreement", agree)
                editor.commit()
            } catch (e: Exception) {
            }
        }

        // 위치 기반 서비스 약관 동의 여부 확인
        fun isLocationServiceAgreed(context: Context): Boolean {
            var isLocationServiceAgreed = false
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                isLocationServiceAgreed = settings.getBoolean("location_agreement", false)
            } catch (e: Exception) {
            }
            return isLocationServiceAgreed
        }

        // 회원 번호 저장
        // 이메일, 전화번호와 같은 개인정보 제외
        fun setEchoCode(context: Context, member_code: String?) {
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                val editor = settings.edit()
                editor.putString("member_code", member_code)
                editor.commit()
            } catch (e: Exception) {
            }
        }

        // 저장된 회원 번호 가져옴
        fun getEchoCode(context: Context): String? {
            var echo_code: String? = null
            try {
                val settings = context.getSharedPreferences(PREFS_NAME, 0)
                echo_code = settings.getString("member_code", null)
            } catch (e: Exception) {
            }
            return echo_code
        }
    }
}
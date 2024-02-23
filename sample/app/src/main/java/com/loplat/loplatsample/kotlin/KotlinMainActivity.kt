package com.loplat.loplatsample.kotlin

import android.Manifest.permission.*
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR2
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.loplat.loplatsample.R
import com.loplat.loplatsample.kotlin.KotlinLoplatSampleApplication.Companion.getEchoCode
import com.loplat.loplatsample.kotlin.KotlinLoplatSampleApplication.Companion.getLocationShouldShowRationale
import com.loplat.loplatsample.kotlin.KotlinLoplatSampleApplication.Companion.isLocationServiceAgreed
import com.loplat.loplatsample.kotlin.KotlinLoplatSampleApplication.Companion.isMarketingServiceAgreed
import com.loplat.loplatsample.kotlin.KotlinLoplatSampleApplication.Companion.setEchoCode
import com.loplat.loplatsample.kotlin.KotlinLoplatSampleApplication.Companion.setLocationServiceAgreement
import com.loplat.loplatsample.kotlin.KotlinLoplatSampleApplication.Companion.setLocationShouldShowRationale
import com.loplat.loplatsample.kotlin.KotlinLoplatSampleApplication.Companion.setMarketingServiceAgreement
import com.loplat.placeengine.*
import kotlinx.android.synthetic.main.activity_main.*

class KotlinMainActivity : AppCompatActivity(), ConnectionCallbacks, OnConnectionFailedListener{
    var mSampleUIReceiver: BroadcastReceiver? = null
    var mProgressDialog: ProgressDialog? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var locationRequest = LocationRequest()
            .setPriority(PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS.toLong())
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS.toLong())

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (SDK_INT >= M) {
                if (checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                    locationPermissionGranted()
                } else if (checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_DENIED) {
                    locationPermissionDenied()
                }
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_WIFI_STATUS || requestCode == REQUEST_LOCATION_STATUS) {
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults[0] == PERMISSION_GRANTED) {
                locationPermissionGranted()
            } else if (grantResults[0] == PERMISSION_DENIED) {
                locationPermissionDenied()
            }
            setLocationShouldShowRationale(this, false)
        } else if (requestCode == REQUEST_POST_NOTIFICATION) {
            //Toast.makeText(this, "Notification is grandResult[${grantResults[0]}", Toast.LENGTH_SHORT).show()
        }
        else if (grantResults[0] == PERMISSION_GRANTED
                || grantResults[1] == PERMISSION_GRANTED) {
        }
    }

    private fun locationPermissionGranted() {
        toastMessage(getText(R.string.toast_location_message_agree))
        setLocationServiceAgreement(this, true)
        Plengi.getInstance(this).start()

        /**
         * loplat SDK는 위치 permission, GPS setting가 WiFi scan을 할 수 없더라도 start된 상태를 유지하고
         * 위치 permission, GPS setting에 따라 WiFi scan이 가능한 상황이 되면 실제 동작.
         * 앱에 로플랫 SDK 적용시 약관의 위치서비스 동의한 사용자에게 설정 변경을 쉽게 할 수 있도록 checkWiFiScanCondition 활용해주세요.
         */
        checkLocationScanCondition()
        tv_status.text = getText(R.string.sdk_started)
        switch_location.isChecked = true
    }

    private fun locationPermissionDenied() {
        toastMessage(getText(R.string.toast_location_message_revoke))
        setLocationServiceAgreement(this, false)
        Plengi.getInstance(this).stop()
        tv_status.text = getText(R.string.sdk_stopped)
        switch_location.isChecked = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * 로그인 성공 시점이라 가정
         * 로그인 후 1)위치서비스 약관 동의 여부, 2)마케팅 수신 동의 여부 3)회원번호(optional) 를 각각 저장
         * 저장된 3가지 정보는 Background 동작 시 사용
         */

        // 로그인 후 서버로 부터 받은 값을 local에 저장
        val memberCodeFromServer = "18497358207"

        // 테스트를 위해 간단하게 로컬에 저장한 값을 그대로 불러오는 과정. 실제로 각 유저들의 데이터는 서버로부터 받길 권장
        val isMarketingServiceAgreedFromServer = isMarketingServiceAgreed(this)
        val isLocationServiceAgreedFromServer = isLocationServiceAgreed(this)

        switch_marketing.isChecked = isMarketingServiceAgreedFromServer
        switch_location.isChecked = isLocationServiceAgreedFromServer

        /**
         * 앱 시작 혹은 로그인 할 때 마다 사용자의 위치약관동의 여부를 매번 확인해서 Loplat SDK start 호출 필수
         */
        if (isLocationServiceAgreedFromServer) {
            /**
             * 하기 코드는 회원번호를 사용하는 경우만 활용
             * 회원번호가 변경된 경우 저장
             */
            if (memberCodeFromServer != null && memberCodeFromServer != getEchoCode(this)) {
                /**
                 * echoCode에는 이메일, 전화번호와 같은 개인정보 반드시 제외
                 */
                setEchoCode(this, memberCodeFromServer)
            }
            // Loplat SDK start
            (applicationContext as KotlinLoplatSampleApplication).loplatSdkConfiguration()

            /**
             * 위치서비스를 동의한 사용자인 경우 wifi scan 환경을 확인하는 것 권장합니다.
             */
            checkLocationScanCondition()
        }

        switch_marketing.setOnClickListener { onMarketServiceAgreement() }
        switch_location.setOnClickListener { onLocationBasedServiceAgreement() }

        // LoplatPlengiListener로 부터 위치 인식 결과를 전달 받는 receiver 등록
        mSampleUIReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                toastMessage("SampleUIReceiver [$action]")
                if (action == "com.loplat.sample.response") {
                    try {
                        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                            mProgressDialog!!.dismiss()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val type = intent.getStringExtra("type") ?: return
                    println("mSampleUIReceiver: $type")
                    if (type == "error" || type == "placeevent") {
                        val response = intent.getStringExtra("response")
                        tv_result.text = response
                    }
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.loplat.sample.response")
        registerReceiver(mSampleUIReceiver, intentFilter)

        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), REQUEST_POST_NOTIFICATION)
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        if (mSampleUIReceiver != null) {
            //LocalBroadcastManager.getInstance(this).unregisterReceiver(mSampleUIReceiver!!)
            unregisterReceiver(mSampleUIReceiver!!)
            mSampleUIReceiver = null
        }
    }

    override fun onResume() {
        super.onResume()

        // Loplat SDK 동작 상태 확인
        val engineStatus = Plengi.getInstance(this).engineStatus
        if (engineStatus == PlaceEngineBase.EngineStatus.STARTED) {
            tv_status.text = getText(R.string.sdk_started)
        } else {
            tv_status.text = getText(R.string.sdk_stopped)
        }

        val currentPlaceStatus = Plengi.getInstance(this).currentPlaceStatus
        when (currentPlaceStatus) {
            PlengiResponse.PlaceStatus.MOVE -> {
            }
            PlengiResponse.PlaceStatus.STAY -> {
                val currentPlace = Plengi.getInstance(this).currentPlaceInfo
                currentPlace?.let {
                    val name = it.name
                    val tags = it.tags
                    val category = it.category
                    val floor = it.floor
                    val info = """
                        $name, $tags, $category, ${floor}F
                        
                        """.trimIndent()
                    tv_result.text = "" + info
                }
            }
        }

        val serverMode = Plengi.getInstance(this).testServerMode
        Toast.makeText(this, "ServerMode [${serverMode}]", Toast.LENGTH_SHORT).show()
    }

    /**
     * 현재 위치 확인 (테스트 시에만 사용 권장)
     */
    fun onRequestLocationInfo(view: View) {
        if (isLocationServiceAgreed(this)) {
            val result = Plengi.getInstance(this).TEST_refreshPlace_foreground(object : OnPlengiListener {
                override fun onSuccess(response: PlengiResponse) {
                    if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                        mProgressDialog!!.dismiss()
                    }
                    var description = ""
                    response.place?.let {
                        val name = it.name
                        val branch = if (it.tags == null) "" else it.tags
                        val floor = it.floor
                        val client_code = it.client_code
                        val accuracy = it.accuracy
                        val threshold = it.threshold
                        description = "[PLACE]$name:$branch, $floor, " +
                                "${String.format("%.3f", accuracy)}/${String.format("%.3f", threshold)}"

                        description += if (accuracy > threshold) {
                            " (In)"
                        } else {
                            " (Nearby)"
                        }

                        if (!client_code.isNullOrBlank()) {
                            description += ", client_code: $client_code"
                        }

                        description += "\n    "
                    }

                    response.area?.let {
                        description += "[${it.id}]${it.name},${it.tag}(${it.lat},${it.lng})"
                    }

                    response.complex?.let {
                        description += "[${it.id}]${it.name},${it.branch_name},${it.category}"
                    }

                    println(description)
                    tv_result.text = description

                    var intent = Intent()
                    intent.action = "com.loplat.sample.response"
                    intent.type = "placeevent"
                    intent.putExtra("response", response.toString())
                    sendBroadcast(intent)

                }

                override fun onFail(plengiResponse: PlengiResponse) {
                    if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                        mProgressDialog!!.dismiss()
                    }
                    if (plengiResponse.errorReason != null) {
                        tv_result.text = plengiResponse.errorReason
                    }
                }
            })
            if (result == PlengiResponse.Result.SUCCESS) {
                mProgressDialog = ProgressDialog(this)
                mProgressDialog!!.setMessage(getText(R.string.message_scanning_wifi))
                mProgressDialog!!.setCancelable(true)
                mProgressDialog!!.show()
            } else if (result == PlengiResponse.Result.FAIL_INTERNET_UNAVAILABLE) {
                toastMessage(getText(R.string.error_network))
            } else if (result == PlengiResponse.Result.FAIL_WIFI_SCAN_UNAVAILABLE) {
                toastMessage(getText(R.string.error_wifi_scan))
                checkLocationScanCondition()
            } else {
                toastMessage(getText(R.string.error_sdk))
            }
        } else {
            toastMessage(getText(R.string.error_location_services))
        }
    }

    /**
     * 마케팅 서비스 동의에 따른 Loplat SDK 설정 (Loplat X Campaigns)
     */
    private fun onMarketServiceAgreement() {
        var message: String
        val builder = AlertDialog.Builder(this)

        if (switch_marketing.isChecked) {
            message = getText(R.string.request_marketing_message_agree) as String
            builder.setPositiveButton(getText(R.string.confirm)) { dialog, which ->
                // 직접 광고(푸시 메세지) 하는 경우
                Plengi.getInstance(this).enableAdNetwork(true, false)

                // loplat SDK의 (푸시 메세지) 활용하는 경우
                // Plengi.getInstance(getApplicationContext()).enableAdNetwork(true);
                // Plengi.getInstance(getApplicationContext()).setAdNotiLargeIcon(R.drawable.ic_launcher);
                // Plengi.getInstance(getApplicationContext()).setAdNotiSmallIcon(R.drawable.ic_launcher);

                // 앱 내 flag 저장
                setMarketingServiceAgreement(this, true)
                toastMessage(getText(R.string.toast_marketing_message_argee))
                switch_marketing.isChecked = true
            }
        } else {
            message = getText(R.string.request_marketing_message_revoke) as String
            builder.setPositiveButton(getText(R.string.confirm)) { dialog, which ->
                Plengi.getInstance(this).enableAdNetwork(false)
                // 앱 내 flag 저장
                setMarketingServiceAgreement(this, false)
                toastMessage(getText(R.string.toast_marketing_message_revoke))
                switch_marketing!!.isChecked = false
            }
        }
        builder.setTitle(getText(R.string.marketing_services_title))
        builder.setMessage(message)
        builder.setNegativeButton(getText(R.string.cancel)) { dialog, which ->
            switch_marketing.isChecked = !switch_marketing.isChecked
        }
        builder.show()
    }

    /**
     * 위치 기반 서비스 동의에 따른 Loplat SDK 동작
     */
    private fun onLocationBasedServiceAgreement() {
        if (switch_location.isChecked) {
            if (isGrantedLocationPermission()) {
                locationPermissionGranted()
            } else {
                if (checkLocationShouldShowRationale()) {
                    if (SDK_INT >= M) {
                        requestPermissions(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
                    }
                } else {
                    switch_location.isChecked = false
                    showSettingDialog()
                }
            }
        } else {
            AlertDialog.Builder(this)
                    .setTitle(getText(R.string.revoke_location_service))
                    .setMessage(getText(R.string.request_revoke_location_service))
                    .setPositiveButton(getText(R.string.confirm)) { dialog, which ->
                        Plengi.getInstance(this).stop()
                        setLocationServiceAgreement(this, false)
                    }
                    .setNegativeButton(getText(R.string.cancel)) { dialog, which ->
                        switch_location.setChecked(!switch_location.isChecked())
                    }
                    .show()
        }
    }

    /**
     * 위치 인식을 할 수 있는 상태인지 확인
     */
    private fun checkLocationScanCondition(): Boolean {
        var available = true
        if (!checkLocationPermissionIfNeeded()) {
            // 위치 권한 상태 확인

            available = false
            toastMessage(getText(R.string.request_location_permission))
        } else if (!checkGpsStatus()) {
            // GPS 활성화 상태 확인

            available = false
            toastMessage(getText(R.string.request_gps))

            /* GPS 켜는 방법은 google play service를 통해 앱내에서 직접 설정, GPS 설정화면 이용 등 2가지 방법이 있음
               현재 sample code에서는 google play service를 이용하는 방법으로 작성되었음
               google-play-service를 사용하지 않고 GPS 설정화면을 넘기는 경우에는 아래 주석처리된 코드 참조 바람

               google play service(location) 사용하는 경우 -> dependency google-play-services 선언이 필요함
               compile 'com.google.android.gms:play-services:[latest version]'
            */
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS) {
                if (isGoogleClientConnected) {
                    turnGpsOnByGooglePlayService()
                } else {
                    connectGoogleClient()
                }
            }

            // GPS 설정화면을 이용하는 방법
            /*Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);*/

        } else if (!checkWifiScanIsAvailable()) {
            // Wi-Fi 스캔 가능 여부 확인

            available = false
            /**
             * 안드로이드 4.3 이상 버전은 background wifi scan 설정이 켜져있으면 됨
             * 확인 방법
             * 1. 삼성 계열 폰:
             * - [설정] -> [연결] -> [위치] -> [정확도 향상] -> WiFi 찾기 On
             * - [설정] -> [개인정보 보호 및 안전] -> [위치] -> [정확도 향상] -> WiFi 찾기 On
             * 2. LG 계열 폰: [설정] -> [잠금화면 및 보안] -> [프라이버시, 위치정보] -> [고급 검색] -> WiFi 찾기 On
             * 공장 초기화 기준 해당 옵션의 default 값은 On 입니다
             * [참고: https://developer.android.com/reference/android/net/wifi/WifiManager.html#isScanAlwaysAvailable()]
             */
            if (SDK_INT >= JELLY_BEAN_MR2) {
                val intent = Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE)
                startActivityForResult(intent, REQUEST_WIFI_STATUS)
            } else {
                // 안드로이드 4.2 이하 버전은 WiFi가 켜져있어야 wifi scanning 가능함
                val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                wifiManager.isWifiEnabled = true
            }
        }
        return available
    }

    /**
     * 위치 기능 활성화 요청
     * [참고: https://developer.android.com/training/location/change-location-settings]
     */
    private fun turnGpsOnByGooglePlayService() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        if (mGoogleApiClient == null) {
            Toast.makeText(this, "GoogleApiClient is Null", Toast.LENGTH_SHORT).show()
        }
        val result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient!!, builder.build())
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(this, REQUEST_LOCATION_STATUS)
                } catch (e: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    /**
     * Wi-Fi 스캔 가능 여부 확인
     */
    private fun checkWifiScanIsAvailable(): Boolean {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        var wifiScanEnabled = false

        if (SDK_INT >= JELLY_BEAN_MR2) {
            wifiScanEnabled = wifiManager.isScanAlwaysAvailable
        }
        return wifiManager.isWifiEnabled || wifiScanEnabled
    }

    /**
     * GPS 활성화 여부 확인
     */
    private fun checkGpsStatus(): Boolean {
        if (SDK_INT >= M) {
            val locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
            val isNetworkEnabled = locationManager.isProviderEnabled(NETWORK_PROVIDER)
            val isGPSEnabled = locationManager.isProviderEnabled(GPS_PROVIDER)
            if (!isNetworkEnabled && !isGPSEnabled) {
                return false
            }
        }
        return true
    }

    /**
     * 위치 권한 교육용 UI 출력 필요 여부 확인
     */
    private fun checkLocationShouldShowRationale(): Boolean {
        return (getLocationShouldShowRationale(this)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION))
    }

    /**
     * 위치 권한 활성화 여부 확인
     */
    private fun isGrantedLocationPermission(): Boolean {
        return if (SDK_INT >= M) {
            (checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
                    || checkSelfPermission(ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED)
        } else true
    }

    /**
     * 위치 권한 활성화 여부 확인 후 거부 상태라면 상태에 따라 처리
     */
    private fun checkLocationPermissionIfNeeded(): Boolean {
        if (SDK_INT >= M) {
            return if (isGrantedLocationPermission()) {
                true
            } else {
                if (checkLocationShouldShowRationale()) {
                    // 앱 내 권한 요청
                    requestPermissions(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, POST_NOTIFICATIONS), REQUEST_LOCATION_PERMISSION)
                } else {
                    // 앱의 권한 설정 화면으로 안내
                    showSettingDialog()
                }
                false
            }
        }
        return true
    }

    /**
     * 앱의 권한 설정 화면으로 안내
     */
    private fun showSettingDialog() {
        AlertDialog.Builder(this)
                .setMessage(getText(R.string.request_revoked_permission))
                .setPositiveButton(getText(R.string.setting)) { dialog, which ->
                    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                    val data = Uri.fromParts("package", this.packageName, null)
                    intent.data = data
                    startActivity(intent)
                }
                .setNegativeButton(getText(R.string.cancel), null)
                .show()
    }

    private fun connectGoogleClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
        }
        mGoogleApiClient!!.connect()
    }

    private val isGoogleClientConnected: Boolean
        get() {
            if (mGoogleApiClient != null) {
                return mGoogleApiClient!!.isConnected || mGoogleApiClient!!.isConnecting
            }
            return false
        }

    private fun toastMessage(message: CharSequence) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onConnected(bundle: Bundle?) {
        // GPS 활성화
        turnGpsOnByGooglePlayService()
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    companion object {
        private const val UPDATE_INTERVAL_MS = 1000 // 1초
        private const val FASTEST_UPDATE_INTERVAL_MS = 500 // 0.5초
        private const val REQUEST_LOCATION_PERMISSION = 10000
        private const val REQUEST_LOCATION_STATUS = 10001
        private const val REQUEST_WIFI_STATUS = 10002
        private const val REQUEST_POST_NOTIFICATION = 2000
    }

}
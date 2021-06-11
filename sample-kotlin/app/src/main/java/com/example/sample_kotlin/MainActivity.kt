package com.example.sample_kotlin

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.loplat.placeengine.*

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    var mSampleUIReceiver: BroadcastReceiver? = null
    var mProgressDialog: ProgressDialog? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var locationRequest: LocationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS.toLong())
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS.toLong())

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_WIFI_STATUS || requestCode == REQUEST_LOCATION_STATUS) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val context: Context = this

        /**
         * 로그인 성공 시점이라 가정
         * 로그인 후 1)위치서비스 약관 동의 여부, 2)마케팅 수신 동의 여부 3)회원번호(optional) 를 각각 저장
         * 저장된 3가지 정보는 Background 동작 시 사용
         */

        // 로그인 후 서버로 부터 받은 값을 local에 저장
        val memberCodeFromServer = "18497358207"
        val isMarketingServiceAgreedFromServer = true
        val isLocationServiceAgreedFromServer = true
        LoplatSampleApplication.setMarketingServiceAgreement(
            context,
            isMarketingServiceAgreedFromServer
        )
        LoplatSampleApplication.setLocationServiceAgreement(
            context,
            isLocationServiceAgreedFromServer
        )
        if (isLocationServiceAgreedFromServer) {
            /**
             * 하기 코드는 회원번호를 사용하는 경우만 활용
             * 회원번호가 변경된 경우 저장
             */
            if (memberCodeFromServer != null
                    && memberCodeFromServer != LoplatSampleApplication.getEchoCode(context)) {
                LoplatSampleApplication.setEchoCode(context, memberCodeFromServer)
            }
            (applicationContext as LoplatSampleApplication).loplatSdkConfiguration()
            /**
             * 위치서비스를 동의한 사용자인 경우 wifi scan 환경을 확인하는 것 권장합니다.
             */
            checkWiFiScanCondition()
        }
        val tv_result = findViewById<View>(R.id.tv_result) as TextView

        // receive response from loplat listener
        mSampleUIReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
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
        LocalBroadcastManager.getInstance(this).registerReceiver(mSampleUIReceiver as BroadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mSampleUIReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mSampleUIReceiver!!)
            mSampleUIReceiver = null
        }
    }

    override fun onResume() {
        super.onResume()
        val tv_status = findViewById<View>(R.id.tv_status) as TextView
        val tv_result = findViewById<View>(R.id.tv_result) as TextView

        //Monitoring 상태 확인하기
        val engineStatus: Int = Plengi.getInstance(this).engineStatus
        if (engineStatus == PlaceEngineBase.EngineStatus.STARTED) {
            tv_status.text = "SDK Started"
        } else {
            tv_status.text = "SDK Stopped"
        }
        val currentPlaceStatus: Int = Plengi.getInstance(this).getCurrentPlaceStatus()
        when (currentPlaceStatus) {
            PlengiResponse.PlaceStatus.MOVE -> {
            }
            PlengiResponse.PlaceStatus.STAY -> {
                val currentPlace: PlengiResponse.Place =
                    Plengi.getInstance(this).getCurrentPlaceInfo()
                if (currentPlace != null) {
                    val name: String = currentPlace.name
                    val tags: String? = currentPlace.tags
                    val category: String = currentPlace.category
                    val floor: Int = currentPlace.floor
                    val info = """
                        $name, $tags, $category, ${floor}F
                        
                        """.trimIndent()
                    tv_result.text = "" + info
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        }
    }

    fun onRequestLocationInfo(view: View?) {
        // request location to loplat engine
        val tv_result = findViewById<View>(R.id.tv_result) as TextView
        val result: Int = Plengi.getInstance(this).TEST_refreshPlace_foreground(object :
            OnPlengiListener {
            override fun onSuccess(response: PlengiResponse) {
                if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                    mProgressDialog!!.dismiss()
                }
                var description = ""
                if (response.place != null) {
                    val name: String = response.place.name // detected place name
                    val branch = if (response.place.tags == null) "" else response.place.tags
                    val floor: Int = response.place.floor // detected place's floor info
                    val client_code: String? = response.place.client_code // client_code
                    val accuracy: Float = response.place.accuracy
                    val threshold: Float = response.place.threshold
                    description =
                        "[PLACE]" + name + ": " + branch + ", " + floor + ", " + String.format(
                            "%.3f",
                            accuracy
                        ) + "/" + String.format("%.3f", threshold)
                    description += if (accuracy > threshold) {
                        // device is within the detected place
                        " (In)"
                    } else {
                        // device is outside the detected place
                        " (Nearby)"
                    }
                    if (client_code != null && !client_code.isEmpty()) {
                        description += ", client_code: $client_code"
                    }
                }
                if (response.area != null) {
                    if (response.place != null) {
                        description += "\n    "
                    }
                    description += ("[" + response.area.id.toString() + "]" + response.area.name.toString() + ","
                            + response.area.tag.toString() + "(" + response.area.lat.toString() + "," + response.area.lng.toString() + ")")
                }
                if (response.complex != null) {
                    if (response.place != null) {
                        description += "\n   "
                    }
                    description += ("[" + response.complex.id.toString() + "]" + response.complex.name.toString() + ","
                            + response.complex.branch_name.toString()
                            + "," + response.complex.category)
                }
                println(description)
                tv_result.text = description
            }

            override fun onFail(plengiResponse: PlengiResponse) {
                if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                    mProgressDialog!!.dismiss()
                }
                if (plengiResponse.errorReason != null) {
                    tv_result.setText(plengiResponse.errorReason)
                }
            }
        })
        when (result) {
            PlengiResponse.Result.SUCCESS -> {
                mProgressDialog = ProgressDialog(this@MainActivity)
                mProgressDialog!!.setMessage("I'm scanning wifi. Please wait...")
                mProgressDialog!!.setCancelable(true)
                mProgressDialog!!.show()
            }
            PlengiResponse.Result.FAIL_INTERNET_UNAVAILABLE -> {
                // internet is not connected
            }
            PlengiResponse.Result.FAIL_WIFI_SCAN_UNAVAILABLE -> {
                // wifi scan is not available
                checkWiFiScanCondition()
            }
        }
    }

    /**
     * Note: 아래의 코드는 loplat X 사용을 이용한 마케팅 동의 서비스 예제입니다.
     * @param view
     */
    fun onMarketServiceAgreement(view: View?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("마케팅 서비스 동의?")
                .setMessage("마케팅 서비스 동의 하시겠습니까?")
                .setPositiveButton("Yes") { dialog, which -> // 직접 광고(푸시 메세지) 하는 경우
                    Plengi.getInstance(this@MainActivity).enableAdNetwork(true, false)
                    // loplat SDK의 (푸시 메세지) 활용하는 경우
                    // Plengi.getInstance(getApplicationContext()).enableAdNetwork(true);
                    // Plengi.getInstance(getApplicationContext()).setAdNotiLargeIcon(R.drawable.ic_launcher);
                    // Plengi.getInstance(getApplicationContext()).setAdNotiSmallIcon(R.drawable.ic_launcher);

                    // 앱 내 flag 저장
                    LoplatSampleApplication.setMarketingServiceAgreement(this@MainActivity, true)
                    Toast.makeText(applicationContext, "푸시 알림 마케팅 수신에 동의 하였습니다", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") { dialog, which -> // loplat X service off
                    Plengi.getInstance(this@MainActivity).enableAdNetwork(false)
                    // 앱 내 flag 저장
                    LoplatSampleApplication.setMarketingServiceAgreement(this@MainActivity, false)
                    Toast.makeText(applicationContext, "푸시 알림 마케팅 수신을 취소 하였습니다", Toast.LENGTH_SHORT).show()
                }
        builder.show()
    }

    /**
     * Note: 아래의 코드는 loplat SDK 구동(start)를 이용한 위치기반 서비스 예제입니다.
     * @param view
     */
    fun onLocationBasedServiceAgreement(view: View?) {
        val tv_status = findViewById<View>(R.id.tv_status) as TextView
        val builder = AlertDialog.Builder(this)
        builder.setTitle("위치기반 서비스 동의?")
                .setMessage("위치기반 서비스 하시겠습니까?")
                .setPositiveButton("Yes") { dialog, which ->
                    Toast.makeText(
                        applicationContext,
                        "loplat 위치 기반 서비스 이용에 동의 하였습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    LoplatSampleApplication.setLocationServiceAgreement(this@MainActivity, true)
                    // loplat sdk init
                    (applicationContext as LoplatSampleApplication).loplatSdkConfiguration()
                    tv_status.text = "SDK Started"
                    /**
                     * loplat SDK는 위치 permission, GPS setting가 WiFi scan을 할 수 없더라도 start된 상태를 유지하고
                     * 위치 permission, GPS setting에 따라 WiFi scan이 가능한 상황이 되면 실제 동작.
                     * 앱에 로플랫 SDK 적용시 약관의 위치서비스 동의한 사용자에게 설정 변경을 쉽게 할 수 있도록 checkWiFiScanCondition 활용해주세요.
                     */
                    /**
                     * loplat SDK는 위치 permission, GPS setting가 WiFi scan을 할 수 없더라도 start된 상태를 유지하고
                     * 위치 permission, GPS setting에 따라 WiFi scan이 가능한 상황이 되면 실제 동작.
                     * 앱에 로플랫 SDK 적용시 약관의 위치서비스 동의한 사용자에게 설정 변경을 쉽게 할 수 있도록 checkWiFiScanCondition 활용해주세요.
                     */
                    checkWiFiScanCondition()
                }
                .setNegativeButton("No") { dialog, which -> // loplat SDK stop, 동의 거부
                    Toast.makeText(
                        applicationContext,
                        "loplat 위치 기반 서비스 이용을 취소 하였습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    LoplatSampleApplication.setLocationServiceAgreement(this@MainActivity, false)
                    (applicationContext as LoplatSampleApplication).loplatSdkConfiguration()
                    tv_status.text = "SDK Stopped"
                }
        builder.show()
    }

    // WiFi Scan을 할 수 있는 상태인지 체크 하는 샘플 코드
    private fun checkWiFiScanCondition(): Boolean {
        var available = true
        if (!checkLocationPermissionIfNeeded()) {
            available = false
            Toast.makeText(this, "Please grant a location permission", Toast.LENGTH_SHORT).show()
        } else if (!checkGpsStatus()) {
            available = false
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show()

            /* GPS 켜는 방법은 google play service를 통해 앱내에서 직접 설정, GPS 설정화면 이용 등 2가지 방법이 있음
               현재 sample code에서는 google play service를 이용하는 방법으로 작성되었음
               google-play-service를 사용하지 않고 GPS 설정화면을 넘기는 경우에는 아래 주석처리된 코드 참조 바람

               google play service(location) 사용하는 경우 -> dependency google-play-services 선언이 필요함
               compile 'com.google.android.gms:play-services:[latest version]'
            */if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext) === ConnectionResult.SUCCESS) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
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

    private fun turnGpsOnByGooglePlayService() {
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> = LocationServices.SettingsApi.checkLocationSettings(
            mGoogleApiClient,
            builder.build()
        )
        result.setResultCallback { result ->
            val status: Status = result.getStatus()
            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(this@MainActivity, REQUEST_LOCATION_STATUS)
                } catch (e: SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun checkWifiScanIsAvailable(): Boolean {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        var wifiScanEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            wifiScanEnabled = wifiManager.isScanAlwaysAvailable
        }
        return wifiManager.isWifiEnabled || wifiScanEnabled
    }

    // check if gps is on
    private fun checkGpsStatus(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!isNetworkEnabled && !isGPSEnabled) {
                //
                // please turn on location settings
                return false
            }
        }
        return true
    }

    // check if a location permission allowed
    private fun checkLocationPermissionIfNeeded(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                        || shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)) {
                    //shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ) {
                    // Explain to the user why we need to write the permission.
                    //Toast.makeText(this, "Accept Permission", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ), REQUEST_LOCATION_PERMISSION
                )
                // MY_PERMISSION is an
                // app-defined int constant
                //}
                return false
            }
        }
        return true
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
        private get() {
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient!!.isConnected || mGoogleApiClient!!.isConnecting) {
                    return true
                }
            }
            return false
        }

    override fun onConnected(bundle: Bundle?) {
        // GPS 활성화
        turnGpsOnByGooglePlayService()
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    companion object {
        private val PREFS_NAME = MainActivity::class.java.simpleName
        private val TAG = PREFS_NAME
        private const val UPDATE_INTERVAL_MS = 1000 // 1초
        private const val FASTEST_UPDATE_INTERVAL_MS = 500 // 0.5초
        private const val REQUEST_LOCATION_PERMISSION = 10000
        private const val REQUEST_LOCATION_STATUS = 10001
        private const val REQUEST_WIFI_STATUS = 10002
    }
}

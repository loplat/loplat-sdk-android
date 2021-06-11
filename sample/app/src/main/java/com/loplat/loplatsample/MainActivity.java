package com.loplat.loplatsample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.loplat.placeengine.OnPlengiListener;
import com.loplat.placeengine.PlaceEngineBase;
import com.loplat.placeengine.Plengi;
import com.loplat.placeengine.PlengiResponse;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    BroadcastReceiver mSampleUIReceiver;
    ProgressDialog mProgressDialog=null;

    private TextView tv_status;
    private TextView tv_result;
    private Switch switchMarketing;
    private Switch switchLocation;

    private static final String PREFS_NAME = MainActivity.class.getSimpleName();
    private static final String TAG = PREFS_NAME;

    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    private static final int REQUEST_LOCATION_PERMISSION = 10000;
    private static final int REQUEST_LOCATION_STATUS = 10001;
    private static final int REQUEST_WIFI_STATUS = 10002;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 10003;

    GoogleApiClient mGoogleApiClient;

    LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_LOCATION_PERMISSION){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationPermissionGranted();
                }else if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
                    locationPermissionDenied();
                }
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_WIFI_STATUS || requestCode == REQUEST_LOCATION_STATUS) {
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_LOCATION_PERMISSION){
            Log.d("LOGTAG/grantResults[0]", String.valueOf(grantResults[0]));
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                locationPermissionGranted();
            }else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                locationPermissionDenied();
            }
        }else if(grantResults[0] == PackageManager.PERMISSION_GRANTED
                || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        }
    }

    private void locationPermissionGranted(){

        Toast.makeText(getApplicationContext(), "loplat 위치 기반 서비스 이용에 동의 하였습니다", Toast.LENGTH_SHORT).show();
        LoplatSampleApplication.setLocationServiceAgreement(MainActivity.this, true);
        // loplat sdk init
        ((LoplatSampleApplication)getApplicationContext()).loplatSdkConfiguration();

        /**
         * loplat SDK는 위치 permission, GPS setting가 WiFi scan을 할 수 없더라도 start된 상태를 유지하고
         * 위치 permission, GPS setting에 따라 WiFi scan이 가능한 상황이 되면 실제 동작.
         * 앱에 로플랫 SDK 적용시 약관의 위치서비스 동의한 사용자에게 설정 변경을 쉽게 할 수 있도록 checkWiFiScanCondition 활용해주세요.
         */
        checkWiFiScanCondition();
        tv_status.setText("SDK Started");
        switchLocation.setChecked(true);
    }

    private void locationPermissionDenied(){
        // loplat SDK stop, 동의 거부
        Toast.makeText(getApplicationContext(), "loplat 위치 기반 서비스 이용을 취소 하였습니다", Toast.LENGTH_SHORT).show();
        LoplatSampleApplication.setLocationServiceAgreement(MainActivity.this, false);
        ((LoplatSampleApplication)getApplicationContext()).loplatSdkConfiguration();
        tv_status.setText("SDK Stopped");
        switchLocation.setChecked(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = this;
        /**
         * 로그인 성공 시점이라 가정
         * 로그인 후 1)위치서비스 약관 동의 여부, 2)마케팅 수신 동의 여부 3)회원번호(optional) 를 각각 저장
         * 저장된 3가지 정보는 Background 동작 시 사용
         */

        // 로그인 후 서버로 부터 받은 값을 local에 저장
        String memberCodeFromServer = "18497358207";
        boolean isMarketingServiceAgreedFromServer = false;
        boolean isLocationServiceAgreedFromServer = false;

        LoplatSampleApplication.setMarketingServiceAgreement(context, isMarketingServiceAgreedFromServer);
        LoplatSampleApplication.setLocationServiceAgreement(context, isLocationServiceAgreedFromServer);
        if (isLocationServiceAgreedFromServer) {
            /**
             * 하기 코드는 회원번호를 사용하는 경우만 활용
             * 회원번호가 변경된 경우 저장
             */
            if (memberCodeFromServer != null
                    && !memberCodeFromServer.equals(LoplatSampleApplication.getEchoCode(context))) {
                LoplatSampleApplication.setEchoCode(context, memberCodeFromServer);
            }
            ((LoplatSampleApplication) getApplicationContext()).loplatSdkConfiguration();
            /**
             * 위치서비스를 동의한 사용자인 경우 wifi scan 환경을 확인하는 것 권장합니다.
             */
            checkWiFiScanCondition();
        }

        tv_status = findViewById(R.id.tv_status);
        tv_result = findViewById(R.id.tv_result);
        switchMarketing = (Switch)findViewById(R.id.switch_marketing);
        switchLocation = (Switch)findViewById(R.id.switch_location);

        switchMarketing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMarketServiceAgreement();
            }
        });

        switchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLocationBasedServiceAgreement();
            }
        });

        // receive response from loplat listener
        mSampleUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals("com.loplat.sample.response")) {
                    try {
                        if (mProgressDialog!=null&&mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                    catch ( Exception e ) {
                        e.printStackTrace();
                    }

                    String type = intent.getStringExtra("type");
                    if(type == null) {
                        return;
                    }

                    System.out.println("mSampleUIReceiver: " + type);

                    if (type.equals("error") || type.equals("placeevent")) {
                        String response = intent.getStringExtra("response");
                        tv_result.setText(response);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.loplat.sample.response");
        LocalBroadcastManager.getInstance(this).registerReceiver(mSampleUIReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSampleUIReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mSampleUIReceiver);
            mSampleUIReceiver = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LOGTAG/","onResume");
        final TextView tv_status = (TextView)findViewById(R.id.tv_status);
        final TextView tv_result = (TextView)findViewById(R.id.tv_result);

        //Monitoring 상태 확인하기
        int engineStatus = Plengi.getInstance(this).getEngineStatus();
        if(engineStatus == PlaceEngineBase.EngineStatus.STARTED)
        {
            tv_status.setText("SDK Started");
        } else {
            tv_status.setText("SDK Stopped");
        }
        int currentPlaceStatus = Plengi.getInstance(this).getCurrentPlaceStatus();

        switch (currentPlaceStatus) {
            case PlengiResponse.PlaceStatus.MOVE:
                break;

            case PlengiResponse.PlaceStatus.STAY:

                PlengiResponse.Place currentPlace = Plengi.getInstance(this).getCurrentPlaceInfo();
                if(currentPlace != null)
                {
                    String name = currentPlace.name;
                    String tags = currentPlace.tags;
                    String category = currentPlace.category;
                    int floor = currentPlace.floor;
                    String info = name + ", " + tags + ", " + category + ", " + floor + "F\n";
                    tv_result.setText("" + info);
                }
                break;
        }
    }

    public void onRequestLocationInfo(View view) {
        // request location to loplat engine
        Log.d("LOGTAG/onRequest", String.valueOf(LoplatSampleApplication.isLocationServiceAgreed(this)));
        if(LoplatSampleApplication.isLocationServiceAgreed(this)){
            Log.d("LOGTAG/onRequestLocationInfo", "start");
            int result = Plengi.getInstance(this).TEST_refreshPlace_foreground(new OnPlengiListener() {
                @Override
                public void onSuccess(PlengiResponse response) {
                    Log.d("LOGTAG/onRequestLocationInfo", "onSuccess");
                    if (mProgressDialog!=null&&mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }

                    String description = "";
                    if (response.place != null) {
                        String name = response.place.name;  // detected place name
                        String branch = (response.place.tags == null) ? "": response.place.tags;
                        int floor = response.place.floor;   // detected place's floor info
                        String client_code = response.place.client_code;    // client_code

                        float accuracy = response.place.accuracy;
                        float threshold = response.place.threshold;

                        description = "[PLACE]"+ name + ": " + branch + ", " + floor + ", " +
                                String.format("%.3f", accuracy) + "/" + String.format("%.3f", threshold);

                        if(accuracy > threshold) {
                            // device is within the detected place
                            description += " (In)";
                        } else {
                            // device is outside the detected place
                            description += " (Nearby)";
                        }

                        if(client_code != null && !client_code.isEmpty()) {
                            description += ", client_code: " + client_code;
                        }
                    }

                    if (response.area != null) {
                        if (response.place != null) {
                            description += "\n    ";
                        }
                        description += "[" + response.area.id + "]" + response.area.name + ","
                                + response.area.tag + "(" + response.area.lat + "," + response.area.lng + ")";
                    }

                    if (response.complex != null) {
                        if (response.place != null) {
                            description += "\n   ";
                        }
                        description += "[" + response.complex.id + "]" + response.complex.name + ","
                                + response.complex.branch_name + "," + response.complex.category;
                    }

                    System.out.println(description);
                    tv_result.setText(description);
                }

                @Override
                public void onFail(PlengiResponse plengiResponse) {
                    Log.d("LOGTAG/onRequestLocationInfo", "onFail");
                    if (mProgressDialog!=null&&mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    if (plengiResponse.errorReason != null) {
                        tv_result.setText(plengiResponse.errorReason);
                    }
                }
            });

            Log.d("LOGTAG/onRequestLocationInfo", "check1");
            if(result == PlengiResponse.Result.SUCCESS) {
                Log.d("LOGTAG/onRequestLocationInfo", "check2");
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage("I'm scanning wifi. Please wait...");
                mProgressDialog.setCancelable(true);
                mProgressDialog.show();
            }
            else if(result == PlengiResponse.Result.FAIL_INTERNET_UNAVAILABLE) {
                Log.d("LOGTAG/onRequestLocationInfo", "check3");
                // internet is not connected
            }
            else if(result == PlengiResponse.Result.FAIL_WIFI_SCAN_UNAVAILABLE) {
                Log.d("LOGTAG/onRequestLocationInfo", "check4");
                // wifi scan is not available
                checkWiFiScanCondition();
            }else{
                Log.d("LOGTAG/onRequestLocationInfo", String.valueOf(result));
            }
            Log.d("LOGTAG/onRequestLocationInfo", "check5");
        }else{
            Toast.makeText(getApplicationContext(), "loplat 위치 기반 서비스 이용에 동의 해야합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Note: 아래의 코드는 loplat X 사용을 이용한 마케팅 동의 서비스 예제입니다.
     */

    public void onMarketServiceAgreement() {
        String title = "마케팅 서비스 동의";
        String message = "마케팅 서비스에";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(switchMarketing.isChecked()){
            message = "마케팅 서비스 동의 하시겠습니까?";
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 직접 광고(푸시 메세지) 하는 경우
                    Plengi.getInstance(MainActivity.this).enableAdNetwork(true, false);
                    // loplat SDK의 (푸시 메세지) 활용하는 경우
                    // Plengi.getInstance(getApplicationContext()).enableAdNetwork(true);
                    // Plengi.getInstance(getApplicationContext()).setAdNotiLargeIcon(R.drawable.ic_launcher);
                    // Plengi.getInstance(getApplicationContext()).setAdNotiSmallIcon(R.drawable.ic_launcher);

                    // 앱 내 flag 저장
                    LoplatSampleApplication.setMarketingServiceAgreement(MainActivity.this, true);
                    Toast.makeText(getApplicationContext(), "푸시 알림 마케팅 수신에 동의 하였습니다", Toast.LENGTH_SHORT).show();
                    switchMarketing.setChecked(true);
                }
            });
        }else{
            message = "마케팅 서비스 동의를 취소하시겠습니까?";
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Plengi.getInstance(MainActivity.this).enableAdNetwork(false);
                    // 앱 내 flag 저장
                    LoplatSampleApplication.setMarketingServiceAgreement(MainActivity.this, false);
                    Toast.makeText(getApplicationContext(), "푸시 알림 마케팅 수신을 취소 하였습니다", Toast.LENGTH_SHORT).show();
                    switchMarketing.setChecked(false);
                }
            });
        }
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switchMarketing.setChecked(!switchMarketing.isChecked());
            }
        });
        builder.show();
    }

    /**
     * Note: 아래의 코드는 loplat SDK 구동(start)를 이용한 위치기반 서비스 예제입니다.
     */
    public void onLocationBasedServiceAgreement() {
        if(switchLocation.isChecked()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }else{

            }
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("위치 기반 서비스 동의 취소");
            builder.setMessage("위치 기반 서비스 동의를 취소하시겠습니까?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_LOCATION_PERMISSION);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switchLocation.setChecked(!switchMarketing.isChecked());
                }
            });
            builder.show();
        }
    }

    // WiFi Scan을 할 수 있는 상태인지 체크 하는 샘플 코드
    private boolean checkWiFiScanCondition() {
        boolean available = true;
        if (!checkLocationPermissionIfNeeded()) {
            available = false;
            Toast.makeText(this, "Please grant a location permission", Toast.LENGTH_SHORT).show();
        } else if (!checkGpsStatus()) {
            available = false;
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();

            /* GPS 켜는 방법은 google play service를 통해 앱내에서 직접 설정, GPS 설정화면 이용 등 2가지 방법이 있음
               현재 sample code에서는 google play service를 이용하는 방법으로 작성되었음
               google-play-service를 사용하지 않고 GPS 설정화면을 넘기는 경우에는 아래 주석처리된 코드 참조 바람

               google play service(location) 사용하는 경우 -> dependency google-play-services 선언이 필요함
               compile 'com.google.android.gms:play-services:[latest version]'
            */
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS)
            {
                if (isGoogleClientConnected()) {
                    turnGpsOnByGooglePlayService();
                } else {
                    connectGoogleClient();
                }
            }

            // GPS 설정화면을 이용하는 방법
            /*Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);*/

        } else if (!checkWifiScanIsAvailable()) {
            available = false;
            /**
             * 안드로이드 4.3 이상 버전은 background wifi scan 설정이 켜져있으면 됨
             * 확인 방법
             * 1. 삼성 계열 폰:
             *  - [설정] -> [연결] -> [위치] -> [정확도 향상] -> WiFi 찾기 On
             *  - [설정] -> [개인정보 보호 및 안전] -> [위치] -> [정확도 향상] -> WiFi 찾기 On
             * 2. LG 계열 폰: [설정] -> [잠금화면 및 보안] -> [프라이버시, 위치정보] -> [고급 검색] -> WiFi 찾기 On
             * 공장 초기화 기준 해당 옵션의 default 값은 On 입니다
             * [참고: https://developer.android.com/reference/android/net/wifi/WifiManager.html#isScanAlwaysAvailable()]
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                startActivityForResult(intent, REQUEST_WIFI_STATUS);
            } else {
                // 안드로이드 4.2 이하 버전은 WiFi가 켜져있어야 wifi scanning 가능함
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
            }
        }
        return available;
    }

    private void turnGpsOnByGooglePlayService(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION_STATUS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }
        });
    }

    private boolean checkWifiScanIsAvailable() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        boolean wifiScanEnabled = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            wifiScanEnabled = wifiManager.isScanAlwaysAvailable();
        }

        if(!wifiManager.isWifiEnabled() && !wifiScanEnabled){
            return false;
        }

        return true;
    }

    // check if gps is on
    private boolean checkGpsStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!isNetworkEnabled && !isGPSEnabled) {
                //
                // please turn on location settings
                return false;
            }
        }

        return true;

    }

    // check if a location permission allowed
    private boolean checkLocationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        || shouldShowRequestPermissionRationale(android.Manifest.permission.GET_ACCOUNTS)){
                    //shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ) {
                    // Explain to the user why we need to write the permission.
                    //Toast.makeText(this, "Accept Permission", Toast.LENGTH_SHORT).show();
                }


                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                // MY_PERMISSION is an
                // app-defined int constant
                //}
                return false;
            }
        }
        return true;
    }

    private void connectGoogleClient() {
        if (mGoogleApiClient == null ){
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
        mGoogleApiClient.connect();
    }

    private boolean isGoogleClientConnected() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
                return true;
            }
        }
        return false;
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // GPS 활성화
        turnGpsOnByGooglePlayService();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

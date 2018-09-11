package com.loplat.loplatsample;

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
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.loplat.placeengine.PlaceEngine;
import com.loplat.placeengine.Plengi;
import com.loplat.placeengine.PlengiResponse;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    BroadcastReceiver  mLoplatBroadcastReceiver;
    ProgressDialog mProgressDialog=null;

    private static final String PREFS_NAME = MainActivity.class.getSimpleName();
    private static final String TAG = PREFS_NAME;

    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    private static final int REQUEST_LOCATION_PERMISSION = 10000;
    private static final int REQUEST_LOCATION_STATUS = 10001;

    GoogleApiClient mGoogleApiClient;

    LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tv_result = (TextView)findViewById(R.id.tv_result);

        // receive response from loplat listener
        mLoplatBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String packageName = intent.getPackage();
                if (!packageName.equals(context.getPackageName())) {
                    return;
                }

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

                    System.out.println("mLoplatBroadcastReceiver: " + type);

                    if(type.equals("error")) {
                        String response = intent.getStringExtra("response");
                        tv_result.setText(response);
                    } else {
                        if (type.equals("placeinfo")) {
                            String response = intent.getStringExtra("response");
                            tv_result.setText(response);
                        }
                        else if(type.equals("placeevent")) {
                            String response = intent.getStringExtra("response");
                            tv_result.setText(response);
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.loplat.sample.response");
        registerReceiver(mLoplatBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mLoplatBroadcastReceiver != null) {
            unregisterReceiver(mLoplatBroadcastReceiver);
            mLoplatBroadcastReceiver = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final TextView tv_status = (TextView)findViewById(R.id.tv_status);
        final TextView tv_result = (TextView)findViewById(R.id.tv_result);

        //Monitoring 상태 확인하기
        int engineStatus = Plengi.getInstance(this).getEngineStatus();

        if(engineStatus == PlaceEngine.EngineStatus.STARTED)
        {
            tv_status.setText("Monitoring On");
        }
        else if(engineStatus == PlaceEngine.EngineStatus.STOPPED)
        {
            tv_status.setText("Monitoring off");
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

        // gravity 설정하기, 광고 마케팅 동의 한 경우
        if (LoplatSampleApplication.isMarketingServiceAgreed(this)) {
            Plengi.getInstance(this).enableAdNetwork(true);
            // 직접 푸쉬 메세지 사용하는 경우
            //Plengi.getInstance(this).enableAdNetwork(true, false);
            Plengi.getInstance(this).setAdNotiLargeIcon(R.drawable.ic_launcher);
            Plengi.getInstance(this).setAdNotiSmallIcon(R.drawable.ic_launcher);
        }
    }


    public void onInitPlaceEngine(View view) {
        //if you want to use Recognizer mode
        Plengi.getInstance(this).setMonitoringType(PlengiResponse.MonitoringType.STAY);
        // set scan period (optional)
        int moveScanPeriod = 3 * 60000; // 3 mins (milliseconds)
        int stayScanPeriod = 6 * 60000; // 6 mins (milliseconds)
        Plengi.getInstance(this).setScanPeriod(moveScanPeriod, stayScanPeriod);

        /*
        / Gravity 연동하기
        / 마케팅 혹은 push 알림 서비스 동의를 받은 후에 동작을 원하시는 경우 아래의 샘플코드(onMarketingServiceAgreement 참고)와 같이 별도로 구현하셔도 됩니다
         */
        Plengi.getInstance(this).enableAdNetwork(true);
        Plengi.getInstance(this).setAdNotiLargeIcon(R.drawable.ic_launcher);
        Plengi.getInstance(this).setAdNotiSmallIcon(R.drawable.ic_launcher);

        //if you want to use Tracker mode
//        Plengi.getInstance(this).setMonitoringType(PlengiResponse.MonitoringType.TRACKING);
//        Plengi.getInstance(this).setScanPeriodTracking(2 * 60000);

    }

    public void onRequestLocationInfo(View view) {
        // request location to loplat engine
        int result = Plengi.getInstance(this).refreshPlace();

        if(result == PlengiResponse.Result.SUCCESS) {
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("I'm scanning wifi. Please wait...");
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }
        else if(result == PlengiResponse.Result.FAIL_INTERNET_UNAVAILABLE) {
            // internet is not connected
        }
        else if(result == PlengiResponse.Result.FAIL_WIFI_SCAN_UNAVAILABLE) {
            // wifi scan is not available
            checkWiFiScanCondition();
        }
    }

    public void onStartPlaceMonitoring(View view) {
        // request location to loplat engine
        int result = Plengi.getInstance(this).start();

        if (result == PlengiResponse.Result.SUCCESS) {
            Log.d(TAG, "PlaceEngine started");
        } else if (result == PlengiResponse.Result.NOT_SUPPORTED_OS_VERSION) {
            Log.d(TAG, "Not supported os version");
        } else if (result == PlengiResponse.Result.FAIL) {
            Log.d(TAG, "PlaceEngine is not initialized");
        }
    }

    public void onStopPlaceMonitoring(View view) {
        // request location to loplat engine

        int result = Plengi.getInstance(this).stop();

        if (result == PlengiResponse.Result.FAIL) {
            Log.d(TAG, "PlaceEngine is already stopped");
        }
    }

    /**
     * Note: 아래의 코드는 Gravity 사용을 이용한 마케팅 동의 서비스 예제입니다.
     * @param view
     */
    public void onMarketServiceAgreement(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("마케팅 서비스 동의?")
                .setMessage("마케팅 서비스 동의 하시겠습니까?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // loplat gravity service off
                        Plengi.getInstance(MainActivity.this).enableAdNetwork(true);
                        // 직접 광고(푸시 메세지) 하는 경우
                        // Plengi.getInstance(MainActivity.this).enableAdNetwork(true, false);
                        // 앱 내 flag 저장
                        LoplatSampleApplication.setMarketingServiceAgreement(MainActivity.this, true);
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // loplat gravity service off
                        Plengi.getInstance(MainActivity.this).enableAdNetwork(false);
                        // 앱 내 flag 저장
                        LoplatSampleApplication.setMarketingServiceAgreement(MainActivity.this, false);
                    }
                });
        builder.show();
    }

    /**
     * Note: 아래의 코드는 loplat SDK 구동(start)를 이용한 위치기반 서비스 예제입니다.
     * @param view
     */
    public void onLocationBasedServiceAgreement(View view) {
        final TextView tv_status = (TextView)findViewById(R.id.tv_status);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치기반 서비스 동의?")
                .setMessage("위치기반 서비스 하시겠습니까?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 1. wifi scan 가능한지 여부 확인 -> 가능 -> loplat SDK start
                        if (checkWiFiScanCondition()) {
                            Plengi.getInstance(MainActivity.this).start();
                            LoplatSampleApplication.setLocationServiceAgreement(MainActivity.this, true);
                            tv_status.setText("Monitoring on");
                        }
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // loplat SDK stop, 동의 거부
                        Plengi.getInstance(MainActivity.this).stop();
                        LoplatSampleApplication.setLocationServiceAgreement(MainActivity.this, false);
                        tv_status.setText("Monitoring off");
                    }
                });
        builder.show();
    }


    // Sample code for checking WiFi Scanning Condition
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
            // 안드로이드 4.3 이상 버전은 background wifi scan 설정이 켜져있으면 됨
            // [참고: https://developer.android.com/reference/android/net/wifi/WifiManager.html#isScanAlwaysAvailable()]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                startActivity(intent);
            } else {
                // 안드로이드 4.2 이하 버전은 WiFi가 켜져있어야 wifi scanning 가능함
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
                Toast.makeText(this, "turn on WiFi", Toast.LENGTH_SHORT).show();
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
                        || shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        || shouldShowRequestPermissionRationale(android.Manifest.permission.GET_ACCOUNTS)){
                    //shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ) {
                    // Explain to the user why we need to write the permission.
                    //Toast.makeText(this, "Accept Permission", Toast.LENGTH_SHORT).show();
                }


                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
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

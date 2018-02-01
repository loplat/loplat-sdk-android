package com.loplat.loplatsample;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.loplat.placeengine.PlaceEngine;
import com.loplat.placeengine.Plengi;
import com.loplat.placeengine.PlengiResponse;


public class MainActivity extends Activity {

    BroadcastReceiver mLoplatBroadcastReceiver;
    ProgressDialog mProgressDialog=null;

    private static final String PREFS_NAME = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tv_result = (TextView)findViewById(R.id.tv_result);

        // receive response from loplat listener
        mLoplatBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals("com.loplat.mode.response")) {
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
                    }
                    else {
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
        intentFilter.addAction("com.loplat.mode.response");
        registerReceiver(mLoplatBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mLoplatBroadcastReceiver);
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

        // gravity 설정하기
        if (getEnableAdNetwork(this)) {
            Plengi.getInstance(this).enableAdNetwork(true);
            Plengi.getInstance(this).setAdNotiIcon(R.drawable.ic_launcher);
        }

    }


    public void onInitPlaceEngine(View view) {
        //if you want to use Recognizer mode
        Plengi.getInstance(this).setMonitoringType(PlengiResponse.MonitoringType.STAY);
        // set scan period (optional)
        int moveScanPeriod = 3 * 60000; // 3 mins (milliseconds)
        int stayScanPeriod = 6 * 60000; // 6 mins (milliseconds)
        Plengi.getInstance(this).setScanPeriod(moveScanPeriod, stayScanPeriod);

        // Gravity 연동하기
        Plengi.getInstance(this).enableAdNetwork(true);
        Plengi.getInstance(this).setAdNotiIcon(R.drawable.ic_launcher);
        // App 내 gravity 연동 설정
        setEnableAdNetwork(this, true);

        //if you want to use Tracker mode
//        Plengi.getInstance(this).setMonitoringType(PlengiResponse.MonitoringType.TRACKING);
//        Plengi.getInstance(this).setScanPeriodTracking(2 * 60000);

    }

    public void onRequestLocationInfo(View view) {
        // request location to loplat engine
        int status = Plengi.getInstance(this).isEngineWorkable();

        if(status == PlengiResponse.Result.SUCCESS) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog = new ProgressDialog(MainActivity.this);
                    mProgressDialog.setMessage("I'm scanning wifi. Please wait...");
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.show();
                }
            });
            Plengi.getInstance(this).refreshPlace();
        }
        else if(status == PlengiResponse.Result.FAIL_INTERNET_UNAVAILABLE) {
            // internet is not connected
        }
        else if(status == PlengiResponse.Result.FAIL_WIFI_SCAN_UNAVAILABLE) {
            // wifi scan is not available
        }
    }

    public void onStartPlaceMonitoring(View view) {
        // request location to loplat engine
        int status = Plengi.getInstance(this).isEngineWorkable();


        if(status == PlengiResponse.Result.SUCCESS) {
            // ok
            Plengi.getInstance(this).start();
        }
        else if(status == PlengiResponse.Result.FAIL_INTERNET_UNAVAILABLE) {
            // internet is not connected
        }
        else if(status == PlengiResponse.Result.FAIL_WIFI_SCAN_UNAVAILABLE) {
            // wifi scan is not available
        }
    }

    public void onStopPlaceMonitoring(View view) {
        // request location to loplat engine
        int result = Plengi.getInstance(this).stop();

        if(result == PlengiResponse.Result.SUCCESS) {
            // ok
        }
        else if(result == PlengiResponse.Result.FAIL_INTERNET_UNAVAILABLE) {
            // internet is not connected
        }
        else if(result == PlengiResponse.Result.FAIL_WIFI_SCAN_UNAVAILABLE) {
            // wifi scan is not available
        }
    }


    // Sample code for checking WiFi Scanning Condition in Mashmallow
    private void checkWiFiScanConditionInMashmallow(Context context) {

        if(Build.VERSION.SDK_INT < 23) {
            return;
        }

        LocationManager locationManager = (LocationManager)context.getSystemService(context.LOCATION_SERVICE);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isNetworkEnabled && !isGPSEnabled) {
            // please turn on location settings
        }


        PackageManager pm = context.getPackageManager();
        int permission = pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, context.getPackageName());
        int subpermission = pm.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, context.getPackageName());
        if(permission != PackageManager.PERMISSION_GRANTED && subpermission != PackageManager.PERMISSION_GRANTED) {
            // please enable permission on location access
        }
    }

    // App에서 광고 연동 여부 설정
    private void setEnableAdNetwork(Context context, boolean enableAdNetwork) {
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("ad_network", enableAdNetwork);
            editor.commit();
        } catch (Exception e) {
        }
    }

    // App에서 광고 연동 여부 확인
    private boolean getEnableAdNetwork(Context context) {
        boolean enableAdNetwork = false;
        try {
            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            enableAdNetwork = settings.getBoolean("ad_network", false);
        } catch (Exception e) {
        }
        return enableAdNetwork;
    }


}

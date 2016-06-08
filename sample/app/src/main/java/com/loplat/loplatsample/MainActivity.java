package com.loplat.loplatsample;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import com.loplat.placeengine.Plengi;
import com.loplat.placeengine.PlengiResponse;

import java.util.List;
import java.util.regex.Pattern;


public class MainActivity extends Activity {

    BroadcastReceiver mLoplatBroadcastReceiver;
    ProgressDialog mProgressDialog=null;

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

        int currentPlaceStatus = Plengi.getInstance(this).getCurrentPlaceStatus();

        switch (currentPlaceStatus) {
            case PlengiResponse.PlaceStatus.MOVE:
                tv_status.setText("Welcome to loplat");
                break;

            case PlengiResponse.PlaceStatus.STAY:
                tv_status.setText("Staying...");

                PlengiResponse.Place currentPlace = Plengi.getInstance(this).getCurrentPlaceInfo();
                List<PlengiResponse.Visit> visits = Plengi.getInstance(null).getVisitList();
                if(visits.size() >= 1) {
                    PlengiResponse.Visit visit = visits.get(visits.size() - 1);
                    long duration = (System.currentTimeMillis() - visit.enter) / 60000;
                    if (currentPlace.name == null) {
                        tv_result.setText("Unknown Place (" + duration / 60 + "시간 " + duration % 60 + "분)");
                    } else {
                        String name = currentPlace.name;
                        String tags = currentPlace.tags;
                        String category = currentPlace.category;
                        int floor = currentPlace.floor;
                        String info = name + ", " + tags + ", " + category + ", " + floor + "F\n";
                        tv_result.setText("" + info + "(" + duration / 60 + "시간 " + duration % 60 + "분째)");
                    }
                }
                break;
        }

    }


    public void onInitPlaceEngine(View view) {
        // do init process only once before calling other functions
        String clientId = "test";
        String clientSecret = "test";
        String uniqueUserId = getUniqueUserId(this);
        int result = Plengi.getInstance(this).init(clientId, clientSecret, uniqueUserId);
        if(result == PlengiResponse.Result.SUCCESS) {
            // ok
        }
        else if(result == PlengiResponse.Result.FAIL_INTERNET_UNAVAILABLE) {
            // internet is not connected
            // need to retry "init"
        }
        else if(result == PlengiResponse.Result.FAIL_WIFI_SCAN_UNAVAILABLE) {
            // wifi scan is not available
            // but, in the "init" process, this error does not matter
        }

        // set scan period (optional)
        int moveScanPeriod = 3 * 60000; // 3 mins (milliseconds)
        int stayScanPeriod = 6 * 60000; // 6 mins (milliseconds)
        Plengi.getInstance(this).setScanPeriod(moveScanPeriod, stayScanPeriod);
    }

    // in this sample, we use email address as a user id
    private String getUniqueUserId(Context context) {
        String email=null;
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.type + ", " + account.name; // com.google
                System.out.println("emails: " + possibleEmail);
                if(account.type.equals("com.google")) {
                    email = account.name;
                    break;
                }
            }
        }
        return email;
    }

    public void onRequestLocationInfo(View view) {
        // request location to loplat engine
        int result = Plengi.getInstance(this).refreshPlace();

        if(result == PlengiResponse.Result.SUCCESS) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog = new ProgressDialog(MainActivity.this);
                    mProgressDialog.setMessage("I'm scanning wifi. Please wait...");
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.show();
                }
            });
        }
        else if(result == PlengiResponse.Result.FAIL_INTERNET_UNAVAILABLE) {
            // internet is not connected
        }
        else if(result == PlengiResponse.Result.FAIL_WIFI_SCAN_UNAVAILABLE) {
            // wifi scan is not available
        }
    }

    public void onStartPlaceMonitoring(View view) {
        // request location to loplat engine
        int result = Plengi.getInstance(this).start();

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

}

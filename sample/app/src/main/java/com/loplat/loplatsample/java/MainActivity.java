package com.loplat.loplatsample.java;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
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
import com.loplat.loplatsample.R;
import com.loplat.placeengine.OnPlengiListener;
import com.loplat.placeengine.PlaceEngineBase;
import com.loplat.placeengine.Plengi;
import com.loplat.placeengine.PlengiResponse;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static com.loplat.loplatsample.java.LoplatSampleApplication.isLocationServiceAgreed;
import static com.loplat.loplatsample.java.LoplatSampleApplication.isMarketingServiceAgreed;
import static com.loplat.loplatsample.java.LoplatSampleApplication.setLocationServiceAgreement;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    BroadcastReceiver mSampleUIReceiver;
    ProgressDialog mProgressDialog = null;

    private TextView tv_status;
    private TextView tv_result;
    private Switch switchMarketing;
    private Switch switchLocation;

    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    private static final int REQUEST_LOCATION_PERMISSION = 10000;
    private static final int REQUEST_LOCATION_STATUS = 10001;
    private static final int REQUEST_WIFI_STATUS = 10002;

    GoogleApiClient mGoogleApiClient;

    LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (SDK_INT >= M) {
                if (checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                    locationPermissionGranted();
                } else if (checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_DENIED) {
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
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults[0] == PERMISSION_GRANTED) {
                locationPermissionGranted();
            } else if (grantResults[0] == PERMISSION_DENIED) {
                locationPermissionDenied();
            }
            LoplatSampleApplication.setLocationShouldShowRationale(this, false);
        } else if (grantResults[0] == PERMISSION_GRANTED
                || grantResults[1] == PERMISSION_GRANTED) {
        }
    }

    private void locationPermissionGranted() {
        toastMessage(getText(R.string.toast_location_message_agree));
        setLocationServiceAgreement(MainActivity.this, true);
        Plengi.getInstance(this).start();

        /**
         * loplat SDK는 위치 permission, GPS setting가 WiFi scan을 할 수 없더라도 start된 상태를 유지하고
         * 위치 permission, GPS setting에 따라 WiFi scan이 가능한 상황이 되면 실제 동작.
         * 앱에 로플랫 SDK 적용시 약관의 위치서비스 동의한 사용자에게 설정 변경을 쉽게 할 수 있도록
         * checkWiFiScanCondition 활용해주세요.
         */
        checkLocationScanCondition();
        tv_status.setText(getText(R.string.sdk_started));
        switchLocation.setChecked(true);
    }

    private void locationPermissionDenied() {
        toastMessage(getText(R.string.toast_location_message_revoke));
        setLocationServiceAgreement(MainActivity.this, false);
        Plengi.getInstance(this).stop();
        tv_status.setText(getText(R.string.sdk_stopped));
        switchLocation.setChecked(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_status = findViewById(R.id.tv_status);
        tv_result = findViewById(R.id.tv_result);
        switchMarketing = (Switch) findViewById(R.id.switch_marketing);
        switchLocation = (Switch) findViewById(R.id.switch_location);

        /**
         * 로그인 성공 시점이라 가정
         * 로그인 후 1)위치서비스 약관 동의 여부, 2)마케팅 수신 동의 여부, 3)회원번호(optional) 를 각각 저장
         * 저장된 3가지 정보는 Background 동작 시 사용
         */

        // 로그인 후 서버로 부터 받은 값을 local에 저장
        String memberCodeFromServer = "18497358207";

        // 테스트를 위해 간단하게 로컬에 저장한 값을 그대로 불러오는 과정. 실제로 각 유저들의 데이터는 서버로부터 받길 권장
        boolean isMarketingServiceAgreedFromServer = isMarketingServiceAgreed(this);
        boolean isLocationServiceAgreedFromServer = isLocationServiceAgreed(this);

        switchMarketing.setChecked(isMarketingServiceAgreedFromServer);
        switchLocation.setChecked(isLocationServiceAgreedFromServer);

        /**
         * 앱 시작 혹은 로그인 할 때 마다 사용자의 위치약관동의 여부를 매번 확인해서 Loplat SDK start 호출 필수
         */
        if (isLocationServiceAgreedFromServer) {
            /**
             * 하기 코드는 회원번호를 사용하는 경우만 활용
             * 회원번호가 변경된 경우 저장
             */
            if (memberCodeFromServer != null
                    && !memberCodeFromServer.equals(LoplatSampleApplication.getEchoCode(this))) {
                /**
                 * echoCode에는 이메일, 전화번호와 같은 개인정보 반드시 제외
                 */
                LoplatSampleApplication.setEchoCode(this, memberCodeFromServer);
            }
            // Loplat SDK start
            ((LoplatSampleApplication) getApplicationContext()).loplatSdkConfiguration();

            /**
             * 위치서비스를 동의한 사용자인 경우 wifi scan 환경을 확인하는 것 권장합니다.
             */
            checkLocationScanCondition();
        }

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

        // LoplatPlengiListener로 부터 위치 인식 결과를 전달 받는 receiver 등록
        mSampleUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("com.loplat.sample.response")) {
                    try {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String type = intent.getStringExtra("type");
                    if (type == null) {
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

        // Loplat SDK 동작 상태 확인
        int engineStatus = Plengi.getInstance(this).getEngineStatus();
        if (engineStatus == PlaceEngineBase.EngineStatus.STARTED) {
            tv_status.setText(getText(R.string.sdk_started));
        } else {
            tv_status.setText(getText(R.string.sdk_stopped));
        }

        int currentPlaceStatus = Plengi.getInstance(this).getCurrentPlaceStatus();
        switch (currentPlaceStatus) {
            case PlengiResponse.PlaceStatus.MOVE:
                break;

            case PlengiResponse.PlaceStatus.STAY:

                PlengiResponse.Place currentPlace = Plengi.getInstance(this).getCurrentPlaceInfo();
                if (currentPlace != null) {
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

    /**
     * 현재 위치 확인 (테스트 시에만 사용 권장)
     */
    public void onRequestLocationInfo(View view) {
        if (LoplatSampleApplication.isLocationServiceAgreed(this)) {
            int result = Plengi.getInstance(this).TEST_refreshPlace_foreground(new OnPlengiListener() {
                @Override
                public void onSuccess(PlengiResponse response) {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }

                    String description = "";
                    if (response.place != null) {
                        String name = response.place.name;
                        String branch = (response.place.tags == null) ? "" : response.place.tags;
                        int floor = response.place.floor;
                        String client_code = response.place.client_code;

                        float accuracy = response.place.accuracy;
                        float threshold = response.place.threshold;

                        description = "[PLACE]" + name + ": " + branch + ", " + floor + ", " +
                                String.format("%.3f", accuracy) + "/" + String.format("%.3f", threshold);

                        if (accuracy > threshold) {
                            description += " (In)";
                        } else {
                            description += " (Nearby)";
                        }

                        if (client_code != null && !client_code.isEmpty()) {
                            description += ", client_code: " + client_code;
                        }
                    }

                    // 상권이 인식 되었을 때
                    if (response.area != null) {
                        if (response.place != null) {
                            description += "\n    ";
                        }
                        description += "[" + response.area.id + "]" + response.area.name + ","
                                + response.area.tag + "(" + response.area.lat + "," + response.area.lng + ")";
                    }

                    // 복합몰이 인식 되었을 때
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
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    if (plengiResponse.errorReason != null) {
                        tv_result.setText(plengiResponse.errorReason);
                    }
                }
            });

            if (result == PlengiResponse.Result.SUCCESS) {
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage(getText(R.string.message_scanning_wifi));
                mProgressDialog.setCancelable(true);
                mProgressDialog.show();
            } else if (result == PlengiResponse.Result.FAIL_INTERNET_UNAVAILABLE) {
                toastMessage(getText(R.string.error_network));
            } else if (result == PlengiResponse.Result.FAIL_WIFI_SCAN_UNAVAILABLE) {
                toastMessage(getText(R.string.error_wifi_scan));
                checkLocationScanCondition();
            } else {
                toastMessage(getText(R.string.error_sdk));
            }
        } else {
            toastMessage(getText(R.string.error_location_services));
        }
    }

    /**
     * 마케팅 서비스 동의에 따른 Loplat SDK 설정 (Loplat X Campaigns)
     */
    public void onMarketServiceAgreement() {
        String message;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (switchMarketing.isChecked()) {
            message = (String) getText(R.string.request_marketing_message_agree);
            builder.setPositiveButton(getText(R.string.confirm), new DialogInterface.OnClickListener() {
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
                    toastMessage(getText(R.string.toast_marketing_message_argee));
                    switchMarketing.setChecked(true);
                }
            });
        } else {
            message = (String) getText(R.string.request_marketing_message_revoke);
            builder.setPositiveButton(getText(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Plengi.getInstance(MainActivity.this).enableAdNetwork(false);
                    // 앱 내 flag 저장
                    LoplatSampleApplication.setMarketingServiceAgreement(MainActivity.this, false);
                    toastMessage(getText(R.string.toast_marketing_message_revoke));
                    switchMarketing.setChecked(false);
                }
            });
        }
        builder.setTitle(getText(R.string.marketing_services_title));
        builder.setMessage(message);
        builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switchMarketing.setChecked(!switchMarketing.isChecked());
            }
        });
        builder.show();
    }

    /**
     * 위치 기반 서비스 동의에 따른 Loplat SDK 동작
     */
    public void onLocationBasedServiceAgreement() {
        if (switchLocation.isChecked()) {
            if (isGrantedLocationPermission()) {
                locationPermissionGranted();
            } else {
                if (checkLocationShouldShowRationale()) {
                    if (SDK_INT >= M) {
                        requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                    }
                } else {
                    switchLocation.setChecked(false);
                    showSettingDialog();
                }
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.revoke_location_service));
            builder.setMessage(getText(R.string.request_revoke_location_service));
            builder.setPositiveButton(getText(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Plengi.getInstance(MainActivity.this).stop();
                    setLocationServiceAgreement(MainActivity.this, false);
                }
            });
            builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switchLocation.setChecked(!switchLocation.isChecked());
                }
            });
            builder.show();
        }
    }

    /**
     * 위치 인식을 할 수 있는 상태인지 확인
     */
    private boolean checkLocationScanCondition() {
        boolean available = true;
        if (!checkLocationPermissionIfNeeded()) {
            // 위치 권한 상태 확인

            available = false;
            toastMessage(getText(R.string.request_location_permission));
        } else if (!checkGpsStatus()) {
            // GPS 활성화 상태 확인

            available = false;
            toastMessage(getText(R.string.request_gps));

            /* GPS 켜는 방법은 google play service를 통해 앱내에서 직접 설정, GPS 설정화면 이용 등 2가지 방법이 있음
               현재 sample code에서는 google play service를 이용하는 방법으로 작성되었음
               google-play-service를 사용하지 않고 GPS 설정화면을 넘기는 경우에는 아래 주석처리된 코드 참조 바람

               google play service(location) 사용하는 경우 -> dependency google-play-services 선언이 필요함
               compile 'com.google.android.gms:play-services:[latest version]'
            */
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS) {
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
            // Wi-Fi 스캔 가능 여부 확인

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
            if (SDK_INT >= JELLY_BEAN_MR2) {
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

    /**
     * 위치 기능 활성화 요청
     * [참고: https://developer.android.com/training/location/change-location-settings]
     */
    private void turnGpsOnByGooglePlayService() {
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

    /**
     * Wi-Fi 스캔 가능 여부 확인
     */
    private boolean checkWifiScanIsAvailable() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        boolean wifiScanEnabled = false;

        if (SDK_INT >= JELLY_BEAN_MR2) {
            wifiScanEnabled = wifiManager.isScanAlwaysAvailable();
        }

        return wifiManager.isWifiEnabled() || wifiScanEnabled;
    }

    /**
     * GPS 활성화 여부 확인
     */
    private boolean checkGpsStatus() {
        if (SDK_INT >= M) {
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isNetworkEnabled && !isGPSEnabled) {
                return false;
            }
        }
        return true;
    }

    /**
     * 위치 권한 교육용 UI 출력 필요 여부 확인
     */
    private boolean checkLocationShouldShowRationale() {
        return LoplatSampleApplication.getLocationShouldShowRationale(this)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION);
    }

    /**
     * 위치 권한 활성화 여부 확인
     */
    private boolean isGrantedLocationPermission() {
        if (SDK_INT >= M) {
            return checkSelfPermission(ACCESS_FINE_LOCATION)
                    == PERMISSION_GRANTED
                    || checkSelfPermission(ACCESS_COARSE_LOCATION)
                    == PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * 위치 권한 활성화 여부 확인 후 거부 상태라면 상태에 따라 처리
     */
    private boolean checkLocationPermissionIfNeeded() {
        if (SDK_INT >= M) {
            if (isGrantedLocationPermission()) {
                return true;
            } else {
                if (checkLocationShouldShowRationale()) {
                    // 앱 내 권한 요청
                    requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                } else {
                    // 앱의 권한 설정 화면으로 안내
                    showSettingDialog();
                }
                return false;
            }
        }
        return true;
    }

    /**
     * 앱의 권한 설정 화면으로 안내
     */
    private void showSettingDialog() {
        new AlertDialog.Builder(this)
                .setMessage(getText(R.string.request_revoked_permission))
                .setPositiveButton(getText(R.string.setting), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri data = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                        intent.setData(data);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getText(R.string.cancel), null)
                .show();
    }

    private void connectGoogleClient() {
        if (mGoogleApiClient == null) {
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
            return mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting();
        }
        return false;
    }

    private void toastMessage(CharSequence message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

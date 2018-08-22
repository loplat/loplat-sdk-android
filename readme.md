# Plengi SDK

## 시작하기

### SDK 추가하기

#### 1. repository 추가 하기
- 프로젝트 내, 최상위 `build.gradle` 에 아래의 코드를 추가하세요.
	
	```groovy
	maven { url "http://maven.loplat.com/artifactory/plengi"}
	```

	모듈의 `build.gradle` 에 아래의 코드를 추가하세요.

	```groovy	
	allprojects {
		repositories {
	        jcenter()
			mavenCentral()
			maven { url "http://maven.loplat.com/artifactory/plengi"}
	        google()
		}
	}
	```

#### 2. loplat SDK dependency 추가 하기
 - 앱 build.gradle (Gradle 3.0 이상)

	```groovy	
	implementation 'com.loplat:placeengine:[version]'
	```
 
- 앱 build.gradle (Gradle 3.0 미만)

	```groovy
	compile 'com.loplat:placeengine:[version]'
	```

## Contents
1. SDK Specification
2. SDK Setup
	- 계정 만들기 
	- Permission 등록
	- Receiver & Service 등록
	- Library 적용하기
	- Constraints 
3. SDK 초기화
	- PlengiListener 생성
	- Plengi Instance 생성 및 EventListener 등록
	-  Plengi Init
4. SDK 구동하기
	- Start/Stop
	- Gravity 연동하기
	- 장소 인식 결과

### 1. SDK Specification

##### SDK 지원 버전
* minSdkversion 14
* targetSdkversion 26


### 2. SDK Setup

#### Permission 
* SDK를 적용하면 하기 권한이 자동으로 추가됩니다.  
	```xml
	<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />  
	<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
	<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
	<uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
    ```

	* `ACCESS_FINE_LOCATION` : GPS를 이용하여 현재 위치의 위도와 경도 값을 획득할 수 있는 권한  
	* `ACCESS_COARSE_LOCATION` : WiFi 혹은 Network를 이용하여 현재 위치의 위도와 경도 값을 획득할 수 있는 권한  
	* `ACCESS_NETWORK_STATE` : 네트워크 상태를 확인할 수 있는 권한  
	* `ACCESS_WIFI_STATE / CHANGE_WIFI_STATE` : 주변 WiFi AP들을 스캔하기 위한 권한
	* `INTERNET` : 인터넷을 사용할 수 있는 권한
	* `RECEIVE_BOOT_COMPLETED` : 핸드폰 부팅되는 과정을 브로드캐스팅하기 위한 권한

#### Constraints

* Android OS 6.0 버전 부터 WiFi Scan시 아래와 같은 위치 권한이 필요합니다.
	```xml
	<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
	<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
	```

	<p class="tip">
	Android OS 6.0 부터 위치 권한 허용 & GPS on 상태에서만 WiFi scan 결과값 획득이 가능합니다.
	</p>
	* Android OS 6.0 부터 위치 권한은 Dangerous Permission으로 구분 되어 권한 획득을 위한 코드가 필요합니다.
	* 권한 설정과 관련하여 좀 더 자세한 사항은 Android Developer를 참고 바랍니다. [Android Developer](http://developer.android.com/intl/ko/training/permissions/requesting.html)

#### Gradle 설정 및 Library 적용

##### Android OS 8.0 이상에서 동작을 위한 gradle 설정 및 library 적용

- compileSdkVersion 설정 

	```groovy
	android {
		compileSdkVersion 26
		...
	}
	```
- android support library 적용
	- 아래의 예시 처럼 appcompat v7 라이브러리 version 26이상으로 적용

	<p class="warning">
	appcompat v7의 version이 26이상이 아닌 경우에는 SDK 동작 중 error가 발생하여 **앱 동작이 중지 될 수 있으니**, 기존의 적용된 라이브러리 버전을 확인 후 업그레이드 하시길 바랍니다.
	</p>

		```groovy
		compile 'com.android.support:appcompat-v7:26.1.0'
		```

##### Google Play Services library 적용하기
- Google play services liabrary는 **11.8.0 이상 버전**을 사용 해야 합니다.
- **(필수)효율적인 위치 정보 획득을 위해서** build.gradle의 dependency에 아래와 같이 google play service library 적용이 필요합니다. 
	
	```groovy
	compile 'com.google.android.gms:play-services-location:11.8.0'
	```

- **Gravity를 사용하기 위해서** build.gradle의 dependency에 아래와 같이 google play service library 적용이 필요합니다.

	```groovy
	compile 'com.google.android.gms:play-services-ads:11.8.0'
	```
	
	<p class="warning">
	아래와 같이 통합 google play service library 사용하는 경우에는 위의 2가지 <b>library를 적용 하지 마세요.</b>
	</p>

	```groovy
	compile 'com.google.android.gms:play-services:11.8.0'
	```

##### Retrofit 및 GSON library 적용하기

- loplat SDK 1.7.10 이상 버전 부터 위치 확인 요청시 서버와의 통신을 위해 Retrofit 및 GSON library 사용합니다. Retrofit 및 GSON 라이브러리 적용을 위해서  Android Studio의 build.gradle에 다음과 같이 추가합니다.

	```groovy
	compile 'com.squareup.retrofit2:retrofit:2.3.0'
	compile 'com.squareup.retrofit2:converter-gson:2.3.0'
	compile 'com.squareup.okhttp3:okhttp:3.8.1'
	```
	
<p class="tip">
Proguard를 사용할 땐, 아래와 같이 proguard 설정을 추가해야 합니다.
</p>

```proguard
	-dontwarn okio.**
	-dontwarn javax.annotation.**
	-keepclasseswithmembers class * {
			@retrofit2.http.* <methods>;
	}
```

### 3. SDK 초기화

#### 1. PlengiListener 생성 
* PlengiListener 인터페이스를 구현합니다.
	- loplat서버로 부터 받은 모든 asynchronous Result는 모두 해당 리스너를 통해 전달됩니다.
	- `PLACE`(Recognize a place), `PLACE_EVENT`(Enter/Leave/Nearby, Recognizer mode), `PLACE_TRACKING`(Enter/Leave/Nearby, Tracker mode) 등의 Event에 따른 결과를 작성합니다.

- 예시코드
```java
public class LoplatPlengiListener implements PlengiListener {
	@Override
	public void listen(PlengiResponse response) {
		String echoCode = response.echoCode; // init시 전달된 echo code
		if(response.result == PlengiResponse.Result.SUCCESS) {
			if(response.type == PlengiResponse.ResponseType.PLACE_EVENT 
				|| response.type == PlengiResponse.ResponseType.PLACE_ADV_TRACKING 
				|| response.type == PlengiResponse.ResponseType.PLACE_TRACKING) { //BACKGROUND
				if (response.place != null) {
					// response.place 값이 null이 아닌 경우만 response.placeEvent(ENTER/LEAVE/NEARBY) 값을 사용
					int event = response.placeEvent;
					if (event == PlengiResponse.PlaceEvent.ENTER) { 
						// 사용자가 장소에 들어 왔을 때
					} else if (event == PlengiResponse.PlaceEvent.LEAVE) {
						// 사용자가 가장 최근에 머문 장소를 떠났을 때
					} else if (event == PlengiResponse.PlaceEvent.NEARBY) {
						// 사용자가 장소 주변(Nearby)을 방문 했을 때
					} 
				}
				if (reponse.area != null) {
					// 상권이 인식 되었을 때
				}
				if (response.complex != null) {
					// 복합몰이 인식 되었을 때
				}
				if (response.advertisement != null) {
					// Gravity 광고 정보가 있을 때
					// loplat SDK 통한 광고 알림을 사용하지 않고 
					// Custom Notification 혹은 직접 이벤트 처리 할 경우 해당 객체를 사용
				}
				if (response.geofence != null) {
					// GeoFence 정보
				}
			} 
		} else {
			// 위치 획득 실패 및 에러
			// response.errorReason 위치 획득 실패 혹은 에러 이유가 포함 되어 있음
			// errorReason -> Location Acquisition Fail(위치 획득 실패), Network Fail(네트워크 연결 실패)
			// Not Allowed Client(잘못된 client id, passwrod 입력), invalid scan results
			if (response.result == PlengiResponse.Result.FAIL) {
				// response.errorReason 확인
			} else if (response.result == PlengiResponse.Result.ERROR_CLOUD_ACCESS) {
				// response.errorReason 확인
			}
		}
	}
}
```

#### 2. Plengi 인스턴스 생성 및 EventListener 등록

<p class="danger">
`Application` 클래스를 상속 받은 클래스에서 작업해주세요.
</p>

`Plengi` 인스턴스를 생성한 후, 1번에서 생성한 `Listener`를 등록해주신 후, 생성한 application name을 `AndroidManifest.xml`에 등록해주세요.


#### 3. Plengi init

<p class="danger">
`Application` 클래스의 `onCreate()`에 `init`(SDK 초기화)과 `setListener`(리스너 등록)을 선언하지 않으면 SDK가 동작하지 않습니다.
</p>

- 사용자의 매장/장소 방문을 모니터링하기 위해 Plengi Engine을 초기화합니다.
- **생성한 Application 클래스(2번 항목 참조)에서 Plengi init을 다음과 같이 선언을 합니다.**

	```java
	Plengi.getInstance(this).init(clientId, clientSecret, echo_code);  
	```
	
- `init`을 위해 `clientid`, `clientsecret`, `echo_code` 인자값으로 전달해주셔야합니다.  
- `clientid` & `clientsecret` : loplat server로 접근하기 위한 ID와 PW입니다.  
- 정식 `id`와 `secret`을 원하는 분은 아래에 기입 된 메일 주소로 연락 바랍니다.  
- `echo_code`: App 사용자 식별(관리용) 및 추적하기 위한  ID입니다 (ex, 광고id,id,....,etc.). `echo_code` 관리를 원하지 않는 경우 `null` 값을 입력하면 됩니다.
	
	<p class="danger">
	이메일, 폰번호와 같이 개인정보와 관련된 정보는 전달하지 마세요!
	</p>

* 예시코드
	<p class="danger">
	생성한 Application 클래스를 AndroidManifest에 등록은 필수입니다. 등록하지 않는 경우 SDK가 동작하지 않습니다. 
	</p>
	
	```java
	public class ModeApplication extends Application {
		@Override
		public void onCreate() {
			super.onCreate();
			Plengi.getInstance(this).setListener(new LoplatPlengiListener());
			Plengi.getInstance(this).init("[CLIENT_ID]", "[CLIENT_SECRET]", "[ECHO_CODE]");
		}
	}
	```

	```xml
	<application
		android:name=".ModeApplication"
		android:icon="@mipmap/ic_launcher"
		android:label="@string/app_name"
		android:theme="@style/AppTheme" >
		<!-- 이하 생략... -->
	>
	```

### 4. SDK 구동하기
#### 1. Start/Stop
- 사용자 장소/매장 방문 모니터링을 시작하거나 정지 할 수 있습니다.
- 설정된 주기마다 WiFi 신호를 스캔하여 사용자의 위치를 확인합니다.  
<p class="tip">
start/stop을 **중복 호출**하더라도 SDK 내에서 **1회만** 호출되도록 구현되어 있습니다.
</p>
- 사용자의 위치 정보는 `PlengiEventListener`로 전달됩니다.
-  모니터링 시작과 정지는 다음과 같이 선언합니다.  
	
	```java	
	Plengi.getInstance(this).start(); //Monitoring Start  
	Plengi.getInstance(this).stop(); //Monitoring Stop
	```

#### 2. Gravity 연동하기

<p class="warning">
Gravity는 (필수항목) 위치 권한 허용, GPS on 상태에서 동작하오니 코드 작성시 유의하시기 바랍니다.
</p>

* **Gravity**를 통해 **푸쉬 메시지** (광고 및 알림 메시지)를 받기 위해서는 광고 알림 허용을 한 시점에 아래와 같이 코드 작성이 필요 합니다.
*  직접 구현한 Notification을 사용을 원하는 경우 (예시 코드 참조), 광고 정보 값은 장소 인식 결과 내의 Advertisement(`response.advertisement`)를 확인하면 됩니다.

	```java
	// 직접 푸쉬 메세지 사용하는 경우
	Plengi.getInstance(this).enableAdNetwork(true, false);		
	// SDK 내 푸쉬 메시지 사용하는 경우
	Plengi.getInstance(this).enableAdNetwork(true);            
	// 푸쉬 메세지 설정이 on된 경우 해당, 직접 Notification을 사용하는 경우 구현할 필요 없음
	Plengi.getInstance(this).setAdNotiSmallIcon([samll icon id]);  // 푸쉬 메세지 small icon
	Plengi.getInstance(this).setAdNotiLargeIcon([large icon id]);  // 푸쉬 메세지 large icon
	 ```
	 
#### 3. 장소 인식 결과

<p class="danger">
장소 인식시 인식된 장소 결과에 따라 `area`(상권정보), `complex`(복합몰) 정보가 추가로 전달됩니다. 상권만 인식 된 경우에는 `place` 정보가 `null`로 넘어가니 코드 작성시 주의 부탁드립니다.
</p>

- Echo Code ( `response.echo_code` )
	- `init`시 전달한 `echo_code` 값이 전달됩니다. (`PlengiListener` 예시를 참고 바랍니다)

- 현재 위치가 인식 된 경우
	- 위치 정보 결과: **`Place`** (`PlengiResponse.Place` 클래스, `response.place`로 획득 가능)
		```java
		class Place {
			public long loplatid;        // 장소 id
			public String name;          // 장소 이름
			public String tags;          // 장소와 관련된 tag
			public int floor;            // 층 정보
			public String category;      // 장소 유형
			public String category_code; // 장소 유형 코드
			public double lat;           // 인식된 장소의 위도
			public double lng;	         // 인식된 장소의 경도
			public float accuracy;       // 정확도
			public float threshold;      // 한계치
			public String client_code;   // 클라이언트 코드
			public String address;       // 장소 (구)주소
			public String address_road;  // 장소 신 주소
			public String post;          // 우편번호
		}
		```
		- `accuracy > threshold`: 현재 위치 내에 있는 경우  
		- 그 외에 경우: 현재 위치 근처에 있는 경우 
	
	- 상권 정보 결과: **`Area`** (`PlengiResponse.Area` 클래스, `response.area`로 획득 가능)
		- 장소 위치 요청한 장소가 상권 안일 경우 상권 정보가 인식 결과에 함께 같이 전달됩니다.
		- 위도 및 경도는 아래의 조건으로 결과가 전달됩니다.
			- 장소 인식 결과값이 있다면 -> 인식된 장소 위도/경도
			- 장소 인식 결과값이 없으면 -> device의 위도/경도
		
	```java
		class Area {
			public int id;         // Area ID
			public String name;    // 상권 이름
			public String tag;     // 상권 위치 [도, 시 단위 ex) 서울, 경기도, 인천]
			public double lat;     // 위도 
			public double lng;     // 경도
		}
	```

	- Complex 정보 결과: **`Complex`** (`PlengiResponse.Complex` 클래스, `response.complex`로 획득 가능)
		- 인식된 장소가 복합몰 내인 경우 복합몰 정보도 함께 인식 결과에 포함되어 전달됩니다.
		
	```java
		class Complex {
			public int id;         // Complex ID
			public String name;    // 복합몰 이름
			public String branch_name;     // 복합몰 지점명
			public String category;     // 카테고리 
			public String category_code;     // 카테고리 코드
		}
	```

	- 광고:  **`Advertisement`** (`PlengiResponse.Advertisement` 클래스, `response.advertisement` 결과 전달)

		```java
		class Advertisement {
			private int campaign_id;	// Gravity 캠페인 ID
			private int msg_id;			// Gravity 광고 ID
			private String title;       // 광고 제목
			private String body;    	// 광고 내용 
			private String intent;     	// 광고 이벤트 타입 (In-App, url link)
			private String target_pkg;	// 광고 대상 앱 패키지 명
			private long delay;			// 광고 알림 delay time
			private String delay_type;	// 광고 알림 delay type (enter, leave)
			private String img; 		// 광고 이미지 URL
			private String client_code;	// 광고에 대한 client code
		}
		```
		
	- Geofence & Fence: `GeoFence` (`PlengiResponse.Geofence` 클래스, `response.geofence` 결과 전달), fence 정보는 geofence 포함되어 전달
	
		```java
		class Geofence {
			private double lat;  				// GeoFence (중심)위도
			private double lng;  				// GeoFence (중심)경도
			private ArrayList<Fence> fences;	// GeoFence 리스트
		}
		
		class Fence {
			private long gfid; 			// 지오펜스 관리 ID
			private float dist;	// 거리; 중심 좌표와 사용자 위치 간 거리 (optional - 중심 좌표가 있는 경우만 속성값 존재)
			private String name; 		// 이름
			private String custom_code; //고객사 측 관리 ID
		}
		```
	    
* 현재위치 획득 실패시
	* result: `PlengiResponse.Result.FAIL`
	* errorReason : Location Acquisition Fail  
	
* Client 인증 실패시
	* result: `PlengiResponse.Result.FAIL`
	* errorReason : Not Allowed Client


## 샘플앱
(샘플앱 다운로드 > https://github.com/loplat/loplat-sdk-android)

(샘플앱도 Gradle을 사용합니다. Gradle 사용법은 위에 명시되어 있습니다.)


## History
* 2081.08.01
	* loplat SDK version 1.8.9.8 release
		* 일부 인식 성능 개선
* 2018.07.13
	* loplat SDK version 1.8.9.7 release
		* Android OS 아이스크림 샌드위치(API 14) 미만 지원 중단 (동작 하지 않음)
		* Custom Notification 지원
		* 일부 인식 성능 개선
* 2018.06.07
	* loplat SDK version 1.8.9.6 release
		* Notification big picture style일 때 large icon 무시 되는 현상 해결
		* 일부 인식 성능 개선 
* 2018.05.10
	* loplat SDK version 1.8.9.5 release
		* Network 연결 실패에 의한 결과 값을 PlengiResponse.Reuslt.FAIL로 처리 (기존 ERROR_CLOUD_ACCESS)
		* 일부 성능 개선
* 2018.04.22
	* loplat SDK version 1.8.9.4 release
		* 광고 알림 시 모바일 기기 화면이 켜지도록 수정
		* 광고 알림 시 sound profile대로 동작하도록 수정
		* 광고 설정시 small, large icon 구분하여 입력 하도록 수정
		* 일부 기능 개선

* 20.18.04.18
	* loplat SDK version 1.8.9.3 release
		* 인식 성능 개선 및 버그 수정

* 2018.04.10
	* loplat SDK version 1.8.9.2 release
		* 인식 성능 개선 및 버그 수정

* 2018.04.06
	* loplat SDK version 1.8.9.1 release
		* 허용되지 않는 계정인 경우 SDK 동작이 정지하도록 조치

* 2018.03.15
	* loplat SDK version 1.8.9 release
		* android 8.1(Oreo) 백그라운드 제약 사항 추가 대응
		* 일부 기능 및 인식 성능 개선, 버그 수정

* 2018.02.12
	* loplat SDK version 1.8.8 release
		* android 8.0(Oreo) 백그라운드 제약 사항 대응
	
* 2018.01.29
	* loplat SDK version 1.8.7 release
		* 인식 성능 개선 및 버그 수정

* 2018.01.22
  - loplat SDK version 1.8.6 release
	- Area, Complex 정보 추가
	- 인식 성능 개선 및 버그 수정

* 2018.01.17
   - loplat SDK version 1.8.5 release
	- Gravity 제공을 위한 기능 개선
	- Advanced Tracker 추가

* 2017.12.27
   - loplat SDK version 1.8.4 release
	- Gravity 연동
	
* 2017.11.23
   - loplat SDK version 1.8.3 release
	- 인식 성능 개선 및 버그 수정
	
* 2017.11.05
   - loplat SDK version 1.8.2 release 
	- 인식 성능 개선 및 버그 수정
	
* 2017.08.07
    - loplat SDK version 1.8.1 release
        - Nearby event, Enter Event 분리
	- EnterType class deprecated

* 2017.07.4
    - loplat SDK version 1.8.0 release
        - 업데이트 내용 : 인식 성능 개선 

* 2017.06.25
    - loplat SDK version 1.7.10 release
	    - 업데이트 내용
		    - Retrofit 라이브러리 적용
		    - 인식 성능 개선

* 2017.06.19
    - loplat SDK version 1.7.9 release
        - 업데이트 내용
            1. SDK 동작 상태를 확인 할 수 있는 기능 추가
* 2017.4.22
    - loplat SDK version 1.7.8 release
        - 업데이트 내용
            1. WiFi SSID 확인 중 발생하는 에러 보완 처리

* 2017.3.23
    - loplat SDK version 1.7.7 release
        - 업데이트 내용: PlengiListener 동작 보완

* 2017.3.7
	- loplat SDK version 1.7.6 release
		- 업데이트 내용
			1. 위치인식된 장소의 id를 loplatid 하나로 통합 (placeid는 더이상 전달되지 않음)
			2. 'unknown place'(학습되지 않은 장소)에 대한 enter/leave event 발생 중단
			3. PlengiResponse.EnterType 추가
            4. Plengi.getInstance(Context context).getVisitList(), Plengi.getInstance(Context context).getPlaceList() 삭제 
* 2016.12.20
    - loplat SDK version 1.7.5 release
        - 업데이트 내용
            1. Location Provider 획득 실패에 대한 예외 처리
            2. DB access error 보완
            3. LocationMonitorService 동작 확인 기능 추가
        
* 2016.12.13
    - loplat SDK version 1.7.4 release
        - 업데이트 내용
            1. DB access error 보완
            2. 위치 권한 미설정으로 WiFi Scan 결과값 획득 실패에 대한 보완 처리
            3. 위치 권한 확인 중 PackageManager가 죽는 현상에 따른 에러 보완 처리

* 2016.11.30
    - SDK version name 변경: 1.71 -> 1.7.1, 1.72 -> 1.7.2
    - lolat SDK version 1.7.3 release
        - 업데이트 내용
            1. wifi scan 요청 후, OS내에서 발생하는 에러에 대해 보완 처리

* 2016.11.17
    - loplat SDK version 1.72 release
        - 업데이트 내용: 일부 모델 wifi state access 에러에 대한 보완 처리  

* 2016.11.02
	- loplat SDK version 1.71 release
		- 업데이트 내용: Tracker Mode 장소 인식 개선

* 2016.10.17
    - 방문 매장/장소 기록 확인하기 (History of Places) function 삭제
    - **주의**: 현재 Plengi.getInstance(Context context).getVisitList()은 deprecated 되었으니, 이점 유의 해주시길 바랍니다.

* 2016.10.11
	* loplat SDK version 1.7 release
		* 업데이트 내용
			1. Init시 uniqueUserId 업데이트 관련 버그 개선
			2. 일부매장에서 현재요청시 발생하는 에러에 대해 보완 처리 
	
* 2016.08.09
	* loplat SDK version 1.6 release
		* 업데이트 내용
		    1. Init시 uniqueUserId 수정이 가능하도록 변경
		    2. BOOT_COMPLETED시 SDK 자동 재시작 설정
		    3. 편의점 매장 인식 속도 개선

* 2016.05.19
	* loplat SDK version 1.5 release 
		- 업데이트 내용
			1. 일부 모델 간헐적 db access 에러에 대해 보완 처리
			2. Tracking Mode, Tracking Event 추가
			3. 위치 정보 결과에 loplatid 추가
			
* 2016.04.22
	* loplat SDK version 1.4 release
		- 업데이트 내용: library 포함하여 build 시에 proguard 관련 오류 제거


* 2016.02.12 
	* loplat SDK version 1.3 release
		* 업데이트 내용
			1. 장소 정보에 client_code 추가
			2. 동일 client_code를 가지는 장소 내에서의 이동 시에 중복해서 ENTER Event를 발생하지 않도록 변경
		- **주의**: 이전 library와 db 호환이 되지 않아, 이전버전으로 만든 앱은 꼭 삭제 후 새 버전 설치
	- 장소학습기 (loplat cook) 릴리즈
	

* 2016.01.27 - initial release

# Plengi SDK

## Intallation

### How to import

#### 1. repository 추가 하기
- In your top-level project build.gradle, add

		maven { url "http://maven.loplat.com/artifactory/plengi"}

	as repositories under allprojects -> repositoreies.
	For example, 
		
		allprojects {
			repositories {
		        jcenter()
		        mavenCentral()
		        maven { url "http://maven.loplat.com/artifactory/plengi}
		        google()
			}
		}

#### 2. loplat SDK dependency 추가 하기
	
	compile 'com.loplat:placeengine:1.8.6'

- **참고**: 현재 최신 버전 1.8.6

## Contents
1. SDK Setup
	- 계정 만들기 
	- Permission 등록
	- Receiver & Service 등록
	- Library 적용하기
	- Constraints 
2. SDK 기능
3. SDK 초기화 및 시작하기
	- PlengiListenr 생성
	- Plengi Instance 생성 및 EventListener 등록
	-  Plengi Init
	- Plengi 모드 설정
	- WiFi 스캔 주기 설정
	- Gravity 연동하기
	- Start/Stop
	- 장소 인식 결과
4. API
	- 현재 위치 확인하기
	- 현재 사용자 상태(Move/Stay) 확인하기 
	- 현재 장소 정보 가져오기

### 1. SDK Setup

#### 계정 만들기  
* Plengi SDK를 사용하기 위해서는 clientid와 clientsecret 필요합니다.  
	
	  > * clientid & clientsecret: loplat server로 접근하기 위한 ID와 PW  
* test를 원하시는 분은 clientid: loplatdemo, clientsecret: loplatdemokey 사용하세요.  
*  정식 clientid와 clientsecret을 원하는 분은 아래에 기입 된 메일 주소로 연락 바랍니다. 
 
#### Permission
* SDK를 적용하면 하기 권한이 자동으로 추가됩니다.  
	<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />  
	<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
	<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
	<uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>

	* ACCESS_FINE_LOCATION: GPS를 이용하여 현재 위치의 위도와 경도 값을 획득할 수 있는 권한  
	* ACCESS_COARSE_LOCATION: WiFi 혹은 Network를 이용하여 현재 위치의 위도와 경도 값을 획득할 수 있는 권한  
	* ACCESS_NETWORK_STATE: 네트워크 상태를 확인할 수 있는 권한  
	* ACCESS_WIFI_STATE / CHANGE_WIFI_STATE: 주변 WiFi AP들을 스캔하기 위한 권한
	* INTERNET: 인터넷을 사용할 수 있는 권한
	* RECEIVE_BOOT_COMPLETED: 핸드폰 부팅되는 과정을 브로드캐스팅하기 위한 권한

#### Constraints

* Android OS Marshmallow 버전 부터 WiFi Scan시 아래와 같은 위치 권한이 필요합니다.

		<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
		<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
		
* Plengi SDK 동작하기 위해서 OS 버전 및 위치 권한을 확인을 위한 작업이 필요합니다.
	
	> * Plengi SDK 기능을 사용 전 OS 버전 과 위치 권한 확인이 필요합니다.

	> * Marshmallow 버전 부터 위치 권한은 Dangerous Permission으로 구분 되어 권한 획득을 위한 코드가 필요합니다.

	> * sample코드에 구현된 checkWiFiScanConditionInMashmallow(Context context) 참고 바랍니다.
	
	> * 좀 더 자세한 사항은 Google Developer를 참고 바랍니다. [Google Developer](http://developer.android.com/intl/ko/training/permissions/requesting.html)

* 사용자의 현재 위치를 확인 하기 위해서 위치 모드가 켜져 있어야 합니다.
	
	>  * sample코드에 구현된 checkWiFiScanConditionInMashmallow(Context context) 참고 바랍니다.

#### Library 적용하기

##### Gravity 사용을 위한 Google Play Services library 적용하기

Gravity를 사용하기 위해서 google play service library 적용을 위해서 build.gradle의 denpendency에 아래와 같이  선언 필요합니다.

	compile 'com.google.android.gms:play-services-ads:11.8.0'

##### Retrofit 및 GSON library 적용하기

loplat SDK 1.7.10 이상 버전 부터 위치 확인 요청시 서버와의 통신을 위해 Retrofit 및 GSON library 사용합니다. Retrofit 및 GSON 라이브러리 적용을 위해서  Android Studio의 build.gradle에 다음과 같이 추가합니다.

 		compile 'com.squareup.retrofit2:retrofit:2.3.0'
   		compile 'com.squareup.retrofit2:converter-gson:2.3.0'
   		compile 'com.squareup.okhttp3:okhttp:3.8.1'

  - 참고: proguard를 사용할 시에는 아래와 같이 proguard 설정을 추가해야 합니다.

            -dontwarn okio.**
            -dontwarn javax.annotation.**
            -keepclasseswithmembers class * {
                @retrofit2.http.* <methods>;
            }

### 2. SDK 기능

#### Recognize a place
* Plengi.getInstance(Context Context).refreshPlace() : 주변 WiFi AP들을 탐색하여, 서버에게 현재 위치 정보를 요청
* PlengiEventListener: listen()를 통해 Plengi 서버로 부터 받은 결과를 수신하여 PlengiBraodcastReceiver로 송신
* PlengiBroadcastReciver: PlengiEventLinstener로 부터 받은 결과 처리
 
#### Place Event
* Plengi.getInstance(Context Context).start()/ Plengi.getInstance(Context Context).stop()을 통해서 모니터링을 on/off
* PlengiEventListener와 PlengiBroadcastReceiver를 통해 Event 결과를 처리

#### Stay or Move
* Plengi.getInstance(Context Context).getCurrentPlaceStatus()를 통해 사용자가 현재 이동 중인지 한 장소에 머물고 있는지 확인 가능

#### Get a current place information
* Plengi.getInstance(Context context).getCurrentPlaceInfo()를 통해 사용자가 방문 중인 장소 정보 불러오기


### 3. SDK 초기화 및 시작하기

1. PlengiListner 생성
	* PlengiListener를 상속받은 listener class를 생성합니다.
		- loplat서버로 부터 받은 모든 asynchronous Result는 모두 해당 리스너를 통해 전달됩니다.
		- PLACE(Recognize a place), PLACE_EVENT(Enter/Leave/Nearby, Recognizer mode), PLACE_TRACKING(Tracker mode) 등의 Event에 따른 결과를 작성합니다. (LoplatPlengiListener.Java 참조 바람)
   
2. Plengi instance 생성 및 EventListner 등록
	- Application class 상속 받아 Plengi class 생성합니다. (LoplatSampleApplication.java 참고 바람)
	- Plengi instance를 생성한 후, 1번에서 생성한 Listener를 등록합니다.
	
3. Plengi init (1회만 수행하면 됨, MainActivity.java 참고 바람)
	- 사용자의 매장/장소 방문을 모니터링하기 위해 Plengi Engine을 초기화합니다.
	- Plengi init은 다음과 같이 선언을 합니다.  
	
			Plengi.getInstance(MainActivity.this).init(clientId,clientSecret,uniqueuserId);  
	- init을 위해 clientid, clientsecret, uniqueUserId를 인자값으로 전달해주셔야합니다.  
		* clientid & clientsecret: loplat server로 접근하기 위한 ID와 PW입니다.  
		* test를 원하시는 분은 clientid: loplatdemo, clientsecret: loplatdemokey 사용하세요.  
		* 정식 id와 secret을 원하는 분은 아래에 기입 된 메일 주소로 연락 바랍니다.  
		* uniqueUserId: App에서 사용자를 식별하기 위한 ID입니다. (ex, 광고id,id,....,etc.)
			* 이메일, 폰번호와 같이 개인정보와 관련된 정보는 전달하지 않도록 주의해주시기 바랍니다.

4. Plengi 모드 설정  
	* 매장/장소 방문을 확인하기 위한 모니터링 모드를 선택합니다.    
	* 사용자의 매장/장소 방문을 확인하기 위하여 아래와 같은 2가지 모드를 제공하고 있습니다.  
	* **참고: 사용자 매장 방문 확인을 위해 기본으로 제공 되는 모드는 Recognizer 모드 입니다. Tracker 모드를 사용하기 위해서는 협의가 필요 하오니 아래에 기입된 메일로 연락 바랍니다.** 

		> * Recognizer Mode: 일정시간동안(5분이상) 한 장소에 머무를 경우 사용자의 위치를 확인합니다.
		> * Tracker Mode: 사용자의 위치를 일정주기마다 확인합니다.
	* 모드 설정은 다음과 같이 선언을 합니다.  (Recognizer, Tracker 둘 중 하나 선택)
    
		    Plengi.getInstance(MainActivity.this).setMonitoringType(PlengiResponse.MonitoringType.STAY);  //Recognizer mode
		    Plengi.getInstance(MainActivity.this).setMonitoringType(PlengiResponse.MonitoringType.TRACKING);  //Tracker mode

5. WiFi 스캔 주기 설정
	* 사용자의 매장/장소 방문 확인을 위한 WiFi Scan 주기를 설정합니다.
	* WiFi scan 주기는 다음과 같이 설정합니다.
		- Recognizer mode 일 경우  move, stay에 대해 주기를 설정합니다. 
	
				Plengi.getInstance(MainActivity.this).setScanPeriod(3*60*1000, 6*60*1000);  // move: 3 mins, stay: 6 mins  
			- move:  매장/장소를 인식하기 위한 기본 WFi scan 주기이며 default 값으로 3분이 설정되어 있습니다.  
                - 3분이하의 분으로 주기 설정시 default 값인 3분으로 설정이 됩니다.
			- stay: 매장/장소가 인식 된 후 WiFi scan 주기이며 default 값으로 6분이 설정되어 있습니다.  
                - 6분이하의 분으로 주기 설정시 default 값인 6분으로 설정이 됩니다.
		- Tracker mode 일 경우 분 단위로 설정이 가능하며 default 값으로 2분이 설정되어 있습니다.  
			   - 1분이하의 분으로 주기 설정시 주기는 1분으로 설정이 됩니다. (최소 주기 값: 1분)
		
                    Plengi.getInstance(MainActivity.this).setScanPeriodTracking(2*60*1000); // scanperiod: 2 mins 

6. Gravity 연동하기
	* Gravity 연동은 **SDK version 1.8.6**부터 연동이 가능합니다.
	* **Gravity**를 통해 **푸쉬 메시지** (광고 및 알림 메시지)를 받기 위해서는 앱이 다시 시작하거나(onResme) 광고 알림 허용을 한 시점 아래와 같이 코드 작성이 필요 합니다.
			
			Plengi.getInstance(mContext).enableAdNetwork(true);            // 푸쉬 메시지 설정 on
	        Plengi.getInstance(mContext).setAdNotiIcon(R.drawable.ic_launcher);  // 푸쉬 메세지 icon
        
7. Start/Stop
	- 사용자 장소/매장 방문 모니터링을 시작하거나 정지 할 수 있습니다.
	- 설정된 주기마다 WiFi 신호를 스캔하여 사용자의 위치를 확인합니다.  
	- 사용자의 위치 정보는 PlengiEventListener로 전달됩니다.
	-  모니터링 시작과 정지는 다음과 같이 선언합니다.  
	
			Plengi.getInstance(MainActivity.this).start(); //Monitoring Start  
			Plengi.getInstance(MainActivity.this).stop(); //Monitoring Stop

    - 모니터링 상태 확인은 Plengi.getEngineStatus를 통해서 확인 할 수 있습니다.
        - 예시코드

				int engineStatus = Plengi.getInstance(this).getEngineStatus();
				if(engineStatus == PlaceEngine.EngineStatus.STARTED)
				{
				    //Monitoring On
				}
				else if(engineStatus == PlaceEngine.EngineStatus.STOPPED)
				{
				    //Monitoring Off
				}

	
8. 장소 인식 결과

* **참고**:
	1. SDK 1.7.5 이하 버전은 장소id는 loplatid(서버에 학습된 장소 id), placeid 둘 다 전달되며,  1.7.6 이상 버전 부터 장소 id는 loplatid로 통합되어 전달 됩니다.**
	2. SDK 1.8.6부터 장소 인식시 인식된 장소 결과에 따라 area(상권정보), complex(복합몰) 정보가 추가로 전달됩니다. 상권만 인식 된 경우에는 place 정보가 null로 넘어가니 코드 작성시 주의 부탁드립니다.
	3. SDK 1.8.6부터 lat_est, lng_est 항목은 삭제 되었습니다.


* 현재 위치가 인식 된 경우

	> * 위치 정보 결과: **Place** (PlengiResponse.Place Class, response.place로 획득 가능)
	> * type: PlengiResponse.ResponseType.PLACE  
		 
		 * accuracy > threshold: 현재 위치 내에 있는 경우  
		 * 그 외에 경우: 현재 위치 근처에 있는 경우  
	> 
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
				    ~~public double lat_est;       // 예측된 위치의 위도~~  v1.8.6에서 삭제
				    ~~public double lng_est;       // 예측된 위치의 경도~~ v1.8.6에서 삭제  
				    public String client_code;   // 클라이언트 코드
				    public String address;       // 장소 (구)주소
				    public String address_road;  // 장소 신 주소
				    public String post                // 우편번호
				    
				    


	> * 상권 정보 결과: **Area** (PlengiResponse.Area Class, response.area로 획득 가능)
	> * type: PlengiResponse.ResponseType.Area  
		
		*  장소 위치 요청한 장소가 상권 안일 경우 상권 정보가 인식 결과에 함께 같이 전달됩니다.
		*  위도 및 경도는 아래의 조건으로 결과가 전달됩니다.
			1. 장소 인식 결과값이 있다면 -> 인식된 장소 위도/ 경도
			2. 장소 인식 결과값이 없으면 -> device의 위도/경도
	> 
					public int id;         // Area ID
			        public String name;    // 상권 이름
			        public String tag;       // 상권 위치 [도, 시 단위 ex) 서울, 경기도, 인천]
			        public double lat;      // 위도 
			        public double lng;     // 경도


	> * Complex 정보 결과: **Complex** (PlengiResponse.Complex Class, reponse.complex로 획득 가능)
	> * type: PlengiResponse.ResponseType.Complex  
		
		* 인식된 장소가 복합몰 내인 경우 복합몰 정도도 함께 인식 결과에 포함되어 전달됩니다.
	>
		        public int id;        // complex ID
		        public String name;   // 복합몰 이름
		        public String branch_name;  //지점명
		        public String category;        //카테고리 명
		        public String category_code;   //카테고리 코드

	* 현재위치 획득 실패시
		>* type: PlengiResponse.ResponseType.PLACE
		>* result: PlengiResponse.Result.ERROR_CLOUD_ACCESS
		>* errorReason : Location Acquisition Fail  
	
	* Client 인증 실패시
		>* type: PlengiResponse.ResponseType.PLACE
		>* result: PlengiResponse.Result.ERROR_CLOUD_ACCESS
		>* errorReason : Not Allowed Client

### 4. API
#### 현재 위치 확인하기

* 현재 사용자가 위치한 장소/매장 정보를 loplat 서버를 통해 확인할 수 있습니다.  
* 현재 장소 정보를 서버에서 받아오고자 하는 경우 다음과 같은 선언을 합니다.
		
		Plengi.getInstance(MainActivity.this).refreshPlace();
* WiFi AP들을 수집하여 loplat 서버에게 현재 사용자의 위치 정보를 요청합니다.
* loplat 서버는 최적의 위치정보를  PlengiEventListener로 전달합니다.  

* 자세한 사항은 API문서를 참조해주시기 바랍니다. [현재 위치 확인하기](https://github.com/loplat/loplat-sdk-android/wiki/1.-현재-위치-확인하기)
	
#### 현재 사용자 상태 확인하기  (Stay or Move)
-  현재 사용자가 이동(Move) 중인지 매장/장소에 머무르고(Stay) 있는지 확인할 수 있습니다.
- 현재 사용자의 상태를 확인하기 위하여 다음과 같이 선언을 합니다. 
 
		Plengi.getInstance(this).getCurrentPlaceStatus();
- 자세한 사항은 API문서를 참조해주시기 바랍니다. [현재 사용자 상태 확인하기](https://github.com/loplat/loplat-sdk-android/wiki/2.-현재-사용자-상태-확인하기)

#### 현재 장소 정보 가져오기
* 현재 사용자가 머무르고 있는 장소/매장 정보를 확인 할 수 있습니다.
* 현재 사용자가 위치한 장소/매장 정보를 확인하기 위하여 다음과 같이 선언을 합니다.

		Plengi.getInstance(this).getCurrentPlaceInfo();  
* 매장/장소 정보는 PlengiResponse.Place로 전달됩니다. ([현재 위치 확인하기 참조](https://github.com/loplat/loplat-sdk-android#4-현재-위치-확인하기))
* **참고사항**: 현재 사용자 상태가 STAY일 경우에만 정확한 장소/매장 정보를 획득 할 수 있습니다. 
* 자세한 사항은 API문서를 참조해주시기 바랍니다. [현재 장소 정보 가져오기](https://github.com/loplat/loplat-sdk-android/wiki/3.-현재-장소-정보-가져오기)

## History
* 2018.01.22
  - loplat SDK versio 1.8.6 release
	- Area, Complex 정보 추가
	- 인식 성능 개선 및 버그 수정

* 2018.01.17
   - loplat SDK versio 1.8.5 release
   		- Gravity 제공을 위한 기능 개선
		- Advanced Tracker 추가

* 2017.12.27
   - loplat SDK versio 1.8.4 release
   		- Gravity 연동
	
* 2017.11.23
   - loplat SDK versio 1.8.3 release
   		- 인식 성능 개선 및 버그 수정
	
* 2017.11.05
   - loplat SDK versio 1.8.2 release 
   		- 인식 성능 개선 및 버그 수정
	
* 2017.08.07
    - loplat SDK versio 1.8.1 release
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

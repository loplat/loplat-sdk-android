#### Note ####
* If you want to see loplat REST API, please refer to https://github.com/loplat/loplat-rest-api for details
* If you want to see Plengi iOS SDK, please refer to https://github.com/loplat/loplat-sdk-ios for details

# Plengi SDK

## History
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

## Function

#### 1. Recognize a place
* Plengi.getInstance(Context Context).refreshPlace() : 주변 WiFi AP들을 탐색하여, 서버에게 현재 위치 정보를 요청
* PlengiEventListener: listen()를 통해 Plengi 서버로 부터 받은 결과를 수신하여 PlengiBraodcastReceiver로 송신
* PlengiBroadcastReciver: PlengiEventLinstener로 부터 받은 결과 처리
 
#### 2. Place Event
* Plengi.getInstance(Context Context).start()/ Plengi.getInstance(Context Context).stop()을 통해서 모니터링을 on/off
* PlengiEventListener와 PlengiBroadcastReceiver를 통해 Event 결과를 처리

#### 3. Stay or Move
* Plengi.getInstance(Context Context).getCurrentPlaceStatus()를 통해 사용자가 현재 이동 중인지 한 장소에 머물고 있는지 확인 가능

#### 4. Get a current place information
* Plengi.getInstance(Context context).getCurrentPlaceInfo()를 통해 사용자가 방문 중인 장소 정보 불러오기

## Contents
1. 디렉토리 및 샘플코드 소개
2. SDK Setup
	- 계정 만들기 
	- Permission 등록
	- Receiver & Service 등록
	- Constraints 
3. SDK 초기화 및 시작하기
	- PlengiListenr 생성
	- Plengi Instance 생성 및 EventListener 등록
	-  Plengi Init
	- Plengi 모드 설정
	- WiFi 스캔 주기 설정
	- Start/Stop
4. 현재 위치 확인하기
5. 현재 사용자 상태(Move/Stay) 확인하기
6. 현재 장소 정보 가져오기

### 1. 디렉토리 소개  및 샘플 코드 소개

#### 디렉토리 소개
- /sample : 샘플코드
- /library : plengi.aar 파일이 실제 라이브러리 파일

	> * jar 라이브러리가 필요한 경우 plengi.jar를 사용하고 AndroidManifest.xml 에 있는 권한을 추가 

- /javadoc : library 설명 문서
- /place_registerer : 장소 학습기 안드로이드 앱

#### 샘플코드 간략 소개
* LoplatSampleApplication.java : Application Class를 상속받아 plengi engine을 초기화 수행

* LoplatPlengiListener.java : PlengiListener를 상속받아 loplat 서버로부터 위치 획득 결과를 받음

	※ loplat서버로부터 다음과 같은 위치 정보가 제공
	  - a. Place Event: enter or leave places
	  - b. Recognizing Places

* MainActivity.java : 장소 변화 모니터링 on/off, 현재 위치 획득 요청 및 결과 표시 해줌

### 2. SDK Setup

#### 계정 만들기  
* Plengi SDK를 사용하기 위해서는 clientid와 clientsecret 필요합니다.  
	
	  > * clientid & clientsecret: loplat server로 접근하기 위한 ID와 PW  
* test를 원하시는 분은 clientid: loplatdemo, clientsecret: loplatdemokey 사용하세요.  
*  정식 clientid와 clientsecret을 원하는 분은 아래에 기입 된 메일 주소로 연락 바랍니다. 
 
#### Permission (AnroidManifest.xml 참고)  
* SDK를 사용하기 위해서 AndroidManifest.xml에 아래와 같은 권한들을 설정해야합니다.  
	 	
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
		

#### Receiver & Service (AnroidManifest.xml 참고)
* SDK를 사용하기 위해서 AndroidManifest.xml에 Broadcast recevier와 Servcie 등록을 해야합니다.

		<receiver
			android:name="com.loplat.placeengine.EventReceiver"
			android:enabled="true"
			android:exported="true" >
			<intent-filter>
				<action android:name="android.intent.action.BOOT_COMPLETED" />
				<action android:name="android.net.wifi.SCAN_RESULTS" />
				<action android:name="android.net.wifi.WIFI_AP_STATE_CHANGED" />
				<action android:name="com.loplat.placeengine.event.scanwifi" />
			</intent-filter>
		</receiver>
		
		<service android:name="com.loplat.placeengine.location.LocationMonitorService" />

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

### 3. SDK 초기화 및 시작하기

1. PlengiListner 생성
	* PlengiListener를 상속받은 listener class를 생성합니다.
		- loplat서버로 부터 받은 모든 asynchronous Result는 모두 해당 리스너를 통해 전달됩니다.
		- PLACE(Recognize a place), PLACE_EVENT(Enter/Leave, Recognizer mode), PLACE_TRACKING(Tracker mode) 등의 Event에 따른 결과를 작성합니다. (LoplatPlengiListener.Java 참조 바람)
		- PLACE_EVENT 발생시 place 정보와 함께 EnterType이 전달됩니다.
			- ENTER, NEARBY 두가의 값으로 제공됨
			- EnterType.ENTER : 현재 사용자의 위치가 매장안 이라고 인식 된 경우, accuracy > threshold
			- EnterType.NEARBY : 현재 사용자의 위치가 매장 주변이라고 인식 된 경우, accuracy < threshold
   
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
6. Start/Stop
	- 사용자 장소/매장 방문 모니터링을 시작하거나 정지 할 수 있습니다.
	- 설정된 주기마다 WiFi 신호를 스캔하여 사용자의 위치를 확인합니다.  
	- 사용자의 위치 정보는 PlengiEventListener로 전달됩니다.
	-  모니터링 시작과 정지는 다음과 같이 선언합니다.  
	
			Plengi.getInstance(MainActivity.this).start(); //Monitoring Start  
			Plengi.getInstance(MainActivity.this).stop(); //Monitoring Stop

### 4. 현재 위치 확인하기

* 현재 사용자가 위치한 장소/매장 정보를 loplat 서버를 통해 확인할 수 있습니다.  
* 현재 장소 정보를 서버에서 받아오고자 하는 경우 다음과 같은 선언을 합니다.
		
		Plengi.getInstance(MainActivity.this).refreshPlace();
* WiFi AP들을 수집하여 loplat 서버에게 현재 사용자의 위치 정보를 요청합니다.
* loplat 서버는 최적의 위치정보를  PlengiEventListener로 전달합니다.  
* PlengiEventListener에 전달 되는 response 종류는 다음과 같습니다.
* **참고: SDK 1.7.5 이하 버전은 장소id는 loplatid(서버에 학습된 장소 id), placeid 둘 다 전달되며,  1.7.6 이상 버전 부터 장소 id는 loplatid로 통합되어 전달 됩니다.**


	*  현재 위치가 인식 된 경우

	> * type: PlengiResponse.ResponseType.PLACE  
	> * 위치 정보 결과 (PlengiResponse.Place Class, response.place로 획득 가능)
	> 
				 	public long loplatid;       // 장소 id
				    public String name;        // 장소 이름
				    public String tags;        // 장소와 관련된 tag
				    public int floor;          // 층 정보
				    public String category;    // 장소 유형
				    public double lat;         // 인식된 장소의 위도
				    public double lng;	       // 인식된 장소의 경도 
				    public float accuracy;     // 정확도
				    public float threshold;    // 한계치
				    public double lat_est;     // 예측된 위치의 위도 
				    public double lng_est;     // 예측된 위치의 경도  
				    public String client_code; // 클라이언트 코드
					
				    
	> * accuracy > threshold: 현재 위치 내에 있는 경우  
	> * 그 외에 경우: 현재 위치 근처에 있는 경우  

	* 현재위치 획득 실패시
		>* type: PlengiResponse.ResponseType.PLACE
		>* result: PlengiResponse.Result.ERROR_CLOUD_ACCESS
		>* errorReason : Location Acquisition Fail  
	
	* Client 인증 실패시
		>* type: PlengiResponse.ResponseType.PLACE
		>* result: PlengiResponse.Result.ERROR_CLOUD_ACCESS
		>* errorReason : Not Allowed Client

* 자세한 사항은 API문서를 참조해주시기 바랍니다. [현재 위치 확인하기](https://github.com/loplat/loplat-sdk-android/wiki/1.-현재-위치-확인하기)
	
### 5. 현재 사용자 상태 확인하기  (Stay or Move)
-  현재 사용자가 이동(Move) 중인지 매장/장소에 머무르고(Stay) 있는지 확인할 수 있습니다.
- 현재 사용자의 상태를 확인하기 위하여 다음과 같이 선언을 합니다. 
 
		Plengi.getInstance(this).getCurrentPlaceStatus();
- 자세한 사항은 API문서를 참조해주시기 바랍니다. [현재 사용자 상태 확인하기](https://github.com/loplat/loplat-sdk-android/wiki/2.-현재-사용자-상태-확인하기)

### 6. 현재 장소 정보 가져오기
* 현재 사용자가 머무르고 있는 장소/매장 정보를 확인 할 수 있습니다.
* 현재 사용자가 위치한 장소/매장 정보를 확인하기 위하여 다음과 같이 선언을 합니다.

		Plengi.getInstance(this).getCurrentPlaceInfo();  
* 매장/장소 정보는 PlengiResponse.Place로 전달됩니다. ([현재 위치 확인하기 참조](https://github.com/loplat/loplat-sdk-android#4-현재-위치-확인하기))
* **참고사항**: 현재 사용자 상태가 STAY일 경우에만 정확한 장소/매장 정보를 획득 할 수 있습니다. 
* 자세한 사항은 API문서를 참조해주시기 바랍니다. [현재 장소 정보 가져오기](https://github.com/loplat/loplat-sdk-android/wiki/3.-현재-장소-정보-가져오기)
		
## Notice 

* 코드 구현과 관련해서는 sample코드와 javadoc 폴더 참고 바람
* 실제 테스트를 위해서는 기존에 학습된 장소가 있어야 함

	> loplat 홈페이지 [Demo&SDK](http://loplat.azurewebsites.net/demo.html#)에서 loplat cook 이라는 학습기 앱을 다운받아서 인식을 원하는 장소에서 학습을 수행함 
	
  	> 그 후에 loplat_demo를 통해 테스트를 해 보면 장소를 인식하는 것을 확인할 수 있음
  	
* 기술 관련 문의는 mjlee@loplat.com으로 메일 보내주시기 바랍니다.
* 정식 id와 secret을 원하시는 분은 yeddie@loplat.com으로 아래의 내용을 기입하여 보내 주시기 바랍니다.
 - a. 이름
 - b. 회사
 - c. 사용 목적 
  
 

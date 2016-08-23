#### Note ####
* If you want to see loplat REST API, please refer to https://github.com/loplat/loplat-rest-api for details
* If you want to see Plengi iOS SDK, please refer to https://github.com/loplat/loplat-sdk-ios for details

# Plengi SDK

## History
* 2016.08.09
    - Init시 uniqueUserId 수정이 가능하도록 변경
    - BOOT_COMPLETED시 SDK 자동 재시작 설정
    - 편의점 매장 인식 속도 개선

* 2016.06.17
    - Tracking Mode, Tracking Event 추가
    - 위치 정보 결과에 loplatid 추가

* 2016.05.19 
	- 일부 모델 간헐적 db access 에러에 대해 보완 처리

* 2016.04.22
	- library 포함하여 build 시에 proguard 관련 오류 제거


* 2016.02.12 
	- 장소 정보에 client_code 추가
	- 동일 client_code를 가지는 장소 내에서의 이동 시에 중복해서 ENTER Event를 발생하지 않도록 변경
	- 장소학습기 (loplat cook) 릴리즈
	- **주의**: 이전 library와 db 호환이 되지 않아, 이전버전으로 만든 앱은 꼭 삭제 후 새 버전 설치

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

#### 4. History of places  
* Plengi.getInstance(this).getVisitList()를 통해 방문 장소 기록을 획득

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
6. 방문 매장/장소 기록 확인하기

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
	  - b. Recognizing Place
	  - c. History of Places

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

	* ACCESS_FINE_LOCATION: GPS를 이용항여 현재 위치의 위도와 경도 값을 획득할 수 있는 권한  
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

* 현재 사용자가 위치한 장소/매장 정보를 확인할 수 있습니다.  
* 현재 장소의 위치를 서버에서 받아오고자 하는 경우 다음과 같은 선언을 합니다.
		
		Plengi.getInstance(MainActivity.this).refreshPlace();
* WiFi AP들을 수집하여 loplat 서버에게 현재 사용자의 위치 정보를 요청합니다.
* loplat 서버는 최적의 위치정보를  PlengiEventListener로 전달합니다.  
* PlengiEventListener에 전달 되는 response 종류는 다음과 같습니다.
   
	> * Type: PlengiResponse.ResponseType.PLACE  
	> * 위치 정보 결과 (PlengiResponse.Place Class, response.place로 획득 가능)
	> 
				 	public long placeid;       // 장소 id
				    public String name;        // 장소 이름
				    public String tags;        // 장소와 관련된 tag
				    public int floor;          // 층 정보
				    public String category;    // 장소 유형
				    public double lat;         // 위도
				    public double lng;	       // 경도 
				    public float accuracy;     // 정확도
				    public float threshold;    // 한계치
				    public String client_code; // 클라이언트 코드
				    public long loplatid;      // 서버에 학습된 장소 id  
				    
	> * accuracy > threshold: 현재 위치 내에 있는 경우  
	> * 그 외에 경우: 현재 위치 근처에 있는 경우  
	> * 현재 위치가 인식 된 경우 현재 위치 정보 획득, 그렇지 않을 경우는 'unknown'으로 표시 됨

* 자세한 사항은 API문서를 참조해주시기 바랍니다. [현재 위치 확인하기](https://github.com/loplat/loplat-sdk-android/wiki/1.-현재-위치-확인하기)
	
### 5. 현재 사용자 상태 확인하기  (Stay or Move)
-  현재 사용자가 이동(Move) 중인지 매장/장소에 머무르고(Stay) 있는지 확인할 수 있습니다.
- 현재 사용자의 상태를 확인하기 위하여 다음과 같이 선언을 합니다. 
 
		Plengi.getInstance(this).getCurrentPlaceStatus
- 자세한 사항은 API문서를 참조해주시기 바랍니다. [현재 사용자 상태 확인하기](https://github.com/loplat/loplat-sdk-android/wiki/2.-현재-사용자-상태-확인하기)

### 6. 방문 매장/장소 기록 확인하기 (History of Places)
* 사용자의 방문한 매장/장소 이력을 확인할 수 있습니다.
* 사용자의 방문 장소 이력을 알고자 하는 경우 다음과 같이 선언을 합니다.
		
		Plengi.getInstance(MainActivity.this).getVisitList()
* 자세한 사항은 API문서를 참조해주시기 바랍니다. [방문 매장/장소 기록 확인하기](https://github.com/loplat/loplat-sdk-android/wiki/3.-방문-매장-장소-기록-확인하기)
		
## Notice 

* 코드 구현과 관련해서는 sample코드와 javadoc 폴더 참고 바람
* 실제 테스트를 위해서는 기존에 학습된 장소가 있어야 함

	> /place_registerer 폴더에 있는 loplat cook 이라는 학습기 앱을 다운받아서 인식을 원하는 장소에서 학습을 수행함 
	
  	> 그 후에 loplat_demo를 통해 테스트를 해 보면 장소를 인식하는 것을 확인할 수 있음
  	
* 기술 관련 문의는 stkment@gmail.com으로 메일 보내주시기 바랍니다.
* 정식 id와 secret을 원하시는 분은 Lamen2357@loplat.com으로 아래의 내용을 기입하여 보내 주시기 바랍니다.
 - a. 이름
 - b. 회사
 - c. 사용 목적 
  
 

#### Note ####
* If you want to see loplat REST API, please refer to https://github.com/loplat/loplat-rest-api for details
* If you want to see Plengi iOS SDK, please refer to https://github.com/loplat/loplat-sdk-ios for details 

# Plengi SDK

## History

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
현재 Plengi SDK에서는 사용자의 위치를 확인하기 위하여 아래와 같은 mode를 제공함

1. Recognizer Mode
	- 장소를 방문 할 때 마다 사용자의 위치를 확인 하는 방식
2. Tracker Mode
	- 일정 주기마다 사용자의 위치를 확인하는 방식


### 1. Recongize Mode

#### a. Recognize a place
		Plengi.getInstance(Context context).refreshPlace()
	
- Plengi.getInstanc(Context Context).refreshPlace() : 주변 WiFi AP들을 탐색하여, 서버에게 현재 위치 정보를 요청
  
- PlengiEventListener: listen()를 통해 Plengi 서버로 부터 받은 결과를 수신하여 PlengiBraodcastReceiver로 송신
  
- PlengiBroadcastReciver: PlengiEventLinstener로 부터 받은 결과 처리

    |parameter|data type|description|
    |:-----:|:-------:|:------:|
    |context| Context|-|
    
- response
    - reuslt: PlengiResponse.Result를 통해서 결과를 전달 받음
    	- ex) PlengiResponse.Result.SUCESS/ERROR_CLOUD_ACCESS
        
    - event type: PlengiResponse.ResponseType를 통해서 전달됨
    	- ex) PlengiResponse.ResponseType.PLACE
			
    | result |data type| description|
    |:---:| :----:|:----:|
    | SUCCESS|PlengiResponse.Result |위치 정보 획득 성공, 아래의 위치 정보 결과 참고 바람|
    | ERROR_CLOUD_ACCESS |PlengiResponse.Result| 유효하지 않는 client 혹은 학습되지 않은 장소 |
            
    > ##### SUCESS

    > * 위치 정보 결과 (PlengiResponse.Place Class, response.place로 획득 가능)
    > 
    		public long placeid;       // 장소 id(단말기 내에서 장소 id)		
    		public String name;        // 장소 이름		
    		public String tags;        // 장소와 관련된 tag		
    		public int floor;          // 층 정보		
    		public String category;    // 장소 유형		
    		public double lat;         // 위도		
    		public double lng;	       // 경도		
    		public float accuracy;     // 정확도		
    		public float threshold;    // 한계치		
    		public String client_code; // 클라이언트 코드		
    		public long loplat_id;     // loplat place id (loplat 서버에 등록된 장소 id)
    >		
    > * accuracy > threshold: 현재 위치 내에 있는 경우

    > * 그 외에 경우: 현재 위치 근처에 있는 경우

    > ##### ERROR_CLOUD_ACCESS
    > - 'Not Allowed Client': 유효하지 않는 client (clientID 및 clientSecret 확인 바람)
    > - 'Location Acquisition Fail' : 학습 되지 않은 장소
       

 
#### b. Place Event
	Plengi.getInstance(Context context).start()/ Plengi.getInstance(Context context).stop()
    
- Plengi.getInstance(Context Context).start()/ Plengi.getInstance(Context Context).stop()을 통해서 모니터링을 on/off

- PlengiEventListener와 PlengiBroadcastReceiver를 통해 Event 결과를 처리
    
    |parameter|data type|description|
    |:-----:|:-------:|:------:|
    |context| Context|-|
    
- response
    - result : PlengiResponse.PlaceEvent로 전달 (ex. PlengiResponse.PlaceEvent.ENTER/LEAVE)
    - event type : PlengiResponse.ResposeType로 전달 (ex. PlengiResponse.ResposeType.PLACE_EVENT)
     
        |result|data type|description|
        |:----:|:---:|:--------:|
        | ENTER |PlengiResponse.PlaceEvent| 현재 위치한 장소가 인식 됨|
        | LEAVE |PlengiResponse.PlaceEvent| 이전 장소 떠남|

#### c. Stay or Move
	Plengi.getInstance(Context Context).getCurrentPlaceStatus()
    
- Plengi.getInstance(Context Context).getCurrentPlaceStatus()를 통해 사용자가 현재 이동 중인지 한 장소에 머물고 있는지 확인 가능

	|parameter|data type|descryption|
    |:-----:|:-------:|:------:|
    |context| Context|-| 

- result
    - type: PlengiResponse.PlaceStatus로 전달 (ex. PlengiResponse.PlaceStatus.STAY/MOVE)
          
      | result | data type |descryption|
      |:----:| :-----:| :-----:|
      |STAY | PlengiResponse.PlaceStatus|현재 사용자가 한 장소에 머물러 있는 상태|
      |MOVE | PlengiResponse.PlaceStatus|현재 사용자가 이동 중인 상태|
      
### 2. Traker Mode
- Recognizer mode에서 제공하는 recoginze a place(a) API도 사용 가능

#### a. Traking Event
- 일정 주기마다 사용자의 위치 정보를 PlengiEventListener를 통해 결과를 처리
- event type:  PlengiResponse.ResponseType를 통해 전달(ex.  PlengiResponse.ResponseType.PLACE_TRACKING)
 
 	|parameter|data type|descryption|
    |:-----:|:-------:|:------:|
    |context| Context|-|
    
- response
    - reuslt: PlengiResponse.Result를 통해서 결과를 전달 받음
    	- ex) PlengiResponse.Result.SUCESS/ERROR_CLOUD_ACCESS
        
    - event type: PlengiResponse.ResponseType를 통해서 전달됨
    	- ex) PlengiResponse.ResponseType.PLACE    
        
        | result |data type| descryption|
        |:---:| :----:|:----:|
        | SUCCESS|PlengiResponse.Result |위치 정보 획득 성공, 아래의 위치 정보 결과 참고 바람|
        | ERROR_CLOUD_ACCESS |PlengiResponse.Result| 유효하지 않는 client 혹은 학습되지 않은 장소 | 
            
    > ##### SUCESS

    > * 위치 정보 결과 (PlengiResponse.Place Class, response.place로 획득 가능)
    > 
    		public long placeid;       // 장소 id(단말기 내에서 장소 id)		
    		public String name;        // 장소 이름		
    		public String tags;        // 장소와 관련된 tag		
    		public int floor;          // 층 정보		
    		public String category;    // 장소 유형		
    		public double lat;         // 위도		
    		public double lng;	       // 경도		
    		public float accuracy;     // 정확도		
    		public float threshold;    // 한계치		
    		public String client_code; // 클라이언트 코드		
    		public long loplat_id;     // loplat place id (loplat 서버에 등록된 장소 id)
    >		
    > * accuracy > threshold: 현재 위치 내에 있는 경우

    > * 그 외에 경우: 현재 위치 근처에 있는 경우

    > ##### ERROR_CLOUD_ACCESS
    > - 'Not Allowed Client': 유효하지 않는 client (clientID 및 clientSecret 확인 바람)
    > - 'Location Acquisition Fail' : 학습 되지 않은 장소
       
- 예시 코드

        public class ModePlengiListener implements PlengiListener {
        @Override
        public void listen(PlengiResponse response) {
            LoplatLogger.writeLog("ModePlengiListener: " + response.type);

            if(response.type == PlengiResponse.ResponseType.PLACE_TRACKING) {
                Intent i = new Intent();
                i.setAction("com.loplat.mode.response");

                if(response.place == null) {
                    // 특정장소 트래킹하다 벗어난 경우 한번 전달
                }
                else {
                    // tracking 결과 값 주기적으로 발생
                    // response.place.name
                    // response.place.loplatid
                }
             }
          }
        }

## Contents


### 1. 디렉토리 소개
- /sample : 샘플코드
- /library : plengi.aar 파일이 실제 라이브러리 파일

	> * jar 라이브러리가 필요한 경우 plengi.jar를 사용하고 AndroidManifest.xml 에 있는 권한을 추가 

- /javadoc : library 설명 문서
- /place_registerer : 장소 학습기 안드로이드 앱

### 2. Permission 

#### Permission (AnroidManifest.xml 참고)
- ACCESS_FINE_LOCATION: GPS를 이용항여 현재 위치의 위도와 경도 값을 획득할 수 있는 권한
- ACCESS_COARSE_LOCATION: WiFi 혹은 Network를 이용하여 현재 위치의 위도와 경도 값을 획득할 수 있는 권한
- ACCESS_NETWORK_STATE: 네트워크 상태를 확인할 수 있는 권한 
- ACCESS_WIFI_STATE / CHANGE_WIFI_STATE: 주변 WiFi AP들을 스캔하기 위한 권한
- INTERNET: 인터넷을 사용할 수 있는 권한

- In AndroidManifest.xml

		<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
		<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
		<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
		<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
		<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
		<uses-permission android:name="android.permission.INTERNET" />

### 3. Receiver & Service

#### Receiver & Service (AnroidManifest.xml 참고)
- loplat server와 통신을 위해서 broadcastreceiver와 service 등록이 필요함

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

### 4. Constraints

#### Constraints

- Android OS Marshmallow 버전 부터 WiFi Scan시 아래와 같은 위치 권한이 필요함

		<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
		<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
		
- Plengi SDK 동작하기 위해서 OS 버전 및 위치 권한을 확인을 위한 작업이 필요함
	
	> * Plengi SDK 기능을 사용 전 OS 버전 과 위치 권한 확인이 필요함

	> * Marshmallow 버전 부터 위치 권한은 Dangerous Permission으로 구분 되어 권한 획득을 위한 코드가 필요함

	> * sample코드에 구현된 checkWiFiScanConditionInMashmallow(Context context) 참고 바람
	
	> * 좀 더 자세한 사항은 Google Developer를 참고 바람 [Google Developer](http://developer.android.com/intl/ko/training/permissions/requesting.html)

- 사용자의 현재 위치를 확인 하기 위해서 위치 모드가 켜져 있어야함
	
	>  * sample코드에 구현된 checkWiFiScanConditionInMashmallow(Context context) 참고 바람

### 5. 샘플코드 간략 소개
- LoplatSampleApplication.java : Application Class를 상속받아 plengi engine을 초기화 수행함

- LoplatPlengiListener.java : PlengiListener를 상속받아 loplat 서버로부터 위치 획득 결과를 받음

	※ loplat서버로부터 다음과 같은 위치 정보가 제공 됨
	  - a. Place Event: enter or leave places
	  - b. Recognizing Place
	  - c. History of Places

- MainActivity.java : 장소 변화 모니터링 on/off, 현재 위치 획득 요청 및 결과 표시 해줌


### 6. 코드 구현 Quick Guide

1. PlengiListener를 상속받은 listener class를 생성 (LoplatSampleApplication.java 참고 바람)
	- Asynchronous Result는 모두 해당 리스너를 통해 전달됨
	- PLACE(Recognize a place), PLACE_EVENT 등의 응답에 따른 결과 처리 함
   
2. Application Class를 만듦 (LoplatSampleApplication.java 참고 바람)
	- Plengi instance 생성
	- Plengi 에 1번에서 생성한 Listener를 등록함
	
3. Plengi init 수행 (1회만 수행하면 됨, MainActivity 참고 바람)
        - Plengi.getInstance(Context context).init((String clientId, String clientSecret, String uniqueUserId)
    | parameter | type | description |
    |:-----:|:-----:|:-----:|
    |context(required)|Context| |
    |clientId(required)|String| SDK를 사용하기 위한 client ID|
    |clientSecret| String| SDK를 사용하기 위한 client secret (password)|
    |uniqueUserId| String| App에서 사용자를 식별하기 위한 ID임 (ex, id,email,....,etc.)|

	- Plengi SDK초기화 하는 기능
	
	  > * clientid & clientsecret: loplat server로 접근하기 위한 ID와 PW임
	  
	  > * test를 원하시는 분은 clientid: loplatdemo, clientsecret: loplatdemokey로 이용 할 수 있음
	
	  > * 정식 id와 secret을 원하는 분은 아래에 기입 된 메일 주소로 연락 바람
	
   - response
   	- PlengiResponse.Result를 통해서 결과를 전달 받음

       | result |type |descryption|
       | :---: |:---: |:-------:|
       | SUCCESS |PlengiResponse.Result |init 성공|
       | FAIL_INTERNET_UNAVAILABLE |PlengiResponse.Result | Network 상태가 불안정하거나 사용 불가능 한 상태|
       | FAIL_WIFI_SCAN_UNAVAILABLE |PlengiResponse.Result | WiFi scan이 불가능 한 상태|
   
4. 필요한 기능 구현 (MainActivity 참고 바람)
	- Recognizing Place: 현재 장소의 위치를 서버에서 받아오고자 하는 경우	
		- Plengi.getInstance(Context context).refreshPlace()을 호출하여 주변 WiFi AP들을 loplat서버로 보

		- loplat 서버는 Plengi Engine으로 부터 받은 WiFi신호를 분석하여 최적의 위치정보를 PlengiEventListenter로 전달
		- PlengiBroadcastReceiver는 PlengiEventListener를 통해서 위치 정보 결과 획득

		- 현재 위치가 인식 된 경우 현재 위치 정보 획득, 그렇지 않을 경우는 'unknown'으로 표시 됨

	- Place Event: Background로 장소 변화를 모니터링을 하고자 하는 경우
		- Plengi.getInstance(Context context).start()/ Plengi.getInstance(Context context).stop()을 통해서 장소 변화 모니터링을 on/off
		
		- loplat 서버는 PlengiEventListenter로 모니터링 결과를 전달
		- PlengiBroadcastReceiver는 PlengiEventListener를 통해서 위치 정보 결과 획득
		- Broadcast를 통해서 서버로 부터 Place Event 결과(Enter or Leaver) 획득
	
	- Stay or Move : 현재 사용자가 머물고 있는 장소의 방문 기록을 알고자 하는 경우
		- Plengi.getInstance(Context context).getCurrentPlaceStatus()를 호출 Stay / Move 상태 획득

		- Stay 일 경우, 현재 사용자가 현재 위치한 장소를 머문 시간을 표시 할 수 있음(코드 참고 바람)
	
	- History of Places: 방문 장소 이력을 알고자 하는 경우
		- Plengi.getInstance(Context context).getVisitList()를 통해 방문 목록을 획득
		- response: 사용자 방문 리스트 값을 얻을 수 있음 
        

## Notice 

* 코드 구현과 관련해서는 smaple코드와 javadoc 폴더 참고 바람
* 실제 테스트를 위해서는 기존에 학습된 장소가 있어야 함

	> /place_registerer 폴더에 있는 loplat cook 이라는 학습기 앱을 다운받아서 인식을 원하는 장소에서 학습을 수행함 
	
  	> 그 후에 loplat_demo를 통해 테스트를 해 보면 장소를 인식하는 것을 확인할 수 있음
  	
* 기술 관련 문의는 stkment@gmail.com으로 메일 보내주시기 바랍니다.
* 정식 id와 secret을 원하시는 분은 Lamen2357@loplat.com으로 아래의 내용을 기입하여 보내 주시기 바랍니다.
 - a. 이름
 - b. 회사
 - c. 사용 목적 
  
 

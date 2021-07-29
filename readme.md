# Plengi SDK
Android용 loplat plengi 라이브러리

## Installation
- 상세한 내용은 [로플랫 개발자](https://developers.loplat.com/#/documentation/android) 페이지 참고 바랍니다.

## How to import

#### 1. loplat maven 저장소 추가하기
 - 프로젝트 내, 최상위 `build.gradle` 에 아래의 코드를 추가하세요.
	
	```groovy
	maven { url "https://maven.loplat.com/artifactory/plengi"}
	```

	모듈의 `build.gradle` 에 아래의 코드를 추가하세요.

	```groovy	
	allprojects {
		repositories {
	        jcenter()
			mavenCentral()
			maven { url "https://maven.loplat.com/artifactory/plengi"}
	        google()
		}
	}
	```

#### 2. loplat SDK dependency 추가하기
 - 앱 build.gradle (Gradle 3.0 이상)

	```groovy	
	implementation 'com.loplat:placeengine:[version]'
	```
 
- 앱 build.gradle (Gradle 3.0 미만)

	```groovy
	compile 'com.loplat:placeengine:[version]'
	```

## Sample app
 - java로 build 시 application name 은 `.java.LoplatSampleApplication` 
   `LAUNCHER intent-filter`는 `.java.MainActivity`로 설정
 
    ```html
   <application
           android:name=".java.LoplatSampleApplication"
           ...
           >
           <activity
               android:name=".java.MainActivity"
               ...
               >
               <intent-filter>
                   <action android:name="android.intent.action.MAIN" />
                   <category android:name="android.intent.category.LAUNCHER" />
               </intent-filter>
           </activity>
   </application>
   ```
   
 - kotlin으로 build 시 application name 은 `.kotlin.KotlinLoplatSampleApplication` 
   `LAUNCHER intent-filter`는 `.kotlin.KotlinMainActivity`로 설정
  
    ```html
   <application
           android:name=".kotlin.KotlinLoplatSampleApplication"
           ...
           >
           <activity
               android:name=".kotlin.KotlinMainActivity"
               ...
                  >
               <intent-filter>
                   <action android:name="android.intent.action.MAIN" />
                   <category android:name="android.intent.category.LAUNCHER" />
               </intent-filter>
           </activity>
   </application>
   ```
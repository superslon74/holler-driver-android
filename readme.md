#Andar


Andar User Application:

1. Extract the android package.

2. Open the Tranxit package in Android Studio and wait till the gradle build successfully.

3. On the project explorer window of Android studio, find and edit the following files

a. Gradle Scripts/build.gradle (Module: app)
i. applicationId “com.andar.app” -> change this to your app’s Bundle ID

b. app/java/com.tranxit.app/Helper/URLHelper.java file

i. public static final String base = "http://app.andarsolutions.com"; -> Change this to your app’s base URL.

ii. public static final int client_id = 2; -> Change this to your app’s client_id.

iii. public static final String client_secret = "yVnKClKDHPcDlqqO1V05RtDRdvtrVHfvjlfasfdaa"; -> Change this to your app’s client_secret.

iv. public static final String STRIPE_TOKEN = "pk_test_0G4SKYM246m8dK6kgayCPwKWTXy"; -> Change this to your app’s Strip token.

c. app/res/values/strings.xml

i. <string name="app_name">Andar</string> -> Change this to your Application name.

ii. <string name="google_map_api">AIzaSyA6e191dVE0EMKbLdwdZ5ONqAh4q2sPn4c</string> -> Change this to your google map api key obtained from google developer console.

4. Configure your Application on the Google Firebase console, and download the google-services.json, and replace them in app/ folder.

5. Change the splash screen and icon for your own brand.
Open the project folder from the PC and follow the below mentioned path: Tranxit->app->src->main->res
You will find ‘mipmap’ folders in the different resolution names, you must save the icons and
splash screens in these folders according to the device size.
Also You will find ‘drawable’ folders in the different resolution names, you must save the icons
and splash screens in these folders too according to the device size.
Now hit on the ‘run’ button the application will run successfully in the virtual device.
If you are planning to upload the app in the Play Store, you must generate the build. To do so,
click on ‘Build’ in Android Studio, choose ‘Generate Signed APK’ from the drop down. Now, click
on create new and hit ‘Next’ to proceed.
Choose the path to the location where you want to save the key, give a folder name for the key
and click ‘OK’. Enter the information in the fields and click on ‘Finish’.
The APK will be generated and stored in the folder named by you.
Building
========

`./gradlew assembleRelease`

Signing
=======
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore ../../io.synchro.client.android.keystore app/build/outputs/apk/app-release-unsigned.apk app
~/Library/Developer/Xamarin/android-sdk-macosx/build-tools/21.0.1/zipalign -v 4 app/build/outputs/apk/app-release-unsigned.apk app/build/outputs/apk/app-release.apk

Testing
=======

To build and run unit tests on Android, with the emulator already running, use:

`./gradlew connectedAndroidTest`

Output will be under `app/build/outputs/androidTest-results` somewhere:

    Blakes-MBP:SynchroClientAndroid blake$ find app/build/outputs/androidTest-results -type f
    app/build/outputs/androidTest-results/connected/TEST-Default_Nexus_5(AVD) - 4.4.2-app-.xml

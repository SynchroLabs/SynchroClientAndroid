Building
========

`./gradlew assembleRelease`

Testing
=======

To build and run unit tests on Android, with the emulator already running, use:

`./gradlew connectedAndroidTest`

Output will be under `app/build/outputs/androidTest-results` somewhere:

    Blakes-MBP:SynchroClientAndroid blake$ find app/build/outputs/androidTest-results -type f
    app/build/outputs/androidTest-results/connected/TEST-Default_Nexus_5(AVD) - 4.4.2-app-.xml

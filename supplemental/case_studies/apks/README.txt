These packages were pulled from their installations on a Nexus 5X.  They were originally installed on the device using Google Play app market in Jan. of 2020.  Then, they were pulled off of the phone via these commands like those below (example shown: gmail)

adb shell pm list packages
adb shell pm path com.google.android.gm
adb pull /data/app/com.google.android.gm-g-lAlRG6TaK_NiuVzBt_Vw==/base.apk

Sometimes the app gives multiple APKs.  I don't know why / how that works.
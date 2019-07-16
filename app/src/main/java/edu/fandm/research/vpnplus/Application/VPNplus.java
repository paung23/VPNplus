package edu.fandm.research.vpnplus.Application;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.logging.Level;

import edu.fandm.research.vpnplus.Utilities.AndroidLoggingHandler;

public class VPNplus extends Application {
    public final static String EXTRA_DATA = "VPN+.DATA";
    public final static String EXTRA_ID = "VPN+.id";
    public final static String EXTRA_APP_NAME = "VPN+.appName";
    public final static String EXTRA_PACKAGE_NAME = "VPN+.packageName";
    public final static String EXTRA_CATEGORY = "VPN+.category";
    public final static String EXTRA_IGNORE = "VPN+.ignore";
    public final static String EXTRA_SIZE = "VPN+.SIZE";
    public final static String EXTRA_DATE_FORMAT = "VPN+.DATE";
    public static boolean doFilter = true;
    public static boolean asynchronous = true;
    public static int tcpForwarderWorkerRead = 0;
    public static int tcpForwarderWorkerWrite = 0;
    public static int socketForwarderWrite = 0;
    public static int socketForwarderRead = 0;

    private static Application sApplication;

    public static Application getApplication() {
        return sApplication;
    }

    public static Context getAppContext() {
        return getApplication().getApplicationContext();
    }//TODO:Nullable?

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        // make sure that java logging messages end up in logcat
        AndroidLoggingHandler loggingHandler = new AndroidLoggingHandler();
        AndroidLoggingHandler.reset(loggingHandler);
        loggingHandler.setLevel(Level.FINEST);
    }
}

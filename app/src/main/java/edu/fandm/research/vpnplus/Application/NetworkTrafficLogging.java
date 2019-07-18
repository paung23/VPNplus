package edu.fandm.research.vpnplus.Application;

import android.os.Environment;

import java.io.*;

public class NetworkTrafficLogging{
    private static final String filename = "networktraffic.csv";
    public static void writeToFile(String appName, String packageName, int srcPort, String destHostName, String destIP, int destPort, String msg) {
        FileWriter fw = null;
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "VPNplus Storage");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(folder, filename);
            fw = new FileWriter(file, true);
            StringBuilder sb = new StringBuilder();

            sb.append(appName);
            sb.append(',');
            sb.append(packageName);
            sb.append(',');
            sb.append(srcPort);
            sb.append(',');
            sb.append(destHostName);
            sb.append(',');
            sb.append(destIP);
            sb.append(',');
            sb.append(destPort);
            sb.append(',');
            sb.append(msg);
            sb.append('\n');

            fw.append(sb.toString());
            fw.flush();
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}






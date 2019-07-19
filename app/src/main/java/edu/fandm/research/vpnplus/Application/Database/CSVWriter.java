package edu.fandm.research.vpnplus.Application.Database;

import android.os.Environment;

import java.io.*;

public class CSVWriter{
    private static final String filename = "userDatabase.csv";
	private static final String leakFile = "leakDatabase.csv";

    public static void writeToCSV(String info, String dest, String choice) {
		FileWriter fw = null;
		try {
			File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "VPNplus Storage");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			File file = new File(folder, filename);
			fw = new FileWriter(file, true);
			StringBuilder sb = new StringBuilder();

			sb.append(info);
			sb.append(',');
			sb.append(dest);
			sb.append(',');
			sb.append(choice);
			sb.append('\n');

			fw.append(sb.toString());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void recordLeak(String appName, String leakCategory, String leakType, String leakClassification, String leakDest) {
		FileWriter fw = null;
		try {
			File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "VPNplus Storage");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			File file = new File(folder, leakFile);
			fw = new FileWriter(file, true);
			StringBuilder sb = new StringBuilder();

			sb.append(appName);
			sb.append(',');
			sb.append(leakCategory);
			sb.append(',');
			sb.append(leakType);
			sb.append(',');
			sb.append(leakClassification);
			sb.append(',');
			sb.append(leakDest);
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
	    
	
	
	
	

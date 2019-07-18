package edu.fandm.research.vpnplus.Application.Database;

import android.os.Environment;

import java.io.*;

public class CSVWriter{
    private static final String filename = "userDatabase.csv";
    public static void writeToCSV(String info, String dest, String choice) {
		FileWriter fw = null;
		try {
			File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "CSV data");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			File file = new File(folder, filename);
			fw = new FileWriter(file, true);
			StringBuilder sb = new StringBuilder();
	    /*
	      sb.append("Info Type");
	      sb.append(',');
	      sb.append("Dest.");
	      sb.append(',');
	      sb.append("Choice");
	      sb.append('\n');
	    */
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
	/*
    public static void main(String args[])throws IOException{
	writeToCSV();
	}*/
	}
}
	    
	
	
	
	

package edu.fandm.research.vpnplus.Plugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Data {
    //The class Data is used to represent a collection of instances as a List.

    public List<NBLeakInstance> instances; // list of instances
    private static String[] appNames = null;
    private int numAttributes; // number of attributes

    /**
     * Create a new Data object given CSV file
     * @param file
     */
    public Data(String file){
        // Takes a csv file as the parameter, 
        // creates a new instance for each line in the file and adds it to the List
        instances = new ArrayList<>();
        BufferedReader br = null;
        String row = null;
        try{
            br = new BufferedReader(new FileReader(file));
            while ((row = br.readLine()) != null) {
                String[] rowList = row.split(",");
                numAttributes = rowList.length-1;
                String category = rowList[numAttributes];
                String[] attributes = Arrays.copyOfRange(rowList,0,numAttributes);
                NBLeakInstance i = new NBLeakInstance(attributes, category);
                instances.add(i);
            }
	    br.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Create a new empty Data object
     */
    public Data(){
        instances = new ArrayList<>();
    }

    /**
     * Returns the number of instances in Data object
     */
    int size(){
        return instances.size();
    }


    private static String[] getAllApps(Context ctx){
        if(Data.appNames == null){
            HashSet<String> names = new HashSet<String>();
            PackageManager pm = ctx.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo packageInfo : packages) {
                String applicationName = (String) (packageInfo != null ? pm.getApplicationLabel(packageInfo) : packageInfo.packageName);
                names.add(applicationName);
            }


            String[] ans = new String[names.size()];
            ans = names.toArray(ans);
            Data.appNames = ans;
        }
        return Data.appNames;
    }


    /**
     * Add a new instance in Data object
     * @param instance
     */
    public void add(NBLeakInstance instance){
        instances.add(instance);
    }

    public static NBLeakInstance randomInstance(Context ctx){
        String[] possibleFeature1 = Data.getAllApps(ctx);
        String[] possibleFeature2 = new String[] {"Advertising ID", "Phone Number", "IMEI", "email", "city", "name", "GPS Coord.", "ZIP", "password", "mac"};
        String[] possibleFeature3 = new String[] {"1st Party", "3rd Party", "Ad Network"};
        String[] possibleFeature4 = new String[] {"Yes", "No"};

        Random r = new Random();
        int f1IDX = r.nextInt(possibleFeature1.length);
        int f2IDX = r.nextInt(possibleFeature2.length);
        int f3IDX = r.nextInt(possibleFeature3.length);
        int f4IDX = r.nextInt(2);

        NBLeakInstance i = new NBLeakInstance(new String[]{possibleFeature1[f1IDX], possibleFeature2[f2IDX], possibleFeature3[f3IDX]}, possibleFeature4[f4IDX]);
        return i;
    }

    /**
     * Add all instances of another Data object
     * @param data
     */
    void addAll(Data data){
        for (NBLeakInstance i: data.instances){
            add(i);
        }
    }

    /**
     * Remove and return an Instance at given index
     * @param index
     */
    NBLeakInstance remove(int index){
        NBLeakInstance instance = instances.remove(index);
        return instance;
    }

    public Data deepCopy(){
	Data copy = new Data();
	for (NBLeakInstance i: instances){
	    copy.instances.add(i);
	}
	return copy;
    }
    /**
     * Return String representation of Data object which is a list of Instance objects
     */
    public String toString(){
        return instances.toString();
    }

    public int getNumAttributes(){
        return numAttributes;
    }
}

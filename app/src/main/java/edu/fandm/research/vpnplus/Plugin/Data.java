package edu.fandm.research.vpnplus.Plugin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Data {
    //The class Data is used to represent a collection of instances as a List.

    List<Instance> instances; // list of instances
    int numAttributes; // number of attributes

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
                Instance i = new Instance(attributes, category);
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

    /**
     * Add a new instance in Data object
     * @param instance
     */
    void add(Instance instance){
        instances.add(instance);
    }

    public Instance randomInstance(){
        String[] possibleFeature1 = new String[] {"Advertising ID", "Phone Number", "IMEI", "email", "city", "name", "GPS Coord.", "ZIP", "password", "mac"};
        String[] possibleFeature2 = new String[] {"1st Party", "3rd Party", "Ad Network"};
        String[] possibleFeature3 = new String[] {"Yes", "No"};

        Random r = new Random();
        int f1IDX = r.nextInt(possibleFeature1.length);
        int f2IDX = r.nextInt(possibleFeature2.length);
        int f3IDX = r.nextInt(2);


        Instance i = new Instance(new String[]{possibleFeature1[f1IDX], possibleFeature2[f2IDX]}, possibleFeature3[f3IDX]);
        return i;
    }

    /**
     * Add all instances of another Data object
     * @param data
     */
    void addAll(Data data){
        for (Instance i: data.instances){
            add(i);
        }
    }

    /**
     * Remove and return an Instance at given index
     * @param index
     */
    Instance remove(int index){
        Instance instance = instances.remove(index);
        return instance;
    }

    public Data deepCopy(){
	Data copy = new Data();
	for (Instance i: instances){
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
}

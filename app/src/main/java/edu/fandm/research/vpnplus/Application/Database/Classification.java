package edu.fandm.research.vpnplus.Application.Database;

import java.io.*;
import java.util.*;
import java.net.*;

public class Classification{
    private static HashSet<String> ads = new HashSet<String>();
    private static void build_ads_set(final File folder) throws FileNotFoundException{
        if (folder.isDirectory()) {
            for (final File file : folder.listFiles()) {
                build_ads_set(file);
            }
        }
        else{
            Scanner sc = new Scanner(folder);
            while (sc.hasNextLine()){
                String line = sc.nextLine();
                if (line.charAt(0) == '|' && line.charAt(1) == '|'){
                    int j = 2;
                    while (j< line.length() && (Character.isLetter(line.charAt(j))||Character.isDigit(line.charAt(j)) || line.charAt(j) == '.' || line.charAt(j) == '-')){
                        j++;
                    }
                    ads.add(line.substring(2,j));
                }
                else if (line.charAt(0) == '|' && line.charAt(1) != '|'){
                    ads.add(line.substring(1,line.length()-1));
                }
                else if (line.charAt(0) != '!'){
                    ads.add(line);
                }
            }
        }
    }
    private static boolean isAd(String domain){
        if (domain.length() == 0){
            return false;
        }
        int i = domain.indexOf(".");
        return i >= 0 && (ads.contains(domain) || i+1 < domain.length() && isAd(domain.substring(i+1)));
    }
    private static boolean isFirstParty(String first, String domain){
        int a = first.length();
        int b = domain.length();
        for (int i = 0; i <= b-a; i++){
            int j;
            for (j = 0; j < a; j++){
                if (first.charAt(j) != domain.charAt(i+j)){
                    break;
                }
            }
            if (j == a){
                return true;
            }
        }
        return false;
    }
    public static String classify(String packagename, String domain){
        try {
            String path = "list/";
            File fold = new File(path);
            build_ads_set(fold);
        }
        catch (FileNotFoundException e) { }

        String[] packArr = packagename.split("\\.");
        String firstParty = packArr[1]+"."+packArr[0];
        if (isAd(domain)){
            return "ads";
        }
        else{
            if (isFirstParty(firstParty, domain)){
                return "firstparty";
            }
        }
        return "others";
    }
    private static HashMap<String,ArrayList<String>> classifyAll(HashMap<String,ArrayList<String>> traffic){
        try {
            String path = "list/";
            File fold = new File(path);
            build_ads_set(fold);
        }
        catch (FileNotFoundException e) { }
        String pack = null;
        String app = null;
        ArrayList<String> domains = new ArrayList<String>();
        HashMap<String, ArrayList<String>> category = new HashMap<>();
        for (String key: traffic.keySet()){
            ArrayList<String> l = traffic.get(key);
            pack = l.get(0);
            l.remove(0);
            domains = l;

        }
        String[] packArr = pack.split("\\.");
        String firstParty = packArr[1]+"."+packArr[0];
        int m = firstParty.length();
        category = new HashMap<String, ArrayList<String>>();
        category.put("first party", new ArrayList<String>());
        category.put("ads", new ArrayList<String>());
        category.put("others", new ArrayList<String>());

        for (String domain: domains){
            if (isAd(domain)){
                category.get("ads").add(domain);
            }
            else{
                if (isFirstParty(firstParty, domain)){
                    category.get("first party").add(domain);
                }
                else{
                    category.get("others").add(domain);
                }
            }
        }

        return category;
    }
    /**
    public static void main(String args[])throws FileNotFoundException{

        //ArrayList<String> l = new ArrayList<String>();
        //String path = "list/";
        //File fold = new File(path);
        //build_ads_set(fold);

        File testFile = new File ("TrafficData.txt");
        Scanner scan = new Scanner(testFile);
        while (scan.hasNextLine()){
            HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
            String li = scan.nextLine();
            String[] aList = li.split(" ");
            String app = aList[0];
            System.out.println("APP: "+app);
            String domains = aList[1];
            String[] domains_list = domains.split(",");
            hm.put(app, new ArrayList<String>());
            for (int i = 0; i < domains_list.length; i++){
                hm.get(app).add(domains_list[i]);

            }
            HashMap<String, ArrayList<String>> cat = classifyAll(hm);
            for (String key: cat.keySet()){
                System.out.print(key+": ");
                for (String domain: cat.get(key)){
                    System.out.print(domain+", ");
                }
            }
        }
    }
     */
}
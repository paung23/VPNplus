package edu.fandm.research.vpnplus.Application.AppInterface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.fandm.research.vpnplus.Plugin.NaiveBayes;
import edu.fandm.research.vpnplus.R;


public class NaiveBayesSpeedEval extends AppCompatActivity {

    public final static String TAG = NaiveBayesSpeedEval.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naive_bayes_speed_eval);
    }


    public void GO(View v){

        TextView tv = (TextView)findViewById(R.id.nbse_tv);
        tv.setMovementMethod(new ScrollingMovementMethod());
        int numMeasurements = 2000;
        double[][] measurements = new double[numMeasurements][4];
        for(int n = 5; n < numMeasurements + 5; n++){

            // See doOneEval function for details on return data
            // [total time, build classifier time, train classifier time, predict instance time] all in ns
            double[] times = NaiveBayes.doOneEval(n);

            // This invokes the garbage collector which is more realistic
            // what we really want to test is "train a classifier with n data points.
            // This is DIFFERENT from training a classifier with n data points immediately after
            // training a classifier of n-1 data points with the same arrays used to store those
            // data points.
            Runtime r = Runtime.getRuntime();
            r.gc();
            // invoke the garbage collector

            measurements[n-5] = times;
            Log.d(TAG, "Run " + n + " took: " + times[0] + "ns");
            tv.append("Run " + n + " took: " + times[0] + "ns\n");

        }






        // Check for storage permissions
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck == PackageManager.PERMISSION_DENIED) {
            // Request the permission be turned on
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }




        // This is used to differentiate the recordings, it's a timestamp
        String DATE_FORMAT_NOW = "yyyy-MM-dd_HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String timestamp = sdf.format(cal.getTime());

        // There is a method: getExternalStoragePublicDirectory
        // that takes a type of data (e.g. Music or Podcasts or Movies)
        // and returns a folder for that type of data (in /sdcard)
        // This use case doesn't fit into those categories too well, so Imma create
        // a new folder
        File dir = new File(Environment.getExternalStorageDirectory(), "nb_eval");
        boolean res = dir.mkdirs();
        File f = new File(dir, "data_" + timestamp + ".csv");



        String result = "Could not save measurement data!";
        try {
            FileOutputStream fos = new FileOutputStream(f);

            for(int i = 0; i < measurements.length; i++){
                double[] row = measurements[i];
                String s = row[0] + "," + row[1] + "," + row[2] + "," + row[3] + "\n";
                fos.write(s.getBytes());
            }
            fos.close();
            result = "Measurement data saved at: " + f.getAbsolutePath();
        } catch (
        FileNotFoundException e1){
            Log.d(TAG, "File not found");
            e1.printStackTrace();
        } catch (
        IOException e2) {
            Log.d(TAG, "IOException");
            e2.printStackTrace();
        }
        Log.d(TAG, result);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

}

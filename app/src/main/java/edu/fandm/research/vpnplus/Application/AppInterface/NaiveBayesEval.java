package edu.fandm.research.vpnplus.Application.AppInterface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

import edu.fandm.research.vpnplus.Plugin.Data;
import edu.fandm.research.vpnplus.Plugin.Instance;
import edu.fandm.research.vpnplus.Plugin.NaiveBayes;
import edu.fandm.research.vpnplus.R;


public class NaiveBayesEval extends AppCompatActivity {

    public final static String TAG = NaiveBayesEval.class.getName();
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naive_bayes_eval);
        this.tv = (TextView)findViewById(R.id.nbe_tv);
        this.tv.setMovementMethod(new ScrollingMovementMethod());
    }


    private void clearTV(){
        this.tv.setText("");
    }


    private void writeFile(double[][] measurements, String namePrefix){

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
        File f = new File(dir,  namePrefix + timestamp + ".csv");



        String result = "Could not save measurement data!";
        try {
            FileOutputStream fos = new FileOutputStream(f);

            for(int i = 0; i < measurements.length; i++){
                double[] row = measurements[i];
                String s = join(",", row) + "\n";
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

    private static String join(String delim, double[] tokens){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < tokens.length; i++){
            sb.append(String.valueOf(tokens[i]) + ",");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }


    private void GOaccuracyHelper(final NaiveBayes nb, final Data d, final int count, final int maxCount, final double[][] measurements){

        if(count >= maxCount){
            writeFile(measurements, "accuracy_data_");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        nb.buildClassifier(d);
        nb.train();

        final Instance newPoint = d.randomInstance(getApplicationContext());
        final String predictedResult = nb.predict(newPoint);


        builder.setTitle("Information Leak!");
        builder.setMessage("The app: " + newPoint.attributes[0] + " is leaking " + newPoint.attributes[1] + " to " + newPoint.attributes[2]);
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Yes! option
                String result = "Yes";
                flushDialog(nb, newPoint, result, predictedResult, d, measurements, count, maxCount, dialog);
            }
        });

        builder.setNegativeButton("Block", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // No! option
                String result = "No";
                flushDialog(nb, newPoint, result, predictedResult, d, measurements, count, maxCount, dialog);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void flushDialog(NaiveBayes nb, Instance newPoint, String result, String predictedResult, Data d, double[][] measurements, int count, int maxCount, DialogInterface dialog){
        newPoint.actual = result;
        newPoint.predicted = predictedResult;
        d.instances.add(newPoint);

        double acc = nb.calculateAccuracy(d);

        tv.append("User response: " + result + "   Prediction: "+ predictedResult + "\n");
        tv.append("Accuracy: " + acc + "\n\n");
        measurements[count] = new double[]{acc};

        dialog.dismiss();

        GOaccuracyHelper(nb, d, count+1, maxCount, measurements);
    }

    public void GOaccuracy(View v){
        clearTV();


        NaiveBayes nb = new NaiveBayes();
        final Data d = new Data();
        d.add(d.randomInstance(getApplicationContext()));

        int maxCount = 50;

        double[][] measurements = new double[maxCount][1];

        GOaccuracyHelper(nb, d, 0, maxCount, measurements);

    }


    public void GOspeed(View v){
        clearTV();

        int numMeasurements = 2000;
        double[][] measurements = new double[numMeasurements][4];
        for(int n = 5; n < numMeasurements + 5; n++){

            // See doOneEval function for details on return data
            // [total time, build classifier time, train classifier time, predict instance time] all in ns
            double[] times = NaiveBayes.doOneSpeedEval(n, getApplicationContext());

            // This invokes the garbage collector which is more realistic
            // what we really want to test is "train a classifier with n data points.
            // This is DIFFERENT from training a classifier with n data points immediately after
            // training a classifier of n-1 data points with the same arrays used to store those
            // data points.
            Runtime r = Runtime.getRuntime();
            r.gc();
            // invoke the garbage collector

            measurements[n-5] = times;
            //Log.d(TAG, "Run " + n + " took: " + times[0] + "ns");
            tv.append("Run " + n + " took: " + times[0] + "ns\n");

        }


        writeFile(measurements, "speed_data_");
    }

}

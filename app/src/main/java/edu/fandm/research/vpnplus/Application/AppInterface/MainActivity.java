package edu.fandm.research.vpnplus.Application.AppInterface;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.VpnService;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.fandm.research.vpnplus.Application.Database.AppSummary;
import edu.fandm.research.vpnplus.Application.Database.DatabaseHandler;
import edu.fandm.research.vpnplus.Application.Helpers.ActivityRequestCodes;
import edu.fandm.research.vpnplus.Application.Helpers.PreferenceHelper;
import edu.fandm.research.vpnplus.Application.Logger;
import edu.fandm.research.vpnplus.Application.VPNplus;
import edu.fandm.research.vpnplus.R;
import edu.fandm.research.vpnplus.Utilities.CertificateManager;
import edu.fandm.research.vpnplus.VPNConfiguration.VPNservice.MyVpnService;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    private View mainLayout;
    private View onIndicator;
    private View offIndicator;
    private View loadingIndicator;
    private FloatingActionButton vpnToggle;

    private ListView listLeak;
    private MainListViewAdapter adapter;

    ServiceConnection mSc;
    MyVpnService mVPN;

    private boolean bounded = false;
    private static float DISABLED_ALPHA = 0.3f;

    private enum Status {
        VPN_ON,
        VPN_OFF,
        VPN_STARTING
    }

    private class ReceiveMessages extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long difference = System.currentTimeMillis() - loadingViewShownTime;

            //The loading view should show for a minimum of 2 seconds to prevent the loading view
            //from appearing and disappearing rapidly.
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showIndicator(Status.VPN_ON);
                }
            }, Math.max(2000 - difference, 0));
        }
    }

    private ReceiveMessages myReceiver = null;
    private boolean myReceiverIsRegistered = false;
    private long loadingViewShownTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        myReceiver = new ReceiveMessages();

        mainLayout = findViewById(R.id.main_screen);
        onIndicator = findViewById(R.id.vpn_on);
        offIndicator = findViewById(R.id.vpn_off);
        loadingIndicator = findViewById(R.id.loading_indicator);
        listLeak = (ListView)findViewById(R.id.leaks);
        vpnToggle = (FloatingActionButton)findViewById(R.id.vpn_button);

        CertificateManager.initiateFactory(MyVpnService.CADir, MyVpnService.CAName, MyVpnService.CertName, MyVpnService.KeyType, MyVpnService.Password.toCharArray());

        vpnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MyVpnService.isRunning()) {
                    Logger.d(TAG, "Connect toggled ON");
                    Intent intent = CertificateManager.trustfakeRootCA(MyVpnService.CADir, MyVpnService.CAName);
                    if (intent != null) {
                        startActivityForResult(intent, ActivityRequestCodes.REQUEST_CERT);
                    } else {
                        startVPN();
                    }
                } else {
                    Logger.d(TAG, "Connect toggled OFF");
                    showIndicator(Status.VPN_OFF);
                    stopVPN();
                }
            }
        });

        mSc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Logger.d(TAG, "VPN Service connected");
                mVPN = ((MyVpnService.MyVpnServiceBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Logger.d(TAG, "VPN Service disconnected");
            }
        };

        checkPermissionsAndRequestAndEnableViews();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                Intent i1 = new Intent(this, MyPreferencesActivity.class);
                startActivity(i1);
                return true;

            case R.id.about:
                Intent i2 = new Intent(this, AboutActivity.class);
                startActivity(i2);
                return true;

            case R.id.clear:
                DatabaseHandler db = DatabaseHandler.getInstance(this);
                db.deleteAll();
                populateLeakList();
                break;

            case R.id.NaiveBayesSpeedEval:
                Intent i3 = new Intent(this, NaiveBayesEval.class);
                startActivity(i3);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void checkPermissionsAndRequestAndEnableViews() {
        if (checkPermissionsAndRequest()) {
            mainLayout.setVisibility(View.VISIBLE);
        }
        else {
            mainLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!bounded) {
            Intent service = new Intent(this, MyVpnService.class);
            this.bindService(service, mSc, Context.BIND_AUTO_CREATE);
            bounded = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        populateLeakList();

        if (!myReceiverIsRegistered) {
            registerReceiver(myReceiver, new IntentFilter(getString(R.string.vpn_running_broadcast_intent)));
            myReceiverIsRegistered = true;
        }

        if (MyVpnService.isStarted()) {
            //If the VPN was started before the user closed the app and still is not running, show
            //the loading indicator once again.
            showIndicator(Status.VPN_STARTING);
        } else if (MyVpnService.isRunning()) {
            showIndicator(Status.VPN_ON);
        } else {
            showIndicator(Status.VPN_OFF);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myReceiverIsRegistered) {
            unregisterReceiver(myReceiver);
            myReceiverIsRegistered = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bounded) {//must unbind the service otherwise the ServiceConnection will be leaked.
            this.unbindService(mSc);
            bounded = false;
        }
    }

    private void showIndicator(Status status) {
        onIndicator.setVisibility(status == Status.VPN_ON ? View.VISIBLE : View.GONE);
        offIndicator.setVisibility(status == Status.VPN_OFF ? View.VISIBLE : View.GONE);
        loadingIndicator.setVisibility(status == Status.VPN_STARTING ? View.VISIBLE : View.GONE);

        vpnToggle.setEnabled(status != Status.VPN_STARTING);
        vpnToggle.setAlpha(status == Status.VPN_STARTING ? DISABLED_ALPHA : 1.0f);

        if (status == Status.VPN_STARTING) {
            loadingViewShownTime = System.currentTimeMillis();
        }
    }

    public void populateLeakList() {
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        List<AppSummary> apps = db.getAllApps();

        Comparator<AppSummary> comparator = PreferenceHelper.getAppLeakOrder(getApplicationContext());
        Collections.sort(apps, comparator);

        if (adapter == null) {
            adapter = new MainListViewAdapter(this, apps);
            listLeak.setAdapter(adapter);
            listLeak.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, AppSummaryActivity.class);

                    Log.d(TAG, "Clicked item at pos: " + position + "   with id: " + id);
                    AppSummary app = (AppSummary)parent.getItemAtPosition(position);

                    intent.putExtra(VPNplus.EXTRA_PACKAGE_NAME, app.getPackageName());
                    intent.putExtra(VPNplus.EXTRA_APP_NAME, app.getAppName());
                    intent.putExtra(VPNplus.EXTRA_IGNORE, app.getIgnore());

                    startActivity(intent);
                }
            });

            listLeak.setLongClickable(true);
            listLeak.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                    final AppSummary app = (AppSummary)parent.getItemAtPosition(position);
                    PackageManager pm = getPackageManager();
                    Drawable appIcon;
                    try {
                        appIcon = pm.getApplicationIcon(app.getPackageName());
                    } catch (PackageManager.NameNotFoundException e) {
                        appIcon = getResources().getDrawable(R.drawable.ic_launcher_foreground);
                    }

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.delete_package_title)
                            .setMessage(String.format(getResources().getString(R.string.delete_package_message), app.getAppName()))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseHandler databaseHandler = DatabaseHandler.getInstance(MainActivity.this);
                                    databaseHandler.deletePackage(app.getPackageName());
                                    populateLeakList();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(appIcon)
                            .show();
                    return true;
                }
            });
        } else {
            adapter.updateData(apps);
        }
    }

    /**
     * Gets called immediately before onResume() when activity is re-starting
     */
    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == ActivityRequestCodes.REQUEST_CERT) {
            boolean keyChainInstalled = result == RESULT_OK;
            if (keyChainInstalled) {
                startVPN();
            }
            else
                new AlertDialog.Builder(this)
                        .setTitle(R.string.certificate_root_store)
                        .setMessage(R.string.certificate_root_store_msg)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
        } else if (request == ActivityRequestCodes.REQUEST_VPN) {
            if (result == RESULT_OK) {
                Logger.d(TAG, "Starting VPN service");

                showIndicator(Status.VPN_STARTING);
                mVPN.startVPN(this);
            }
        } else if (request == ActivityRequestCodes.PERMISSIONS_SETTINGS) {
            // After giving the user the opportunity to manually turn on
            // the required permissions, check whether they have been granted.
            checkPermissionsAndRequestAndEnableViews();
        }
    }

    private void startVPN() {
        Log.d(TAG, "Trying to startVPN()");
        if (!bounded) {
            Intent service = new Intent(this, MyVpnService.class);
            this.bindService(service, mSc, Context.BIND_AUTO_CREATE);
            bounded = true;
        }
        /**
         * prepare() sometimes would misbehave:
         * https://code.google.com/p/android/issues/detail?id=80074
         *
         * if this affects our app, we can let vpnservice update main activity for status
         * http://stackoverflow.com/questions/4111398/notify-activity-from-service
         *
         */
        Intent intent = VpnService.prepare(this);
        Logger.d(TAG, "VPN prepare done");
        if (intent != null) {
            startActivityForResult(intent, ActivityRequestCodes.REQUEST_VPN);
        } else {
            onActivityResult(ActivityRequestCodes.REQUEST_VPN, RESULT_OK, null);
        }
    }

    private void stopVPN() {
        Logger.d(TAG, "Stopping VPN service");
        if (bounded) {
            this.unbindService(mSc);
            bounded = false;
        }
        mVPN.stopVPN();
    }

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 4;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COURSE_LOCATION = 5;

    /**
     * Requests permissions if they are not granted.
     * @return Whether the app has all the required permissions.
     */
    private boolean checkPermissionsAndRequest() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            return false;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            return false;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return false;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return false;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COURSE_LOCATION);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        String permission = null;

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                permission = Manifest.permission.READ_CONTACTS;
                break;
            }
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                permission = Manifest.permission.READ_PHONE_STATE;
                break;
            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                permission = Manifest.permission.READ_EXTERNAL_STORAGE;
                break;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                permission = Manifest.permission.ACCESS_FINE_LOCATION;
                break;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_COURSE_LOCATION: {
                permission = Manifest.permission.ACCESS_COARSE_LOCATION;
                break;
            }
        }

        if (permission == null) throw new RuntimeException("Should not be null.");

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // If an individual permission was granted, check once again.
            checkPermissionsAndRequestAndEnableViews();
        } else {
            // If an individual permission was not granted.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // If the user did not select "never ask again", check once again.
                checkPermissionsAndRequest();
            } else {
                // In this case, the user has selected "never ask again" and declined a permission.
                // Since we require all permissions to be granted, and can no longer ask for this
                // permission, give the user access to the permissions screen to turn on all the
                // permissions manually.
                mainLayout.setVisibility(View.GONE);
            }
        }
    }
}

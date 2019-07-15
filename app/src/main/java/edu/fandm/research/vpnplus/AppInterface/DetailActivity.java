package edu.fandm.research.vpnplus.AppInterface;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import edu.fandm.research.vpnplus.Database.DataLeak;
import edu.fandm.research.vpnplus.Database.DatabaseHandler;
import edu.fandm.research.vpnplus.R;
import edu.fandm.research.vpnplus.Utilities.AppManager;

/**
 * Created by justinhu on 16-03-11.
 */
public class DetailActivity extends AppCompatActivity {

    private int notifyId;
    private String packageName;
    private String appName;
    private String category;

    private ListView list;
    private DetailListViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_detail);

        // Get the message from the intent
        Intent intent = getIntent();
        notifyId = intent.getIntExtra(AppManager.EXTRA_ID, -1);
        packageName = intent.getStringExtra(AppManager.EXTRA_PACKAGE_NAME);
        appName = intent.getStringExtra(AppManager.EXTRA_APP_NAME);
        category = intent.getStringExtra(AppManager.EXTRA_CATEGORY);

        TextView title = (TextView) findViewById(R.id.detail_title);
        title.setText(category);
        TextView subtitle = (TextView) findViewById(R.id.detail_subtitle);
        subtitle.setText("[" + appName + "]");

        list = (ListView) findViewById(R.id.detail_list);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateList();
    }

    private void updateList() {
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        List<DataLeak> details = db.getAppLeaks(packageName, category);

        if (details == null) {
            return;
        }

        if (adapter == null) {
            adapter = new DetailListViewAdapter(this, details);

            View header = getLayoutInflater().inflate(R.layout.listview_detail, null);
            ((TextView) header.findViewById(R.id.detail_type)).setText(R.string.type_label);
            ((TextView) header.findViewById(R.id.detail_time)).setText(R.string.time_label);
            //((TextView) header.findViewById(R.id.detail_content)).setText(R.string.content_label);
            ((TextView) header.findViewById(R.id.detail_destination)).setText(R.string.destination_label);

            list.addHeaderView(header);
            list.setAdapter(adapter);
        } else {
            adapter.updateData(details);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = getParentActivityIntent();
                if (shouldUpRecreateTask(upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    navigateUpTo(upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

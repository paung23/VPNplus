package edu.fandm.research.vpnplus.AppInterface;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import edu.fandm.research.vpnplus.Database.CategorySummary;
import edu.fandm.research.vpnplus.Database.DatabaseHandler;
import edu.fandm.research.vpnplus.R;
import edu.fandm.research.vpnplus.Utilities.AppManager;

public class AppSummaryActivity extends AppCompatActivity {

    private String packageName;
    private String appName;
    private ListView list;
    private SummaryListViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_app_summary);

        // Get the message from the intent
        Intent intent = getIntent();
        packageName= intent.getStringExtra(AppManager.EXTRA_PACKAGE_NAME);
        appName = intent.getStringExtra(AppManager.EXTRA_APP_NAME);

        TextView title = (TextView) findViewById(R.id.summary_title);
        title.setText(appName);
        TextView subtitle = (TextView) findViewById(R.id.summary_subtitle);
        subtitle.setText("[" + packageName + "]");

        list = (ListView) findViewById(R.id.summary_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategorySummary category = (CategorySummary) parent.getItemAtPosition(position);
                Intent intent;

                intent = new Intent(AppSummaryActivity.this, DetailActivity.class);

                intent.putExtra(AppManager.EXTRA_ID, category.notifyId);
                intent.putExtra(AppManager.EXTRA_PACKAGE_NAME, packageName);
                intent.putExtra(AppManager.EXTRA_APP_NAME, appName);
                intent.putExtra(AppManager.EXTRA_CATEGORY, category.category);
                intent.putExtra(AppManager.EXTRA_IGNORE, category.ignore);

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateList();
    }

    private void updateList(){
        DatabaseHandler db = DatabaseHandler.getInstance(this);
        List<CategorySummary> details = db.getAppDetail(packageName);

        if (details == null) {
            return;
        }
        if (adapter == null) {
            adapter = new SummaryListViewAdapter(this, details);
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

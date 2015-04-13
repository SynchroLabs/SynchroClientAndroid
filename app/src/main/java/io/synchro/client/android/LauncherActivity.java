package io.synchro.client.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;


public class LauncherActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ListView appListView = (ListView) findViewById(R.id.appListView);

        List<SynchroApp> appsList = AndroidSynchroAppManager.getAppManager(this).getApps();
        final SynchroApp[] apps = new SynchroApp[appsList.size()];
        appsList.toArray(apps);

        appListView.setAdapter(new SynchroAppTwoLineArrayAdapter(this, apps));
        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                                           {
                                               @Override
                                               public void onItemClick(
                                                       AdapterView<?> parent, View view,
                                                       int position, long id
                                                                      )
                                               {
                                                   SynchroApp synchroApp = apps[position];

                                                   Intent intent = new Intent(LauncherActivity.this, SynchroPageActivity.class);
                                                   intent.putExtra("endpoint", synchroApp.getEndpoint());
                                                   startActivity(intent);
//                                                   finish();
                                               }
                                           });
        appListView.setLongClickable(true);
        appListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
                                           {
                                               @Override
                                               public boolean onItemLongClick(
                                                       AdapterView<?> parent, View view,
                                                       final int position, long id)
                                               {
                                                   SynchroApp synchroApp = apps[position];
                                                   Intent intent = new Intent(LauncherActivity.this, AppDetailActivity.class);
                                                   intent.putExtra("endpoint", synchroApp.getEndpoint());
                                                   startActivity(intent);
                                                   return true;
                                               }
                                           });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_add)
        {
            Intent intent = new Intent(this, AppDetailActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package io.synchro.client.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


public class LauncherActivity extends Activity
{
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ListView appListView = (ListView) findViewById(R.id.appListView);
        appListView.setAdapter(
                new SynchroAppTwoLineArrayAdapter(
                        this, AndroidSynchroAppManager.getAppManager(this).getApps()
                )
                              );
        appListView.setLongClickable(true);
        appListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
                                           {
                                               @Override
                                               public boolean onItemLongClick(
                                                       AdapterView<?> parent, View view,
                                                       int position, long id)
                                               {
                                                   if (actionMode != null)
                                                   {
                                                       return false;
                                                   }

                                                   actionMode = startActionMode(new ActionMode.Callback()
                                                                                {
                                                                                    @Override
                                                                                    public boolean onCreateActionMode(
                                                                                            ActionMode mode,
                                                                                            Menu menu
                                                                                                                     )
                                                                                    {
                                                                                        MenuInflater inflater = mode.getMenuInflater();
                                                                                        inflater.inflate(R.menu.menu_app_context, menu);
                                                                                        return true;
                                                                                    }

                                                                                    @Override
                                                                                    public boolean onPrepareActionMode(
                                                                                            ActionMode mode,
                                                                                            Menu menu
                                                                                                                      )
                                                                                    {
                                                                                        return false;
                                                                                    }

                                                                                    @Override
                                                                                    public boolean onActionItemClicked(
                                                                                            ActionMode mode,
                                                                                            MenuItem item
                                                                                                                      )
                                                                                    {
                                                                                        return false;
                                                                                    }

                                                                                    @Override
                                                                                    public void onDestroyActionMode(
                                                                                            ActionMode mode
                                                                                                                   )
                                                                                    {
                                                                                        actionMode = null;
                                                                                    }
                                                                                });
                                                   view.setSelected(true);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

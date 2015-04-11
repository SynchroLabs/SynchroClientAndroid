package io.synchro.client.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import io.synchro.json.JObject;


public class AppDetailActivity extends Activity
{
    public static final String TAG = AppDetailActivity.class.getSimpleName();

    SynchroAppManager appManager;
    SynchroApp        app;

    LinearLayout layoutFind;
    EditText     editEndpoint;
    Button       btnFind;

    LinearLayout layoutDetails;
    TextView     textEndpoint;
    TextView     textName;
    TextView     textDescription;
    Button       btnSave;
    Button       btnLaunch;
    Button       btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        this.getActionBar().setDisplayHomeAsUpEnabled(true);

        layoutFind = (LinearLayout) findViewById(R.id.linearLayoutFind);
        editEndpoint = (EditText) findViewById(R.id.editEndpoint);
        btnFind = (Button) findViewById(R.id.btnFind);

        btnFind.setOnClickListener(new View.OnClickListener()
                                   {
                                       @Override
                                       public void onClick(View v)
                                       {
                                           btnFind_Click();
                                       }
                                   });

        layoutDetails = (LinearLayout) findViewById(R.id.linearLayoutDetails);
        textEndpoint = (TextView)findViewById(R.id.textEndpoint);
        textName = (TextView)findViewById(R.id.textName);
        textDescription = (TextView)findViewById(R.id.textDescription);
        btnSave = (Button)findViewById(R.id.btnSave);
        btnLaunch = (Button)findViewById(R.id.btnLaunch);
        btnDelete = (Button)findViewById(R.id.btnDelete);

        btnSave.setOnClickListener(new View.OnClickListener()
                                   {
                                       @Override
                                       public void onClick(View v)
                                       {
                                           btnSave_Click();
                                       }
                                   });
        btnLaunch.setOnClickListener(new View.OnClickListener()
                                   {
                                       @Override
                                       public void onClick(View v)
                                       {
                                           btnLaunch_Click();
                                       }
                                   });
        btnDelete.setOnClickListener(new View.OnClickListener()
                                     {
                                         @Override
                                         public void onClick(View v)
                                         {
                                            btnDelete_Click();
                                         }
                                     });

        appManager = AndroidSynchroAppManager.getAppManager(this);
//        try
//        {
//            appManager.loadState();
//        }
//        catch (IOException e)
//        {
//            Log.wtf(TAG, e);
//        }

        String endpoint = (this.getIntent().getExtras() != null) ? this.getIntent().getExtras().getString("endpoint", null) : null;

        if (endpoint != null)
        {
            // App details mode...
            this.app = appManager.GetApp(endpoint);
            this.layoutFind.setVisibility(View.GONE);
            this.layoutDetails.setVisibility(View.VISIBLE);
            this.btnSave.setVisibility(View.GONE);
            this.btnLaunch.setVisibility(View.VISIBLE);
            this.btnDelete.setVisibility(View.VISIBLE);
            this.populateControlsFromApp();
        }
        else
        {
            // "Find" mode...
            this.layoutFind.setVisibility(View.VISIBLE);
            this.layoutDetails.setVisibility(View.GONE);
        }
    }

    void populateControlsFromApp()
    {
        this.textEndpoint.setText(this.app.getEndpoint());
        this.textName.setText(this.app.getName());
        this.textDescription.setText(this.app.getDescription());
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app_detail, menu);
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

    void btnFind_Click()
    {
        final String endpoint = this.editEndpoint.getText().toString();

        SynchroApp managedApp = appManager.GetApp(endpoint);
        if (managedApp != null)
        {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Synchro Application Search");
            builder.setMessage(
                    "You already have a Synchro application with the supplied endpoint in your list"
                              );
            builder.setPositiveButton("OK", null);
            builder.setCancelable(true);
            builder.show();
            return;
        }

        URL endpointUri;
        try
        {
            endpointUri = TransportAndroidHttpClient.UrlFromEndpoint(endpoint);
        }
        catch (MalformedURLException e)
        {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Synchro Application Search");
            builder.setMessage("Endpoint not formatted correctly");
            builder.setPositiveButton("OK", null);
            builder.setCancelable(true);
            builder.show();
            return;
        }

        final URL endpointUrl = endpointUri;

        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                Transport transport = new TransportAndroidHttpClient(endpointUrl);

                JObject appDefinition;
                try
                {
                    appDefinition = transport.getAppDefinition();
                }
                catch (IOException e)
                {
                    appDefinition = null;
                    Log.wtf(TAG, e);
                }


                final JObject finalAppDefinition = appDefinition;
                AppDetailActivity.this.runOnUiThread(new Runnable()
                                                     {
                                                         @Override
                                                         public void run()
                                                         {
                                                             if (finalAppDefinition == null)
                                                             {
                                                                 AlertDialog.Builder builder;
                                                                 builder = new AlertDialog.Builder(AppDetailActivity.this);
                                                                 builder.setTitle("Synchro Application Search");
                                                                 builder.setMessage("No Synchro application found at the supplied endpoint");
                                                                 builder.setPositiveButton("OK", null);
                                                                 builder.setCancelable(true);
                                                                 builder.show();
                                                             }
                                                             else
                                                             {
                                                                 AppDetailActivity.this.app = new SynchroApp(endpoint, finalAppDefinition, null);
                                                                 AppDetailActivity.this.layoutFind.setVisibility(View.GONE);
                                                                 AppDetailActivity.this.layoutDetails.setVisibility(View.VISIBLE);
                                                                 AppDetailActivity.this.btnSave.setVisibility(View.VISIBLE);
                                                                 AppDetailActivity.this.btnLaunch.setVisibility(View.GONE);
                                                                 AppDetailActivity.this.btnDelete.setVisibility(View.GONE);
                                                                 AppDetailActivity.this.populateControlsFromApp();
                                                             }

                                                         }
                                                     });

                return null;
            }
        }.execute();
    }

    void btnSave_Click()
    {
        appManager.getApps().add(this.app);
        try
        {
            appManager.saveState();
        }
        catch (IOException e)
        {
            Log.wtf(TAG, e);
        }
        Intent intent = new Intent(this, LauncherActivity.class);
        NavUtils.navigateUpTo(this, intent);
    }

    void btnLaunch_Click()
    {
        Intent intent = new Intent(this, SynchroPageActivity.class);
        intent.putExtra("endpoint", this.app.getEndpoint());
        startActivity(intent);
    }

    void btnDelete_Click()
    {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Synchro Application Delete");
        builder.setMessage(
                "Are you sure you want to remove this Synchro application from your list"
                          );
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                  {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which)
                                      {
                                          SynchroApp app = appManager.GetApp(AppDetailActivity.this.app.getEndpoint());
                                          appManager.getApps().remove(app);
                                          try
                                          {
                                              appManager.saveState();
                                          }
                                          catch (IOException e)
                                          {
                                              Log.wtf(TAG, e);
                                          }
                                          Intent intent = new Intent(AppDetailActivity.this, LauncherActivity.class);
                                          NavUtils.navigateUpTo(AppDetailActivity.this, intent);
                                      }
                                  });
        builder.setNegativeButton("No", null);
        builder.setCancelable(false);
        builder.show();
    }
}

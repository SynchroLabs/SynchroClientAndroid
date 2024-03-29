package io.synchro.client.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;

import java.net.MalformedURLException;

import io.synchro.json.JObject;

/**
 * Created by blake on 3/16/15.
 */
public class SynchroPageActivity extends Activity
{
    public static final String TAG = SynchroPageActivity.class.getSimpleName();
    public static final String STATE_RESUMING = "Synchro_Resuming";

    StateManager _stateManager;
    AndroidPageView _pageView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return _pageView.OnCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            return _pageView.OnCommandBarUp(item);
        }
        else if (_pageView.OnOptionsItemSelected(item))
        {
            // Page view handled the item
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        boolean restoreStateFromServer = (savedInstanceState != null) ? savedInstanceState.getBoolean(STATE_RESUMING, false) : false;

        Log.d(TAG, "onCreate");
        String endpoint = this.getIntent().getExtras().getString("endpoint");

        AndroidSynchroDeviceMetrics deviceMetrics = new AndroidSynchroDeviceMetrics(this);

        // This ScrollView will consume all available screen space by default, which is what we want...
        //
        ScrollView layout = new ScrollView(this);

        AndroidSynchroAppManager appManager = AndroidSynchroAppManager.getAppManager(this);
//        try
//        {
//            appManager.loadState();
//        }
//        catch (IOException e)
//        {
//            Log.wtf(TAG, e);
//        }

        final SynchroApp app = appManager.GetApp(endpoint);

        // Using OkHttpNetworkHandler via ModernHttpClient component
        //
        // !!! Doesn't appear to support cookies out of the box
        //
        // HttpClient httpClient = new HttpClient(new OkHttpNetworkHandler());
        //
        Transport transport = null;
        try
        {
            transport = new TransportAndroidHttpClient(TransportAndroidHttpClient.UrlFromEndpoint(
                    endpoint
                                                                                                 ));
        }
        catch (MalformedURLException e)
        {
            Log.wtf(TAG, e);
        }

        final boolean launchedFromMenu = (appManager.getAppSeed() == null);

        StateManager.IProcessAppExit processAppExit = new StateManager.IProcessAppExit() {
            @Override
            public void ProcessAppExit()
            {
                if (launchedFromMenu)
                {
                    // If we are't nailed to a predefined app, then we'll allow the app to navigate back to
                    // this page from its top level page.
                    //
//                    Intent intent = new Intent(this, typeof(AppDetailActivity));
//                    intent.putExtra("endpoint", app.getEndpoint());
//                    NavUtils.navigateUpTo(SynchroPageActivity.this, intent);
                    finish();

                    // Detach StateManager
                    _stateManager = null;
                }
            }
        };

        _stateManager = new StateManager(appManager, app, transport, deviceMetrics, SynchroPageActivity.this);
        _pageView = new AndroidPageView(_stateManager, _stateManager.getViewModel(), this, layout, launchedFromMenu);

        _pageView.setSetPageTitle(new PageView.ISetPageTitle()
                                  {
                                      @Override
                                      public void setPageTitle(final String title)
                                      {
                                          SynchroPageActivity.this.runOnUiThread(new Runnable()
                                                                                 {
                                                                                     @Override
                                                                                     public void run()
                                                                                     {
                                                                                         SynchroPageActivity.this.getActionBar().setTitle(title);
                                                                                     }
                                                                                 });
                                      }
                                  });

        setContentView(layout);

        _stateManager.SetProcessingHandlers(new StateManager.IProcessPageView()
                                            {
                                                @Override
                                                public void ProcessPageView(final JObject pageView)
                                                {
                                                    SynchroPageActivity.this.runOnUiThread(new Runnable()
                                                                                           {
                                                                                               @Override
                                                                                               public void run()
                                                                                               {
                                                                                                   _pageView.ProcessPageView(pageView);
                                                                                               }
                                                                                           });
                                                }
                                            },
                                            processAppExit,
                                            new StateManager.IProcessMessageBox()
                                            {
                                                @Override
                                                public void ProcessMessageBox(
                                                        final JObject messageBox,
                                                        final StateManager.ICommandHandler commandHandler
                                                                             )
                                                {
                                                    SynchroPageActivity.this.runOnUiThread(new Runnable()
                                                                                           {
                                                                                               @Override
                                                                                               public void run()
                                                                                               {
                                                                                                   _pageView.ProcessMessageBox(messageBox, commandHandler);
                                                                                               }
                                                                                           });
                                                }
                                            }, new StateManager.IProcessUrl() {

                                                @Override
                                                public void ProcessUrl(
                                                        final String primaryUrl, final String secondaryUrl
                                                                      )
                                                {
                                                    SynchroPageActivity.this.runOnUiThread(
                                                            new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    _pageView.ProcessLaunchUrl(primaryUrl, secondaryUrl);
                                                                }
                                                            }
                                                                                          );
                                                }
                                            });
        if (restoreStateFromServer)
        {
            Log.d(TAG, "Restoring state from server (sendResyncRequestAsync)");
            _stateManager.sendResyncRequestAsync();
        }
        else
        {
            Log.d(TAG, "Normal application start (startApplicationAsync)");
            _stateManager.startApplicationAsync();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        if (_stateManager != null)
        {
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                Log.d(TAG, "Screen oriented to Portrait");
                _stateManager.sendViewUpdateAsync(SynchroOrientation.PORTRAIT);
            }
            else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                Log.d(TAG, "Screen oriented to Landscape");
                _stateManager.sendViewUpdateAsync(SynchroOrientation.LANDSCAPE);
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (_pageView.HasBackCommand())
        {
            _pageView.GoBack();
        }
        else
        {
            this.finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.d(TAG, "onSaveInstanceState");
        outState.putBoolean(STATE_RESUMING, true);
        super.onSaveInstanceState(outState);
    }
}

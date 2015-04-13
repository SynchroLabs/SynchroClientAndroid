package io.synchro.client.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
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

        PageView.IDoBackToMenu backToMenu = null;
        if (appManager.getAppSeed() == null)
        {
            // If we are't nailed to a predefined app, then we'll allow the app to navigate back to
            // this page from its top level page.
            //
            backToMenu = new PageView.IDoBackToMenu()
            {
                @Override
                public void doBackToMenu()
                {
//                    Intent intent = new Intent(this, typeof(AppDetailActivity));
//                    intent.putExtra("endpoint", app.getEndpoint());
//                    NavUtils.navigateUpTo(SynchroPageActivity.this, intent);
                    finish();
                }
            };
        }

        _stateManager = new StateManager(appManager, app, transport, deviceMetrics);
        _pageView = new AndroidPageView(_stateManager, _stateManager.getViewModel(), this, layout, backToMenu);

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
                                            }, new StateManager.IProcessMessageBox()
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
                                            });
        _stateManager.startApplicationAsync();
    }

    public int getScreenOrientation()
    {
        // !!! Fix this in the really complicated way required.
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
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
}

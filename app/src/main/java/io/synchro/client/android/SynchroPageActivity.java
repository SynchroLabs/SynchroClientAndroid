package io.synchro.client.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;

import java.io.IOException;
import java.net.MalformedURLException;

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
        if (item.getItemId() == R.id.home)
        {
            try
            {
                return _pageView.OnCommandBarUp(item);
            }
            catch (IOException e)
            {
                Log.wtf(TAG, e);
            }
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

        AndroidSynchroAppManager appManager = new AndroidSynchroAppManager(this);
        try
        {
            appManager.loadState();
        }
        catch (IOException e)
        {
            Log.wtf(TAG, e);
        }

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
                }
            };
        }

        _stateManager = new StateManager(appManager, app, transport, deviceMetrics);
        _pageView = new AndroidPageView(_stateManager, _stateManager.getViewModel(), this, layout, backToMenu);

        _pageView.setSetPageTitle(new PageView.ISetPageTitle()
                                  {
                                      @Override
                                      public void setPageTitle(String title)
                                      {
                                          SynchroPageActivity.this.getActionBar().setTitle(title);
                                      }
                                  });

        setContentView(layout);

        _stateManager.SetProcessingHandlers(new StateManager.IProcessPageView()
                                            {
                                                @Override
                                                public void ProcessPageView(JObject pageView)
                                                {
                                                    _pageView.ProcessPageView(pageView);
                                                }
                                            }, new StateManager.IProcessMessageBox()
                                            {
                                                @Override
                                                public void ProcessMessageBox(
                                                        JObject messageBox,
                                                        StateManager.ICommandHandler commandHandler
                                                                             )
                                                {
                                                    _pageView.ProcessMessageBox(messageBox, commandHandler);
                                                }
                                            });
        try
        {
            _stateManager.startApplicationAsync();
        }
        catch (IOException e)
        {
            Log.wtf(TAG, e);
        }
    }
}

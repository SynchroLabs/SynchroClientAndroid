package io.synchro.client.android;

import android.app.Activity;
import android.test.AndroidTestCase;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;

import io.synchro.json.JObject;
import io.synchro.json.JValue;

/**
 * Created by blake on 2/26/15.
 */
public class StateManagerTest extends AndroidTestCase
{
    // This could probably be more thourough.  We create a StateManager and use it to connect to the local server, starting
    // the samples app (which consists of getting the app definition from the server, then getting the "main" page), then on
    // receipt of that page (the Menu page), issue a command which navigates to the Hello page.
    //
    public void testStateManager()
            throws IOException, InterruptedException
    {
        // Force fresh load of AppManager from the bundled seed...
        //
        SynchroAppTest.TestAppManager appManager = new SynchroAppTest.TestAppManager();
        JObject appDefinition = new JObject();
        final Object waitObject = new Object();

        appDefinition.put("name", new JValue("synchro-samples"));
        appDefinition.put("description", new JValue("Synchro API Samples"));

        SynchroApp app = new SynchroApp("10.0.2.2:1337/api/samples", appDefinition, null);

        appManager.getApps().add(app);

        TransportAndroidHttpClient transport = new TransportAndroidHttpClient(new URL("http://" + app.getEndpoint()));

        StateManager stateManager = new StateManager(appManager, app, transport, new AndroidSynchroDeviceMetrics(getContext()), null);

        final int responseNumber[] = new int[] { 0 };
        final JObject thePageView[] = new JObject[] { null };

        StateManager.IProcessAppExit processAppExit = new StateManager.IProcessAppExit() {
            @Override
            public void ProcessAppExit()
            {
            }
        };

        stateManager.SetProcessingHandlers(new StateManager.IProcessPageView()
                                           {
                                               @Override
                                               public void ProcessPageView(JObject pageView)
                                               {
                                                   ++responseNumber[0];
                                                   thePageView[0] = pageView;
                                                   synchronized (waitObject)
                                                   {
                                                       waitObject.notify();
                                                   }
                                               }
                                           },
                                           processAppExit,
                                           new StateManager.IProcessMessageBox()
                                           {
                                               @Override
                                               public void ProcessMessageBox(
                                                       JObject messageBox,
                                                       StateManager.ICommandHandler commandHandler
                                                                            )
                                               {
                                                   fail("Unexpected message box call in test");
                                               }
                                           }, new StateManager.IProcessUrl()
                                           {
                                               @Override
                                               public void ProcessUrl(String primaryUrl, String secondaryUrl)
                                               {
                                                   fail("Unexpected processLaunchUrl call in test");
                                               }
                                           });

        stateManager.startApplicationAsync();

        synchronized (waitObject)
        {
            waitObject.wait();
        }
        assertEquals(1, responseNumber[0]);

        JObject commandParameters = new JObject();

        commandParameters.put("view", new JValue("hello"));

        stateManager.sendCommandRequestAsync("goToView", commandParameters);

        synchronized (waitObject)
        {
            waitObject.wait();
        }
        assertEquals(2, responseNumber[0]);
        assertEquals("Hello World", thePageView[0].get("title").asString());

        assertFalse(stateManager.IsOnMainPath());
    }
}

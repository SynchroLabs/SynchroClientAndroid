package io.synchro.client.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by blake on 2/16/15.
 */
// If AppSeed is present, client apps should launch that app directly and suppress any "launcher" interface. If not present,
// then client apps should provide launcher interface showing content of Apps.
//
// Implementation:
//
// On startup:
//   Inspect bundled seed.json to see if it contains a "seed"
//     If yes: Start Maaas app at that seed.
//     If no: determine whether any local app manager state exists (stored locally on the device)
//       If no: initialize local app manager state from seed.json
//     Show launcher interface based on local app manager state
//
// Launcher interface shows a list of apps (from the "apps" key in the app manager state)
//   Provides ability to launch an app
//   Provides ability to add (find?) and remove app
//     Add/find app:
//       User provides endpoint
//       We look up app definition at endpoint and display to user
//       User confirms and we add to AppManager.Apps (using endpoint and appDefinition to create MaaasApp)
//       We serialize AppManager via saveState()
//
public abstract class SynchroAppManager
{
    protected SynchroApp            _appSeed = null;
    protected ArrayList<SynchroApp> _apps    = new ArrayList<>();

    public SynchroApp getAppSeed()
    {
        return _appSeed;
    }

    public List<SynchroApp> getApps()
    {
        return _apps;
    }

    public SynchroApp GetApp(String endpoint)
    {
        if ((_appSeed != null) && (_appSeed.getEndpoint().equals(endpoint)))
        {
            return _appSeed;
        }
        else
        {
            for (SynchroApp app : _apps)
            {
                if (app.getEndpoint().equals(endpoint))
                {
                    return app;
                }
            }
        }
        return null;
    }

    public void UpdateApp(SynchroApp app)
    {
        if (_appSeed.getEndpoint().equals(app.getEndpoint()))
        {
            _appSeed = app;
        }
        else
        {
            for (int i = _apps.size() - 1; i >= 0; i--)
            {
                if (app.getEndpoint().equals(_apps.get(i).getEndpoint()))
                {
                    _apps.remove(i);
                }
            }
            _apps.add(app);
        }
    }

    private static SynchroApp appFromJson(JObject json)
    {
        String endpoint = json.get("endpoint").asString();
        JObject appDefinition = (JObject) json.get("definition").deepClone();
        JToken sessionIdToken = json.get("sessionId");
        String sessionId = (sessionIdToken == null) ? null : sessionIdToken.asString();

        return new SynchroApp(endpoint, appDefinition, sessionId);
    }

    private static JObject appToJson(SynchroApp app)
    {
        JObject returnObject = new JObject();
        returnObject.put("endpoint", new JValue(app.getEndpoint()));
        returnObject.put("definition", app.getAppDefinition().deepClone());
        returnObject.put("sessionId", new JValue(app.getSessionId()));

        return returnObject;
    }

    public void serializeFromJson(JObject json)
    {
        JObject seed = (JObject) json.get("seed");
        if (seed != null)
        {
            _appSeed = appFromJson(seed);
        }

        JArray apps = (JArray) json.get("apps");
        if (apps != null)
        {
            for (JToken item : apps)
            {
                JObject app = (JObject) item;
                if (app != null)
                {
                    _apps.add(appFromJson(app));
                }
            }
        }
    }

    public JObject serializeToJson()
    {
        JObject obj = new JObject();

        if (_appSeed != null)
        {
            obj.put("seed", appToJson(_appSeed));
        }

        if (_apps.size() > 0)
        {
            JArray array = new JArray();
            for (SynchroApp app : _apps)
            {
                array.add(appToJson(app));
            }
            obj.put("apps", array);
        }

        return obj;
    }

    public boolean loadState()
            throws IOException
    {
        String bundledState = loadBundledState();
        JObject parsedBundledState = (JObject)JToken.parse(bundledState);

        JObject seed = (JObject) parsedBundledState.get("seed");
        if (seed != null)
        {
            // If the bundled state contains a "seed", then we're just going to use that as the
            // app state (we'll launch the app inidicated by the seed and suppress the launcher).
            //
            serializeFromJson(parsedBundledState);
        }
        else
        {
            // If the bundled state doesn't contain a seed, load the local state...
            //
            String localState = loadLocalState();
            if (localState == null)
            {
                // If there is no local state, initialize the local state from the bundled state.
                //
                localState = bundledState;
                saveLocalState(localState);
            }
            JObject parsedLocalState = (JObject)JToken.parse(localState);
            serializeFromJson(parsedLocalState);
        }

        return true;
    }

    public boolean saveState()
            throws IOException
    {
        JObject json = this.serializeToJson();
        return saveLocalState(json.toJson());
    }

    // Abstract serialization that may or may not be async per platform.
    //
    // The pattern used here is not totally obvious.  Each platform will implement the methods below.
    // Those method implementation may or may not be asynchronous.  To accomodate this, the calls to
    // these methods from the base class need to use await and the implementations must return a task.
    // Derived class implementations that are async will just declare as async and return the basic
    // return value (string or bool, as appropriate).  Derived class implementations that are not async
    // will simpley execute synchronously and return a completed task (wrapped around the basic return
    // value).  This is really the only workable way to deal with the issue of not knowing whether all
    // derived class implentations either will or will not be async.
    //
    protected abstract String loadBundledState() throws IOException;
    protected abstract String loadLocalState() throws IOException;
    protected abstract boolean saveLocalState(String state) throws IOException;
}
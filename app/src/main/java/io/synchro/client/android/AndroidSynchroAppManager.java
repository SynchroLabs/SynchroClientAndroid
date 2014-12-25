package io.synchro.client.android;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.TreeMap;

/**
 * Created by blake on 12/24/14.
 */
public class AndroidSynchroAppManager
{
    public static final String TAG = AndroidSynchroAppManager.class.getSimpleName();

    // http://stackoverflow.com/questions/14057273/android-singleton-with-global-context

    private static AndroidSynchroAppManager instance;

    private static final String SEED_FILENAME = "seed.json";

    private final Context context;
    private final TreeMap<String, SynchroApp> apps = new TreeMap<>();

    public AndroidSynchroAppManager(Context context)
    {
        this.context = context;
    }

    public void load()
            throws IOException
    {
        PushbackReader reader = new PushbackReader(
                new InputStreamReader(context.getAssets().open(SEED_FILENAME))
        );

        //noinspection TryFinallyCanBeTryWithResources
        try
        {
            JObject returnedObject = (JObject) JsonParser.parseValue(reader);
            Log.d(TAG, String.format("Returned parsed object is %s", returnedObject.toJson()));

            for (JToken app : ((JArray)returnedObject.get("apps")))
            {
                SynchroApp synchroApp = new SynchroApp((JObject) app);
                apps.put(synchroApp.getEndpoint(), synchroApp);
                Log.d(TAG, String.format("Added app \"%s\" at endpoint %s", synchroApp.getName(), synchroApp.getEndpoint()));
            }
        }
        finally
        {
            reader.close();
        }
    }

    public SynchroApp[] getApps()
    {
        SynchroApp[] returnArray = new SynchroApp[apps.size()];
        apps.values().toArray(returnArray);
        return returnArray;
    }

    public static AndroidSynchroAppManager getAppManager(Context context)
    {
        if (instance == null)
        {
            instance = getSync(context);
        }

        return instance;
    }

    private static synchronized AndroidSynchroAppManager getSync(Context context)
    {
        if (instance == null)
        {
            instance = new AndroidSynchroAppManager(context.getApplicationContext());
        }
        
        return instance;
    }
}

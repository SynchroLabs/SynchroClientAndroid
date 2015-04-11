package io.synchro.client.android;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;

/**
 * Created by blake on 12/24/14.
 */
public class AndroidSynchroAppManager extends SynchroAppManager
{
    // http://stackoverflow.com/questions/14057273/android-singleton-with-global-context

    private static AndroidSynchroAppManager instance;

    private static final String SEED_FILENAME = "seed.json";

    private static final String STATE_FILE = "synchro";
    private static final String STATE_KEY = "seed.json";

    private final Context context;

    private AndroidSynchroAppManager(Context context)
    {
        this.context = context;
    }

    @Override
    protected String loadBundledState()
            throws IOException
    {
        PushbackReader reader = new PushbackReader(
                new InputStreamReader(context.getAssets().open(SEED_FILENAME))
        );
        StringBuilder finalString = new StringBuilder();

        // Current Android does not support TryWithResources
        //noinspection TryFinallyCanBeTryWithResources
        try
        {
            int thisChar;
            while ((thisChar = reader.read()) != -1)
            {
                finalString.append((char) thisChar);
            }
        }
        finally
        {
            reader.close();
        }

        return finalString.toString();
    }

    @Override
    protected String loadLocalState()
            throws IOException
    {
        SharedPreferences preferences = context.getSharedPreferences(STATE_FILE, Context.MODE_PRIVATE);
        //ISharedPreferences preferences = _activity.GetPreferences(FileCreationMode.Private);
        return preferences.getString(STATE_KEY, null);
    }

    @Override
    protected boolean saveLocalState(String state)
            throws IOException
    {
        SharedPreferences preferences = context.getSharedPreferences(STATE_FILE, Context.MODE_PRIVATE);
        // ISharedPreferences preferences = _activity.GetPreferences(FileCreationMode.Private);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(STATE_KEY, state);
        return editor.commit();
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

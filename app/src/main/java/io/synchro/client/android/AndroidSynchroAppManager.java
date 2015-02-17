package io.synchro.client.android;

import android.content.Context;

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

    private final Context context;

    public AndroidSynchroAppManager(Context context)
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
        return null;
    }

    @Override
    protected boolean saveLocalState(String state)
            throws IOException
    {
        return false;
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

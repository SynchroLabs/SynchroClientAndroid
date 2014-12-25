package io.synchro.client.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class SplashActivity extends Activity
{
    public static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AndroidSynchroAppManager appManager = AndroidSynchroAppManager.getAppManager(this);
        try
        {
            appManager.load();
        }
        catch (IOException e)
        {
            Log.wtf(TAG, e);
        }

        startActivity(new Intent(this, LauncherActivity.class));
    }
}

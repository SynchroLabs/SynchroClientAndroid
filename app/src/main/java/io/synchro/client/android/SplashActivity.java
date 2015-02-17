package io.synchro.client.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

public class SplashActivity extends Activity
{
    public static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_TIME_OUT_MS = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // http://www.androidhive.info/2013/07/how-to-implement-android-splash-screen-2/

        new Handler().postDelayed(new Runnable() {
              @Override
              public void run() {
                  // This method will be executed once the timer is over
                  // Start your app main activity

                  AndroidSynchroAppManager appManager = AndroidSynchroAppManager.getAppManager(SplashActivity.this);
                  try
                  {
                      appManager.loadState();
                  }
                  catch (IOException e)
                  {
                      Log.wtf(TAG, e);
                  }

                  startActivity(new Intent(SplashActivity.this, LauncherActivity.class));
                  finish();
              }
          }, SPLASH_TIME_OUT_MS);
    }
}

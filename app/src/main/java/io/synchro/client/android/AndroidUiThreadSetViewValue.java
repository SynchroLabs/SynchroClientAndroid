package io.synchro.client.android;

import android.app.Activity;

import io.synchro.json.JToken;

/**
 * Created by blake on 3/18/15.
 */
public abstract class AndroidUiThreadSetViewValue implements ISetViewValue
{
    private final Activity activity;

    public AndroidUiThreadSetViewValue(Activity activity)
    {
        this.activity = activity;
    }

    @Override
    public void SetViewValue(final JToken value)
    {
        activity.runOnUiThread(new Runnable()
                               {
                                   @Override
                                   public void run()
                                   {
                                       UiThreadSetViewValue(value);
                                   }
                               });
    }

    protected abstract void UiThreadSetViewValue(JToken value);
}

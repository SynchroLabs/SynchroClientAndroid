package io.synchro.client.android.controls;

import android.util.Log;
import android.widget.ProgressBar;

import io.synchro.client.android.BindingContext;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.JObject;

/**
 * Created by blake on 3/23/15.
 */
public class AndroidProgressRingWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidProgressRingWrapper.class.getSimpleName();

    public AndroidProgressRingWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                                     )
    {
        super(parent, bindingContext);
        Log.d(TAG, "Creating progress ring element");

        ProgressBar bar = new ProgressBar(((AndroidControlWrapper)parent).getControl().getContext());
        bar.setIndeterminate(true);

        this._control = bar;

        applyFrameworkElementDefaults(bar);

        // processElementProperty((string)controlSpec["value"], value => button.Text = ToString(value));
    }
}

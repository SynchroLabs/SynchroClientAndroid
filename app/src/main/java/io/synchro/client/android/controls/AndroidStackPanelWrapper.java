package io.synchro.client.android.controls;

import android.util.Log;
import android.widget.LinearLayout;

import io.synchro.client.android.BindingContext;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.ISetViewValue;
import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

/**
 * Created by blake on 3/16/15.
 */
public class AndroidStackPanelWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidStackPanelWrapper.class.getSimpleName();

    public AndroidStackPanelWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                                   )
    {
        super(parent, bindingContext);
        Log.d(TAG, "Creating stack panel element");

        final LinearLayout layout = new LinearLayout(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = layout;

        applyFrameworkElementDefaults(layout);

        // When orientation is horizontal, items are baseline aligned by default, and in this case all the vertical gravity does is specify
        // how to position the entire group of baseline aligned items if there is extra vertical space.  This is not what we want.  Turning
        // off baseline alignment causes the vertical gravity to work as expected (aligning each item to top/center/bottom).
        //
        layout.setBaselineAligned(false);

        if (controlSpec.get("orientation") == null)
        {
            layout.setOrientation(LinearLayout.VERTICAL);
        }
        else
        {
            processElementProperty(controlSpec.get("orientation"), new ISetViewValue()
                                   {
                                       @Override
                                       public void SetViewValue(JToken value)
                                       {
                                           layout.setOrientation(
                                                   ToOrientation(
                                                           value, LinearLayout.VERTICAL
                                                                ) == LinearLayout.HORIZONTAL ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL
                                                                );
                                       }
                                   });
        }

        processThicknessProperty(controlSpec.get("padding"), new PaddingThicknessSetter(this.getControl()));

        if (controlSpec.get("contents") != null)
        {
            createControls((JArray)controlSpec.get("contents"), new IAndroidCreateControl()
                           {
                               @Override
                               public void onCreateControl(
                                       JObject controlSpec, AndroidControlWrapper controlWrapper
                                                          )
                               {
                                   controlWrapper.AddToLinearLayout(layout, controlSpec);
                               }
                           });
        }

        layout.forceLayout();
    }
}

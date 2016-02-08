package io.synchro.client.android.controls;

import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.ControlWrapper;
import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

/**
 * Created by blake on 3/23/15.
 */
public class AndroidWrapPanelWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidWrapPanelWrapper.class.getSimpleName();
    public AndroidWrapPanelWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                                  )
    {
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, "Creating wrap panel element");

        final FlowLayout layout = new FlowLayout(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = layout;

        applyFrameworkElementDefaults(layout);

        if (controlSpec.get("orientation") == null)
        {
            layout.setOrientation(LinearLayout.VERTICAL);
        }
        else
        {
            processElementProperty(controlSpec, "orientation", new AndroidUiThreadSetViewValue((Activity) layout.getContext())
                                   {
                                       @Override
                                       protected void UiThreadSetViewValue(JToken value)
                                       {
                                           layout.setOrientation(ToOrientation(value, LinearLayout.VERTICAL));
                                       }
                                   });
        }

        processElementProperty(controlSpec, "itemHeight", new AndroidUiThreadSetViewValue((Activity) layout.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       layout.setItemHeight((int)ToDeviceUnits(value));
                                   }
                               });
        processElementProperty(controlSpec, "ItemWidth", new AndroidUiThreadSetViewValue((Activity) layout.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       layout.setItemWidth((int) ToDeviceUnits(value));
                                   }
                               });

        processThicknessProperty(controlSpec, "padding", new PaddingThicknessSetter(this.getControl()));

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

package io.synchro.client.android.controls;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.AbsoluteLayout;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.ControlWrapper;
import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

/**
 * Created by blake on 11/9/15.
 */
public class AndroidCanvasWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidCanvasWrapper.class.getSimpleName();

    public AndroidCanvasWrapper(ControlWrapper parent, BindingContext bindingContext, JObject controlSpec)
    {
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, "Creating canvas element");

        final AbsoluteLayout absLayout = new AbsoluteLayout(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = absLayout;
        final Drawable defaultBackground = absLayout.getBackground();

        // !!! Absolute layout supports padding
        // !!! http://alvinalexander.com/java/jwarehouse/android/core/java/android/widget/AbsoluteLayout.java.shtml

        applyFrameworkElementDefaults(absLayout);
        processElementProperty(
                controlSpec, "background", new AndroidUiThreadSetViewValue((Activity) _control.getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        Integer color = ToColor(value, null);
                        if (color != null)
                        {
                            absLayout.setBackgroundColor(color);
                        }
                        else
                        {
                            absLayout.setBackground(defaultBackground);
                        }
                    }
                });

        if (controlSpec.get("contents") != null)
        {
            createControls((JArray)controlSpec.get("contents"), new IAndroidCreateControl()
                           {
                               @Override
                               public void onCreateControl(
                                       JObject childControlSpec, final AndroidControlWrapper childControlWrapper
                                                          )
                               {
                                   // Create an appropriate set of LayoutParameters...
                                   //
                                   if (childControlWrapper.getControl().getLayoutParams() != null)
                                   {
                                       childControlWrapper.getControl().setLayoutParams(new AbsoluteLayout.LayoutParams(
                                               childControlWrapper.getControl().getLayoutParams().width, childControlWrapper.getControl().getLayoutParams().height, 0, 0
                                       ));
                                   }
                                   else
                                   {
                                       childControlWrapper.getControl().setLayoutParams(new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0));
                                   }

                                   absLayout.addView(childControlWrapper.getControl());

                                   // Bind the x and y position to the appropriate properties of the AbsoluteLayout.LayoutParams....
                                   //
                                   childControlWrapper.processElementProperty(
                                           childControlSpec, "left",
                                           new AndroidUiThreadSetViewValue((Activity) _control.getContext())
                                           {
                                               @Override
                                               protected void UiThreadSetViewValue(JToken value)
                                               {
                                                   ((AbsoluteLayout.LayoutParams)childControlWrapper.getControl().getLayoutParams()).x = (int)ToDeviceUnits(value);
                                                   childControlWrapper.getControl().setLayoutParams(
                                                           childControlWrapper.getControl()
                                                                              .getLayoutParams()
                                                                                                   );
                                                   absLayout.forceLayout();
                                               }
                                           });

                                   childControlWrapper.processElementProperty(
                                           childControlSpec, "top",
                                           new AndroidUiThreadSetViewValue((Activity) _control.getContext())
                                           {
                                               @Override
                                               protected void UiThreadSetViewValue(JToken value)
                                               {
                                                   ((AbsoluteLayout.LayoutParams)childControlWrapper.getControl().getLayoutParams()).y = (int)ToDeviceUnits(value);
                                                   childControlWrapper.getControl().setLayoutParams(
                                                           childControlWrapper.getControl().getLayoutParams());
                                                   absLayout.forceLayout();
                                               }
                                           });
                               }
                           });
        }
    }
}

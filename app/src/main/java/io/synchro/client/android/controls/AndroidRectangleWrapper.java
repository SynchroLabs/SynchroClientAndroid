package io.synchro.client.android.controls;

import android.app.Activity;
import android.util.Log;

import io.synchro.client.android.*;

/**
 * Created by blake on 3/2/15.
 */
public class AndroidRectangleWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidRectangleWrapper.class.getSimpleName();

    AndroidSynchroRectDrawable _rect = new AndroidSynchroRectDrawable();

    public AndroidRectangleWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
              )
{
        super(parent, bindingContext);
        Log.d(TAG, "Creating rectangle");

        AndroidDrawableView drawableView = new AndroidDrawableView(((AndroidControlWrapper)parent).getControl().getContext(), _rect);
        this._control = drawableView;

        applyFrameworkElementDefaults(drawableView);

        processElementProperty(controlSpec.get("border"), new AndroidUiThreadSetViewValue((Activity) drawableView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.SetStrokeColor(ToColor(value));
                                   }
                               });

        processElementProperty(controlSpec.get("borderThickness"), new AndroidUiThreadSetViewValue((Activity) drawableView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.SetStrokeWidth((int)ToDeviceUnits(value));
                                   }
                               });

        processElementProperty(controlSpec.get("cornerRadius"), new AndroidUiThreadSetViewValue((Activity) drawableView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.setCornerRadius(((float)ToDeviceUnits(value)));
                                   }
                               });

        processElementProperty(controlSpec.get("fill"), new AndroidUiThreadSetViewValue((Activity) drawableView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.SetFillColor(ToColor(value));
                                   }
                               });
    }
}

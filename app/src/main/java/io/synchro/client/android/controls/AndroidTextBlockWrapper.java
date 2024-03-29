package io.synchro.client.android.controls;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.ISetViewValue;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

/**
 * Created by blake on 3/17/15.
 */
public class AndroidTextBlockWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidTextBlockWrapper.class.getSimpleName();

    public AndroidTextBlockWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                                  )
    {
        super(parent, bindingContext, controlSpec);
        Log.d(
                TAG, String.format(
                        "Creating text view element with text of: %s",
                        (controlSpec.get("value")) != null ? controlSpec.get("value").asString() : "(none)"
                                  )
             );

        final TextView textView = new TextView(
                ((AndroidControlWrapper) parent).getControl().getContext()
        );

        // TextAppearance.Medium value of 18 seems to be a good default (the real default is TextAppearance.Small,
        // which is 14, and is pretty small).  Of course any explicit font size will override this.
        textView.setTextSize(18);

        this._control = textView;

        applyFrameworkElementDefaults(textView);

        processElementProperty(controlSpec, "value", new AndroidUiThreadSetViewValue((Activity) textView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       textView.setText(ToString(value, ""));
                                   }
                               });

        processElementProperty(controlSpec, "ellipsize", new ISetViewValue()
                               {
                                   @Override
                                   public void SetViewValue(JToken value)
                                   {
                                       // Other trimming options:
                                       //
                                       //   Android.Text.TextUtils.TruncateAt.Start;
                                       //   Android.Text.TextUtils.TruncateAt.Middle;
                                       //   Android.Text.TextUtils.TruncateAt.Marquee;
                                       //
                                       boolean bEllipsize = ToBoolean(value, false);
                                       if (bEllipsize)
                                       {
                                           textView.setEllipsize(TextUtils.TruncateAt.END);
                                       }
                                       else
                                       {
                                           textView.setEllipsize(null);
                                       }
                                   }
                               });

        processElementProperty(controlSpec, "textAlignment", new ISetViewValue()
                               {
                                   @Override
                                   public void SetViewValue(JToken value)
                                   {
                                       // This gravity here specifies how this control's contents should be aligned within the control box, whereas
                                       // the layout gravity specifies how the control box itself should be aligned within its container.
                                       //
                                       String alignString = ToString(value, "");
                                       switch (alignString)
                                       {
                                           case "Left":
                                               textView.setGravity(Gravity.LEFT);
                                               break;
                                           case "Center":
                                               textView.setGravity(Gravity.CENTER_HORIZONTAL);
                                               break;
                                           case "Right":
                                               textView.setGravity(Gravity.RIGHT);
                                               break;
                                       }
                                   }
                               });
    }
}

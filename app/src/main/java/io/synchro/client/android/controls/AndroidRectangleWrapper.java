package io.synchro.client.android.controls;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import io.synchro.client.android.*;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

/**
 * Created by blake on 3/2/15.
 */
public class AndroidRectangleWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidRectangleWrapper.class.getSimpleName();
    static              String[] Commands = new String[]{CommandName.getOnTap().getAttribute()};

    AndroidSynchroRectDrawable _rect = new AndroidSynchroRectDrawable();

    public AndroidRectangleWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
              )
{
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, "Creating rectangle");

        AndroidDrawableView drawableView = new AndroidDrawableView(((AndroidControlWrapper)parent).getControl().getContext(), _rect);
        this._control = drawableView;

        applyFrameworkElementDefaults(drawableView);

        processElementProperty(controlSpec, "border", new AndroidUiThreadSetViewValue((Activity) drawableView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.SetStrokeColor(ToColor(value));
                                   }
                               });

        processElementProperty(controlSpec, "borderThickness", new AndroidUiThreadSetViewValue((Activity) drawableView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.SetStrokeWidth((int)ToDeviceUnits(value));
                                   }
                               });

        processElementProperty(controlSpec, "cornerRadius", new AndroidUiThreadSetViewValue((Activity) drawableView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.setCornerRadius(((float)ToDeviceUnits(value)));
                                   }
                               });

        processElementProperty(controlSpec, "fill", new AndroidUiThreadSetViewValue((Activity) drawableView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.SetFillColor(ToColor(value));
                                   }
                               });

        JObject bindingSpec = BindingHelper
                .GetCanonicalBindingSpec(
                        controlSpec, CommandName.getOnTap().getAttribute(), Commands
                                        );
        ProcessCommands(bindingSpec, Commands);

        if (GetCommand(CommandName.getOnTap()) != null)
        {
            drawableView.setOnClickListener(new View.OnClickListener()
                                     {
                                         @Override
                                         public void onClick(View v)
                                         {
                                             CommandInstance command = GetCommand(CommandName.getOnTap());
                                             if (command != null)
                                             {
                                                 Log.d(TAG, String.format("Rectangle tap with command: %s", command.getCommand()));
                                                 AndroidRectangleWrapper.this.getStateManager().sendCommandRequestAsync(
                                                         command.getCommand(),
                                                         command.GetResolvedParameters(
                                                                 getBindingContext()
                                                                                      ));
                                             }
                                         }
                                     });
        }
    }
}

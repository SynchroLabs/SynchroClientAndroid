package io.synchro.client.android.controls;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import io.synchro.client.android.BindingContext;
import io.synchro.client.android.BindingHelper;
import io.synchro.client.android.CommandInstance;
import io.synchro.client.android.CommandName;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.ISetViewValue;
import io.synchro.client.android.JObject;
import io.synchro.client.android.JToken;

/**
 * Created by blake on 3/17/15.
 */
public class AndroidButtonWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidButtonWrapper.class.getSimpleName();
    static String[] Commands = new String[] { CommandName.getOnClick().getAttribute() };

    public AndroidButtonWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                               )
    {
        super(parent, bindingContext);
        Log.d(TAG, String.format("Creating button element with caption of: %s", controlSpec.get("caption").asString()));
        final Button button = new Button(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = button;

        applyFrameworkElementDefaults(button);

        processElementProperty(controlSpec.get("caption"), new ISetViewValue()
                               {
                                   @Override
                                   public void SetViewValue(JToken value)
                                   {
                                       button.setText(ToString(value, ""));
                                   }
                               });

        JObject bindingSpec = BindingHelper
                .GetCanonicalBindingSpec(controlSpec, CommandName.getOnClick().getAttribute(), Commands);
        ProcessCommands(bindingSpec, Commands);

        if (GetCommand(CommandName.getOnClick()) != null)
        {
            button.setOnClickListener(new View.OnClickListener()
                                      {
                                          @Override
                                          public void onClick(View v)
                                          {
                                              CommandInstance command = GetCommand(CommandName.getOnClick());
                                              if (command != null)
                                              {
                                                  Log.d(TAG, String.format("Button click with command: %s", command));
                                                  AndroidButtonWrapper.this.getStateManager().sendCommandRequestAsync(
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

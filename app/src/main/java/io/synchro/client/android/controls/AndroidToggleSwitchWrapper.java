package io.synchro.client.android.controls;

import android.app.Activity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.BindingHelper;
import io.synchro.client.android.CommandInstance;
import io.synchro.client.android.CommandName;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.IGetViewValue;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JValue;

/**
 * Created by blake on 3/23/15.
 */
public class AndroidToggleSwitchWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidToggleSwitchWrapper.class.getSimpleName();
    static String[] Commands = new String[]{CommandName.getOnToggle().getAttribute()};

    public AndroidToggleSwitchWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                                     )
    {
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, "Creating toggle switch");

        final Switch toggleSwitch = new Switch(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = toggleSwitch;

        applyFrameworkElementDefaults(toggleSwitch);

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "value", Commands);
        ProcessCommands(bindingSpec, Commands);

        if (!processElementBoundValue("value", bindingSpec.get("value").asString(), new IGetViewValue()
                                      {
                                          @Override
                                          public JToken GetViewValue()
                                          {
                                              return new JValue(toggleSwitch.isChecked());
                                          }
                                      }, new AndroidUiThreadSetViewValue((Activity) toggleSwitch.getContext())
                                      {
                                          @Override
                                          protected void UiThreadSetViewValue(JToken value)
                                          {
                                            toggleSwitch.setChecked(ToBoolean(value, false));
                                          }
                                      }))
        {
            processElementProperty(controlSpec, "value", new AndroidUiThreadSetViewValue((Activity) toggleSwitch.getContext())
                                   {
                                       @Override
                                       protected void UiThreadSetViewValue(JToken value)
                                       {
                                           toggleSwitch.setChecked(ToBoolean(value, false));
                                       }
                                   });
        }

        processElementProperty(controlSpec, "caption", new AndroidUiThreadSetViewValue((Activity) toggleSwitch.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       toggleSwitch.setText(ToString(value, ""));
                                   }
                               });

        processElementProperty(controlSpec, "onLabel", new AndroidUiThreadSetViewValue((Activity) toggleSwitch.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       toggleSwitch.setTextOn(ToString(value, ""));
                                   }
                               });

        processElementProperty(controlSpec, "offLabel", new AndroidUiThreadSetViewValue((Activity) toggleSwitch.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       toggleSwitch.setTextOff(ToString(value, ""));
                                   }
                               });

        // Since the Toggled handler both updates the view model (locally) and may potentially have a command associated,
        // we have to add handler in all cases (even when there is no command).
        //
        toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                                                {
                                                    @Override
                                                    public void onCheckedChanged(
                                                            CompoundButton buttonView,
                                                            boolean isChecked
                                                                                )
                                                    {
                                                        updateValueBindingForAttribute("value");

                                                        CommandInstance command = GetCommand(CommandName.getOnToggle());
                                                        if (command != null)
                                                        {
                                                            Log.d(TAG, String.format("ToggleSwitch toggled with command: %s", command.getCommand()));
                                                            AndroidToggleSwitchWrapper.this.getStateManager().sendCommandRequestAsync(command.getCommand(), command.GetResolvedParameters(getBindingContext()));
                                                        }
                                                    }
                                                });
    }
}

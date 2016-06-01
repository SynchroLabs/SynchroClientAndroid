package io.synchro.client.android.controls;

import android.app.Activity;
import android.util.Log;
import android.view.MenuItem;

import io.synchro.client.android.AndroidActionBarItem;
import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.BindingHelper;
import io.synchro.client.android.CommandInstance;
import io.synchro.client.android.CommandName;
import io.synchro.client.android.ControlWrapper;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

/**
 * Created by blake on 3/24/15.
 */
public class AndroidActionWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidActionWrapper.class.getSimpleName();
    static String[] Commands = new String[] { CommandName.getOnClick().getAttribute() };

    public AndroidActionWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                               )
    {
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, String.format("Creating action bar item with title of: %s", controlSpec.get("text").asString()));

        this._isVisualElement = false;

        final AndroidActionBarItem actionBarItem = _pageView.CreateAndAddActionBarItem();

        processElementProperty(controlSpec, "text", new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       actionBarItem.setTitle(ToString(value, ""));
                                   }
                               });
        processElementProperty(controlSpec, "icon", new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       actionBarItem.setIcon(getResourceNameFromIcon(ToString(value, "")));
                                   }
                               });
        processElementProperty(controlSpec, "enabled", new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       actionBarItem.setEnabled(ToBoolean(value, false));
                                   }
                               });

        actionBarItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if (controlSpec.get("showAsAction") != null)
        {
            if (controlSpec.get("showAsAction").asString().equals("Always"))
            {
                actionBarItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            else if (controlSpec.get("showAsAction").asString().equals("IfRoom"))
            {
                actionBarItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        if (controlSpec.get("showActionAsText") != null)
        {
            if (ToBoolean(controlSpec.get("showActionAsText"), false))
            {
                actionBarItem.setShowAsAction(actionBarItem.getShowAsAction() | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
        }

        JObject bindingSpec = BindingHelper
                .GetCanonicalBindingSpec(controlSpec, CommandName.getOnClick().getAttribute(), Commands);
        ProcessCommands(bindingSpec, Commands);

        if (GetCommand(CommandName.getOnClick()) != null)
        {
            actionBarItem.setOnItemSelected(new AndroidActionBarItem.IOnItemSelected()
                                            {
                                                @Override
                                                public void OnItemSelected()
                                                {
                                                    CommandInstance command = GetCommand(CommandName.getOnClick());
                                                    if (command != null)
                                                    {
                                                        AndroidActionWrapper.this.getStateManager().sendCommandRequestAsync(command.getCommand(), command.GetResolvedParameters(getBindingContext()));
                                                    }

                                                }
                                            });
        }
    }
}

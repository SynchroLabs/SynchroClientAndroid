package io.synchro.client.android.controls;

import android.app.Activity;
import android.util.Log;
import android.view.MenuItem;

import io.synchro.client.android.*;
import io.synchro.json.*;

/**
 * Created by blake on 11/9/15.
 */
public class AndroidActionToggleWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidActionToggleWrapper.class.getSimpleName();

    static String[] Commands = new String[]{CommandName.getOnToggle().getAttribute()};

    protected AndroidActionBarItem _actionBarItem;

    protected boolean _isChecked = false;
    protected String _uncheckedText;
    protected String _checkedText;
    protected String _uncheckedIcon;
    protected String _checkedIcon;

    public boolean getIsChecked()
    {
        return _isChecked;
    }

    public void setIsChecked(boolean value)
    {
        if (_isChecked != value)
        {
            _isChecked = value;
            if (_isChecked)
            {
                if (_checkedText != null)
                {
                    _actionBarItem.setTitle(_checkedText);
                }
                if (_checkedIcon != null)
                {
                    _actionBarItem.setIcon(_checkedIcon);
                }
            }
            else
            {
                if (_uncheckedText != null)
                {
                    _actionBarItem.setTitle(_uncheckedText);
                }
                if (_uncheckedIcon != null)
                {
                    _actionBarItem.setIcon(_uncheckedIcon);
                }
            }
        }
    }

    public String getUncheckedText()
    {
        return _uncheckedText;
    }

    public void setUncheckedText(String value)
    {
        _uncheckedText = value;
        if (!_isChecked)
        {
            _actionBarItem.setTitle(_uncheckedText);
        }
    }

    public String getCheckedText()
    {
        return _checkedText;
    }

    public void setCheckedText(String value)
    {
        _checkedText = value;
        if (_isChecked)
        {
            _actionBarItem.setTitle(_checkedText);
        }
    }

    public String getUncheckedIcon()
    {
        return _uncheckedIcon;
    }

    public void setUncheckedIcon(String value)
    {
        _uncheckedIcon = value;
        if (!_isChecked)
        {
            _actionBarItem.setIcon(_uncheckedIcon);
        }
    }

    public String getCheckedIcon()
    {
        return _checkedIcon;
    }

    public void setCheckedIcon(String value)
    {
        _checkedIcon = value;
        if (_isChecked)
        {
            _actionBarItem.setIcon(_checkedIcon);
        }
    }

    public AndroidActionToggleWrapper(ControlWrapper parent, BindingContext bindingContext, JObject controlSpec)
    {
        super(parent, bindingContext);
        Log.d(TAG, String.format("Creating action bar toggle item with title of: %s",(controlSpec.get("text") != null) ? controlSpec.get("text").asString() : "(null)"));

        this._isVisualElement = false;

        _actionBarItem = _pageView.CreateAndAddActionBarItem();

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "value", Commands);
        ProcessCommands(bindingSpec, Commands);

        if (!processElementBoundValue(
                "value", bindingSpec.get("value").asString(), new IGetViewValue()
                {
                    @Override
                    public JToken GetViewValue()
                    {
                        return new JValue(getIsChecked());
                    }
                }, new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        setIsChecked(ToBoolean(value, false));
                    }
                }))
        {
            processElementProperty(
                    controlSpec.get("value"), new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                    {
                        @Override
                        protected void UiThreadSetViewValue(JToken value)
                        {
                            setIsChecked(ToBoolean(value, false));
                        }
                    });
        }

        processElementProperty(
                controlSpec.get("text"), new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        _actionBarItem.setTitle(ToString(value, ""));
                    }
                });
        processElementProperty(
                controlSpec.get("icon"), new AndroidUiThreadSetViewValue(
                        (Activity) ((AndroidControlWrapper) parent).getControl().getContext()
                )
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        _actionBarItem.setIcon(ToString(value, ""));
                    }
                }
                              );

        processElementProperty(
                controlSpec.get("uncheckedtext"), new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        setUncheckedText(ToString(value, ""));
                    }
                });
        processElementProperty(
                controlSpec.get("checkedtext"), new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        setCheckedText(ToString(value, ""));
                    }
                });
        processElementProperty(
                controlSpec.get("uncheckedicon"), new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        setUncheckedIcon(ToString(value, ""));
                    }
                });
        processElementProperty(
                controlSpec.get("checkedicon"), new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        setCheckedIcon(ToString(value, ""));
                    }
                });

        processElementProperty(
                controlSpec.get("enabled"), new AndroidUiThreadSetViewValue(
                        (Activity) ((AndroidControlWrapper) parent).getControl().getContext()
                )
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        _actionBarItem.setEnabled(ToBoolean(value, false));
                    }
                }
                              );

        _actionBarItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if (controlSpec.get("showAsAction") != null)
        {
            if (controlSpec.get("showAsAction").asString().equals("Always"))
            {
                _actionBarItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            else if (controlSpec.get("showAsAction").asString().equals("IfRoom"))
            {
                _actionBarItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        if (controlSpec.get("showActionAsText") != null)
        {
            if (ToBoolean(controlSpec.get("showActionAsText"), false))
            {
                _actionBarItem.setShowAsAction(_actionBarItem.getShowAsAction() | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
        }

        _actionBarItem.setOnItemSelected(
                new AndroidActionBarItem.IOnItemSelected()
                {
                    @Override
                    public void OnItemSelected()
                    {
                        setIsChecked(!getIsChecked());

                        updateValueBindingForAttribute("value");

                        CommandInstance command = GetCommand(CommandName.getOnToggle());
                        if (command != null)
                        {
                            getStateManager().sendCommandRequestAsync(
                                    command.getCommand(), command.GetResolvedParameters(getBindingContext()));
                        }
                    }
                });
    }
}
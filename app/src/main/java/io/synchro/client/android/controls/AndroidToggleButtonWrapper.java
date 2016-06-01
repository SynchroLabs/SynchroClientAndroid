package io.synchro.client.android.controls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
 * Created by bob on 5/31/16.
 */
public class AndroidToggleButtonWrapper extends AndroidControlWrapper {
    public static final String TAG = AndroidToggleButtonWrapper.class.getSimpleName();
    static String[] Commands = new String[]{CommandName.getOnToggle().getAttribute()};

    protected boolean _isChecked = false;

    public boolean getIsChecked() {
        return _isChecked;
    }

    public void setIsChecked(boolean value) {
        if (_isChecked != value)
        {
            _isChecked = value;
            updateVisualState();
        }
    }

    protected String _caption;
    protected String _checkedCaption;
    protected String _uncheckedCaption;

    protected String _icon;
    protected String _checkedIcon;
    protected String _uncheckedIcon;

    protected Integer _color;
    protected Integer _checkedColor;
    protected Integer _uncheckedColor;

    protected void setCaption(String caption)
    {
        SynchroButton button = (SynchroButton)_control;
        button.setText(caption);
    }

    protected void setIcon(String icon)
    {
        SynchroButton button = (SynchroButton)_control;
        button.setIcon(getIconDrawable(button.getContext(), getResourceNameFromIcon(icon)));
    }

    protected void setColor(Integer color)
    {
        SynchroButton button = (SynchroButton)_control;
        button.setTextColor(color);
    }

    protected void updateVisualState()
    {
        // If the user specified configuration that visually communicates the state change, then we will use only those
        // configuration elements to show the state change.  If they do not include any such elements, then we will gray
        // the toggle button out to show the unchecked state.
        //
        boolean isVisualStateExplicit = ((_checkedCaption != null) || (_checkedIcon != null) || (_checkedColor != null));

        if (getIsChecked())
        {
            if (isVisualStateExplicit)
            {
                // One or more of the explicit checked items will be set below...
                //
                if (_checkedCaption != null)
                {
                    setCaption(_checkedCaption);
                }
                if (_checkedIcon != null)
                {
                    setIcon(_checkedIcon);
                }
                if (_checkedColor != null)
                {
                    setColor(_checkedColor);
                }
            }
            else
            {
                // There was no explicit visual state specified, so we will use default color for checked
                //
                setColor(_color);
            }
        }
        else
        {
            if (isVisualStateExplicit)
            {
                // One or more of the explicit unchecked items will be set below...
                //
                if (_uncheckedCaption != null)
                {
                    setCaption(_uncheckedCaption);
                }
                if (_uncheckedIcon != null)
                {
                    setIcon(_uncheckedIcon);
                }
                if (_uncheckedColor != null)
                {
                    setColor(_uncheckedColor);
                }
            }
            else
            {
                // There was no explicit visual state specified, so we will use "gray" for unchecked
                //
                setColor(Color.GRAY);
            }
        }
    }

    public AndroidToggleButtonWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
    )
    {
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, String.format("Creating button element with caption of: %s", (controlSpec.get("caption") != null) ? controlSpec.get("caption").asString() : "(no caption)"));

        int btnStyle = android.R.attr.buttonStyle;
        if (ToBoolean(controlSpec.get("borderless"), true))
        {
            btnStyle = android.R.attr.borderlessButtonStyle;
        }
        final SynchroButton button = new SynchroButton(((AndroidControlWrapper)parent).getControl().getContext(), null, btnStyle);
        this._control = button;

        this._color = button.getCurrentTextColor();

        button.setCompoundDrawablePadding(20);

        applyFrameworkElementDefaults(button);

        processElementProperty(controlSpec, "caption", new AndroidUiThreadSetViewValue((Activity) button.getContext())
        {
            @Override
            public void UiThreadSetViewValue(JToken value)
            {
                _caption = ToString(value, "");
                setCaption(_caption);
                updateVisualState();
            }
        });

        processElementProperty(controlSpec, "checkedcaption", new AndroidUiThreadSetViewValue((Activity) button.getContext())
        {
            @Override
            public void UiThreadSetViewValue(JToken value)
            {
                _checkedCaption = ToString(value, "");
                updateVisualState();
            }
        });

        processElementProperty(controlSpec, "uncheckedcaption", new AndroidUiThreadSetViewValue((Activity) button.getContext())
        {
            @Override
            public void UiThreadSetViewValue(JToken value)
            {
                _uncheckedCaption = ToString(value, "");
                updateVisualState();
            }
        });

        processElementProperty(controlSpec, "icon", new AndroidUiThreadSetViewValue((Activity) button.getContext())
        {
            @Override
            public void UiThreadSetViewValue(JToken value)
            {
                _icon = ToString(value, "");
                setIcon(_icon);
                updateVisualState();
            }
        });

        processElementProperty(controlSpec, "checkedicon", new AndroidUiThreadSetViewValue((Activity) button.getContext())
        {
            @Override
            public void UiThreadSetViewValue(JToken value)
            {
                _checkedIcon = ToString(value, "");
                updateVisualState();
            }
        });

        processElementProperty(controlSpec, "uncheckedicon", new AndroidUiThreadSetViewValue((Activity) button.getContext())
        {
            @Override
            public void UiThreadSetViewValue(JToken value)
            {
                _uncheckedIcon = ToString(value, "");
                updateVisualState();
            }
        });

        processElementProperty(controlSpec, "color", new AndroidUiThreadSetViewValue((Activity) button.getContext())
        {
            @Override
            public void UiThreadSetViewValue(JToken value)
            {
                _color = ToColor(value, null);
                setColor(_color);
                updateVisualState();
            }
        });

        processElementProperty(controlSpec, "checkedcolor", new AndroidUiThreadSetViewValue((Activity) button.getContext())
        {
            @Override
            public void UiThreadSetViewValue(JToken value)
            {
                _checkedColor = ToColor(value, null);
                updateVisualState();
            }
        });

        processElementProperty(controlSpec, "uncheckedcolor", new AndroidUiThreadSetViewValue((Activity) button.getContext())
        {
            @Override
            public void UiThreadSetViewValue(JToken value)
            {
                _uncheckedColor = ToColor(value, null);
                updateVisualState();
            }
        });

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
                    controlSpec, "value", new AndroidUiThreadSetViewValue((Activity) ((AndroidControlWrapper)parent).getControl().getContext())
                    {
                        @Override
                        protected void UiThreadSetViewValue(JToken value)
                        {
                            setIsChecked(ToBoolean(value, false));
                        }
                    });
        }

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setIsChecked(!getIsChecked());

                updateValueBindingForAttribute("value");

                CommandInstance command = GetCommand(CommandName.getOnToggle());
                if (command != null)
                {
                    Log.d(TAG, String.format("Toggle Button click with command: %s", command.getCommand()));
                    AndroidToggleButtonWrapper.this.getStateManager().sendCommandRequestAsync(
                            command.getCommand(),
                            command.GetResolvedParameters(
                                    getBindingContext()
                            ));
                }
            }
        });
    }
}

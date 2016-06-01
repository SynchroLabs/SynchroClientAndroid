package io.synchro.client.android.controls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.reflect.Field;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.BindingHelper;
import io.synchro.client.android.CommandInstance;
import io.synchro.client.android.CommandName;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.R;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

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
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, String.format("Creating button element with caption of: %s", (controlSpec.get("caption") != null) ? controlSpec.get("caption").asString() : "(no caption)"));

        int btnStyle = android.R.attr.buttonStyle;
        if (ToBoolean(controlSpec.get("borderless"), false))
        {
            btnStyle = android.R.attr.borderlessButtonStyle;
        }
        final SynchroButton button = new SynchroButton(((AndroidControlWrapper)parent).getControl().getContext(), null, btnStyle);
        this._control = button;

        button.setCompoundDrawablePadding(20);

        applyFrameworkElementDefaults(button);

        processElementProperty(controlSpec, "caption", new AndroidUiThreadSetViewValue((Activity) button.getContext())
                               {
                                   @Override
                                   public void UiThreadSetViewValue(JToken value)
                                   {
                                       button.setText(ToString(value, ""));
                                   }
                               });

        processElementProperty(controlSpec, "icon", new AndroidUiThreadSetViewValue((Activity) button.getContext())
                                {
                                    @Override
                                    public void UiThreadSetViewValue(JToken value)
                                    {
                                        button.setIcon(getIconDrawable(button.getContext(), getResourceNameFromIcon(ToString(value, ""))));
                                    }
                                });

        JObject bindingSpec = BindingHelper
                .GetCanonicalBindingSpec(controlSpec, CommandName.getOnClick().getAttribute(), Commands);
        ProcessCommands(bindingSpec, Commands);

        // !!! We don't do anything to indicate the disabled state of the image button.  Test and see what, if anything,
        //     Android is doing for us (and whether we should attempt to gray it out ourselves).

        processElementProperty(
                controlSpec, "resource",
                new AndroidUiThreadSetViewValue((Activity) button.getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        final String img = ToString(value, "");
                        if (img.equals(""))
                        {
                            button.setBackground(null);
                        }
                        else
                        {
                            Picasso picasso = Picasso.with((Activity) button.getContext());
                            // picasso.setIndicatorsEnabled(true);
                            Log.d(TAG, String.format("Going to get image from %s", img));
                            picasso.load(img).into(
                                    new Target()
                                    {
                                        @Override
                                        public void onBitmapLoaded(
                                                Bitmap bitmap, Picasso.LoadedFrom from
                                                                  )
                                        {
                                            Log.d(TAG, String.format("Got image from %s, loadedfrom = %s", img, from));
                                            button.setBackground(new BitmapDrawable(bitmap));
                                        }

                                        @Override
                                        public void onBitmapFailed(Drawable errorDrawable)
                                        {
                                            Log.d(TAG, String.format("onBitmapFailed from %s", img));
                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable)
                                        {

                                        }
                                    });
                        }
                    }
                });

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
                                                  Log.d(TAG, String.format("Button click with command: %s", command.getCommand()));
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

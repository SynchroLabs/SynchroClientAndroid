package io.synchro.client.android.controls;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
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
        super(parent, bindingContext);
        Log.d(TAG, String.format("Creating button element with caption of: %s", (controlSpec.get("caption") != null) ? controlSpec.get("caption").asString() : "(no caption)"));
        final Button button = new Button(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = button;

        applyFrameworkElementDefaults(button);

        processElementProperty(controlSpec.get("caption"), new AndroidUiThreadSetViewValue((Activity) button.getContext())
                               {
                                   @Override
                                   public void UiThreadSetViewValue(JToken value)
                                   {
                                       button.setText(ToString(value, ""));
                                   }
                               });

        JObject bindingSpec = BindingHelper
                .GetCanonicalBindingSpec(controlSpec, CommandName.getOnClick().getAttribute(), Commands);
        ProcessCommands(bindingSpec, Commands);

        processElementProperty(
                controlSpec.get("resource"),
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

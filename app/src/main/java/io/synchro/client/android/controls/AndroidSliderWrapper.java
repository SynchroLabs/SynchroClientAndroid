package io.synchro.client.android.controls;

import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.BindingHelper;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.IGetViewValue;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JValue;

/**
 * Created by blake on 3/23/15.
 */
public class AndroidSliderWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidSliderWrapper.class.getSimpleName();

    // Since SeekBar only has a max range, we'll simulate the min/max
    //
    int _min = 0;
    int _max = 0;
    int _progress = 0;

    void upadateBar()
    {
        ProgressBar bar = (ProgressBar)this.getControl();
        bar.setMax(_max - _min);
        bar.setProgress(_progress - _min);
        Log.d(TAG, String.format("upadateBar: _min = %d, _max = %d, _progress = %d", _min, _max, _progress));
    }

    void setMin(double min)
    {
        _min = (int)min;
        upadateBar();
    }

    void setMax(double max)
    {
        _max = (int)max;
        upadateBar();
    }

    void setProgress(double progress)
    {
        _progress = (int)progress;
        upadateBar();
    }

    double getProgress()
    {
        ProgressBar bar = (ProgressBar)this.getControl();
        return bar.getProgress() + _min;
    }

    public AndroidSliderWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                               )
    {
        super(parent, bindingContext, controlSpec);
        ProgressBar bar = null;

        if (controlSpec.get("control").asString().equals("progressbar"))
        {
            Log.d(TAG, "Creating progress bar element");
            bar = new ProgressBar(((AndroidControlWrapper)parent).getControl().getContext(), null, android.R.attr.progressBarStyleHorizontal);
            bar.setIndeterminate(false);
        }
        else
        {
            Log.d(TAG, "Creating slider element");
            bar = new SeekBar(((AndroidControlWrapper)parent).getControl().getContext());
            ((SeekBar)bar).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                                                      {
                                                          @Override
                                                          public void onProgressChanged(
                                                                  SeekBar seekBar, int progress,
                                                                  boolean fromUser
                                                                                       )
                                                          {
                                                              updateValueBindingForAttribute("value");
                                                          }

                                                          @Override
                                                          public void onStartTrackingTouch(
                                                                  SeekBar seekBar
                                                                                          )
                                                          {
                                                          }

                                                          @Override
                                                          public void onStopTrackingTouch(
                                                                  SeekBar seekBar
                                                                                         )
                                                          {
                                                          }
                                                      });
        }

        this._control = bar;

        applyFrameworkElementDefaults(bar);

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "value", null);
        if (!processElementBoundValue(
                "value",
                (bindingSpec.get("value") != null) ? bindingSpec.get("value").asString() : null,
                new IGetViewValue()
                {
                    @Override
                    public JToken GetViewValue()
                    {
                        return new JValue(getProgress());
                    }
                }, new AndroidUiThreadSetViewValue((Activity) _control.getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        setProgress(ToDouble(value, 0.0));
                    }
                }
                                     ))
        {
            processElementProperty(controlSpec, "value", new AndroidUiThreadSetViewValue((Activity) _control.getContext())
                                   {
                                       @Override
                                       protected void UiThreadSetViewValue(JToken value)
                                       {
                                           setProgress(ToDouble(value, 0.0));
                                       }
                                   });
        }

        processElementProperty(controlSpec, "minimum", new AndroidUiThreadSetViewValue((Activity) _control.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       setMin(ToDouble(value, 0.0));
                                   }
                               });

        processElementProperty(controlSpec, "maximum", new AndroidUiThreadSetViewValue((Activity) _control.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       setMax(ToDouble(value, 0.0));
                                   }
                               });
    }
}

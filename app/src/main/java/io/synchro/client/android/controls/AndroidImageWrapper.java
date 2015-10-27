package io.synchro.client.android.controls;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.ControlWrapper;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JTokenType;

/**
 * Created by blake on 3/22/15.
 */
public class AndroidImageWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidImageWrapper.class.getSimpleName();

    protected URL _loadedImage = null;

    public AndroidImageWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                              )
    {
        super(parent, bindingContext);
        Log.d(TAG, "Creating image element");
        final ImageView image = new ImageView(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = image;

        applyFrameworkElementDefaults(image);

        // !!! Image scaling
        //
        // image.SetScaleType(ImageView.ScaleType.FitXy);        // Stretch to fill
        // image.SetScaleType(ImageView.ScaleType.CenterCrop);   // Fill preserving aspect
        // image.SetScaleType(ImageView.ScaleType.CenterInside); // Fit preserving aspect

        processElementProperty(controlSpec.get("resource"), new AndroidUiThreadSetViewValue((Activity) image.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       String img = ToString(value, "");
                                       if (img.equals(""))
                                       {
                                           image.setImageDrawable(null);
                                           _loadedImage = null;
                                       }
                                       else if ((_loadedImage != null) && (_loadedImage.toString().equals(value.asString())))
                                       {
                                           // NOOP
                                           //
                                           Log.d(TAG, "Image being loaded is same as current image, NOOP");
                                       }
                                       else
                                       {
                                           URL url = null;
                                           try
                                           {
                                               url = new URL(img);
                                           }
                                           catch (MalformedURLException e)
                                           {
                                               Log.wtf(TAG, e);
                                           }
                                           loadImageAsync(url);
                                       }
                                   }
                               });
    }

    private void loadImageAsync(URL url)
    {
        new AsyncTask<URL, Void, Void>()
        {
            @Override
            protected Void doInBackground(URL... params)
            {
                try
                {
                    Log.d(TAG, String.format("Getting image from %s", params[0]));
                    final Bitmap bmp = BitmapFactory.decodeStream(params[0].openConnection().getInputStream());

                    ((Activity) (AndroidImageWrapper.this._control.getContext())).runOnUiThread(new Runnable()
                                                                                                {
                                                                                                    @Override
                                                                                                    public void run()
                                                                                                    {
                                                                                                        ((ImageView)AndroidImageWrapper.this._control).setImageBitmap(bmp);
                                                                                                    }
                                                                                                });
                }
                catch (IOException e)
                {
                    Log.wtf(TAG, e);
                }

                return null;
            }
        }.execute(url);
    }
}

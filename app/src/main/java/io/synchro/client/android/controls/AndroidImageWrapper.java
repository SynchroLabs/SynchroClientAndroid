package io.synchro.client.android.controls;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

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

    private ScaleType ToImageScaleType(JToken value, ScaleType defaultType)
    {
        ScaleType scaleType = defaultType;

        String scaleTypeValue = ToString(value, "");

        switch (scaleTypeValue)
        {
            case "Stretch":
                scaleType = ScaleType.FIT_XY;
                break;
            case "Fit":
                scaleType = ScaleType.FIT_CENTER;
                break;
            case "Fill":
                scaleType = ScaleType.CENTER_CROP;
                break;
            default:
                break;
        }

        return scaleType;
    }

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

        processElementProperty(
                controlSpec.get("scale"), new AndroidUiThreadSetViewValue((Activity) image.getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        ScaleType scaleType = ToImageScaleType(value, ScaleType.FIT_CENTER);
                        Log.d(TAG, String.format("New image scale type is %s", scaleType));
                        image.setScaleType(scaleType);
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

                    // This is suppressing a warning that getContext has to be called from a UI thread.
                    // This function is being called inside an AsyncTask which is by definition on the
                    // UI thread.
                    //noinspection ResourceType
                    ((Activity) (AndroidImageWrapper.this._control.getContext())).runOnUiThread(new Runnable()
                                                                                                {
                                                                                                    @Override
                                                                                                    public void run()
                                                                                                    {
                                                                                                        ImageView imageView = (ImageView)AndroidImageWrapper.this._control;
                                                                                                        Bitmap finalBmp = bmp;

                                                                                                        // Fix up bitmap sizes based on scaling

                                                                                                        if (_heightSpecified && !_widthSpecified)
                                                                                                        {
                                                                                                            int newWidth = (int) (bmp.getWidth() / (double)bmp.getHeight() * imageView.getHeight());

                                                                                                            // Only height specified, set width based on image aspect
                                                                                                            //
                                                                                                            imageView.setMinimumWidth(newWidth);
                                                                                                            _width = newWidth;
                                                                                                            updateSize();
//                                                                                                            finalBmp = Bitmap.createScaledBitmap(bmp, newWidth, bmp.getHeight(), false);
                                                                                                        }
                                                                                                        else if (_widthSpecified && !_heightSpecified)
                                                                                                        {
                                                                                                            int newHeight = (int) (bmp.getHeight() / (double)bmp.getWidth() * imageView.getWidth());

                                                                                                            // Only width specified, set height based on image aspect
                                                                                                            //
                                                                                                            imageView.setMinimumHeight(newHeight);
                                                                                                            _height = newHeight;
                                                                                                            updateSize();
//                                                                                                            finalBmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), newHeight, false);
                                                                                                        }

                                                                                                        imageView.setImageBitmap(finalBmp);
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

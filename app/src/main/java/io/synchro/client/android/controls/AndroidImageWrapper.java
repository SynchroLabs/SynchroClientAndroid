package io.synchro.client.android.controls;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.BindingHelper;
import io.synchro.client.android.CommandInstance;
import io.synchro.client.android.CommandName;
import io.synchro.client.android.ControlWrapper;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

/**
 * Created by blake on 3/22/15.
 */
public class AndroidImageWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidImageWrapper.class.getSimpleName();
    static              String[] Commands = new String[]{CommandName.getOnTap().getAttribute()};

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
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, "Creating image element");
        final ImageView image = new ImageView(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = image;

        applyFrameworkElementDefaults(image);

        // !!! Image scaling
        //
        // image.SetScaleType(ImageView.ScaleType.FitXy);        // Stretch to fill
        // image.SetScaleType(ImageView.ScaleType.CenterCrop);   // Fill preserving aspect
        // image.SetScaleType(ImageView.ScaleType.CenterInside); // Fit preserving aspect

        processElementProperty(
                controlSpec, "resource",
                new AndroidUiThreadSetViewValue((Activity) image.getContext())
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
                        else if ((_loadedImage != null) && (_loadedImage.toString()
                                                                        .equals(value.asString())))
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
                            loadImageAsync(url, (Activity) image.getContext());
                        }
                    }
                }
                              );

        processElementProperty(
                controlSpec, "scale",
                new AndroidUiThreadSetViewValue((Activity) image.getContext())
                {
                    @Override
                    protected void UiThreadSetViewValue(JToken value)
                    {
                        ScaleType scaleType = ToImageScaleType(value, ScaleType.FIT_CENTER);
                        Log.d(TAG, String.format("New image scale type is %s", scaleType));
                        image.setScaleType(scaleType);
                    }
                }
                              );

        JObject bindingSpec = BindingHelper
                .GetCanonicalBindingSpec(
                        controlSpec, CommandName.getOnTap().getAttribute(), Commands
                                        );
        ProcessCommands(bindingSpec, Commands);

        if (GetCommand(CommandName.getOnTap()) != null)
        {
            image.setOnClickListener(new View.OnClickListener()
                                      {
                                          @Override
                                          public void onClick(View v)
                                          {
                                              CommandInstance command = GetCommand(CommandName.getOnTap());
                                              if (command != null)
                                              {
                                                  Log.d(TAG, String.format("Image tap with command: %s", command.getCommand()));
                                                  AndroidImageWrapper.this.getStateManager().sendCommandRequestAsync(
                                                          command.getCommand(),
                                                          command.GetResolvedParameters(
                                                                  getBindingContext()
                                                                                       ));
                                              }
                                          }
                                      });
        }
    }

    private static InputStream getUrlInputStreamFollowingRedirectsAcrossSchemes(URL url)
            throws IOException
    {
        Log.d(TAG, String.format("Opening connection to %s", url));
        URLConnection connection = url.openConnection();

        if (connection instanceof HttpURLConnection)
        {
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            Log.d(TAG, String.format("Initial URL is %s", httpURLConnection.getURL()));
            switch (httpURLConnection.getResponseCode())
            {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    String location = httpURLConnection.getHeaderField("Location");
                    Log.d(TAG, String.format("Resourced moved, Location is %s", location));

                    URL newUrl = new URL(url, location);
                    Log.d(TAG, String.format("New URL is %s", newUrl));

                    connection = newUrl.openConnection();
                    break;
            }
        }

        connection.connect();
        Log.d(TAG, String.format("After connect URL is %s", connection.getURL()));
        InputStream inputStream = connection.getInputStream();
        Log.d(TAG, String.format("After get input stream URL is %s", connection.getURL()));

        return inputStream;
    }

    private void loadImageAsync(final URL url, final Activity activity)
    {
        Picasso picasso = Picasso.with(activity);
        // picasso.setIndicatorsEnabled(true);
        Log.d(TAG, String.format("Going to get image from %s", url));
        picasso.load(url.toString()).into(
                (ImageView) AndroidImageWrapper.this._control, new Callback()
                {
                    @Override
                    public void onSuccess()
                    {
                        Log.d(TAG, String.format("Back with image from %s", url));
                        ImageView imageView = (ImageView) AndroidImageWrapper.this._control;

                        // Fix up bitmap sizes based on scaling

                        if (_heightSpecified && !_widthSpecified)
                        {
                            int newWidth = (int) (imageView.getDrawable()
                                                           .getIntrinsicWidth() / (double) imageView
                                    .getDrawable().getIntrinsicHeight() * imageView
                                    .getHeight());

                            // Only height specified, set width based on image aspect
                            //
                            imageView.setMinimumWidth(newWidth);
                            _width = newWidth;
                            updateSize();
                        }
                        else if (_widthSpecified && !_heightSpecified)
                        {
                            int newHeight = (int) (imageView.getDrawable()
                                                            .getIntrinsicHeight() / (double) imageView
                                    .getDrawable().getIntrinsicWidth() * imageView
                                    .getWidth());

                            // Only width specified, set height based on image aspect
                            //
                            imageView.setMinimumHeight(newHeight);
                            _height = newHeight;
                            updateSize();
                        }
                    }

                    @Override
                    public void onError()
                    {
                        Log.d(TAG, String.format("onError with image from %s", url));
                    }
                });
    }
}

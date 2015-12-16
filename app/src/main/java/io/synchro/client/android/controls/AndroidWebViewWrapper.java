package io.synchro.client.android.controls;

import android.app.Activity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.ControlWrapper;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

/**
 * Created by blake on 3/23/15.
 */
public class AndroidWebViewWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidWebViewWrapper.class.getSimpleName();

    public AndroidWebViewWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                                )
    {
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, "Creating web view button");
        final WebView webView = new WebView(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = webView;

        // http://developer.android.com/guide/webapps/webview.html
        webView.setWebViewClient(new WebViewClient());

        applyFrameworkElementDefaults(webView);

        // !!! TODO - Android Web View
        processElementProperty(controlSpec, "contents", new AndroidUiThreadSetViewValue((Activity) webView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       webView.loadData(
                                               ToString(value, ""), "text/html; charset=UTF-8", null
                                                       );
                                   }
                               });
        processElementProperty(controlSpec, "url", new AndroidUiThreadSetViewValue((Activity) webView.getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       webView.loadUrl(ToString(value, ""));
                                   }
                               });
    }
}

package io.synchro.client.android;

import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by blake on 12/8/14.
 */
public class TransportAndroidHttpClient
{
    public static final String TAG = TransportAndroidHttpClient.class.getSimpleName();

    private final URL _url;

    public TransportAndroidHttpClient(URL url)
    {
        _url = url;
    }

    public JObject getAppDefinition()
            throws IOException
    {
        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("AppDefinition"));
        requestObject.put("TransactionId", new JValue(0));

        Log.i(TAG, String.format("Connecting to %s", _url));
        HttpURLConnection connection = (HttpURLConnection) _url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setRequestProperty("User-Agent", "SynchroClientAndroid/1");
        connection.setDoOutput(true);

        Log.d(TAG, "Performing write");
        {
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            //noinspection TryFinallyCanBeTryWithResources
            try
            {
                String json = requestObject.toJson();

                Log.d(TAG, String.format("Request data is %s", json));
                writer.write(json);
            }
            finally
            {
                writer.close();
            }
        }

        Log.d(TAG, "Performing read");
        {
            PushbackReader reader = new PushbackReader(new InputStreamReader(connection.getInputStream()));
            //noinspection TryFinallyCanBeTryWithResources
            try
            {
                JObject returnedObject = (JObject) JsonParser.parseValue(reader);
                Log.d(TAG, String.format("Returned parsed object is %s", returnedObject.toJson()));
                Log.d(TAG, "Returning object");
                return (JObject) returnedObject.get("App");
            }
            finally
            {
                reader.close();
            }
        }
    }
}

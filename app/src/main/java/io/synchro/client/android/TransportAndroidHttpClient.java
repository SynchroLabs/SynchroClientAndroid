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
public class TransportAndroidHttpClient extends Transport
{
    public static final String TAG = TransportAndroidHttpClient.class.getSimpleName();
    private static final String SessionIdHeader = "synchro-api-session-id";

    private final URL _url;

    public TransportAndroidHttpClient(URL url)
    {
        _url = url;
    }

    @Override
    public JObject sendMessage(String sessionId, JObject request)
            throws IOException
    {
        Log.i(TAG, String.format("Connecting to %s", _url));
        HttpURLConnection connection = (HttpURLConnection) _url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        if (sessionId != null)
        {
            connection.setRequestProperty(SessionIdHeader, sessionId);
        }
//        connection.setRequestProperty("User-Agent", "SynchroClientAndroid/1");
        connection.setDoOutput(true);

        Log.d(TAG, "Performing write");
        {
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            //noinspection TryFinallyCanBeTryWithResources
            try
            {
                String json = request.toJson();

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
                return returnedObject;
            }
            finally
            {
                reader.close();
            }
        }
    }
}

package io.synchro.client.android;

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

        HttpURLConnection connection = (HttpURLConnection) _url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setRequestProperty("User-Agent", "SynchroClientAndroid/1");
        connection.setDoOutput(true);

        {
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            //noinspection TryFinallyCanBeTryWithResources
            try
            {
                writer.write(requestObject.toJson());
            }
            finally
            {
                writer.close();
            }
        }

        {
            PushbackReader reader = new PushbackReader(new InputStreamReader(connection.getInputStream()));
            //noinspection TryFinallyCanBeTryWithResources
            try
            {
                return (JObject) (((JObject) JsonParser.parseValue(reader)).get("App"));
            }
            finally
            {
                reader.close();
            }
        }
    }
}

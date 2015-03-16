package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;

/**
 * Created by blake on 12/8/14.
 */
public class TransportAndroidHttpClientTest extends TestCase
{
    // 10.0.2.2 magic explained here
    // http://stackoverflow.com/questions/5806220/how-to-connect-to-my-http-localhost-web-server-from-android-emulator-in-eclips

    private static final String testEndpoint = "http://10.0.2.2:1337/api/samples";

    public void testGetAppDefinition()
            throws IOException
    {
        TransportAndroidHttpClient transport = new TransportAndroidHttpClient(new URL(testEndpoint));
        JObject expectedObject = new JObject();

        expectedObject.put("name", new JValue("synchro-samples"));
        expectedObject.put("version", new JValue("0.0.0"));
        expectedObject.put("description", new JValue("Synchro API Samples"));
        expectedObject.put("main", new JValue("menu"));
        expectedObject.put("author", new JValue("Bob Dickinson <bob@synchro.io> (http://synchro.io/)"));
        expectedObject.put("private", new JValue(true));
        {
            JObject enginesObject = new JObject();
            enginesObject.put("synchro", new JValue("*"));
            expectedObject.put("engines", enginesObject);
        }

        assertEquals(expectedObject, transport.getAppDefinition());
    }
}

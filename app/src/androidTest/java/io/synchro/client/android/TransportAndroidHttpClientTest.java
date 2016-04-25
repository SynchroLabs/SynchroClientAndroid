package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;

import io.synchro.json.JObject;
import io.synchro.json.JValue;

/**
 * Created by blake on 12/8/14.
 */
public class TransportAndroidHttpClientTest extends TestCase
{
    // 10.0.2.2 magic explained here
    // http://stackoverflow.com/questions/5806220/how-to-connect-to-my-http-localhost-web-server-from-android-emulator-in-eclips

    private static final String testEndpoint = "http://10.0.2.2:1337/api/samples";

    public void testUrlFromEndpoint()
            throws IOException
    {
        assertEquals(new URL("http://api.synchro.io/api"), TransportAndroidHttpClient.UrlFromEndpoint("api.synchro.io/api"));
        assertEquals(new URL("http://api.synchro.io/api"), TransportAndroidHttpClient.UrlFromEndpoint("http://api.synchro.io/api"));
        assertEquals(new URL("https://api.synchro.io/api"), TransportAndroidHttpClient.UrlFromEndpoint("https://api.synchro.io/api"));
    }
}

package io.synchro.client.android;

import junit.framework.TestCase;

import java.net.URI;

/**
 * Created by blake on 12/8/14.
 */
public class TransportAndroidHttpClientTest extends TestCase
{
    private static final String testEndpoint = "http://localhost:1337/api/samples";

    public void testNothing()
    {
        TransportAndroidHttpClient transport = new TransportAndroidHttpClient(URI.create(testEndpoint));
    }
}

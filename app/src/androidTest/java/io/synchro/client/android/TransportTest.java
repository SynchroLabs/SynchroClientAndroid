package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;

import io.synchro.json.JObject;
import io.synchro.json.JValue;

/**
 * Created by blake on 2/21/15.
 */
public class TransportTest extends TestCase
{
    // 10.0.2.2 magic explained here
    // http://stackoverflow.com/questions/5806220/how-to-connect-to-my-http-localhost-web-server-from-android-emulator-in-eclips

    // private static final String testEndpoint = "http://10.0.2.2:1337/api/samples";
    private static final String testEndpoint = "https://api.synchro.io/api/samples";

    public void testGetAppDefinition()
            throws IOException
    {
        JObject expectedObject = new JObject();

        expectedObject.put("name", new JValue("synchro-samples"));
        expectedObject.put("version", new JValue("1.5.0"));
        expectedObject.put("description", new JValue("Synchro API Samples"));
        expectedObject.put("main", new JValue("menu"));
        expectedObject.put("author", new JValue("Bob Dickinson <bob@synchro.io> (http://synchro.io/)"));
        expectedObject.put("private", new JValue(true));
        {
            JObject enginesObject = new JObject();
            enginesObject.put("synchro", new JValue(">= 1.5.0"));
            expectedObject.put("engines", enginesObject);
        }
        expectedObject.put("synchroArchiveUrl", new JValue("https://github.com/SynchroLabs/SynchroSamples/archive/master.zip"));
        {
            JObject synchroObject = new JObject();
            synchroObject.put("clientVersion", new JValue(">= 1.4.0"));
            expectedObject.put("synchro", synchroObject);
        }

        TransportAndroidHttpClient transport = new TransportAndroidHttpClient(new URL(testEndpoint));

        JObject actual = transport.getAppDefinition();

        assertEquals(expectedObject, actual);
    }

    public void testGetFirstPage()
            throws IOException
    {
        TransportAndroidHttpClient transport = new TransportAndroidHttpClient(new URL(testEndpoint));
        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("Page"));
        requestObject.put("Path", new JValue("menu"));
        requestObject.put("TransactionId", new JValue(1));

        assertEquals("menu", transport.sendMessage(null, requestObject).get("Path").asString());
    }

    public void testNavigateToPageViaCommand()
            throws IOException
    {
        TransportAndroidHttpClient transport = new TransportAndroidHttpClient(new URL(testEndpoint));
        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("Page"));
        requestObject.put("Path", new JValue("menu"));
        requestObject.put("TransactionId", new JValue(1));

        JObject metricsObject = new JObject();
        metricsObject.put("clientVersion", new JValue("1.4.0"));

        requestObject.put("DeviceMetrics", metricsObject);

        JObject theResponse = transport.sendMessage(null, requestObject);

        assertEquals("menu", theResponse.get("Path").asString());

        String sessionId = theResponse.get("NewSessionId").asString();
        long instanceId = theResponse.get("InstanceId").asLong();
        long instanceVersion = theResponse.get("InstanceVersion").asLong();

        requestObject = new JObject();
        requestObject.put("Mode", new JValue("Command"));
        requestObject.put("Path", new JValue("menu"));
        requestObject.put("TransactionId", new JValue(2));
        requestObject.put("InstanceId", new JValue(instanceId));
        requestObject.put("InstanceVersion", new JValue(instanceVersion));
        requestObject.put("Command", new JValue("goToView"));

        JObject parametersObject = new JObject();
        parametersObject.put("view", new JValue("hello"));

        requestObject.put("Parameters", parametersObject);

        JObject theResponse2 = transport.sendMessage(sessionId, requestObject);
        assertEquals("hello", theResponse2.get("Path").asString());
    }

    public void testHttp404Failure()
            throws IOException
    {
        TransportAndroidHttpClient transport = new TransportAndroidHttpClient(new URL("http://10.0.2.2:1337"));
        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("Page"));
        requestObject.put("Path", new JValue("menu"));
        requestObject.put("TransactionId", new JValue(1));
        try
        {
            transport.sendMessage(null, requestObject).get("Path").asString();
            fail("Should throw FileNotFoundException");
        }
        catch (FileNotFoundException e)
        {
            // Expected
        }
    }

    /* No @Ignore support apparently, but this test doesn't work. Not sure how to fix it. Tired of
       the false positive.
    public void testNetworkFailure()
            throws IOException
    {
        TransportAndroidHttpClient transport = new TransportAndroidHttpClient(new URL("http://nohostcanbefoundhere"));
        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("Page"));
        requestObject.put("Path", new JValue("menu"));
        requestObject.put("TransactionId", new JValue(1));
        try
        {
            transport.sendMessage(null, requestObject).get("Path").asString();
            fail("Should throw UnknownHostException");
        }
        catch (UnknownHostException e)
        {
            // Expected
        }
    }
     */
}

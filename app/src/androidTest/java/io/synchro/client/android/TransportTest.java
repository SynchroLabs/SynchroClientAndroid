package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;

/**
 * Created by blake on 2/21/15.
 */
public class TransportTest extends TestCase
{
    // 10.0.2.2 magic explained here
    // http://stackoverflow.com/questions/5806220/how-to-connect-to-my-http-localhost-web-server-from-android-emulator-in-eclips

    private static final String testEndpoint = "http://10.0.2.2:1337/api/samples";

    public void testGetAppDefinition()
            throws IOException
    {
        JObject expected = new JObject();

        expected.put("name", new JValue("synchro-samples"));
        expected.put("version", new JValue("0.0.0"));
        expected.put("description", new JValue("Synchro API Samples"));
        expected.put("mainPage", new JValue("menu"));
        expected.put("author", new JValue("Bob Dickinson <bob@synchro.io> (http://synchro.io/)"));

        TransportAndroidHttpClient transport = new TransportAndroidHttpClient(new URL(testEndpoint));

        JObject actual = transport.getAppDefinition();

        assertEquals(expected, actual);
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

        JObject theResponse = transport.sendMessage(null, requestObject);

        assertEquals("menu", theResponse.get("Path").asString());

        String sessionId = theResponse.get("NewSessionId").asString();
        int instanceId = theResponse.get("InstanceId").asInt();
        int instanceVersion = theResponse.get("InstanceVersion").asInt();

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
}

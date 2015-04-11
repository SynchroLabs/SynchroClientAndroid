package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;

import io.synchro.json.JObject;
import io.synchro.json.JValue;

/**
 * Created by blake on 2/16/15.
 */
public class SynchroAppTest extends TestCase
{
    public static class TestAppManager extends SynchroAppManager
    {
        String _bundledState =
                "        {\n" +
                "          /*\n" +
                "          \"seed\":\n" +
                "          {\n" +
                "            \"endpoint\": \"api.synchro.io/api/samples\",\n" +
                "            \"definition\": { \"name\": \"synchro-samples\", \"description\": \"Synchro API Samples\" }\n" +
                "          }\n" +
                "          */\n" +
                "          \"apps\":\n" +
                "          [\n" +
                "            {\n" +
                "              \"endpoint\": \"api.synchro.io/api/samples\",\n" +
                "              \"definition\": { \"name\": \"synchro-samples\", \"description\": \"Synchro API Samples\" }\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "\n";
        String _localState;

        @Override
        protected String loadBundledState()
                throws IOException
        {
            return _bundledState;
        }

        @Override
        protected String loadLocalState()
                throws IOException
        {
            return _localState;
        }

        @Override
        protected boolean saveLocalState(String state)
                throws IOException
        {
            _localState = state;
            return true;
        }
    }

    public void testLoadBundledState()
            throws IOException
    {
        SynchroAppManager appManager = new TestAppManager();

        appManager.loadState();

        assertNull(appManager.getAppSeed());
        assertEquals(1, appManager.getApps().size());

        SynchroApp app = appManager.getApps().get(0);

        assertEquals("synchro-samples", app.getName());
        assertEquals("Synchro API Samples", app.getDescription());
        assertEquals("api.synchro.io/api/samples", app.getEndpoint());

        JObject expected = new JObject();
        expected.put("name", new JValue("synchro-samples"));
        expected.put("description", new JValue("Synchro API Samples"));

        assertEquals(expected, app.getAppDefinition());
    }
}

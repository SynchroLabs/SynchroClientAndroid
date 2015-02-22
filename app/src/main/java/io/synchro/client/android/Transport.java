package io.synchro.client.android;

import java.io.IOException;

/**
 * Created by blake on 2/21/15.
 */
public abstract class Transport
{
    public abstract JObject sendMessage(String sessionId, JObject request) throws IOException;

    public JObject getAppDefinition()
            throws IOException
    {
        JObject requestObject = new JObject();

        requestObject.put("Mode", new JValue("AppDefinition"));
        requestObject.put("TransactionId", new JValue(0));

        return (JObject) sendMessage(null, requestObject).get("App");
    }
}

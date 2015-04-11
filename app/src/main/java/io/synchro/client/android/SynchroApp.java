package io.synchro.client.android;

import io.synchro.json.JObject;

/**
 * Created by blake on 12/24/14.
 */
public class SynchroApp
{
    private String  endpoint;
    private JObject appDefinition;
    private String  sessionId;

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public JObject getAppDefinition()
    {
        return appDefinition;
    }

    public void setAppDefinition(JObject appDefinition)
    {
        this.appDefinition = appDefinition;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getName()
    {
        return appDefinition.get("name").asString();
    }

    public String getDescription()
    {
        return appDefinition.get("description").asString();
    }

    public SynchroApp(String endpoint, JObject appDefinition, String sessionId)
    {
        this.endpoint = endpoint;
        this.appDefinition = appDefinition; // !!! Should we use appDefinition.DeepClone();
        this.sessionId = sessionId;
    }
}

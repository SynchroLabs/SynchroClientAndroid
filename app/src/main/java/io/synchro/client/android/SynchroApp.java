package io.synchro.client.android;

/**
 * Created by blake on 12/24/14.
 */
public class SynchroApp
{
    private String endpoint;
    private String name;
    private String description;

    public String getEndpoint()
    {
        return endpoint;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public SynchroApp(JObject parsedJsonApp)
    {
        endpoint = parsedJsonApp.get("endpoint").asString();
        name = ((JObject) parsedJsonApp.get("definition")).get("name").asString();
        description = ((JObject) parsedJsonApp.get("definition")).get("description").asString();
    }
}

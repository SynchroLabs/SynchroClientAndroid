package io.synchro.client.android;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by blake on 12/15/14.
 */
public class JObject extends JToken
{
    private TreeMap<String, JToken> backingMap = new TreeMap<>();

    public JObject()
    {
        super(JTokenType.Object);
    }

    public void put(String key, JToken value)
    {
        backingMap.put(key, value);
    }

    public JToken get(String key)
    {
        return backingMap.get(key);
    }

    public Set<String> keySet()
    {
        return backingMap.keySet();
    }

    @Override
    public String asString()
    {
        return null;
    }

    @Override
    public int asInt()
    {
        return 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            if (obj instanceof JObject)
            {
                return backingMap.equals(((JObject) obj).backingMap);
            }
        }

        return false;
    }
}

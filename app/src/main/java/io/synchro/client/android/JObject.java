package io.synchro.client.android;

import java.util.Map;
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
        if (backingMap.containsKey(key))
        {
            JToken oldValue = backingMap.get(key);
            oldValue.setParent(null);
        }
        backingMap.put(key, value);
        value.setParent(this);
    }

    public JToken get(String key)
    {
        return backingMap.get(key);
    }

    public Set<String> keySet()
    {
        return backingMap.keySet();
    }

    public void replace(JToken oldToken, JToken newToken)
    {
        for (Map.Entry<String, JToken> entry : backingMap.entrySet())
        {
            if (entry.getValue() == oldToken)
            {
                put(entry.getKey(), newToken);
                break;
            }
        }
    }

    public boolean removeChild(JToken oldToken)
    {
        for (Map.Entry<String, JToken> entry : backingMap.entrySet())
        {
            if (entry.getValue() == oldToken)
            {
                backingMap.remove(entry.getKey());
                oldToken.setParent(null);
                return true;
            }
        }

        return false;
    }

    public String keyForValue(JToken value)
    {
        for (Map.Entry<String, JToken> entry : backingMap.entrySet())
        {
            if (entry.getValue() == value)
            {
                return entry.getKey();
            }
        }

        return null;
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
    public boolean asBoolean()
    {
        return false;
    }

    @Override
    public double asDouble()
    {
        return 0;
    }

    @Override
    public JToken deepClone()
    {
        JObject clone = new JObject();

        for (String key : keySet())
        {
            clone.put(key, get(key).deepClone());
        }

        return clone;
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

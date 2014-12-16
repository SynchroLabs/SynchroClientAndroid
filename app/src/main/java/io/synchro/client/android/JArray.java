package io.synchro.client.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by blake on 12/15/14.
 */
public class JArray extends JToken implements Iterable<JToken>
{
    private ArrayList<JToken> backingArray = new ArrayList<>();

    public JArray()
    {
        super(JTokenType.Array);
    }

    public JArray(JToken[] initializer)
    {
        this();

        Collections.addAll(backingArray, initializer);
    }

    public void add(JToken element)
    {
        backingArray.add(element);
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
    public Iterator<JToken> iterator()
    {
        return backingArray.iterator();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            if (obj instanceof JArray)
            {
                return backingArray.equals(((JArray) obj).backingArray);
            }
        }

        return false;
    }
}

package io.synchro.json;

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
        element.setParent(this);
    }

    public int size()
    {
        return backingArray.size();
    }

    public JToken get(int index) { return (backingArray.size() > index) ? backingArray.get(index) : null; }

    public JToken set(int index, JToken value)
    {
        return backingArray.set(index, value);
    }

    public int indexOf(JToken element) { return backingArray.indexOf(element); }

    public void replace(JToken oldToken, JToken newToken)
    {
        oldToken.setParent(null);
        backingArray.set(backingArray.indexOf(oldToken), newToken);
        newToken.setParent(this);
    }

    public boolean removeChild(JToken oldToken)
    {
        for (int counter = 0;counter < backingArray.size();++counter)
        {
            if (backingArray.get(counter) == oldToken)
            {
                oldToken.setParent(null);
                backingArray.remove(counter);
                return true;
            }
        }
        return false;
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
        JArray clone = new JArray();

        for (JToken element : backingArray)
        {
            clone.add(element.deepClone());
        }

        return clone;
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

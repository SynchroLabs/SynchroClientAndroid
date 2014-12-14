package io.synchro.client.android;

/**
 * Created by blake on 12/9/14.
 */
public class JValue extends JToken
{
    private Object value;

    public JValue(String value)
    {
        super(JTokenType.String);

        this.value = value;
    }

    public JValue(int value)
    {
        super(JTokenType.Integer);

        this.value = value;
    }

    public String asString()
    {
        return (type == JTokenType.String) ? ((String) value) : null;
    }

    public int asInt()
    {
        return (type == JTokenType.Integer) ? ((int) value) : null;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            if (obj instanceof JValue)
            {
                return value.equals(((JValue) obj).value);
            }
        }

        return false;
    }
}

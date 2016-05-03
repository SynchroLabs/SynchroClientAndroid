package io.synchro.json;

/**
 * Created by blake on 12/9/14.
 */
public class JValue extends JToken
{
    private Object value;

    public JValue()
    {
        super(JTokenType.Null);
    }

    public JValue(String value)
    {
        super(JTokenType.String);

        if (value == null)
        {
            type = JTokenType.Null;
        }

        this.value = value;
    }

    public JValue(long value)
    {
        super(JTokenType.Integer);

        this.value = value;
    }

    public JValue(boolean value)
    {
        super(JTokenType.Boolean);

        this.value = value;
    }

    public JValue(double value)
    {
        super(JTokenType.Float);

        this.value = value;
    }

    public JValue(JValue value)
    {
        super(value.type);

        this.value = value.value;
    }

    public String asString()
    {
        return (type == JTokenType.String) ? ((String) value) : null;
    }

    public long asLong()
    {
        return (type == JTokenType.Integer) ? ((long) value) : null;
    }

    public boolean asBoolean()
    {
        return (type == JTokenType.Boolean) ? ((boolean) value) : null;
    }

    public double asDouble()
    {
        if (type == JTokenType.Integer)
        {
            return (double) (long) value;
        }

        return (type == JTokenType.Float) ? ((double) value) : null;
    }

    public void copyFrom(JValue other)
    {
        type = other.type;
        value = other.value;
    }

    @Override
    public JToken deepClone()
    {
        return new JValue(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            if (obj instanceof JValue)
            {
                return (value == null) ? (((JValue) obj).value == null) : (value.equals(((JValue) obj).value));
            }
        }

        return false;
    }
}

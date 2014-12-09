package io.synchro.client.android;

/**
 * Created by blake on 12/9/14.
 */
public class JValue extends JToken
{
    private String stringValue;

    public JValue(String value)
    {
        super(JTokenType.String);

        stringValue = value;
    }

    public String asString()
    {
        return (type == JTokenType.String) ? stringValue : null;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            if (obj instanceof JValue)
            {
                switch (type)
                {
                    case String:
                        return stringValue.equals(((JValue) obj).stringValue);
                }
            }
        }

        return false;
    }
}

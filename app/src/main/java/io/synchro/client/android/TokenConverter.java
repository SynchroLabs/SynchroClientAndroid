package io.synchro.client.android;

import io.synchro.json.JArray;
import io.synchro.json.JToken;
import io.synchro.json.JTokenType;
import io.synchro.json.JValue;

/**
 * Created by blake on 1/19/15.
 */
public class TokenConverter
{
    public static String ToString(JToken token, String defaultValue)
    {
        String result = defaultValue;

        if (token != null)
        {
            switch (token.getType())
            {
                case Array:
                    JArray array = (JArray) token;
                    result = Integer.toString(array.size());
                    break;
                case String:
                    result = token.asString();
                    break;
                case Integer:
                    result = Integer.toString(token.asInt());
                    break;
                case Float:
                    result = Double.toString(token.asDouble());
                    break;
                case Boolean:
                    result = token.asBoolean() ? "true" : "false";
                    break;
                default:
                    result = token.toString();
                    break;
            }
        }

        return result;
    }

    public static boolean ToBoolean(JToken token, boolean defaultValue)
    {
        boolean result = defaultValue;

        if (token != null)
        {
            switch (token.getType())
            {
                case Boolean:
                    result = token.asBoolean();
                    break;
                case String:
                    String str = token.asString();
                    result = str.length() > 0;
                    break;
                case Float:
                    result = token.asDouble() != 0;
                    break;
                case Integer:
                    result = token.asInt() != 0;
                    break;
                case Array:
                    JArray array = (JArray) token;
                    result = array.size() > 0;
                    break;
                case Object:
                    result = true;
                    break;
            }
        }

        return result;
    }

    public static Double ToDouble(JToken value, Double defaultValue)
    {
        Double result = defaultValue;

        if (value != null)
        {
            if (value instanceof JValue)
            {
                JValue jValue = (JValue) value;
                if (jValue.getType() == JTokenType.String)
                {
                    try
                    {
                        result = Double.parseDouble(value.asString());
                    }
                    catch (NumberFormatException e)
                    {
                        // Not formatted as a number, no biggie...
                    }
                }
                else
                {
                    result = jValue.asDouble();
                }
            }
            else if (value instanceof JArray)
            {
                return (double) ((JArray)value).size();
            }
        }

        return result;
    }
}

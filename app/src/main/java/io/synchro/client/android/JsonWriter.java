package io.synchro.client.android;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

/**
 * Created by blake on 12/9/14.
 */
public class JsonWriter
{
    private static HashMap<Character, String> charSubstitutions = new HashMap<>();

    static
    {
        charSubstitutions.put('\\', "\\");
        charSubstitutions.put('/', "\\/");
        charSubstitutions.put('"', "\\\"");
        charSubstitutions.put('\b', "\\b");
        charSubstitutions.put('\f', "\\f");
        charSubstitutions.put('\n', "\\n");
        charSubstitutions.put('\r', "\\r");
        charSubstitutions.put('\t', "\\t");
    }

    private static void writeString(Writer writer, String _string)
            throws IOException
    {
        writer.write('\"');
        for (char _char : _string.toCharArray())
        {
            if (charSubstitutions.containsKey(_char))
            {
                writer.write(charSubstitutions.get(_char));
            }
            else if ((_char < ' ') || (_char > '\u007E'))
            {
                writer.write(String.format("\\u%04X", (int) _char));
            }
            else
            {
                writer.write(_char);
            }
        }
        writer.write('\"');
    }

    private static void writeNumber(Writer writer, int i)
            throws IOException
    {
        writer.write(Integer.toString(i));
    }

    private static void writeNumber(Writer writer, double d)
            throws IOException
    {
        writer.write(Double.toString(d));
    }

    private static void writeArray(Writer writer, JArray array)
            throws IOException
    {
        boolean firstElement = true;

        writer.write('[');
        for (JToken value : array)
        {
            if (!firstElement)
            {
                writer.write(',');
            }
            else
            {
                firstElement = false;
            }

            writeValue(writer, value);
        }
        writer.write(']');
    }

    private static void writeObject(Writer writer, JObject _object)
            throws IOException
    {
        boolean firstKey = true;

        writer.write('{');

        for (String key : _object.keySet())
        {
            JToken value = _object.get(key);

            if (!firstKey)
            {
                writer.write(',');
            }
            else
            {
                firstKey = false;
            }

            writeString(writer, key);

            writer.write(':');

            writeValue(writer, value);
        }

        writer.write('}');
    }

    public static void writeBoolean(Writer writer, boolean b)
            throws IOException
    {
        writer.write(b ? "true" : "false");
    }

    public static void writeNull(Writer writer)
            throws IOException
    {
        writer.write("null");
    }

    public static void writeValue(Writer writer, JToken value)
            throws IOException
    {
        switch (value.type)
        {
            case Integer:
                writeNumber(writer, value.asInt());
                break;

            case Float:
                writeNumber(writer, value.asDouble());
                break;

            case Array:
                writeArray(writer, (JArray) value);
                break;

            case Object:
                writeObject(writer, (JObject) value);
                break;

            case Boolean:
                writeBoolean(writer, value.asBoolean());
                break;

            case Null:
                writeNull(writer);
                break;

            default:
//            case String:
                writeString(writer, value.asString());
                break;
        }
    }
}

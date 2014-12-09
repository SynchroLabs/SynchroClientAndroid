package io.synchro.client.android;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by blake on 12/9/14.
 */
public class JsonWriter
{
    public static void writeValue(Writer writer, JToken value)
            throws IOException
    {
        writer.write('"');
        writer.write(value.asString());
        writer.write('"');
    }
}

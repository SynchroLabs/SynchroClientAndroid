package io.synchro.client.android;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by blake on 12/9/14.
 */
public abstract class JToken
{
    protected final JTokenType type;

    public JToken(JTokenType type)
    {
        this.type = type;
    }

    public static JToken parse(String json)
            throws IOException
    {
        return JsonParser.parseValue(new PushbackReader(new StringReader(json)));
    }

    public abstract String asString();
    public abstract int asInt();

    public String toJson()
            throws IOException
    {
        StringWriter writer = new StringWriter();

        JsonWriter.writeValue(writer, this);
        return writer.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof JToken && type == ((JToken) obj).type;
    }
}

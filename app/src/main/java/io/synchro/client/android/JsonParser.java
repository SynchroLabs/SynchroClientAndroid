package io.synchro.client.android;

import java.io.IOException;
import java.io.PushbackReader;

/**
 * Created by blake on 12/9/14.
 */
public class JsonParser
{
    static String parseString(PushbackReader reader)
            throws IOException
    {
        StringBuilder builder = new StringBuilder();
        int currentChar;

        // Skip the opening quotes

        reader.read();

        // Read until closing quotes

        while ((currentChar = reader.read()) != '"')
        {
            builder.append((char) currentChar);
        }

        // Already skipped the closing quotes

        return builder.toString();
    }

    static JToken parseValue(PushbackReader reader)
            throws IOException
    {
        int lookahead = reader.read();
        reader.unread(lookahead);

        return new JValue(parseString(reader));
    }
}

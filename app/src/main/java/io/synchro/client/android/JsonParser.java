package io.synchro.client.android;

import java.io.IOException;
import java.io.PushbackReader;

/**
 * Created by blake on 12/9/14.
 */
public class JsonParser
{
    private static void skipWhitespace(PushbackReader reader)
            throws IOException
    {
        char thisChar;

        while (Character.isSpaceChar(thisChar = (char) reader.read()))
        {
        }

        if (thisChar != -1)
        {
            reader.unread(thisChar);
        }
    }

    static String parseString(PushbackReader reader)
            throws IOException
    {
        int thisChar;
        StringBuilder returnString = new StringBuilder();

        skipWhitespace(reader);

        // Skip the opening quotes

        reader.read();

        // Read until closing quotes

        while ((thisChar = reader.read()) != '"')
        {
            if (thisChar == -1)
            {
                throw new IOException("Unexpected end of stream");
            }

            if (thisChar == '\\')
            {
                thisChar = reader.read();

                switch (thisChar)
                {
                    case 'b':
                        thisChar = '\b';
                        break;
                    case 'f':
                        thisChar = '\f';
                        break;
                    case 'r':
                        thisChar = '\r';
                        break;
                    case 'n':
                        thisChar = '\n';
                        break;
                    case 't':
                        thisChar = '\t';
                        break;
                    case 'u':
                        // Parse four hex digits
                        StringBuilder hexBuilder = new StringBuilder(4);
                        for (int counter = 0; counter < 4; ++counter)
                        {
                            hexBuilder.append((char)reader.read());
                        }
                        thisChar = Integer.parseInt(hexBuilder.toString(), 16);
                        break;
                    case '\\':
                    case '"':
                    case '/':
                    default:
                        break;
                }
            }

            returnString.append((char) thisChar);
        }

        return returnString.toString();
    }

    static JToken parseValue(PushbackReader reader)
            throws IOException
    {
        int lookahead = reader.read();
        reader.unread(lookahead);

        return new JValue(parseString(reader));
    }
}

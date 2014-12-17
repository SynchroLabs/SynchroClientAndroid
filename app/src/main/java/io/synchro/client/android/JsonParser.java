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

    static JValue parseString(PushbackReader reader)
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

        return new JValue(returnString.toString());
    }

    private static JValue parseNumber(PushbackReader reader)
            throws IOException
    {
        StringBuilder numberBuilder = new StringBuilder();
        int thisChar;

        skipWhitespace(reader);

        while (true)
        {
            thisChar = reader.read();

            if ("0123456789Ee.-+".indexOf(thisChar) >= 0)
            {
                numberBuilder.append((char) thisChar);
                continue;
            }
            else if (thisChar != -1)
            {
                reader.unread(thisChar);
            }

            break;
        }

        String numberData = numberBuilder.toString();

        if (numberData.matches(".*[eE.].*"))
        {
            return new JValue(Double.parseDouble(numberData)); //, CultureInfo.InvariantCulture.NumberFormat);
        }
        else
        {
            return new JValue(Integer.parseInt(numberData));
        }
    }

    private static JArray parseArray(PushbackReader reader)
            throws IOException
    {
        JArray finalList = new JArray();
        int thisChar;

        skipWhitespace(reader);

        // Skip the opening bracket

        reader.read();

        skipWhitespace(reader);

        // Read until closing bracket

        while ((thisChar = reader.read()) != ']')
        {
            reader.unread(thisChar);

            // Read a value

            finalList.add(parseValue(reader));

            skipWhitespace(reader);

            // Skip the comma if any

            if ((thisChar = reader.read()) != ',')
            {
                reader.unread(thisChar);
            }

            skipWhitespace(reader);
        }

        return finalList;
    }

    private static JObject parseObject(PushbackReader reader)
            throws IOException
    {
        JObject finalObject = new JObject();
        int thisChar;

        skipWhitespace(reader);

        // Skip the opening brace

        reader.read();

        skipWhitespace(reader);

        // Read until closing brace

        while ((thisChar = reader.read()) != '}')
        {
            JToken name;
            JToken value;

            reader.unread(thisChar);

            // Read a string

            name = parseString(reader);

            skipWhitespace(reader);

            // Skip the colon

            reader.read();

            skipWhitespace(reader);

            // Read the value

            value = parseValue(reader);

            skipWhitespace(reader);

            finalObject.put(name.asString(), value);

            // Skip the comma if any

            if ((thisChar = reader.read()) != ',')
            {
                reader.unread(thisChar);
            }

            skipWhitespace(reader);
        }

        return finalObject;
    }

    private static JToken parseTrue(PushbackReader reader)
            throws IOException
    {
        // Skip 't', 'r', 'u', 'e'

        reader.read();
        reader.read();
        reader.read();
        reader.read();

        return new JValue(true);
    }

    private static JToken parseFalse(PushbackReader reader)
            throws IOException
    {
        // Skip 'f', 'a', 'l', 's', 'e'

        reader.read();
        reader.read();
        reader.read();
        reader.read();
        reader.read();

        return new JValue(false);
    }

    private static JToken parseNull(PushbackReader reader)
            throws IOException
    {
        // Skip 'n', 'u', 'l', 'l'

        reader.read();
        reader.read();
        reader.read();
        reader.read();

        return new JValue();
    }

    static JToken parseValue(PushbackReader reader)
            throws IOException
    {
        int lookahead = reader.read();
        reader.unread(lookahead);

        if ((lookahead == '-') || ((lookahead >= '0') && (lookahead <= '9')))
        {
            return parseNumber(reader);
        }
        else if (lookahead == '[')
        {
            return parseArray(reader);
        }
        else if (lookahead == '{')
        {
            return parseObject(reader);
        }
        else if (lookahead == 't')
        {
            return parseTrue(reader);
        }
        else if (lookahead == 'f')
        {
            return parseFalse(reader);
        }
        else if (lookahead == 'n')
        {
            return parseNull(reader);
        }
        else
        {
            return parseString(reader);
        }
    }
}

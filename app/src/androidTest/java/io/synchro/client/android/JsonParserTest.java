package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Created by blake on 12/9/14.
 */
public class JsonParserTest extends TestCase
{
    private void validateRoundTrip(String jsonInput, JValue expected)
    {
        JToken token = null;
        String jsonOutput = null;

        try
        {
            token = JToken.parse(jsonInput);
            jsonOutput = token.toJson();
        }
        catch (IOException e)
        {
            assertTrue("Unexpected exception", true);
        }

        assertEquals(jsonInput, jsonOutput);
        assertEquals(expected, token);
    }

    public void testParseString()
    {
        validateRoundTrip("\"abc\"", new JValue("abc"));
    }

    public void testParseStringEscapes()
    {
        validateRoundTrip("\"\\\"\\\\/\\b\\f\\n\\r\\t\\u20AC\\u007F\"", new JValue("\"\\/\b\f\n\r\t\u20AC\u007F"));
    }

    public void testParseInteger()
    {
        validateRoundTrip("0", new JValue(0));
        validateRoundTrip(String.format("%d", Integer.MAX_VALUE), new JValue(Integer.MAX_VALUE));
        validateRoundTrip(String.format("%d", Integer.MIN_VALUE), new JValue(Integer.MIN_VALUE));
    }
}

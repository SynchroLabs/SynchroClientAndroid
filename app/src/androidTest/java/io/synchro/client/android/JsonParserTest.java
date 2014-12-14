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
}

package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Created by blake on 12/9/14.
 */
public class JsonParserTest extends TestCase
{
    private void validateRoundTrip(String jsonInput, JToken expected)
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
            assertTrue("Unexpected exception", false);
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

    public void testParseArray()
    {
        validateRoundTrip("[]", new JArray());
        validateRoundTrip("[0]", new JArray(new JToken[] { new JValue(0) }));
        validateRoundTrip("[\"abc\"]", new JArray(new JToken[] { new JValue("abc") }));
        validateRoundTrip("[0,\"abc\"]", new JArray(new JToken[] { new JValue(0), new JValue("abc") }));
        validateRoundTrip("[0,\"abc\",[1,\"def\"]]", new JArray(new JToken[] { new JValue(0), new JValue("abc"), new JArray(new JToken[] { new JValue(1), new JValue("def") }) }));
    }

    public void testParseObject()
    {
        validateRoundTrip("{}", new JObject());
        JObject expectedObject = new JObject();

        expectedObject.put("foo", new JValue(0));
        expectedObject.put("bar", new JValue("kitty"));
        expectedObject.put("baz", new JArray(new JToken[] { new JValue(8), new JValue("dog") }));

        validateRoundTrip("{\"bar\":\"kitty\",\"baz\":[8,\"dog\"],\"foo\":0}", expectedObject);
    }

    public void testParseObjectWithWhitespace()
            throws IOException
    {
        String json = "  {  \"foo\"  :  0  ,  \"bar\"  :  \"kitty\"  ,  \"baz\"  :  [8  ,  \"dog\"  ]  }  ";
        JToken actual = JToken.parse(json);
        JObject expectedObject = new JObject();

        expectedObject.put("foo", new JValue(0));
        expectedObject.put("bar", new JValue("kitty"));
        expectedObject.put("baz", new JArray(new JToken[] { new JValue(8), new JValue("dog") }));

        assertEquals(expectedObject, actual);
    }

    public void testParseObjectWithNewlines()
        throws IOException
    {
        String json = "  {  \"foo\"  :  0\n  ,  \"bar\"\r\n  :  \"kitty\"  ,  \"baz\"  :  [8  ,  \"dog\"  ]  }  ";
        JToken actual = JToken.parse(json);
        JObject expectedObject = new JObject();

        expectedObject.put("foo", new JValue(0));
        expectedObject.put("bar", new JValue("kitty"));
        expectedObject.put("baz", new JArray(new JToken[] { new JValue(8), new JValue("dog") }));

        assertEquals(expectedObject, actual);
    }

    public void testComments()
            throws IOException
    {
        String json =
                "// This is a comment\n" +
                "{\n" +
                "// The foo element is my favorite\n" +
                "\"foo\"  :  0,\n" +
                "\"bar\"  :  \"kitty\",\n" +
                "// The baz element, he's OK also\n" +
                "\"baz\"  :  [  8  ,  \"dog\"  ]\n" +
                "}\n" +
                "\n";
        JToken actual = JToken.parse(json);
        JObject expectedObject = new JObject();

        expectedObject.put("foo", new JValue(0));
        expectedObject.put("bar", new JValue("kitty"));
        expectedObject.put("baz", new JArray(new JToken[] { new JValue(8), new JValue("dog") }));

        assertEquals(expectedObject, actual);
    }

    public void testParseBoolean()
    {
        validateRoundTrip("true", new JValue(true));
        validateRoundTrip("false", new JValue(false));
    }

    public void testParseNull()
    {
        validateRoundTrip("null", new JValue());
    }

    public void  testParseDouble()
    {
        validateRoundTrip("0.001", new JValue(0.001));
        validateRoundTrip("6.02E23", new JValue(6.02E23));
    }
}

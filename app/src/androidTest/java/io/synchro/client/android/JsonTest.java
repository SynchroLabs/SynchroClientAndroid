package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Created by blake on 1/25/15.
 */
public class JsonTest extends TestCase
{
    public void testPath()
            throws IOException
    {
        JObject stuff = (JObject) JToken.parse("{\"a\",{\"b\",{\"c\",\"d\"}},\"e\",[{\"f\",\"g\"},\"h\"]}");

        assertSame((((JObject)((JArray)stuff.get("e")).get(0)).get("f")), stuff.selectToken(
                           "e[0].f"
                                                                                           ));
    }

    public void testDeepClone()
            throws IOException
    {
        JObject stuff = (JObject) JToken
                .parse("{\"a\",{\"b\",{\"c\",\"d\"}},\"e\",[{\"f\",\"g\"},\"h\"]}");
        JObject duplicateStuff = (JObject) JToken
                .parse("{\"a\",{\"b\",{\"c\",\"d\"}},\"e\",[{\"f\",\"g\"},\"h\"]}");

        JObject cloneStuff = (JObject) stuff.deepClone();

        assertEquals(stuff, duplicateStuff);
        assertEquals(stuff, cloneStuff);

        stuff.put("foo", new JValue("bar"));

        assertFalse(stuff.equals(duplicateStuff));
        assertFalse(stuff.equals(cloneStuff));

        duplicateStuff.put("foo", new JValue("bar"));

        assertEquals(stuff, duplicateStuff);
        assertFalse(stuff.equals(cloneStuff));
    }

    public void testUpdate()
    {
        JObject stuff = new JObject();

        stuff.put("a", new JValue(1));
        stuff.put("b", new JValue(2));

        JToken vmItemValue = stuff.selectToken("a");
        JObject newVmItemValue = new JObject();
        newVmItemValue.put("baz", new JValue("Fraz"));

        boolean rebindRequired = (JToken.updateTokenValue(vmItemValue, newVmItemValue) != null);

        JObject expected = new JObject();
        JObject expectedA = new JObject();
        expectedA.put("baz", new JValue("Fraz"));
        expected.put("a", expectedA);
        expected.put("b", new JValue(2));

        assertTrue(rebindRequired);
        assertEquals(expected, stuff);
    }
}

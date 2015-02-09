package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Created by blake on 2/2/15.
 */
public class PropertyValueTest extends TestCase
{
    public void testPropertyValue()
            throws IOException
    {
        JObject viewModel = (JObject) JObject.parse(
                "{" +
                        "\"serial\" : 0," +
                        "\"title\" : \"Colors\"," +
                        "\"colors\" :" +
                        "[" +
                        "{" +
                        "\"name\" : \"Red\"," +
                        "\"color\" : \"red\"," +
                        "\"value\" : \"0xff0000\"" +
                        "}," +
                        "{" +
                        "\"name\" : \"Green\"," +
                        "\"color\" : \"green\"," +
                        "\"value\" : \"0x00ff00\"" +
                        "}," +
                        "{" +
                        "\"name\" : \"Blue\"," +
                        "\"color\" : \"blue\"," +
                        "\"value\" : \"0x0000ff\"" +
                        "}," +
                        "]" +
                        "}");

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The {title} are {colors[0].name}, {colors[1].name}, and {colors[2].name}", bindingCtx);

        assertEquals("The Colors are Red, Green, and Blue", propVal.Expand().asString());
    }

    public void testPropertyValueModelUpdate()
            throws IOException
    {
        JObject viewModel = (JObject) JObject.parse(
                "{" +
                        "\"serial\" : 0," +
                        "\"title\" : \"Colors\"," +
                        "\"colors\" :" +
                        "[" +
                        "{" +
                        "\"name\" : \"Red\"," +
                        "\"color\" : \"red\"," +
                        "\"value\" : \"0xff0000\"" +
                        "}," +
                        "{" +
                        "\"name\" : \"Green\"," +
                        "\"color\" : \"green\"," +
                        "\"value\" : \"0x00ff00\"" +
                        "}," +
                        "{" +
                        "\"name\" : \"Blue\"," +
                        "\"color\" : \"blue\"," +
                        "\"value\" : \"0x0000ff\"" +
                        "}," +
                        "]" +
                        "}");

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The {title} are {colors[0].name}, {colors[1].name}, and {colors[2].name}", bindingCtx);

        assertEquals("The Colors are Red, Green, and Blue", propVal.Expand().asString());

        JObject newColor = new JObject();
        newColor.put("name", new JValue("Greenish"));
        newColor.put("color", new JValue("green"));
        newColor.put("value", new JValue("0x00ff00"));
        ((JArray)viewModel.get("colors")).set(1, newColor);
        for (BindingContext bindingContext : propVal.getBindingContexts())
        {
            bindingContext.Rebind();
        }

        assertEquals("The Colors are Red, Greenish, and Blue", propVal.Expand().asString());
    }

    public void testPropertyValueModelUpdateOneTimeToken()
            throws IOException
    {
        JObject viewModel = (JObject) JObject.parse(
                "{" +
                        "\"serial\" : 0," +
                        "\"title\" : \"Colors\"," +
                        "\"colors\" :" +
                        "[" +
                        "{" +
                        "\"name\" : \"Red\"," +
                        "\"color\" : \"red\"," +
                        "\"value\" : \"0xff0000\"" +
                        "}," +
                        "{" +
                        "\"name\" : \"Green\"," +
                        "\"color\" : \"green\"," +
                        "\"value\" : \"0x00ff00\"" +
                        "}," +
                        "{" +
                        "\"name\" : \"Blue\"," +
                        "\"color\" : \"blue\"," +
                        "\"value\" : \"0x0000ff\"" +
                        "}," +
                        "]" +
                        "}");

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The {title} are {colors[0].name}, {colors[1].name}, and {^colors[2].name}", bindingCtx);

        assertEquals("The Colors are Red, Green, and Blue", propVal.Expand().asString());

        JObject newColor = new JObject();
        newColor.put("name", new JValue("Greenish"));
        newColor.put("color", new JValue("green"));
        newColor.put("value", new JValue("0x00ff00"));
        ((JArray)viewModel.get("colors")).set(1, newColor);

        newColor = new JObject();
        newColor.put("name", new JValue("Blueish"));
        newColor.put("color", new JValue("blue"));
        newColor.put("value", new JValue("0x0000ff"));
        ((JArray)viewModel.get("colors")).set(2, newColor);

        for (BindingContext bindingContext : propVal.getBindingContexts())
        {
            bindingContext.Rebind();
        }

        assertEquals("The Colors are Red, Greenish, and Blue", propVal.Expand().asString());
    }

    public void testPropertyValueIntToken()
    {
        JObject viewModel = new JObject();
        viewModel.put("serial", new JValue(420));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("{serial}", bindingCtx);
        JToken expandedPropValToken = propVal.Expand();

        assertEquals(JTokenType.Integer, expandedPropValToken.getType());
        assertEquals(420, expandedPropValToken.asInt());
    }

    public void testPropertyValueFloatToken()
    {
        JObject viewModel = new JObject();
        viewModel.put("serial", new JValue(13.69));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("{serial}", bindingCtx);
        JToken expandedPropValToken = propVal.Expand();

        assertEquals(JTokenType.Float, expandedPropValToken.getType());
        assertEquals(13.69, expandedPropValToken.asDouble());
    }

    public void testPropertyValueBoolToken()
    {
        JObject viewModel = new JObject();
        viewModel.put("serial", new JValue(true));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("{serial}", bindingCtx);
        JToken expandedPropValToken = propVal.Expand();

        assertEquals(JTokenType.Boolean, expandedPropValToken.getType());
        assertEquals(true, expandedPropValToken.asBoolean());
    }

    public void testPropertyValueBoolTokenNegated()
    {
        JObject viewModel = new JObject();
        viewModel.put("serial", new JValue(true));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("{!serial}", bindingCtx);
        JToken expandedPropValToken = propVal.Expand();

        assertEquals(JTokenType.Boolean, expandedPropValToken.getType());
        assertEquals(false, expandedPropValToken.asBoolean());
    }

    public void testPropertyValueStringToken()
    {
        JObject viewModel = new JObject();
        viewModel.put("serial", new JValue("foo"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("{serial}", bindingCtx);
        JToken expandedPropValToken = propVal.Expand();

        assertEquals(JTokenType.String, expandedPropValToken.getType());
        assertEquals("foo", expandedPropValToken.asString());
    }

    public void testPropertyValueStringTokenNegated()
    {
        JObject viewModel = new JObject();
        viewModel.put("serial", new JValue("foo"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("{!serial}", bindingCtx);
        JToken expandedPropValToken = propVal.Expand();

        // When we negate a string, the type is coerced (converted) to bool, then inverted...
        assertEquals(JTokenType.Boolean, expandedPropValToken.getType());
        assertEquals(false, expandedPropValToken.asBoolean());
    }

    public void testEscapedCurlyBrackets()
    {
        JObject viewModel = new JObject();
        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("This is how you indicate a token: {{serial}}", bindingCtx);
        assertEquals("This is how you indicate a token: {serial}", propVal.Expand().asString());

        propVal = new PropertyValue("Open {{ only", bindingCtx);
        assertEquals("Open { only", propVal.Expand().asString());

        propVal = new PropertyValue("Close }} only", bindingCtx);
        assertEquals("Close } only", propVal.Expand().asString());

        propVal = new PropertyValue("{{{{Double}}}}", bindingCtx);
        assertEquals("{{Double}}", propVal.Expand().asString());
    }

    public void testContainsBindingToken()
    {
        assertFalse(PropertyValue.ContainsBindingTokens(""));
        assertFalse(PropertyValue.ContainsBindingTokens("{{foo}}"));
        assertFalse(PropertyValue.ContainsBindingTokens("Foo {{bar}} baz"));
        assertTrue(PropertyValue.ContainsBindingTokens("{bar}"));
        assertTrue(PropertyValue.ContainsBindingTokens("Foo {bar} baz"));
        assertTrue(PropertyValue.ContainsBindingTokens("Foo {bar} {baz}"));
    }

    public void testNumericFormattingIntNoSpec()
    {
        JObject viewModel = new JObject();
        viewModel.put("serial", new JValue(69));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The number is: {serial}", bindingCtx);

        assertEquals("The number is: 69", propVal.Expand().asString());
    }

    public void testNumericFormattingFloatNoSpec()
    {
        JObject viewModel = new JObject();
        viewModel.put("serial", new JValue(13.69));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The number is: {serial}", bindingCtx);

        assertEquals("The number is: 13.69", propVal.Expand().asString());
    }

    public void testNumericFormattingAsPercentage()
    {
        JObject viewModel = new JObject();
        viewModel.put("intVal", new JValue(13));
        viewModel.put("doubleVal", new JValue(0.69139876));
        viewModel.put("strVal", new JValue("threeve"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The int percentage is {intVal:P}, the double is: {doubleVal:P2}, and the str is {strVal:P2}", bindingCtx);

        // !!! On .NET we get a space between the value and the percent sign.  The iOS and Android percent formatter does not do this.  All formatters are
        //     using a locale-aware formatter and presumably know what they're doing, so I'm not inclined to try to "fix" one of them to make them
        //     match.
        //
        assertEquals("The int percentage is 1,300.00%, the double is: 69.14%, and the str is threeve", propVal.Expand().asString());
    }

    public void testNumericFormattingAsDecimal()
    {
        JObject viewModel = new JObject();
        viewModel.put("intVal", new JValue(-13420));
        viewModel.put("doubleVal", new JValue(69.139876));
        viewModel.put("strVal", new JValue("threeve"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The int val is {intVal:D}, the double val is: {doubleVal:D4}, and the str val is {strVal:D2}", bindingCtx);

        assertEquals("The int val is -13420, the double val is: 0069, and the str val is threeve", propVal.Expand().asString());
    }

    public void testNumericFormattingAsNumber()
    {
        JObject viewModel = new JObject();
        viewModel.put("intVal", new JValue(-13420));
        viewModel.put("doubleVal", new JValue(69.139876));
        viewModel.put("strVal", new JValue("threeve"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The int val is {intVal:N}, the double val is: {doubleVal:N4}, and the str val is {strVal:N2}", bindingCtx);

        assertEquals("The int val is -13,420.00, the double val is: 69.1399, and the str val is threeve", propVal.Expand().asString());
    }
}

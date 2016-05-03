package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;

import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JTokenType;
import io.synchro.json.JValue;

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
        assertEquals(420, expandedPropValToken.asLong());
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

    public void testNumericFormattingAsHex()
    {
        JObject viewModel = new JObject();
        viewModel.put("intVal", new JValue(254));
        viewModel.put("doubleVal", new JValue(254.139876));
        viewModel.put("strVal", new JValue("threeve"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The int val is {intVal:x}, the double val is: {doubleVal:X4}, and the str val is {strVal:X2}", bindingCtx);

        assertEquals("The int val is fe, the double val is: 00FE, and the str val is threeve", propVal.Expand().asString());
    }

    public void testNumericFormattingAsFixedPoint()
    {
        JObject viewModel = new JObject();
        viewModel.put("intVal", new JValue(-13420));
        viewModel.put("doubleVal", new JValue(254.139876));
        viewModel.put("strVal", new JValue("threeve"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The int val is {intVal:F2}, the double val is: {doubleVal:F4}, and the str val is {strVal:F2}", bindingCtx);

        assertEquals("The int val is -13420.00, the double val is: 254.1399, and the str val is threeve", propVal.Expand().asString());
    }

    public void testNumericFormattingAsExponential()
    {
        JObject viewModel = new JObject();
        viewModel.put("intVal", new JValue(-69));
        viewModel.put("doubleVal", new JValue(69.123456789));
        viewModel.put("strVal", new JValue("threeve"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The int val is {intVal:E2}, the double val is: {doubleVal:e4}, and the str val is {strVal:e2}", bindingCtx);

        // !!! .NET uses the "e+001" notation, whereas iOS and Android use "e1" notation.  Since they both use locale-aware built-in formatters for this,
        //     I'm not inclined to try to "fix" one of them to make them match.
        //
        // Also, for Java, it appears that the rounding on the last digit of the decimal is not controllable. Enjoy!
        assertEquals("The int val is -6.90E1, the double val is: 6.9120E1, and the str val is threeve", propVal.Expand().asString());
    }

    public void testNumericFormattingParsesStringAsNumber()
    {
        JObject viewModel = new JObject();
        viewModel.put("strVal", new JValue("13"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("The numeric value is {strVal:F2}", bindingCtx);

        assertEquals("The numeric value is 13.00", propVal.Expand().asString());
    }

    public void testEvalStringLength()
    {
        JObject viewModel = new JObject();
        viewModel.put("strVal", new JValue("13"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval({strVal}.length)", bindingCtx);

        assertEquals(2, propVal.Expand().asLong());
    }

    public void testEvalStringResult()
    {
        JObject viewModel = new JObject();
        viewModel.put("strVal", new JValue("hello"));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval({strVal} + ' world')", bindingCtx);

        assertEquals("hello world", propVal.Expand().asString());
    }

    public void testEvalNumericResult()
    {
        JObject viewModel = new JObject();
        viewModel.put("strVal", new JValue("hello"));
        viewModel.put("intVal", new JValue(10));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval({strVal}.length + {intVal})", bindingCtx);

        assertEquals(15.0, propVal.Expand().asDouble());
    }

    public void testEvalNumericResultIntAsInt()
    {
        JObject viewModel = new JObject();
        viewModel.put("strVal", new JValue("hello"));
        viewModel.put("intVal", new JValue(10));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval({strVal}.length + {intVal})", bindingCtx);

        assertEquals(15, propVal.Expand().asLong());
    }

    public void testEvalNumericResultDoubleAsDouble()
    {
        JObject viewModel = new JObject();
        viewModel.put("strVal", new JValue("hello"));
        viewModel.put("intVal", new JValue(10));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval({strVal}.length / {intVal})", bindingCtx);

        assertEquals(JTokenType.Float, propVal.Expand().getType());
        assertEquals(0.5, propVal.Expand().asDouble());
    }

    public void testEvalBoolResult()
    {
        JObject viewModel = new JObject();
        viewModel.put("intVal", new JValue(10));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval({intVal} == 10)", bindingCtx);

        assertEquals(true, propVal.Expand().asBoolean());
    }

    public void testEvalBoolParam()
    {
        JObject viewModel = new JObject();
        viewModel.put("boolVal", new JValue(true));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval(false || {boolVal})", bindingCtx);

        assertEquals(true, propVal.Expand().asBoolean());
    }

    public void testEvalNullParam()
    {
        JObject viewModel = new JObject();
        viewModel.put("nullVal", new JValue((String) null));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval(null === {nullVal})", bindingCtx);

        assertEquals(true, propVal.Expand().asBoolean());
    }

    public void testEvalUnsupportedParamType()
            throws IOException
    {
        JObject viewModel = (JObject) JObject.parse(
                "{" +
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

        PropertyValue propVal = new PropertyValue("eval({colors})", bindingCtx);

        // Unsupport JValue type will be converted to string in the Synchro way (array will get converted to string value representing
        // length of the array).
        //
        assertEquals("3", propVal.Expand().asString());
    }

    public void testEvalNullResult()
    {
        JObject viewModel = new JObject();
        viewModel.put("intVal", new JValue(10));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval(null)", bindingCtx);

        assertEquals(JTokenType.Null, propVal.Expand().getType());
    }

    public void testEvalUnsupportResultType()
    {
        JObject viewModel = new JObject();
        viewModel.put("intVal", new JValue(10));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval(['foo', 'bar'])", bindingCtx);

        // Expected result is toString() of actual result when not of support type (Boolean, Number, String, Null)
        assertEquals("foo,bar", propVal.Expand().asString());
    }

    public void testEvalBadScript()
    {
        JObject viewModel = new JObject();
        viewModel.put("intVal", new JValue(10));

        BindingContext bindingCtx = new BindingContext(viewModel);

        PropertyValue propVal = new PropertyValue("eval()foo)", bindingCtx);

        assertEquals("syntax error", propVal.Expand().asString());
    }
}

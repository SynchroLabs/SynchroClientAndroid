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
}

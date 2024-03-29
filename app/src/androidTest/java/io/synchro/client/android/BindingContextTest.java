package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.List;

import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JValue;

/**
 * Created by blake on 1/26/15.
 */
public class BindingContextTest extends TestCase
{
    JObject viewModel;

    public void setUp()
            throws IOException
    {
        viewModel = (JObject) JObject.parse(
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
                        "\"board\" :" +
                        "[" +
                            "[ \"s00\", \"s01\" ]," +
                            "[ \"s10\", \"s11\" ]," +
                        "]" +
                    "}");
    }

    public void testSelectChild()
    {
        BindingContext bindingCtx = new BindingContext(viewModel);

        BindingContext titleCtx = bindingCtx.Select("title");
        assertEquals(titleCtx.GetValue(), viewModel.get("title"));
    }

    public void testSelectChildren()
    {
        BindingContext bindingCtx = new BindingContext(viewModel);

        BindingContext colorsCtx = bindingCtx.Select("colors");
        List<BindingContext> colors = colorsCtx.SelectEach("");

        assertEquals(3, colors.size());
    }

    public void testSelectChildWithPath()
    {
        BindingContext bindingCtx = new BindingContext(viewModel);

        assertEquals("Green", bindingCtx.Select("colors[1].name").GetValue().asString());
    }

    public void testDataElement()
    {
        BindingContext bindingCtx = new BindingContext(viewModel);

        assertEquals("Green", bindingCtx.Select("colors[1].name").Select("$data").GetValue().asString());
    }

    public void testParentElement()
    {
        BindingContext bindingCtx = new BindingContext(viewModel);

        assertEquals("Green", bindingCtx.Select("colors[1].name").Select("$parent.name").GetValue().asString());
        assertEquals(
                "Red",
                bindingCtx.Select("colors[1].name").Select("$parent.$parent[0].name")
                                   .GetValue().asString()
                    );
        assertEquals(
                "Colors", bindingCtx.Select("colors[1].name")
                                             .Select("$parent.$parent.$parent.title").GetValue().asString()
                    );
        assertEquals(null, bindingCtx.Select("colors[1].name")
                                                 .Select("$parent.$parent.$parent.$parent")
                                                 .GetValue()
                       );
    }

    public void testParentElementInArrayOfArray()
    {
        BindingContext bindingCtx = new BindingContext(viewModel);

        assertEquals(1, bindingCtx.Select("board[1][0]").Select("$parent.$index").GetValue().asLong());
    }

    public void testRootElement()
    {
        BindingContext bindingCtx = new BindingContext(viewModel);

        assertEquals("Colors", bindingCtx.Select("colors[1].name").Select("$root.title").GetValue().asString());
    }

    public void testIndexElementOnArrayItem()
    {
        BindingContext bindingCtx = new BindingContext(viewModel);

        assertEquals(1, bindingCtx.Select("colors[1]").Select("$index").GetValue().asLong());
    }

    public void testIndexElementInsideArrayItem()
    {
        BindingContext bindingCtx = new BindingContext(viewModel);

        assertEquals(1, bindingCtx.Select("colors[1].name").Select("$index").GetValue().asLong());
    }

    public void testSetValue()
    {
        JObject testViewModel = (JObject)viewModel.deepClone();
        JObject newColor = new JObject();

        assertEquals(testViewModel, viewModel);
        assertTrue(testViewModel.equals(viewModel));

        newColor.put("name", new JValue("Greenish"));
        newColor.put("color", new JValue("green"));
        newColor.put("value", new JValue("0x00ff00"));

        ((JArray)testViewModel.get("colors")).set(1, newColor);

        assertFalse(testViewModel.equals(viewModel));

        BindingContext bindingCtx = new BindingContext(testViewModel);
        BindingContext colorNameCtx = bindingCtx.Select("colors[1].name");

        colorNameCtx.SetValue(new JValue("Green"));

        assertEquals(testViewModel, viewModel);
        assertTrue(testViewModel.equals(viewModel));
    }

    public void testRebind()
    {
        JObject testViewModel = (JObject)viewModel.deepClone();

        BindingContext bindingCtx = new BindingContext(testViewModel);
        BindingContext colorNameCtx = bindingCtx.Select("colors[1].name");

        JObject newColor = new JObject();

        newColor.put("name", new JValue("Purple"));
        newColor.put("color", new JValue("purp"));
        newColor.put("value", new JValue("0x696969"));

        ((JArray)testViewModel.get("colors")).set(1, newColor);

        assertEquals("Green", colorNameCtx.GetValue().asString());
        colorNameCtx.Rebind();
        assertEquals("Purple", colorNameCtx.GetValue().asString());
    }
}

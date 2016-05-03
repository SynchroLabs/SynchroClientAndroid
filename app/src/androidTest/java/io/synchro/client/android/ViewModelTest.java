package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Map;

import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JValue;

/**
 * Created by blake on 2/14/15.
 */
public class ViewModelTest extends TestCase
{
    JObject viewModelObj;

    public void setUp()
            throws IOException
    {
        viewModelObj = (JObject) JObject.parse(
                "{" +
                        "\"serial\" : 1," +
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
    }

    public void testUpdateView()
    {
        // Create a binding of each type, initialize them from the view model, verify that their values were set properly
        //
        ViewModel viewModel = new ViewModel();

        viewModel.InitializeViewModelData(viewModelObj);

        final String[] serialString = new String[1];
        PropertyBinding propBinding = viewModel.CreateAndRegisterPropertyBinding(
                viewModel.getRootBindingContext(),
                "Serial: {serial}",
                new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        serialString[0] = value.asString();
                    }
                }
             );

        final long[] serialValue = new long[] { -1 };
        ValueBinding valBinding = viewModel.CreateAndRegisterValueBinding(
                viewModel.getRootBindingContext().Select("serial"),
                new IGetViewValue()
                {
                    @Override
                    public JToken GetViewValue()
                    {
                        return new JValue(serialValue[0]);
                    }
                },
                new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        serialValue[0] = value.asLong();
                    }
                }
             );

        propBinding.UpdateViewFromViewModel();
        valBinding.UpdateViewFromViewModel();

        assertEquals("Serial: 1", serialString[0]);
        assertEquals(1, serialValue[0]);
    }

    public void testUpdateViewFromValueBinding()
    {
        ViewModel viewModel = new ViewModel();

        viewModel.InitializeViewModelData(viewModelObj);

        final boolean[] bindingsInitialized = new boolean[] { false };

        final String[] serialString = new String[] { "" };
        PropertyBinding propBinding = viewModel.CreateAndRegisterPropertyBinding(
                viewModel.getRootBindingContext(),
                "Serial: {serial}",
                new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        serialString[0] = value.asString();
                    }
                }
            );

        final String[] titleString = new String[] { "" };
        PropertyBinding propBindingTitle = viewModel.CreateAndRegisterPropertyBinding(
                viewModel.getRootBindingContext(),
                "Title: {title}",
                new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        titleString[0] = value.asString();
                        if (bindingsInitialized[0])
                        {
                            fail("Property binding setter for title should not be called after initialization (since its token wasn't impacted by the value binding change)");
                        }
                    }
                }
             );

        final long[] serialValue = new long[] { -1 };
        ValueBinding valBinding = viewModel.CreateAndRegisterValueBinding(
                viewModel.getRootBindingContext().Select("serial"),
                new IGetViewValue()
                {
                    @Override
                    public JToken GetViewValue()
                    {
                        return new JValue(serialValue[0]);
                    }
                },
                new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        serialValue[0] = value.asLong();
                        if (bindingsInitialized[0])
                        {
                            fail("Value bining setter should not be called after initialization (its change shouldn't update itself)");
                        }
                    }
                }
            );

        propBinding.UpdateViewFromViewModel();
        propBindingTitle.UpdateViewFromViewModel();
        valBinding.UpdateViewFromViewModel();

        bindingsInitialized[0] = true;

        assertEquals("Serial: 1", serialString[0]);
        assertEquals("Title: Colors", titleString[0]);
        assertEquals(1, serialValue[0]);

        // When the value binding updates the view model, the propBinding (that has a token bound to the same context/path) will automatically
        // update (its setter will be called), but the value binding that triggered the update will not have its setter called.
        //
        serialValue[0] = 2;
        valBinding.UpdateViewModelFromView();

        assertEquals("Serial: 2", serialString[0]);

        // Now let's go collect the changes caused by value binding updates and verify them...
        //
        Map<String, JToken> changes = viewModel.CollectChangedValues();
        assertEquals(1, changes.size());
        assertEquals(2, changes.get("serial").asLong());

        // Collecting the changes (above) should have cleared the dirty indicators, so there shouldn't be any changes now...
        //
        assertEquals(0, viewModel.CollectChangedValues().size());
    }

    public void testUpdateViewFromViewModelDeltas()
    {
        ViewModel viewModel = new ViewModel();

        viewModel.InitializeViewModelData(viewModelObj);

        final boolean[] bindingsInitialized = new boolean[] { false };

        final String serialString[] = new String[] { "" };
        PropertyBinding propBinding = viewModel.CreateAndRegisterPropertyBinding(
                viewModel.getRootBindingContext(),
                "Serial: {serial}",
                new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        serialString[0] = value.asString();
                    }
                }
            );

        final String titleString[] = new String[] { "" };
        PropertyBinding propBindingTitle = viewModel.CreateAndRegisterPropertyBinding(
                viewModel.getRootBindingContext(),
                "Title: {title}",
                new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        titleString[0] = value.asString();
                        if (bindingsInitialized[0])
                        {
                            fail("Property binding setter for title should not be called after initialization (since its token wasn't impacted by the deltas)");
                        }
                    }
                }
             );

        final long serialValue[] = new long[] { -1 };
        ValueBinding valBinding = viewModel.CreateAndRegisterValueBinding(
                viewModel.getRootBindingContext().Select("serial"),
                new IGetViewValue()
                {
                    @Override
                    public JToken GetViewValue()
                    {
                        return new JValue(serialValue[0]);
                    }
                },
                new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        serialValue[0] = value.asLong();
                    }
                }
                                                                         );

        propBinding.UpdateViewFromViewModel();
        propBindingTitle.UpdateViewFromViewModel();
        valBinding.UpdateViewFromViewModel();

        bindingsInitialized[0] = true;

        assertEquals("Serial: 1", serialString[0]);
        assertEquals("Title: Colors", titleString[0]);
        assertEquals(1, serialValue[0]);

        // We're going to apply some deltas to the view model and verify that the correct dependant bindings got updated,
        // and that no non-dependant bindings got updated
        //
        JArray deltas = new JArray();
        JObject deltaObject = new JObject();

        deltaObject.put("path", new JValue("serial"));
        deltaObject.put("change", new JValue("update"));
        deltaObject.put("value", new JValue(2));

        deltas.add(deltaObject);

        viewModel.UpdateViewModelData(deltas, true);

        assertEquals("Serial: 2", serialString[0]);
        assertEquals(2, serialValue[0]);
    }
}


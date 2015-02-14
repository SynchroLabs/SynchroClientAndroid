package io.synchro.client.android;

import junit.framework.TestCase;

import java.io.IOException;

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

        final int[] serialValue = new int[] { -1 };
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
                        serialValue[0] = value.asInt();
                    }
                }
             );

        propBinding.UpdateViewFromViewModel();
        valBinding.UpdateViewFromViewModel();

        assertEquals("Serial: 1", serialString[0]);
        assertEquals(1, serialValue[0]);
    }
}

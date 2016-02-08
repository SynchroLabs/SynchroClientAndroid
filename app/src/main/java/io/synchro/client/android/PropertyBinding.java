package io.synchro.client.android;

import java.util.List;

import io.synchro.json.JToken;

/**
 * Created by blake on 2/9/15.
 */
// For one-way binding of any property (binding to a pattern string than can incorporate multiple bound values)
//
public class PropertyBinding
{
    PropertyValue _propertyValue;
    ISetViewValue _setViewValue;

    public PropertyBinding(BindingContext bindingContext, String value, ISetViewValue setViewValue)
    {
        _propertyValue = new PropertyValue(value, bindingContext);
        _setViewValue = setViewValue;
    }

    public JToken UpdateViewFromViewModel()
    {
        JToken value = _propertyValue.Expand();
        if (_setViewValue != null)
        {
            _setViewValue.SetViewValue(value);
        }
        return value;
    }

    public List<BindingContext> getBindingContexts()
    {
        return _propertyValue.getBindingContexts();
    }
}
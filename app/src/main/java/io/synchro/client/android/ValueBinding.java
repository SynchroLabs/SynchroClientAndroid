package io.synchro.client.android;

/**
 * Created by blake on 2/9/15.
 */
// For two-way binding (typically of primary "value" property) - binding to a single value only
//
public class ValueBinding
{
    ViewModel _viewModel;
    BindingContext _bindingContext;
    IGetViewValue _getViewValue;
    ISetViewValue _setViewValue;
    boolean _isDirty;

    public boolean getIsDirty()
    {
        return _isDirty;
    }

    public void setIsDirty(boolean isDirty)
    {
        _isDirty = isDirty;
    }

    public ValueBinding(ViewModel viewModel, BindingContext bindingContext, IGetViewValue getViewValue, ISetViewValue setViewValue)
    {
        _viewModel = viewModel;
        _bindingContext = bindingContext;
        _getViewValue = getViewValue;
        _setViewValue = setViewValue;
        setIsDirty(false);
    }

    public void UpdateViewModelFromView()
    {
        _viewModel.UpdateViewModelFromView(_bindingContext, _getViewValue);
    }

    public void UpdateViewFromViewModel()
    {
        if (_setViewValue != null)
        {
            _setViewValue.SetViewValue(_bindingContext.GetValue());
        }
    }

    public BindingContext getBindingContext()
    {
        return _bindingContext;
    }
}
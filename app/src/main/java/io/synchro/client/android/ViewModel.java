package io.synchro.client.android;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by blake on 2/9/15.
 */
// The ViewModel will manage the client view model data (initialize and update it).  It will also manage all bindings
// to view model data, including managing updates on changes.
//
public class ViewModel
{
    public static final String TAG = ViewModel.class.getSimpleName();

    BindingContext _rootBindingContext;
    JObject        _rootObject;
    boolean _updatingView = false;

    List<ValueBinding> _valueBindings = new ArrayList<>();
    List<PropertyBinding> _propertyBindings = new ArrayList<>();

    public ViewModel()
    {
        _rootBindingContext = new BindingContext(_rootObject);
    }

    public BindingContext getRootBindingContext()
    {
        return _rootBindingContext;
    }

    public JObject getRootObject()
    {
        return _rootObject;
    }
    // Only used by BindingContext - "internal"?

    public ValueBinding CreateAndRegisterValueBinding(BindingContext bindingContext, IGetViewValue getValue, ISetViewValue setValue)
    {
        ValueBinding valueBinding = new ValueBinding(this, bindingContext, getValue, setValue);
        _valueBindings.add(valueBinding);
        return valueBinding;
    }

    public void UnregisterValueBinding(ValueBinding valueBinding)
    {
        _valueBindings.remove(valueBinding);
    }

    public PropertyBinding CreateAndRegisterPropertyBinding(BindingContext bindingContext, String value, ISetViewValue setValue)
    {
        PropertyBinding propertyBinding = new PropertyBinding(bindingContext, value, setValue);
        _propertyBindings.add(propertyBinding);
        return propertyBinding;
    }

    public void UnregisterPropertyBinding(PropertyBinding propertyBinding)
    {
        _propertyBindings.remove(propertyBinding);
    }

    // Tokens in the view model have a "ViewModel." prefix (as the view model itself is a child node of a larger
    // JSON response).  We need to prune that off so future SelectToken operations will work when applied to the
    // root binding context (the context associated with the "ViewModel" JSON object).
    //
    public static String GetTokenPath(JToken token)
    {
        String path = token.getPath();

        if (path.startsWith("ViewModel."))
        {
            path = path.substring("ViewModel.".length());
        }

        return path;
    }

    public void InitializeViewModelData(JObject viewModel)
    {
        if (viewModel == null)
        {
            viewModel = new JObject();
        }
        _rootObject = viewModel;
        _valueBindings.clear();
        _propertyBindings.clear();
        _rootBindingContext.setBindingRoot(_rootObject);
    }

    // This object represents a binding update (the path of the bound item and an indication of whether rebinding is required)
    //
    public class BindingUpdate
    {
        private String bindingPath;
        private boolean rebindRequired;

        public void setBindingPath(String bindingPath)
        {
            this.bindingPath = bindingPath;
        }

        public String getBindingPath()
        {
            return bindingPath;
        }

        public void setRebindRequired(boolean rebindRequired)
        {
            this.rebindRequired = rebindRequired;
        }

        public boolean getRebindRequired()
        {
            return this.rebindRequired;
        }

        public BindingUpdate(String bindingPath, boolean rebindRequired)
        {
            this.bindingPath = bindingPath;
            this.rebindRequired = rebindRequired;
        }
    }

    // If bindingUpdates is provided, any binding other than an optionally specified sourceBinding
    // that is impacted by a token in bindingUpdates will have its view updated.  If no bindingUpdates
    // is provided, all bindings will have their view updated.
    //
    // If bindingUpdates is provided, any binding impacted by a path for which rebinding is indicated
    // will be rebound.
    //
    // Usages:
    //    On new view model - no params - update view for all bindings, no rebind needed
    //    On update view model - pass list containing all updates
    //    On update view (from ux) - pass list containing the single update, and the sourceBinding (that triggered the update)
    //
    public void UpdateViewFromViewModel(List<BindingUpdate> bindingUpdates, BindingContext sourceBinding)
    {
        _updatingView = true;

        for (ValueBinding valueBinding : _valueBindings)
        {
            if (valueBinding.getBindingContext() != sourceBinding)
            {
                boolean isUpdateRequired = (bindingUpdates == null);
                boolean isBindingDirty = false;
                if (bindingUpdates != null)
                {
                    for (BindingUpdate update : bindingUpdates)
                    {
                        if (valueBinding.getBindingContext().IsBindingUpdated(update.getBindingPath(), update.getRebindRequired()))
                        {
                            isUpdateRequired = true;
                            if (update.getRebindRequired())
                            {
                                isBindingDirty = true;
                                break;
                            }
                        }
                    }
                }

                if (isBindingDirty)
                {
                    Log.d(
                            TAG, String.format(
                                    "Rebind value binding with path: %s",
                                    valueBinding.getBindingContext().getBindingPath()
                                              )
                         );
                    valueBinding.getBindingContext().Rebind();
                }

                if (isUpdateRequired)
                {
                    valueBinding.UpdateViewFromViewModel();
                }
            }
        }

        for (PropertyBinding propertyBinding : _propertyBindings)
        {
            boolean isUpdateRequired = (bindingUpdates == null);

            for (BindingContext propBinding : propertyBinding.getBindingContexts())
            {
                boolean isBindingDirty = false;
                if (bindingUpdates != null)
                {
                    for (BindingUpdate update : bindingUpdates)
                    {
                        if (propBinding.IsBindingUpdated(update.getBindingPath(), update.getRebindRequired()))
                        {
                            isUpdateRequired = true;
                            if (update.getRebindRequired())
                            {
                                isBindingDirty = true;
                                break;
                            }
                        }
                    }
                }

                if (isBindingDirty)
                {
                    Log.d(TAG, String.format("Rebind property binding with path: %s", propBinding.getBindingPath()));
                    propBinding.Rebind();
                }
            }

            if (isUpdateRequired)
            {
                propertyBinding.UpdateViewFromViewModel();
            }
        }

        _updatingView = false;
    }

    public void UpdateViewModelData(JToken viewModelDeltas, boolean updateView)
    {
        List<BindingUpdate> bindingUpdates = new ArrayList<>();

        Log.d(TAG, String.format("Processing view model updates: %s", viewModelDeltas));
        if ((viewModelDeltas.getType() == JTokenType.Array))
        {
            // Removals are generally reported as removals from the end of the list with increasing indexes.  If
            // we process them in this way, the first removal will change the list positions of remaining items
            // and cause subsequent removals to be off (typically to fail).  And we don't really want to rely
            // on ordering in the first place.  So what we are going to do is track all of the removals, and then
            // actually remove them at the end.
            //
            List<JToken> removals = new ArrayList<>();

            for (JToken viewModelDeltaToken : (JArray)viewModelDeltas)
            {
                JObject viewModelDelta = (JObject) viewModelDeltaToken;
                String path = viewModelDelta.get("path").asString();
                String changeType = viewModelDelta.get("change").asString();

                Log.d(TAG, String.format("View model item change (%s) for path: %s", changeType, path));
                switch (changeType)
                {
                    case "object":
                        // For "object" changes, this just means that an existing object had a property added/updated/removed or
                        // an array had items added/updated/removed.  We don't need to actually do any updates for this notification,
                        // we just need to make sure any bound elements get their views updated appropriately.
                        //
                        bindingUpdates.add(new BindingUpdate(path, false));
                        break;
                    case "update":
                    {
                        JToken vmItemValue = _rootObject.selectToken(path);
                        if (vmItemValue != null)
                        {
                            Log.d(TAG,
                                    String.format("Updating view model item for path: %s to value: %s", path,
                                    viewModelDelta.get("value")));

                            boolean rebindRequired = JToken
                                    .updateTokenValue(vmItemValue, viewModelDelta.get("value")) != null;
                            bindingUpdates.add(new BindingUpdate(path, rebindRequired));
                        }
                        else
                        {
                            Log.e(
                                    TAG,
                                    String.format(
                                            "VIEW MODEL SYNC WARNING: Unable to find existing value when processing update, something went wrong, path: %s",
                                            path
                                                 )
                                 );
                        }
                        break;
                    }
                    case "add":
                {
                    Log.d(
                            TAG,
                            String.format(
                                    "Adding bound item for path: %s with value: %s", path,
                                    viewModelDelta.get("value").asString()
                                         )
                         );
                    bindingUpdates.add(new BindingUpdate(path, true));

                    // First, double check to make sure the path doesn't actually exist
                    JToken vmItemValue = _rootObject.selectToken(path);
                    if (vmItemValue == null)
                    {
                        if (path.endsWith("]"))
                        {
                            // This is an array element...
                            String parentPath = path.substring(0, path.lastIndexOf("["));
                            JToken parentToken = _rootObject.selectToken(parentPath);
                            if ((parentToken != null) && (parentToken instanceof JArray))
                            {
                                ((JArray) parentToken).add(viewModelDelta.get("value"));
                            }
                            else
                            {
                                Log.e(
                                        TAG,
                                        String.format(
                                                "VIEW MODEL SYNC WARNING: Attempt to add array member, but parent didn't exist or was not an array, parent path: %s",
                                                parentPath
                                                     )
                                     );
                            }
                        }
                        else if (path.contains("."))
                        {
                            // This is an object property...
                            String parentPath = path.substring(0, path.lastIndexOf("."));
                            String attributeName = path.substring(path.lastIndexOf(".") + 1);
                            JToken parentToken = _rootObject.selectToken(parentPath);
                            if ((parentToken != null) && (parentToken instanceof JObject))
                            {
                                ((JObject) parentToken).put(
                                        attributeName, viewModelDelta.get("value")
                                                           );
                            }
                            else
                            {
                                Log.e(TAG,
                                        String.format("VIEW MODEL SYNC WARNING: Attempt to add object property, but parent didn't exist or was not an object, parent path: %s",
                                        parentPath)
                                            );
                            }
                        }
                        else
                        {
                            // This is a root property...
                            _rootObject.put(path, viewModelDelta.get("value"));
                        }
                    }
                    else
                    {
                        Log.e(TAG,
                                String.format("VIEW MODEL SYNC WARNING: Found existing value when processing add, something went wrong, path: %s",
                                path)
                                    );
                    }
                    break;
                } case "remove":
                {
                    Log.d(TAG, String.format("Removing bound item for path: %s", path));
                    bindingUpdates.add(new BindingUpdate(path, true));

                    JToken vmItemValue = _rootObject.selectToken(path);
                    if (vmItemValue != null)
                    {
                        Log.d(TAG, String.format("Removing bound item for path: %s", vmItemValue.getPath()));
                        // Just track this removal for now - we'll remove it at the end
                        removals.add(vmItemValue);
                    }
                    else
                    {
                        Log.e(TAG,
                                String.format("VIEW MODEL SYNC WARNING: Attempt to remove object property or array element, but it wasn't found, path: %s",
                                path)
                                    );
                    }
                    break;
                }
                }
            }

            // Remove all tokens indicated as removed
            for (JToken vmItemValue : removals)
            {
                vmItemValue.remove();
            }

            try
            {
                Log.d(
                        TAG, String.format(
                                "View model after processing updates: %s", this._rootObject.toJson()
                                          )
                     );
            }
            catch (IOException e)
            {
                Log.wtf(TAG, e);
            }
        }

        if (updateView)
        {
            UpdateViewFromViewModel(bindingUpdates, null);
        }
    }

    // This is called when a value change is triggered from the UX, specifically when the control calls
    // the UpdateValue member of it's ValueBinding.  We will change the value, record the change, and
    // update any binding that depends on this value.  This is the mechanism that allows for "client side
    // dynamic binding".
    //
    public void UpdateViewModelFromView(BindingContext bindingContext, IGetViewValue getValue)
    {
        if (_updatingView)
        {
            // When we update the view from the view model, the UX generates a variety of events to indicate
            // that values changed (text changed, list contents changed, selection changed, etc).  We don't
            // want those events to trigger a view model update (and mark as dirty), so we bail here.  This
            // check is not sufficient (by itself), since some of these events can be posted and will show up
            // asynchronously, so we do some other checks, but this is quick and easy and catches most of it.
            //
            return;
        }

        JToken newValue = getValue.GetViewValue();
        JToken currentValue = bindingContext.GetValue();
        if (newValue == currentValue)
        {
            // Only record changes and update dependant UX objects for actual value changes - some programmatic
            // changes to set the view to the view model state will trigger otherwise unidentifiable change events,
            // and this check will weed those out (if they got by the _updatingView check above).
            //
            return;
        }

        // Update the view model
        //
        boolean rebindRequired = bindingContext.SetValue(newValue);

        // Find the ValueBinding that triggered this update and mark it as dirty...
        //
        for (ValueBinding valueBinding : _valueBindings)
        {
            if (valueBinding.getBindingContext() == bindingContext)
            {
                // logger.Debug("Marking dirty - binding with path: {0}", bindingContext.BindingPath);
                valueBinding.setIsDirty(true);
            }
        }

        // Process all of the rest of the bindings (rebind and update view as needed)...
        //
        List<BindingUpdate> bindingUpdates = new ArrayList<>();
        bindingUpdates.add(new BindingUpdate(bindingContext.getBindingPath(), rebindRequired));
        UpdateViewFromViewModel(bindingUpdates, bindingContext);
    }

    public boolean IsDirty()
    {
        for (ValueBinding valueBinding : _valueBindings)
        {
            if (valueBinding.getIsDirty())
            {
                return true;
            }
        }
        return false;
    }

    public Map<String, JToken> CollectChangedValues()
    {
        Map<String,JToken> vmDeltas = new HashMap<>();

        for (ValueBinding valueBinding : _valueBindings)
        {
            if (valueBinding.getIsDirty())
            {
                String path = valueBinding.getBindingContext().getBindingPath();
                JToken value = valueBinding.getBindingContext().GetValue();
                Log.d(TAG, String.format("Changed view model item - path: %s - value: %s", path, value.asString()));
                vmDeltas.put(path, value);
                valueBinding.setIsDirty(false);
            }
        }

        return vmDeltas;
    }
}
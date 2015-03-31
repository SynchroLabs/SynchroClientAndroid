package io.synchro.client.android;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blake on 1/19/15.
 */
// Corresponds to a specific location in a JSON oject (which may or may not exist at the time the BindingContext is created).
//
public class BindingContext
{
    public static final String TAG = BindingContext.class.getSimpleName();

    JObject _bindingRoot;

    String _bindingPath;
    JToken _boundToken;
    boolean _isIndex = false;

    public JObject getBindingRoot()
    {
        return _bindingRoot;
    }

    public void setBindingRoot(JObject value)
    {
        if (value != _bindingRoot)
        {
            _bindingRoot = value;
            this.Rebind();
        }
    }

    // Creates the root binding context, from which all other binding contexts will be created
    //
    public BindingContext(JObject bindingRoot)
    {
        _bindingRoot = bindingRoot;
        _bindingPath = "";
        _boundToken = _bindingRoot;
    }

    private void attemptToBindTokenIfNeeded()
    {
        if (_boundToken == null)
        {
            _boundToken = _bindingRoot.selectToken(_bindingPath, false);
        }
    }

    private void resolveBinding(String parentPath, String bindingPath)
    {
        // Process path elements:
        //
        //  $root
        //  $parent
        //  $data
        //  $index
        //
        _bindingPath = "";
        for (String pathElement : bindingPath.split("\\."))
        {
            if (pathElement.startsWith("$"))
            {
                pathElement = pathElement.substring(1);
                Log.d(TAG, String.format("Found binding path element: %s", pathElement));
                switch (pathElement)
                {
                    case "root":
                        parentPath = "";
                        break;
                    case "parent":
                        if (parentPath.length() != 0)
                        {
                            int lastDot = parentPath.lastIndexOf(".");
                            if (lastDot == -1)
                            {
                                // Remove the only remaining path segment
                                parentPath = "";
                            }
                            else
                            {
                                // Remove the last (rightmost) path segment
                                parentPath = parentPath.substring(0, lastDot);
                            }
                        }
                        break;
                    case "data":
                        // We're going to treat $data as a noop
                        break;
                    case "index":
                        _isIndex = true;
                        break;
                }
            }
            else
            {
                _bindingPath = _bindingPath + ((_bindingPath.length() == 0) ? "" : ".") + pathElement;
            }
        }

        if ((parentPath.length() > 0) && (_bindingPath.length() > 0))
        {
            _bindingPath = parentPath + "." + _bindingPath;
        }
        else if (parentPath.length() > 0)
        {
            _bindingPath = parentPath;
        }
    }

    private BindingContext(BindingContext context, String bindingPath)
    {
        _bindingRoot = context._bindingRoot;
        resolveBinding(context._bindingPath, bindingPath);
        this.attemptToBindTokenIfNeeded();
    }

    private BindingContext(BindingContext context, int index, String bindingPath)
    {
        _bindingRoot = context._bindingRoot;
        resolveBinding(context._bindingPath + "[" + index + "]", bindingPath);
        this.attemptToBindTokenIfNeeded();
    }

    //
    // Public interface starts here...
    //

    // Given a path to a changed element, determine if the binding is impacted.
    //
    public boolean IsBindingUpdated(String updatedElementPath, boolean objectChange)
    {
        if (objectChange && (_bindingPath.startsWith(updatedElementPath)))
        {
            // If this is an object change (meaning the object/array itself changed), then a binding
            // update is required if the path matches or is an ancestor of the binging path.
            //
            return true;
        }
        else if (_bindingPath.equals(updatedElementPath))
        {
            // If this is a primitive value change, or an object/array contents change (meaning
            // that the object itself did not change), then a binding update is only required if
            // the path matches exactly.
            //
            return true;
        }

        return false;
    }

    public BindingContext Select(String bindingPath)
    {
        return new BindingContext(this, bindingPath);
    }

    public List<BindingContext> SelectEach(String bindingPath)
    {
        List<BindingContext> bindingContexts = new ArrayList<BindingContext>();

        if ((_boundToken != null) && (_boundToken.getType() == JTokenType.Array))
        {
            int index = 0;
            for (JToken arrayElement : (JArray)_boundToken)
            {
                bindingContexts.add(new BindingContext(this, index, bindingPath));
                index++;
            }
        }

        return bindingContexts;
    }

    public String getBindingPath()
    {
        return _bindingPath;
    }

    public JToken GetValue()
    {
        this.attemptToBindTokenIfNeeded();
        if (_boundToken != null)
        {
            if (_isIndex)
            {
                // Find first ancestor that is an array and get the position of that ancestor's child
                //
                JToken child = _boundToken;
                JToken parent = child.getParent();

                while (parent != null)
                {
                    if (parent instanceof JArray)
                    {
                        return new JValue(((JArray) parent).indexOf(child));
                    }
                    else
                    {
                        child = parent;
                        parent = child.getParent();
                    }
                }
            }
            else
            {
                return _boundToken;
            }
        }

        // Token could not be bound at this time (no corresponding token) - no value returned!
        return null;
    }

    // Return boolean indicating whether the bound token was changed (and rebinding needs to be triggered)
    //
    public boolean SetValue(JToken value)
    {
        this.attemptToBindTokenIfNeeded();
        if (_boundToken != null)
        {
            if (!_isIndex)
            {
                return JToken.updateTokenValue(_boundToken, value) != null;
            }
        }

        // Token could not be bound at this time (no corresponding token) - value not set!
        return false;
    }

    public void Rebind()
    {
        _boundToken = _bindingRoot.selectToken(_bindingPath, false);
    }
}

package io.synchro.client.android;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by blake on 1/19/15.
 */
public class BindingHelper
{
    public static final String TAG = BindingHelper.class.getSimpleName();

    // Binding is specified in the "binding" attribute of an element.  For example, binding: { value: "foo" } will bind the "value"
    // property of the control to the "foo" value in the current binding context.  For controls that can call commands, the command
    // handlers are bound similarly, for example, binding: { onClick: "someCommand" } will bind the onClick action of the control to
    // the "someCommand" command.
    //
    // A control type may have a default binding attribute, so that a simplified syntax may be used, where the binding contains a
    // simple value to be bound to the default binding attribute of the control.  For example, an edit control might use binding: "username"
    // to bind the default attribute ("value") to username.  A button might use binding: "someCommand" to bind the default attribute ("onClick")
    // to someCommand.
    //
    // This function extracts the binding value, and if the default/shorthand notation is used, expands it to a fully specified binding object.
    //
    //     For example, for an edit control with a default binding attribute of "value" a binding of:
    //
    //       binding: "username"
    //
    //         becomes
    //
    //       binding: {value: "username"}
    //
    //     For commands:
    //
    //       binding: "doSomething"
    //
    //         becomes
    //
    //       binding: { onClick: "doSomething" }
    //
    //         becomes
    //
    //       binding: { onClick: { command: "doSomething" } }
    //
    //     Also (default binding atttribute is 'onClick', which is also in command attributes list):
    //
    //       binding: { command: "doSomething" value: "theValue" }
    //
    //         becomes
    //
    //       binding: { onClick: { command: "doSomething", value: "theValue" } }
    //
    public static JObject GetCanonicalBindingSpec(JObject controlSpec, String defaultBindingAttribute, String[] commandAttributes)
    {
        JObject bindingObject;

        boolean defaultAttributeIsCommand = false;
        if (commandAttributes != null)
        {
            defaultAttributeIsCommand = Arrays.asList(commandAttributes).contains(defaultBindingAttribute);
        }

        JToken bindingSpec = controlSpec.get("binding");

        if (bindingSpec != null)
        {
            if (bindingSpec.getType() == JTokenType.Object)
            {
                // Encountered an object spec, return that (subject to further processing below)
                //
                bindingObject = (JObject)bindingSpec.deepClone();

                if (defaultAttributeIsCommand && (bindingObject.get("command") != null))
                {
                    // Top-level binding spec object contains "command", and the default binding attribute is a command, so
                    // promote { command: "doSomething" } to { defaultBindingAttribute: { command: "doSomething" } }
                    //
                    bindingObject = new JObject();
                    bindingObject.put(defaultBindingAttribute, bindingObject);
                }
            }
            else
            {
                // Top level binding spec was not an object (was an array or value), so promote that value to be the value
                // of the default binding attribute
                //
                bindingObject = new JObject();
                bindingObject.put(defaultBindingAttribute, bindingSpec.deepClone());
            }

            // Now that we've handled the default binding attribute cases, let's look for commands that need promotion...
            //
            if (commandAttributes != null)
            {
                for (String commandAttribute : commandAttributes)
                {
                    // Processing a command (attribute name corresponds to a command)
                    //
                    if (bindingObject.get(commandAttribute) instanceof JValue)
                    {
                        // If attribute value is simple value type, promote "attributeValue" to { command: "attributeValue" }
                        //
                        // !!! The creation of the wrapper object below failed on the iOS port because the bindingObject[commandAttribute]
                        //     already had a parent.  Fix was to wrap in JValue copy constructor.  Verify and fix as needed once we port
                        //     the unit tests back.
                        //
                        JObject value = new JObject();
                        value.put("command", bindingObject.get(commandAttribute));
                        bindingObject.put(commandAttribute, value);
                    }
                }
            }

            Log.d(TAG, String.format("Found binding object: %s", bindingObject));
        }
        else
        {
            // No binding spec
            bindingObject = new JObject();
        }

        return bindingObject;
    }
}

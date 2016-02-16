package io.synchro.client.android;

import java.util.HashMap;
import java.util.Map;

import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JTokenType;
import io.synchro.json.JValue;

/**
 * Created by blake on 3/1/15.
 */
public class CommandInstance
{
    String _command;
    HashMap<String, JToken> _parameters = new HashMap<>();

    public CommandInstance(String command)
    {
        _command = command;
    }

    public void SetParameter(String parameterName, JToken parameterValue)
    {
        _parameters.put(parameterName, parameterValue);
    }

    public String getCommand()
    {
        return _command;
    }

    // If a parameter is not a string type, then that parameter is passed directly.  This allows for parameters to
    // be boolean, numeric, or even objects.  If a parameter is a string, it will be evaluated to see if it has
    // any property bindings, and if so, those bindings will be expanded.  This allows for parameters that vary
    // based on the current context, for example, and also allows for complex values (such as property bindings
    // that refer to a single value of a type other than string, such as an object).
    //
    public JObject GetResolvedParameters(BindingContext bindingContext)
    {
        JObject obj = new JObject();
        for (Map.Entry<String,JToken> parameter : _parameters.entrySet())
        {
            JToken value = parameter.getValue();
            if (value.getType() == JTokenType.String)
            {
                value = PropertyValue.Expand(value.asString(), bindingContext);
            }

            if (value != null)
            {
                value = value.deepClone();
            }
            else
            {
                value = new JValue((String) null);
            }

            obj.put(parameter.getKey(), value);
        }
        return obj;
    }
}

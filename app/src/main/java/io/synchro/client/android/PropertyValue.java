package io.synchro.client.android;

import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.synchro.json.JToken;
import io.synchro.json.JTokenType;
import io.synchro.json.JValue;

/**
 * Created by blake on 2/1/15.
 */
public class PropertyValue
{
    public static final String TAG = PropertyValue.class.getSimpleName();

    // To deal with "escaped" braces (double open braces), our brace contents regex checks around our potential open brace
    // to see if another one precedes or follows is using:
    //
    //    Negative lookbehind (zero length assertion to make sure brace not preceded by brace) = (?!<[}])
    //    Negative lookahead (zero length assertion to make sure brace not followed by brace) = {?![}])
    //
    private static Pattern _braceContentsRE = Pattern.compile("(?<![{])[{](?![{])([^}]*)[}]");

    private String                              _formatString;
    private boolean                             _isExpression = false;
    private List<BoundAndPossiblyResolvedToken> _boundTokens;

    private static final String javascriptExpressionPrefix = "eval(";
    private static final String javascriptExpressionSuffix = ")";

    // Construct and return the unresolved binding contexts (the one-way bindings, excluding the one-time bindings)
    //
    public List<BindingContext> getBindingContexts()
    {
        List<BindingContext> bindingContexts = new ArrayList<>();
        for (BoundAndPossiblyResolvedToken boundToken : _boundTokens)
        {
            if (!boundToken.getResolved())
            {
                bindingContexts.add(boundToken.getBindingContext());
            }
        }
        return bindingContexts;
    }

    // http://www.javamex.com/tutorials/regular_expressions/search_replace_loop.shtml

    public PropertyValue(String tokenString, BindingContext bindingContext)
    {
        _boundTokens = new ArrayList<>();
        StringBuffer finalString = new StringBuffer();
        int tokenIndex = 0;

        if (tokenString.startsWith(javascriptExpressionPrefix) && tokenString.endsWith(javascriptExpressionSuffix))
        {
            _isExpression = true;
            tokenString = tokenString.substring(javascriptExpressionPrefix.length(), tokenString.length() - javascriptExpressionSuffix.length());
            Log.d(TAG, String.format("Property value string is expression: %s", tokenString));
        }

        Matcher m = _braceContentsRE.matcher(tokenString);
        while (m.find())
        {
            Log.d(TAG, String.format("Found boundtoken: %s", m.group(1)));

            // Parse out any format specifier...
            //
            String token = m.group(1);
            String format = null;
            if (token.contains(":"))
            {
                String[] result = token.split(":");
                token = result[0];
                format = result[1];
            }

            // Parse out and record any one-time binding indicator
            //
            boolean oneTimeBinding = false;
            if (token.startsWith("^"))
            {
                token = token.substring(1);
                oneTimeBinding = true;
            }

            // Parse out and record negation indicator
            //
            boolean negated = false;
            if (token.startsWith("!"))
            {
                token = token.substring(1);
                negated = true;
            }

            BoundAndPossiblyResolvedToken boundToken = new BoundAndPossiblyResolvedToken(bindingContext.Select(token), oneTimeBinding, negated, format);
            _boundTokens.add(boundToken);

            m.appendReplacement(finalString, _isExpression ? String.format("var%d", tokenIndex) : "%s");
            ++tokenIndex;
        }
        m.appendTail(finalString);
        _formatString = finalString.toString();

        // De-escape any escaped braces...
        //
        _formatString = _formatString.replace("{{", "{");
        _formatString = _formatString.replace("}}", "}");
    }

    private static JValue randomJavascriptObjectToJValue(Context context, Object randomJavascriptObject)
    {
        JValue returnValue;

        if (randomJavascriptObject == null)
        {
            returnValue = new JValue((String) randomJavascriptObject);
        }
        else if (randomJavascriptObject instanceof Boolean)
        {
            returnValue = new JValue((Boolean) randomJavascriptObject);
        }
        else if (randomJavascriptObject instanceof Double)
        {
            // Rhino said it was a double. BUT IS IT REALLY?!? Such a liar.

            double doubleValue = (double) randomJavascriptObject;
            if (Math.floor(doubleValue) == doubleValue)
            {
                returnValue = new JValue((int) (double) (Double) randomJavascriptObject);
            }
            else
            {
                returnValue = new JValue((Double) randomJavascriptObject);
            }
        }
        else if (randomJavascriptObject instanceof Integer)
        {
            returnValue = new JValue((Integer) randomJavascriptObject);
        }
        else
        {
            returnValue = new JValue(context.toString(randomJavascriptObject));
        }

        return returnValue;
    }

    public JToken Expand()
    {
        if (_isExpression)
        {
            Context context = Context.getCurrentContext().enter();
            context.setOptimizationLevel(-1); // http://stackoverflow.com/questions/3859305/problems-using-rhino-on-android
            Scriptable scope = context.initStandardObjects();

            for (int tokenIndex = 0;tokenIndex < _boundTokens.size();++tokenIndex)
            {
                String name = String.format("var%d", tokenIndex);
                Object value;

                JToken token = _boundTokens.get(tokenIndex).getResolvedValue();

                switch (token.getType())
                {
                    case Boolean:
                        value = token.asBoolean();
                        break;

                    case Integer:
                    case Float:
                        value = token.asDouble();
                        break;

                    case Null:
                        value = null;
                        break;

                    default:
                        value = _boundTokens.get(tokenIndex).getResolvedValueAsString();
                        break;
                }

                ScriptableObject.putProperty(scope, name, value);
            }

            Object result = null;

            try
            {
                result = context.evaluateString(scope, _formatString, null, 0, null);
            }
            catch (EvaluatorException e)
            {
                // On iOS there are no parse exceptions, the script just resolves to "undefined" (so you literally get back
                // the string "undefined" on iOS.  Here we have some details about why your script sucked.  I guess that's fine
                // and worth passing along.
                //
                result = e.getMessage();
            }

            return new JValue(randomJavascriptObjectToJValue(context, result));
        }
        else if (_formatString.equals("%s"))
        {
            // If there is a binding containing exactly a single token, then that token may resolve to
            // a value of any type (not just string), and we want to preserve that type, so we process
            // that special case here...
            //
            BoundAndPossiblyResolvedToken token = _boundTokens.get(0);
            return token.getResolvedValue();
        }
        else
        {
            // Otherwise we replace all tokens with the string representations of the values.
            //
            Object[] resolvedTokens = new Object[_boundTokens.size()];
            for (int i = 0; i < _boundTokens.size(); i++)
            {
                resolvedTokens[i] = _boundTokens.get(i).getResolvedValueAsString();
            }

            return new JValue(String.format(_formatString, resolvedTokens));
        }
    }

    public static boolean ContainsBindingTokens(String value)
    {
        return _braceContentsRE.matcher(value).find();
    }

    public static JToken Expand(String tokenString, BindingContext bindingContext)
    {
        PropertyValue propertyValue = new PropertyValue(tokenString, bindingContext);
        return propertyValue.Expand();
    }

    public static String ExpandAsString(String tokenString, BindingContext bindingContext)
    {
        return Expand(tokenString, bindingContext).asString();
    }
}
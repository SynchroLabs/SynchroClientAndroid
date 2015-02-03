package io.synchro.client.android;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private List<BoundAndPossiblyResolvedToken> _boundTokens;

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

            m.appendReplacement(finalString, "%s");
        }
        m.appendTail(finalString);
        _formatString = finalString.toString();
    }

    public JToken Expand()
    {
        if (_formatString.equals("%s"))
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
        return _braceContentsRE.matcher(value).matches();
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
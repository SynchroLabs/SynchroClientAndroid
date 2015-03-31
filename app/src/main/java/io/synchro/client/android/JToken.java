package io.synchro.client.android;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by blake on 12/9/14.
 */
public abstract class JToken
{
    protected JTokenType type;
    protected JToken _parent;

    public JToken(JTokenType type)
    {
        this.type = type;
    }

    public static JToken parse(String json)
            throws IOException
    {
        return JsonParser.parseValue(new PushbackReader(new StringReader(json)));
    }

    public abstract String asString();
    public abstract int asInt();
    public abstract boolean asBoolean();
    public abstract double asDouble();
    public abstract JToken deepClone();

    public boolean remove()
    {
        boolean bRemoved = false;

        if (getParent() != null)
        {
            if (getParent() instanceof JObject)
            {
                bRemoved = ((JObject)getParent()).removeChild(this);
            }
            else if (getParent() instanceof JArray)
            {
                bRemoved = ((JArray) getParent()).removeChild(this);
            }

            if (bRemoved && (getParent() != null))
            {
                // Parent should handle nulling parent when this when item removed...
                throw new IllegalStateException("Item was removed, but parent was not cleared");
            }
        }

        return bRemoved;
    }

    public JTokenType getType()
    {
        return type;
    }

    public String toJson()
            throws IOException
    {
        StringWriter writer = new StringWriter();

        JsonWriter.writeValue(writer, this);
        return writer.toString();
    }

    private static Pattern pathRegex = Pattern.compile("\\[(\\d+)\\]");

    public JToken selectToken(String path, boolean errorWhenNoMatch)
    {
        try
        {
            Matcher matcher = pathRegex.matcher(path);
            String[] pathElements = matcher.replaceAll(".$1").split("\\.");
            JToken currentToken = this;
            for (String element : pathElements)
            {
                if (currentToken instanceof JArray)
                {
                    currentToken = ((JArray) currentToken).get(Integer.parseInt(element));
                }
                else if (currentToken instanceof JObject)
                {
                    currentToken = ((JObject) currentToken).get(element);
                }
                else
                {
                    // If you try to go into anything other than an object or array looking for a
                    // child element, you are barking up the wrong tree...
                    //
                    throw new IllegalArgumentException(
                            "The provided path did not resolve to a token"
                    );
                }
            }

            return currentToken;
        }
        catch (Exception e)
        {
            if (errorWhenNoMatch)
            {
                throw e;
            }
        }

        return null;
    }

    public JToken getParent()
    {
        return _parent;
    }

    public void setParent(JToken parent)
    {
        _parent = parent;
    }

    public String getPath()
    {
        boolean useDotNotation = false;
        String path = "";

        JToken parent = getParent();
        if (parent != null)
        {
            path += parent.getPath();

            if (parent instanceof JObject)
            {
                JObject parentObject = (JObject) parent;
                if (path.length() > 0)
                {
                    path += ".";
                }
                path += parentObject.keyForValue(this);
            }
            else if (parent instanceof JArray)
            {
                JArray parentArray = (JArray) parent;
                int pos = parentArray.indexOf(this);
                if (useDotNotation)
                {
                    if (path.length() > 0)
                    {
                        path += ".";
                    }
                    path += Integer.toString(pos);
                }
                else
                {
                    path += "[" + Integer.toString(pos) + "]";
                }
            }
        }

        return path;
    }

    public static JToken updateTokenValue(JToken currentToken, JToken newToken)
    {
        if (currentToken != newToken)
        {
            if ((currentToken instanceof JValue) && (newToken instanceof JValue))
            {
                // If the current token and the new token are both primitive values, then we just do a
                // value assignment...
                //
                ((JValue)currentToken).copyFrom(((JValue)newToken));
            }
            else
            {
                // Otherwise we have to replace the current token with the new token in the current token's parent...
                //
                if (currentToken.replace(newToken))
                {
                    return newToken; // Token change
                }
            }
        }
        return null; // Value-only change, or no change
    }

    public boolean replace(JToken token)
    {
        boolean bReplaced = false;

        if ((_parent != null) && (token != this))
        {
            // Find ourself in our parent, and replace...
            //
            if (_parent instanceof JObject)
            {
                JObject parentObject = (JObject) _parent;
                parentObject.replace(this, token);
                bReplaced = true;
            }
            else if (_parent instanceof JArray)
            {
                JArray parentArray = (JArray) _parent;
                parentArray.replace(this, token);
                bReplaced = true;
            }

            if (bReplaced && (_parent != null))
            {
                // Parent should handle nulling parent when this when item removed...
                throw new IllegalStateException("Item was replaced, but parent was not cleared");
            }
        }

        return bReplaced;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof JToken && type == ((JToken) obj).type;
    }
}

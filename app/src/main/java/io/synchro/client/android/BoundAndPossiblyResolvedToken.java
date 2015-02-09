package io.synchro.client.android;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by blake on 1/19/15.
 */
// PropertyValue objects maintain a list of things that provide values to the expanded output.  Some of
// things things are binding contexts that will be evalutated each time the underlying value changes (one-way
// bindings), but some of them will be resolved based on the initial view model contents at the time of
// creation (one-time bindings).  This object accomodates both states and provides a convenient way to determine
// which type of binding it is, and to extract the resolved/expanded value without needing to know which type
// of binding it is.
//
public class BoundAndPossiblyResolvedToken
{
    public static final String TAG = BoundAndPossiblyResolvedToken.class.getSimpleName();

    BindingContext _bindingContext;
    JToken _resolvedValue;

    // OK - The way negation is handled here is pretty crude.  The idea is that in the future we will support
    // complex value converters, perhaps even functions which themselves have more than one token as parameters.
    // So a more generalized concept of a value converter (delegate) passed in here from the parser and used
    // to produce the resolved value would be better.
    //
    boolean _negated;

    // Here is the list of .NET number format specifiers: http://msdn.microsoft.com/en-us/library/dwhawy9k(v=vs.110).aspx
    //
    // In pratice I think D, E, F, N, P, and X are what we support.
    //
    String _formatSpec; // If present, this is the .NET format specifier (whatever came after the colon)

    public BoundAndPossiblyResolvedToken(BindingContext bindingContext, boolean oneTime, boolean negated, String formatSpec)
    {
        _bindingContext = bindingContext;
        _negated = negated;
        _formatSpec = formatSpec;

        if (oneTime)
        {
            // Since we're potentially storing this over time and don't want any underlying view model changes
            // to impact this value, we need to clone it.
            //
            _resolvedValue = _bindingContext.GetValue().deepClone();
            if (_negated)
            {
                _resolvedValue = new JValue(!TokenConverter.ToBoolean(_resolvedValue, null));
            }
        }
    }

    public BindingContext getBindingContext()
    {
        return _bindingContext;
    }

    public boolean getResolved()
    {
        return _resolvedValue != null;
    }

    public JToken getResolvedValue()
    {
        if (_resolvedValue != null)
        {
            return _resolvedValue;
        }
        else
        {
            JToken resolvedValue = _bindingContext.GetValue();
            if (_negated)
            {
                resolvedValue = new JValue(!TokenConverter.ToBoolean(resolvedValue, null));
            }
            return resolvedValue;
        }
    }

    public String getResolvedValueAsString()
    {
        if (_formatSpec != null)
        {
            Double numericValue = TokenConverter.ToDouble(this.getResolvedValue(), null);
            if (numericValue != null)
            {
                char formatSpecifier = _formatSpec.charAt(0);
                switch (formatSpecifier)
                {
                    case 'C': // Currency
                    case 'c':
                    {
                        Log.e(TAG, "Currency formatting not supported");
                    }
                    break;

                    case 'G': // General
                    case 'g':
                    {
                        Log.e(TAG, "General formatting not supported");
                    }
                    break;

                    case 'R': // Round-trip
                    case 'r':
                    {
                        Log.e(TAG, "Round-trip formatting not supported");
                    }
                    break;

                    case 'D': // Decimal (int)
                    case 'd':
                    {
                        StringBuffer formatString = new StringBuffer();
                        String precisionSpec = _formatSpec.substring(1);
                        if (precisionSpec.length() > 0)
                        {
                            for (int counter = 0;counter < Integer.parseInt(precisionSpec);++counter)
                            {
                                formatString.append('0');
                            }
                        }
                        DecimalFormat decimalFormat = new DecimalFormat(formatString.toString());
                        return decimalFormat.format(numericValue);
//                        return String.Format("{0:" + _formatSpec + "}", (int)numericValue);
                    }

                    case 'X': // Hex (uint)
                    case 'x':
                    {
//                        return String.Format("{0:" + _formatSpec + "}", (uint)(int)numericValue);
                        return null;
                    }

                    case 'E': // Exponential
                    case 'e':
                    case 'F': // Fixed-point
                    case 'f':
                    {
//                        return String.Format("{0:" + _formatSpec + "}", numericValue);
                        return null;
                    }
                    case 'N': // Number
                    case 'n':
                    {
                        NumberFormat percentFormat = NumberFormat.getNumberInstance();
                        String precisionSpec = _formatSpec.substring(1);
                        if (precisionSpec.length() > 0)
                        {
                            percentFormat.setMinimumFractionDigits(Integer.parseInt(precisionSpec));
                        }
                        else
                        {
                            // The C# default is 2 digits of percent precision apparently
                            percentFormat.setMinimumFractionDigits(2);
                        }
                        return percentFormat.format(numericValue);
//                        return String.Format("{0:" + _formatSpec + "}", numericValue);
                    }
                    case 'P': // Percent
                    case 'p':
                    {
                        NumberFormat percentFormat = NumberFormat.getPercentInstance();
                        String precisionSpec = _formatSpec.substring(1);
                        if (precisionSpec.length() > 0)
                        {
                            percentFormat.setMinimumFractionDigits(Integer.parseInt(precisionSpec));
                        }
                        else
                        {
                            // The C# default is 2 digits of percent precision apparently
                            percentFormat.setMinimumFractionDigits(2);
                        }
                        return percentFormat.format(numericValue);
//                        return String.Format("{0:" + _formatSpec + "}", numericValue);
                    }
                }
            }
        }

        return TokenConverter.ToString(this.getResolvedValue(), null);
    }
}

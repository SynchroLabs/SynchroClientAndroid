package io.synchro.client.android;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by blake on 3/1/15.
 */
public class ControlWrapper
{
    public static final String TAG = ControlWrapper.class.getSimpleName();

    public static enum ListSelectionMode
    {
        None,
        Single,
        Multiple
    }

    StateManager   _stateManager;
    ViewModel      _viewModel;
    BindingContext _bindingContext;

    HashMap<String, CommandInstance> _commands         = new HashMap<>();
    HashMap<String, ValueBinding>    _valueBindings    = new HashMap<>();
    ArrayList<PropertyBinding>       _propertyBindings = new ArrayList<>();
    ArrayList<ControlWrapper>             _childControls    = new ArrayList<>();

    protected boolean _isVisualElement = true;
    public boolean isVisualElement()
    {
        return _isVisualElement;
    }

    public ControlWrapper(
            StateManager stateManager, ViewModel viewModel, BindingContext bindingContext)
    {
        _stateManager = stateManager;
        _viewModel = viewModel;
        _bindingContext = bindingContext;
    }

    public ControlWrapper(ControlWrapper parent, BindingContext bindingContext)
    {
        _stateManager = parent.getStateManager();
        _viewModel = parent.getViewModel();
        _bindingContext = bindingContext;
    }

    public StateManager getStateManager()
    {
        return _stateManager;
    }
    public ViewModel getViewModel()
    {
        return _viewModel;
    }

    public BindingContext getBindingContext()
    {
        return _bindingContext;
    }

    public List<ControlWrapper> getChildControls()
    {
        return _childControls;
    }

    protected void SetCommand(String attribute, CommandInstance command)
    {
        _commands.put(attribute, command);
    }

    public CommandInstance GetCommand(CommandName commandName)
    {
        if (_commands.containsKey(commandName.getAttribute()))
        {
            return _commands.get(commandName.getAttribute());
        }
        return null;
    }

    protected void SetValueBinding(String attribute, ValueBinding valueBinding)
    {
        _valueBindings.put(attribute, valueBinding);
    }

    public ValueBinding GetValueBinding(String attribute)
    {
        if (_valueBindings.containsKey(attribute))
        {
            return _valueBindings.get(attribute);
        }
        return null;
    }

    // Given min and max range limiters, either of which may be undefined (double.NaN), and a target value,
    // determine the range-limited value.
    //
    // !!! Use this for min/max height/width, as needed...
    //
    public static double getRangeLimitedValue(double value, double min, double max)
    {
        double result = value;

        if (!Double.isNaN(min) && (min > result))
        {
            // There is a min value and it's greater than the current value...
            result = min;
        }

        if (!Double.isNaN(max) && (max < result))
        {
            // There is a max value, and it's less than the current value...
            result = max;
        }

        return result;
    }

    //
    // Value conversion helpers
    //

    public static int GetStarCount(String starString)
    {
        int starCnt = 0;
        if ((starString != null) && (starString.endsWith("*")))
        {
            starCnt = 1;
            String valueString = starString.replace("*","");
            if (valueString.length() > 0)
            {
                starCnt = Integer.parseInt(valueString);
            }
        }

        return starCnt;
    }

    // Basic token conversions
    //

    public String ToString(JToken token, String defaultValue)
    {
        return TokenConverter.ToString(token, defaultValue);
    }

    public boolean ToBoolean(JToken token, Boolean defaultValue)
    {
        return TokenConverter.ToBoolean(token, defaultValue);
    }

    public double ToDouble(JToken value, double defaultValue)
    {
        return TokenConverter.ToDouble(value, defaultValue).floatValue();
    }

    // Conversion functions to go from Maaas units or typographic points to device units
    //

    public double ToDeviceUnits(Double value)
    {
        return getStateManager().getDeviceMetrics().MaaasUnitsToDeviceUnits(value);
    }

    public double ToDeviceUnits(JToken value)
    {
        return ToDeviceUnits(ToDouble(value, 0));
    }

    public double ToDeviceUnitsFromTypographicPoints(JToken value)
    {
        return ToDeviceUnits(getStateManager().getDeviceMetrics().TypographicPointsToMaaasUnits(
                                     ToDouble(value, 0)
                                                                                               ));
    }

    public ListSelectionMode ToListSelectionMode(JToken value, ListSelectionMode defaultSelectionMode)
    {
        ListSelectionMode selectionMode = defaultSelectionMode;
        String selectionModeValue = ToString(value, "");
        switch (selectionModeValue)
        {
            case "None":
                selectionMode = ListSelectionMode.None;
                break;
            case "Single":
                selectionMode = ListSelectionMode.Single;
                break;
            case "Multiple":
                selectionMode = ListSelectionMode.Multiple;
                break;
        }
        return selectionMode;
    }

    // Silverlight colors
    //
    // http://msdn.microsoft.com/en-us/library/system.windows.media.colors(v=vs.110).aspx
    //
    public static HashMap<String, Integer> ColorNames;

    static
    {
        ColorNames = new HashMap<>();
        ColorNames.put("AliceBlue", 0xFFF0F8FF);
        ColorNames.put("AntiqueWhite", 0xFFFAEBD7);
        ColorNames.put("Aqua", 0xFF00FFFF);
        ColorNames.put("Aquamarine", 0xFF7FFFD4);
        ColorNames.put("Azure", 0xFFF0FFFF);
        ColorNames.put("Beige", 0xFFF5F5DC);
        ColorNames.put("Bisque", 0xFFFFE4C4);
        ColorNames.put("Black", 0xFF000000);
        ColorNames.put("BlanchedAlmond", 0xFFFFEBCD);
        ColorNames.put("Blue", 0xFF0000FF);
        ColorNames.put("BlueViolet", 0xFF8A2BE2);
        ColorNames.put("Brown", 0xFFA52A2A);
        ColorNames.put("BurlyWood", 0xFFDEB887);
        ColorNames.put("CadetBlue", 0xFF5F9EA0);
        ColorNames.put("Chartreuse", 0xFF7FFF00);
        ColorNames.put("Chocolate", 0xFFD2691E);
        ColorNames.put("Coral", 0xFFFF7F50);
        ColorNames.put("CornflowerBlue", 0xFF6495ED);
        ColorNames.put("Cornsilk", 0xFFFFF8DC);
        ColorNames.put("Crimson", 0xFFDC143C);
        ColorNames.put("Cyan", 0xFF00FFFF);
        ColorNames.put("DarkBlue", 0xFF00008B);
        ColorNames.put("DarkCyan", 0xFF008B8B);
        ColorNames.put("DarkGoldenrod", 0xFFB8860B);
        ColorNames.put("DarkGray", 0xFFA9A9A9);
        ColorNames.put("DarkGreen", 0xFF006400);
        ColorNames.put("DarkKhaki", 0xFFBDB76B);
        ColorNames.put("DarkMagenta", 0xFF8B008B);
        ColorNames.put("DarkOliveGreen", 0xFF556B2F);
        ColorNames.put("DarkOrange", 0xFFFF8C00);
        ColorNames.put("DarkOrchid", 0xFF9932CC);
        ColorNames.put("DarkRed", 0xFF8B0000);
        ColorNames.put("DarkSalmon", 0xFFE9967A);
        ColorNames.put("DarkSeaGreen", 0xFF8FBC8F);
        ColorNames.put("DarkSlateBlue", 0xFF483D8B);
        ColorNames.put("DarkSlateGray", 0xFF2F4F4F);
        ColorNames.put("DarkTurquoise", 0xFF00CED1);
        ColorNames.put("DarkViolet", 0xFF9400D3);
        ColorNames.put("DeepPink", 0xFFFF1493);
        ColorNames.put("DeepSkyBlue", 0xFF00BFFF);
        ColorNames.put("DimGray", 0xFF696969);
        ColorNames.put("DodgerBlue", 0xFF1E90FF);
        ColorNames.put("Firebrick", 0xFFB22222);
        ColorNames.put("FloralWhite", 0xFFFFFAF0);
        ColorNames.put("ForestGreen", 0xFF228B22);
        ColorNames.put("Fuchsia", 0xFFFF00FF);
        ColorNames.put("Gainsboro", 0xFFDCDCDC);
        ColorNames.put("GhostWhite", 0xFFF8F8FF);
        ColorNames.put("Gold", 0xFFFFD700);
        ColorNames.put("Goldenrod", 0xFFDAA520);
        ColorNames.put("Gray", 0xFF808080);
        ColorNames.put("Green", 0xFF008000);
        ColorNames.put("GreenYellow", 0xFFADFF2F);
        ColorNames.put("Honeydew", 0xFFF0FFF0);
        ColorNames.put("HotPink", 0xFFFF69B4);
        ColorNames.put("IndianRed", 0xFFCD5C5C);
        ColorNames.put("Indigo", 0xFF4B0082);
        ColorNames.put("Ivory", 0xFFFFFFF0);
        ColorNames.put("Khaki", 0xFFF0E68C);
        ColorNames.put("Lavender", 0xFFE6E6FA);
        ColorNames.put("LavenderBlush", 0xFFFFF0F5);
        ColorNames.put("LawnGreen", 0xFF7CFC00);
        ColorNames.put("LemonChiffon", 0xFFFFFACD);
        ColorNames.put("LightBlue", 0xFFADD8E6);
        ColorNames.put("LightCoral", 0xFFF08080);
        ColorNames.put("LightCyan", 0xFFE0FFFF);
        ColorNames.put("LightGoldenrodYellow", 0xFFFAFAD2);
        ColorNames.put("LightGray", 0xFFD3D3D3);
        ColorNames.put("LightGreen", 0xFF90EE90);
        ColorNames.put("LightPink", 0xFFFFB6C1);
        ColorNames.put("LightSalmon", 0xFFFFA07A);
        ColorNames.put("LightSeaGreen", 0xFF20B2AA);
        ColorNames.put("LightSkyBlue", 0xFF87CEFA);
        ColorNames.put("LightSlateGray", 0xFF778899);
        ColorNames.put("LightSteelBlue", 0xFFB0C4DE);
        ColorNames.put("LightYellow", 0xFFFFFFE0);
        ColorNames.put("Lime", 0xFF00FF00);
        ColorNames.put("LimeGreen", 0xFF32CD32);
        ColorNames.put("Linen", 0xFFFAF0E6);
        ColorNames.put("Magenta", 0xFFFF00FF);
        ColorNames.put("Maroon", 0xFF800000);
        ColorNames.put("MediumAquamarine", 0xFF66CDAA);
        ColorNames.put("MediumBlue", 0xFF0000CD);
        ColorNames.put("MediumOrchid", 0xFFBA55D3);
        ColorNames.put("MediumPurple", 0xFF9370DB);
        ColorNames.put("MediumSeaGreen", 0xFF3CB371);
        ColorNames.put("MediumSlateBlue", 0xFF7B68EE);
        ColorNames.put("MediumSpringGreen", 0xFF00FA9A);
        ColorNames.put("MediumTurquoise", 0xFF48D1CC);
        ColorNames.put("MediumVioletRed", 0xFFC71585);
        ColorNames.put("MidnightBlue", 0xFF191970);
        ColorNames.put("MintCream", 0xFFF5FFFA);
        ColorNames.put("MistyRose", 0xFFFFE4E1);
        ColorNames.put("Moccasin", 0xFFFFE4B5);
        ColorNames.put("NavajoWhite", 0xFFFFDEAD);
        ColorNames.put("Navy", 0xFF000080);
        ColorNames.put("OldLace", 0xFFFDF5E6);
        ColorNames.put("Olive", 0xFF808000);
        ColorNames.put("OliveDrab", 0xFF6B8E23);
        ColorNames.put("Orange", 0xFFFFA500);
        ColorNames.put("OrangeRed", 0xFFFF4500);
        ColorNames.put("Orchid", 0xFFDA70D6);
        ColorNames.put("PaleGoldenrod", 0xFFEEE8AA);
        ColorNames.put("PaleGreen", 0xFF98FB98);
        ColorNames.put("PaleTurquoise", 0xFFAFEEEE);
        ColorNames.put("PaleVioletRed", 0xFFDB7093);
        ColorNames.put("PapayaWhip", 0xFFFFEFD5);
        ColorNames.put("PeachPuff", 0xFFFFDAB9);
        ColorNames.put("Peru", 0xFFCD853F);
        ColorNames.put("Pink", 0xFFFFC0CB);
        ColorNames.put("Plum", 0xFFDDA0DD);
        ColorNames.put("PowderBlue", 0xFFB0E0E6);
        ColorNames.put("Purple", 0xFF800080);
        ColorNames.put("Red", 0xFFFF0000);
        ColorNames.put("RosyBrown", 0xFFBC8F8F);
        ColorNames.put("RoyalBlue", 0xFF4169E1);
        ColorNames.put("SaddleBrown", 0xFF8B4513);
        ColorNames.put("Salmon", 0xFFFA8072);
        ColorNames.put("SandyBrown", 0xFFF4A460);
        ColorNames.put("SeaGreen", 0xFF2E8B57);
        ColorNames.put("SeaShell", 0xFFFFF5EE);
        ColorNames.put("Sienna", 0xFFA0522D);
        ColorNames.put("Silver", 0xFFC0C0C0);
        ColorNames.put("SkyBlue", 0xFF87CEEB);
        ColorNames.put("SlateBlue", 0xFF6A5ACD);
        ColorNames.put("SlateGray", 0xFF708090);
        ColorNames.put("Snow", 0xFFFFFAFA);
        ColorNames.put("SpringGreen", 0xFF00FF7F);
        ColorNames.put("SteelBlue", 0xFF4682B4);
        ColorNames.put("Tan", 0xFFD2B48C);
        ColorNames.put("Teal", 0xFF008080);
        ColorNames.put("Thistle", 0xFFD8BFD8);
        ColorNames.put("Tomato", 0xFFFF6347);
        ColorNames.put("Transparent", 0x00FFFFFF);
        ColorNames.put("Turquoise", 0xFF40E0D0);
        ColorNames.put("Violet", 0xFFEE82EE);
        ColorNames.put("Wheat", 0xFFF5DEB3);
        ColorNames.put("White", 0xFFFFFFFF);
        ColorNames.put("WhiteSmoke", 0xFFF5F5F5);
        ColorNames.put("Yellow", 0xFFFFFF00);
        ColorNames.put("YellowGreen", 0xFF9ACD3);
    }

    public static class ColorARGB
    {
        byte _a;
        byte _r;
        byte _g;
        byte _b;

        public ColorARGB(byte a, byte r, byte g, byte b)
        {
            _a = a;
            _r = r;
            _g = g;
            _b = b;
        }

        public ColorARGB(int color)
        {
            _a = (byte) ((color >> 24) & 0x0FF);
            _r = (byte) ((color >> 16) & 0x0FF);
            _g = (byte) ((color >> 8) & 0x0FF);
            _b = (byte) ((color /* >> 0 */) & 0x0FF);
        }

        public byte getA()
        {
            return _a;
        }
        public byte getR()
        {
            return _r;
        }
        public byte getG()
        {
            return _g;
        }
        public byte getB()
        {
            return _b;
        }
        public int getARGB() { return (_a << 24) | (_r << 16) | (_g << 8) | (_b); }
    }

    public static ColorARGB getColor(String colorValue)
    {
        // I use parseLong below because if you have the high bit set on a four byte parseInt, it
        // will fail to parse. No, I don't know why. Presumably because you put in a number that
        // overflows a positive value.

        if (colorValue.startsWith("#"))
        {
            if (colorValue.length() == 7) // #RRGGBB = 7
            {
                // Alpha is 100% if only RGB, so patch it in
                return new ColorARGB((int) (Long.parseLong(colorValue.substring(1), 16) | 0xFF000000));
            }
            else
            {
                return new ColorARGB((int) Long.parseLong(colorValue.substring(1), 16));
            }
        }
        else
        {
            return new ColorARGB(ColorNames.get(colorValue));
        }
    }

    public enum FontFaceType
    {
        FONT_DEFAULT,
        FONT_SERIF,
        FONT_SANSERIF,
        FONT_MONOSPACE
    }

    public interface IFontSetter
    {
        public void SetFaceType(FontFaceType faceType);
        public void SetSize(double size);
        public void SetBold(boolean bold);
        public void SetItalic(boolean italic);
    }

    public void processFontAttribute(JObject controlSpec, final IFontSetter fontSetter)
    {
        JToken fontAttributeValue = controlSpec.get("font");
        if (fontAttributeValue instanceof JObject)
        {
            JObject fontObject = (JObject) fontAttributeValue;

            processElementProperty(fontObject.get("face"), new ISetViewValue()
                                   {
                                       @Override
                                       public void SetViewValue(JToken value)
                                       {
                                           FontFaceType faceType = FontFaceType.FONT_DEFAULT;
                                           String faceTypeString = ToString(value, "");
                                           switch (faceTypeString)
                                           {
                                               case "Serif":
                                                   faceType = FontFaceType.FONT_SERIF;
                                                   break;
                                               case "SanSerif":
                                                   faceType = FontFaceType.FONT_SANSERIF;
                                                   break;
                                               case "Monospace":
                                                   faceType = FontFaceType.FONT_MONOSPACE;
                                                   break;
                                           }
                                           fontSetter.SetFaceType(faceType);
                                       }
                                   });
            processElementProperty(fontObject.get("size"), new ISetViewValue()
                                   {
                                       @Override
                                       public void SetViewValue(JToken value)
                                       {
                                           if (value != null)
                                           {
                                               fontSetter.SetSize(ToDeviceUnitsFromTypographicPoints(value));
                                           }
                                       }
                                   });
            processElementProperty(fontObject.get("bold"), new ISetViewValue()
                                   {
                                       @Override
                                       public void SetViewValue(JToken value)
                                       {
                                           fontSetter.SetBold(ToBoolean(value, false));
                                       }
                                   });
            processElementProperty(fontObject.get("italic"), new ISetViewValue()
                                   {
                                       @Override
                                       public void SetViewValue(JToken value)
                                       {
                                           fontSetter.SetItalic(ToBoolean(value, false));
                                       }
                                   });
        }

        // This will handle the simple style "fontsize" attribute (this is the most common font attribute and is
        // very often used by itself, so we'll support this alternate syntax).
        //
        processElementProperty(controlSpec.get("fontsize"), new ISetViewValue()
                               {
                                   @Override
                                   public void SetViewValue(JToken value)
                                   {
                                       fontSetter.SetSize(ToDeviceUnitsFromTypographicPoints(value));
                                   }
                               });
    }

    // Process a value binding on an element.  If a value is supplied, a value binding to that binding context will be created.
    //
    protected boolean processElementBoundValue(String attributeName, String value, IGetViewValue getValue, ISetViewValue setValue)
    {
        if (value != null)
        {
            BindingContext valueBindingContext = this.getBindingContext().Select(value);
            ValueBinding binding = getViewModel().CreateAndRegisterValueBinding(
                    valueBindingContext, getValue, setValue
                                                                               );
            SetValueBinding(attributeName, binding);

            // Immediate content update during configuration.
            binding.UpdateViewFromViewModel();

            return true;
        }

        return false;
    }

    // Process an element property, which can contain a plain value, a property binding token string, or no value at all,
    // in which case any optionally supplied defaultValue will be used.  This call *may* result in a property binding to
    // the element property, or it may not.
    //
    // This is "public" because there are cases when a parent element needs to process properties on its children after creation.
    //
    public void processElementProperty(JToken value, ISetViewValue setValue)
    {
        if (value == null)
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
        else if ((value.getType() == JTokenType.String) && PropertyValue.ContainsBindingTokens(value.asString()))
        {
            // If value contains a binding, create a Binding and add it to metadata
            PropertyBinding binding = getViewModel().CreateAndRegisterPropertyBinding(
                    this.getBindingContext(), value.asString(), setValue
                                                                                     );
            _propertyBindings.add(binding);

            // Immediate content update during configuration.
            binding.UpdateViewFromViewModel();
        }
        else
        {
            // Otherwise, just set the property value
            setValue.SetViewValue(value);
        }
    }

    // This helper is used by control update handlers.
    //
    protected void updateValueBindingForAttribute(String attributeName)
    {
        ValueBinding binding = GetValueBinding(attributeName);
        if (binding != null)
        {
            // Update the local ViewModel from the element/control
            binding.UpdateViewModelFromView();
        }
    }

    // Process and record any commands in a binding spec
    //
    protected void ProcessCommands(JObject bindingSpec, String[] commands)
    {
        for (String command : commands)
        {
            JObject commandSpec = (JObject) bindingSpec.get(command);
            if (commandSpec != null)
            {
                // A command spec contains an attribute called "command".  All other attributes are considered parameters.
                //
                CommandInstance commandInstance = new CommandInstance(commandSpec.get("command").asString());
                for (String property : commandSpec.keySet())
                {
                    if (!property.equals("command"))
                    {
                        commandInstance.SetParameter(property, commandSpec.get(property));
                    }
                }
                SetCommand(command, commandInstance);
            }
        }
    }

    // When we remove a control, we need to unbind it and its descendants (by unregistering all bindings
    // from the view model).  This is important as often times a control is removed when the underlying
    // bound values go away, such as when an array element is removed, causing a cooresponding (bound) list
    // or list view item to be removed.
    //
    public void Unregister()
    {
        for (ValueBinding valueBinding : _valueBindings.values())
        {
            _viewModel.UnregisterValueBinding(valueBinding);
        }

        for (PropertyBinding propertyBinding : _propertyBindings)
        {
            _viewModel.UnregisterPropertyBinding(propertyBinding);
        }

        for (ControlWrapper childControl : _childControls)
        {
            childControl.Unregister();
        }
    }

    public interface ICreateControl
    {
        public void onCreateControl(BindingContext bindingContext, JObject element);
    }

    // This will create controls from a list of control specifications.  It will apply any "foreach" and "with" bindings
    // as part of the process.  It will call the supplied callback to actually create the individual controls.
    //
    public void createControls(BindingContext bindingContext, JArray controlList, ICreateControl onCreateControl)
    {
        for (int counter = 0;counter < controlList.size();++counter)
        {
            BindingContext controlBindingContext = bindingContext;
            Boolean controlCreated = false;
            JObject element = (JObject) controlList.get(counter);

            if ((element.get("binding") != null) && (element.get("binding").getType() == JTokenType.Object))
            {
                Log.d(TAG, "Found binding object");
                JObject bindingSpec = (JObject) element.get("binding");
                if (bindingSpec.get("foreach") != null)
                {
                    // First we create a BindingContext for the "foreach" path (a context to the elements to be iterated)
                    String bindingPath = bindingSpec.get("foreach").asString();
                    Log.d(TAG, String.format("Found 'foreach' binding with path: %s", bindingPath));
                    BindingContext forEachBindingContext = bindingContext.Select(bindingPath);

                    // Then we determine the bindingPath to use on each element
                    String withPath = "$data";
                    if (bindingSpec.get("with") != null)
                    {
                        // It is possible to use "foreach" and "with" together - in which case "foreach" is applied first
                        // and "with" is applied to each element in the foreach array.  This allows for path navigation
                        // both up to, and then after, the context to be iterated.
                        //
                        withPath = bindingSpec.get("with").asString();
                    }

                    // Then we get each element at the foreach binding, apply the element path, and create the controls
                    List<BindingContext> bindingContexts = forEachBindingContext.SelectEach(withPath);
                    for (BindingContext elementBindingContext : bindingContexts)
                    {
                        Log.d(TAG, String.format("foreach - creating control with binding context: %s", elementBindingContext.getBindingPath()));
                        onCreateControl.onCreateControl(elementBindingContext, element);
                    }
                    controlCreated = true;
                }
                else if (bindingSpec.get("with") != null)
                {
                    String withBindingPath = bindingSpec.get("with").asString();
                    Log.d(TAG, String.format("Found 'with' binding with path: %s", withBindingPath));
                    controlBindingContext = bindingContext.Select(withBindingPath);
                }
            }

            if (!controlCreated)
            {
                onCreateControl.onCreateControl(controlBindingContext, element);
            }
        }
    }
}
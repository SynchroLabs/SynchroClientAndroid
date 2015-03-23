package io.synchro.client.android.controls;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.synchro.client.android.*;

/**
 * Created by blake on 3/2/15.
 */
public class AndroidControlWrapper extends ControlWrapper
{
    public static final String TAG = AndroidControlWrapper.class.getSimpleName();

    protected View _control;

    public View getControl()
    {
        return _control;
    }

    protected AndroidPageView _pageView;

    public AndroidPageView getPageView()
    {
        return _pageView;
    }

    protected int _height = ViewGroup.LayoutParams.WRAP_CONTENT;
    protected int _width  = ViewGroup.LayoutParams.WRAP_CONTENT;

    public AndroidControlWrapper(
            AndroidPageView pageView, StateManager stateManager, ViewModel viewModel,
            BindingContext bindingContext, View control
                                )
    {
        super(stateManager, viewModel, bindingContext);
        _pageView = pageView;
        _control = control;
    }

    public AndroidControlWrapper(ControlWrapper parent, BindingContext bindingContext)
    {
        super(parent, bindingContext);
        _pageView = ((AndroidControlWrapper) parent).getPageView();
    }

    public void InitializeLayoutParameters()
    {
        if (_isVisualElement && (_control.getLayoutParams() == null))
        {
            _control.setLayoutParams(new ViewGroup.MarginLayoutParams(_width, _height));
        }
    }

    public void updateSize()
    {
        if (_isVisualElement)
        {
            InitializeLayoutParameters();

            // We don't want to overwrite an actual height/width value with WrapContent...
            //
            if (_width != ViewGroup.LayoutParams.WRAP_CONTENT)
            {
                _control.getLayoutParams().width = _width;
            }

            if (_height != ViewGroup.LayoutParams.WRAP_CONTENT)
            {
                _control.getLayoutParams().height = _height;
            }

            _control.requestLayout();
        }
    }

    public int getWidth()
    {
        return _width;
    }

    public void setWidth(int width)
    {
        _width = width;
        if (_width >= 0)
        {
            _control.setMinimumWidth(width);
        }
        this.updateSize();
    }

    public int getHeight()
    {
        return _height;
    }

    public void setHeight(int height)
    {
        _height = height;
        if (_height >= 0)
        {
            _control.setMinimumHeight(height);
        }
        this.updateSize();
    }

    public void AddToLinearLayout(ViewGroup layout, JObject childControlSpec)
    {
        LinearLayout.LayoutParams linearLayoutParams = null;

        if (this.getControl().getLayoutParams() instanceof LinearLayout.LayoutParams)
        {
            linearLayoutParams = (LinearLayout.LayoutParams) this.getControl().getLayoutParams();
        }

        if (linearLayoutParams == null)
        {
            // Here we are essentially "upgrading" any current LayoutParams to LinearLayout.LayoutParams (if needed)
            //
            // The LinearLayout.LayoutParams constructor is too dumb to look at the class of the LayoutParams passed in, and
            // instead requires you to bind to the correct constructor variant based on the class of the provided layout params.
            //
            if (this.getControl().getLayoutParams() instanceof ViewGroup.MarginLayoutParams)
            {
                linearLayoutParams = new LinearLayout.LayoutParams(
                        (ViewGroup.MarginLayoutParams) this.getControl().getLayoutParams()
                );
            }
            else if (this.getControl().getLayoutParams() != null)
            {
                linearLayoutParams = new LinearLayout.LayoutParams(
                        this.getControl().getLayoutParams()
                );
            }
            else
            {
                linearLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
            }

            // The control might have had gravity (h/v alignment) set before getting added to the linear layout, and if so, we
            // want to pick up those values in the new LayoutParams now...
            //
            linearLayoutParams.gravity = getVerticalAlignment() | getHorizontalAlignment();
        }

        int heightStarCount = GetStarCount((childControlSpec.get("height") != null) ? childControlSpec.get("height").asString() : null);
        int widthStarCount = GetStarCount((childControlSpec.get("width") != null) ? childControlSpec.get("width").asString() : null);

        int orientation = LinearLayout.HORIZONTAL;
        if (layout instanceof LinearLayout)
        {
            orientation = ((LinearLayout) layout).getOrientation();
        }
        else if (layout instanceof FlowLayout)
        {
            orientation = ((FlowLayout) layout).getOrientation();
        }

        if (orientation == LinearLayout.HORIZONTAL)
        {
            if (heightStarCount > 0)
            {
                linearLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            }

            if (widthStarCount > 0)
            {
                linearLayoutParams.width = 0;
                linearLayoutParams.weight = widthStarCount;
            }
        }
        else // Orientation.Vertical
        {
            if (widthStarCount > 0)
            {
                linearLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }

            if (heightStarCount > 0)
            {
                linearLayoutParams.height = 0;
                linearLayoutParams.weight = heightStarCount;
            }
        }

        this.getControl().setLayoutParams(linearLayoutParams);

        layout.addView(this.getControl());
    }

    protected void updateGravity()
    {
        if (this.getControl().getLayoutParams() != null)
        {
            LinearLayout.LayoutParams linearLayoutParams = (LinearLayout.LayoutParams) this
                    .getControl().getLayoutParams();
            if (linearLayoutParams != null)
            {
                linearLayoutParams.gravity = _horizontalAlignment | _verticalAlignment;
                _control.requestLayout();
            }
        }
    }

    protected int _verticalAlignment = Gravity.TOP;

    public int getVerticalAlignment()
    {
        return _verticalAlignment;
    }

    public void setVerticalAlignment(int verticalAlignment)
    {
        _verticalAlignment = verticalAlignment;
        updateGravity();
    }

    protected int _horizontalAlignment = Gravity.LEFT;

    public int getHorizontalAlignment()
    {
        return _horizontalAlignment;
    }

    public void setHorizontalAlignment(int horizontalAlignment)
    {
        _horizontalAlignment = horizontalAlignment;
        updateGravity();
    }

    // No idea why this doesn't work.
//    @android.widget.LinearLayout.OrientationMode
    public int ToOrientation(JToken value, int defaultOrientation)
    {
        int orientation = defaultOrientation;
        String orientationValue = ToString(value, "");
        if (orientationValue.equals("Horizontal"))
        {
            orientation = LinearLayout.HORIZONTAL;
        }
        else if (orientationValue.equals("Vertical"))
        {
            orientation = LinearLayout.VERTICAL;
        }
        return orientation;
    }

    public int ToHorizontalAlignment(JToken value, int defaultAlignment)
    {
        int alignment = defaultAlignment;
        String alignmentValue = ToString(value, "");
        switch (alignmentValue)
        {
            case "Left":
                alignment = Gravity.LEFT;
                break;
            case "Right":
                alignment = Gravity.RIGHT;
                break;
            case "Center":
                alignment = Gravity.CENTER_HORIZONTAL;
                break;
            case "Stretch":
                alignment = Gravity.FILL_HORIZONTAL;
                break;
        }
        return alignment;
    }

    public int ToVerticalAlignment(JToken value, int defaultAlignment)
    {
        int alignment = defaultAlignment;
        String alignmentValue = ToString(value, "");
        switch (alignmentValue)
        {
            case "Top":
                alignment = Gravity.TOP;
                break;
            case "Bottom":
                alignment = Gravity.BOTTOM;
                break;
            case "Center":
                alignment = Gravity.CENTER_VERTICAL;
                break;
            case "Stretch":
                alignment = Gravity.FILL_VERTICAL;
                break;
        }
        return alignment;
    }

    public int ToColor(JToken value)
    {
        ColorARGB color = ControlWrapper.getColor(ToString(value, ""));
        if (color != null)
        {
            return color.getARGB();
        }
        else
        {
            return Color.TRANSPARENT;
        }
    }

    protected void applyFrameworkElementDefaults(View element)
    {
        // !!! This could be a little more thourough ;)
    }

    public double ToAndroidDpFromTypographicPoints(JToken value)
    {
        // A typographic point is 1/72 of an inch.  Convert to logical pixel value for device.
        //
        double typographicPoints = ToDouble(value, 0.0);
        return typographicPoints * 160f / 72f;
    }

    public static abstract class ThicknessSetter
    {
        public void SetThickness(int thickness)
        {
            this.SetThicknessTop(thickness);
            this.SetThicknessLeft(thickness);
            this.SetThicknessBottom(thickness);
            this.SetThicknessRight(thickness);
        }

        public abstract void SetThicknessLeft(int thickness);

        public abstract void SetThicknessTop(int thickness);

        public abstract void SetThicknessRight(int thickness);

        public abstract void SetThicknessBottom(int thickness);
    }

    public class MarginThicknessSetter extends ThicknessSetter
    {
        protected View _control;

        public MarginThicknessSetter(AndroidControlWrapper controlWrapper)
        {
            controlWrapper.InitializeLayoutParameters();
            _control = controlWrapper.getControl();
        }

        @Override
        public void SetThicknessLeft(int thickness)
        {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) _control
                    .getLayoutParams();
            if (layoutParams != null)
            {
                layoutParams.leftMargin = thickness;
                _control.setLayoutParams(layoutParams); // Required to trigger real-time update
            }
        }

        @Override
        public void SetThicknessTop(int thickness)
        {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) _control
                    .getLayoutParams();
            if (layoutParams != null)
            {
                layoutParams.topMargin = thickness;
                _control.setLayoutParams(layoutParams); // Required to trigger real-time update
            }
        }

        @Override
        public void SetThicknessRight(int thickness)
        {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) _control
                    .getLayoutParams();
            if (layoutParams != null)
            {
                layoutParams.rightMargin = thickness;
                _control.setLayoutParams(layoutParams); // Required to trigger real-time update
            }
        }

        @Override
        public void SetThicknessBottom(int thickness)
        {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) _control
                    .getLayoutParams();
            if (layoutParams != null)
            {
                layoutParams.bottomMargin = thickness;
                _control.setLayoutParams(layoutParams); // Required to trigger real-time update
            }
        }
    }

    public static class PaddingThicknessSetter extends ThicknessSetter
    {
        protected View _control;

        public PaddingThicknessSetter(View control)
        {
            _control = control;
        }

        public void SetThickness(int thickness)
        {
            _control.setPadding(thickness, thickness, thickness, thickness);
        }

        public void SetThicknessLeft(int thickness)
        {
            _control.setPadding(thickness, _control.getPaddingTop(), _control.getPaddingRight(), _control.getPaddingBottom());
        }

        public void SetThicknessTop(int thickness)
        {
            _control.setPadding(_control.getPaddingLeft(), thickness, _control.getPaddingRight(), _control.getPaddingBottom());
        }

        public void SetThicknessRight(int thickness)
        {
            _control.setPadding(_control.getPaddingLeft(), _control.getPaddingTop(), thickness, _control.getPaddingBottom());
        }

        public void SetThicknessBottom(int thickness)
        {
            _control.setPadding(_control.getPaddingLeft(), _control.getPaddingTop(), _control.getPaddingRight(), thickness);
        }
    }
    public void processThicknessProperty(
            JToken thicknessAttributeValue, final ThicknessSetter thicknessSetter
                                        )
    {
        if (thicknessAttributeValue instanceof JValue)
        {
            if (thicknessAttributeValue == null)
            {
                thicknessSetter.SetThickness(0);
            }
            else
            {
                processElementProperty(
                        thicknessAttributeValue, new ISetViewValue()
                        {
                            @Override
                            public void SetViewValue(JToken value)
                            {
                                thicknessSetter.SetThickness((int) ToDeviceUnits(value));
                            }
                        }
                                      );
            }
        }
        else if (thicknessAttributeValue instanceof JObject)
        {
            JObject marginObject = (JObject) thicknessAttributeValue;

            if (marginObject.get("left") == null)
            {
                thicknessSetter.SetThicknessLeft(0);
            }
            else
            {
                processElementProperty(
                        marginObject.get("left"), new ISetViewValue()
                        {
                            @Override
                            public void SetViewValue(JToken value)
                            {
                                thicknessSetter.SetThicknessLeft((int) ToDeviceUnits(value));
                            }
                        }
                                      );
            }

            if (marginObject.get("top") == null)
            {
                thicknessSetter.SetThicknessTop(0);
            }
            else
            {
                processElementProperty(
                        marginObject.get("top"), new ISetViewValue()
                        {
                            @Override
                            public void SetViewValue(JToken value)
                            {
                                thicknessSetter.SetThicknessTop(
                                        (int) ToDeviceUnits(value)
                                                               );
                            }
                        }
                                      );
            }

            if (marginObject.get("right") == null)
            {
                thicknessSetter.SetThicknessRight(0);
            }
            else
            {
                processElementProperty(
                        marginObject.get("right"), new ISetViewValue()
                        {
                            @Override
                            public void SetViewValue(JToken value)
                            {
                                thicknessSetter.SetThicknessRight(
                                        (int) ToDeviceUnits(value)
                                                                 );
                            }
                        }
                                      );
            }

            if (marginObject.get("bottom") == null)
            {
                thicknessSetter.SetThicknessBottom(0);
            }
            else
            {
                processElementProperty(
                        marginObject.get("bottom"), new ISetViewValue()
                        {
                            @Override
                            public void SetViewValue(JToken value)
                            {
                                thicknessSetter.SetThicknessBottom(
                                        (int) ToDeviceUnits(value)
                                                                  );
                            }
                        }
                                      );
            }
        }
    }

    protected void setHeight(JToken value)
    {
        String heightString = ToString(value, "");
        if (heightString.indexOf('*') >= 0)
        {
            Log.d(TAG, String.format("Got star height string: %s", value.asString()));
            this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        }
        else
        {
            this.setHeight((int) ToDeviceUnits(value));
        }
    }

    protected void setWidth(JToken value)
    {
        String widthString = ToString(value, "");
        if (widthString.indexOf('*') >= 0)
        {
            Log.d(TAG, String.format("Got star width string: %s", value));
            this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        }
        else
        {
            this.setWidth((int) ToDeviceUnits(value));
        }
    }

    public class AndroidFontSetter implements IFontSetter
    {
        TextView _control;
        boolean  _bold;
        boolean  _italic;

        public AndroidFontSetter(View control)
        {
            if (control instanceof TextView)
            {
                _control = (TextView) control;
            }
        }

        protected int getTypefaceStyle(boolean bold, boolean italic)
        {
            if (bold && italic)
            {
                return Typeface.BOLD_ITALIC;
            }
            else if (bold)
            {
                return Typeface.BOLD;
            }
            else if (italic)
            {
                return Typeface.ITALIC;
            }
            else
            {
                return Typeface.NORMAL;
            }
        }

        // The SetTypeface method used below takes an "extra" stlye param, which is documented as:
        //
        //     "Sets the typeface and style in which the text should be displayed, and turns on the fake bold and italic bits
        //      in the Paint if the Typeface that you provided does not have all the bits in the style that you specified."
        //
        // When using this method with such a font (like the default system monospace font), this works fine unless you are
        // trying to set the style from any non-normal value to normal, in which case it fails to restore it to normal.
        // Not sure if this is an Android TextView bug or a Xamarin bug, but would guess the former.  Setting the typeface
        // to a face that does support the style bits, then setting it to the proper typeface with the extra style param
        // seems to work (and doesn't produce any visible flickering or other artifacts).  So we're going with that for now.
        //
        protected void setStyledTypeface(final Typeface tf)
        {
            ((Activity) _control.getContext()).runOnUiThread(
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            int tfStyle = getTypefaceStyle(_bold, _italic);
                            Typeface newtf = Typeface.create(tf, tfStyle);
                            if (tfStyle == Typeface.NORMAL)
                            {
                                _control.setTypeface(
                                        Typeface.DEFAULT
                                                    ); // This is the hackaround described above
                            }
                            _control.setTypeface(newtf, tfStyle);
                        }
                    }
                                                            );
        }

        @Override
        public void SetFaceType(FontFaceType faceType)
        {
            if (_control != null)
            {
                Typeface tf = null;

                switch (faceType)
                {
                    case FONT_DEFAULT:
                        tf = Typeface.DEFAULT;
                        break;
                    case FONT_SANSERIF:
                        tf = Typeface.SANS_SERIF;
                        break;
                    case FONT_SERIF:
                        tf = Typeface.SERIF;
                        break;
                    case FONT_MONOSPACE:
                        tf = Typeface.MONOSPACE;
                        break;
                }

                if (tf != null)
                {
                    this.setStyledTypeface(tf);
                }
            }
        }

        @Override
        public void SetSize(double size)
        {
            if (_control != null)
            {
                // !!! These seem to be equivalent, but produce fonts that are larger than on other platforms (the glyph span is the specified height,
                //     with the total box being a fair amount larger, as opposed to most platforms where the box is the specified height).
                //
                _control.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size);
            }
        }

        protected boolean isBold(int tfStyle)
        {
            return ((tfStyle == Typeface.BOLD) || (tfStyle == Typeface.BOLD_ITALIC));
        }

        protected boolean isItalic(int tfStyle)
        {
            return ((tfStyle == Typeface.ITALIC) || (tfStyle == Typeface.BOLD_ITALIC));
        }

        @Override
        public void SetBold(boolean bold)
        {
            _bold = bold;
            if (_control != null)
            {
                Typeface tf = _control.getTypeface();
                this.setStyledTypeface(tf);
            }
        }

        @Override
        public void SetItalic(boolean italic)
        {
            _italic = italic;
            if (_control != null)
            {
                Typeface tf = _control.getTypeface();
                this.setStyledTypeface(tf);
            }
        }
    }

    protected void processCommonFrameworkElementProperies(JObject controlSpec)
    {
        Log.d(TAG, "Processing framework element properties");

        // !!! This could be a little more thourough ;)

        //processElementProperty((string)controlSpec["name"], value => this.Control.Name = ToString(value));

        processElementProperty(
                controlSpec.get("horizontalAlignment"), new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        setHorizontalAlignment(ToHorizontalAlignment(value, Gravity.LEFT));
                    }
                }
                              );

        processElementProperty(
                controlSpec.get("verticalAlignment"), new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        setVerticalAlignment(ToVerticalAlignment(value, Gravity.TOP));
                    }
                }
                              );

        processElementProperty(
                controlSpec.get("height"), new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        setHeight(value);
                    }
                }
                              );

        processElementProperty(
                controlSpec.get("width"), new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        setWidth(value);
                    }
                }
                              );

        updateSize(); // To init the layout params

        processElementProperty(
                controlSpec.get("minheight"), new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        getControl().setMinimumHeight((int) ToDeviceUnits(value));
                    }
                }
                              );

        processElementProperty(
                controlSpec.get("minwidth"), new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        getControl().setMinimumWidth((int) ToDeviceUnits(value));
                    }
                }
                              );

        //processElementProperty(controlSpec["maxheight"], value => this.Control.MaxHeight = ToDeviceUnits(value));
        //processElementProperty(controlSpec["maxwidth"], value => this.Control.MaxWidth = ToDeviceUnits(value));

        processElementProperty(
                controlSpec.get("opacity"), new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        getControl().setAlpha((float) ToDouble(value, 0.0));
                    }
                }
                              );

        processElementProperty(
                controlSpec.get("visibility"), new AndroidUiThreadSetViewValue((Activity) getControl().getContext())
                {
                    @Override
                    public void UiThreadSetViewValue(JToken value)
                    {
                        getControl().setVisibility(
                                ToBoolean(value, false) ? View.VISIBLE : View.GONE
                                                  );
                    }
                }
                              );

        processElementProperty(
                controlSpec.get("enabled"), new AndroidUiThreadSetViewValue((Activity) getControl().getContext())
                {
                    @Override
                    public void UiThreadSetViewValue(JToken value)
                    {
                        getControl().setEnabled(ToBoolean(value, false));
                    }
                }
                              );


        processThicknessProperty(controlSpec.get("margin"), new MarginThicknessSetter(this));
        // Since some controls have to treat padding differently, the padding attribute is handled by the individual control classes

        if (!(this instanceof AndroidBorderWrapper) && !(this instanceof AndroidRectangleWrapper))
        {
            processElementProperty(
                    controlSpec.get("background"), new ISetViewValue()
                    {
                        @Override
                        public void SetViewValue(JToken value)
                        {
                            getControl().setBackgroundColor(ToColor(value));
                        }
                    }
                                  );
        }

        processFontAttribute(controlSpec, new AndroidFontSetter(this.getControl()));

        final TextView textView = (this.getControl() instanceof TextView) ? (TextView) this
                .getControl() : null;
        if (textView != null)
        {
            processElementProperty(
                    controlSpec.get("foreground"), new AndroidUiThreadSetViewValue((Activity) textView.getContext())
                    {
                        @Override
                        public void UiThreadSetViewValue(JToken value)
                        {
                            textView.setTextColor(ToColor(value));
                        }
                    }
                                  );
        }

        // These elements are very common among derived classes, so we'll do some runtime reflection...
        //processElementPropertyIfPresent((string)controlSpec["foreground"], "Foreground", value => ToBrush(value));
    }

    public AndroidControlWrapper getChildControlWrapper(View control)
    {
        // Find the child control wrapper whose control matches the supplied value...
        for (ControlWrapper child : this.getChildControls())
        {
            if (((AndroidControlWrapper) child).getControl() == control)
            {
                return (AndroidControlWrapper) child;
            }
        }

        return null;
    }

    public static AndroidControlWrapper WrapControl(
            AndroidPageView pageView, StateManager stateManager, ViewModel viewModel,
            BindingContext bindingContext, View control
                                                   )
    {
        return new AndroidControlWrapper(
                pageView, stateManager, viewModel, bindingContext, control
        );
    }

    public static AndroidControlWrapper CreateControl(
            ControlWrapper parent, BindingContext bindingContext, JObject controlSpec
                                                     )
    {
        AndroidControlWrapper controlWrapper = null;

        Log.d(TAG, String.format("Creating control %s", controlSpec.get("control").asString()));
        switch (controlSpec.get("control").asString())
        {
//            case "actionBar.item":
//                controlWrapper = new AndroidActionWrapper(parent, bindingContext, controlSpec);
//                break;
//            case "actionBar.toggle":
//                controlWrapper = new AndroidActionToggleWrapper(
//                        parent, bindingContext, controlSpec
//                );
//                break;
            case "border":
                controlWrapper = new AndroidBorderWrapper(parent, bindingContext, controlSpec);
                break;
            case "button":
                controlWrapper = new AndroidButtonWrapper(parent, bindingContext, controlSpec);
                break;
//            case "canvas":
//                controlWrapper = new AndroidCanvasWrapper(parent, bindingContext, controlSpec);
//                break;
            case "edit":
                controlWrapper = new AndroidTextBoxWrapper(parent, bindingContext, controlSpec);
                break;
//            case "gridview":
//                controlWrapper = new AndroidGridViewWrapper(parent, bindingContext, controlSpec);
//                break;
            case "image":
                controlWrapper = new AndroidImageWrapper(parent, bindingContext, controlSpec);
                break;
//            case "listbox":
//                controlWrapper = new AndroidListBoxWrapper(parent, bindingContext, controlSpec);
//                break;
//            case "listview":
//                controlWrapper = new AndroidListViewWrapper(parent, bindingContext, controlSpec);
//                break;
//            case "location":
//                controlWrapper = new AndroidLocationWrapper(parent, bindingContext, controlSpec);
//                break;
            case "password":
                controlWrapper = new AndroidTextBoxWrapper(parent, bindingContext, controlSpec);
                break;
//            case "picker":
//                controlWrapper = new AndroidPickerWrapper(parent, bindingContext, controlSpec);
//                break;
            case "progressbar":
                controlWrapper = new AndroidSliderWrapper(parent, bindingContext, controlSpec);
                break;
            case "progressring":
                controlWrapper = new AndroidProgressRingWrapper(
                        parent, bindingContext, controlSpec
                );
                break;
            case "rectangle":
                controlWrapper = new AndroidRectangleWrapper(parent, bindingContext, controlSpec);
                break;
//            case "scrollview":
//                controlWrapper = new AndroidScrollWrapper(parent, bindingContext, controlSpec);
//                break;
            case "slider":
                controlWrapper = new AndroidSliderWrapper(parent, bindingContext, controlSpec);
                break;
            case "stackpanel":
                controlWrapper = new AndroidStackPanelWrapper(parent, bindingContext, controlSpec);
                break;
            case "text":
                controlWrapper = new AndroidTextBlockWrapper(parent, bindingContext, controlSpec);
                break;
//            case "toggle":
//                controlWrapper = new AndroidToggleSwitchWrapper(
//                        parent, bindingContext, controlSpec
//                );
//                break;
//            case "webview":
//                controlWrapper = new AndroidWebViewWrapper(parent, bindingContext, controlSpec);
//                break;
//            case "wrappanel":
//                controlWrapper = new AndroidWrapPanelWrapper(parent, bindingContext, controlSpec);
//                break;
        }

        if (controlWrapper != null)
        {
            if (controlWrapper.getControl() != null)
            {
                controlWrapper.processCommonFrameworkElementProperies(controlSpec);
            }
            parent.getChildControls().add(controlWrapper);
            if (controlWrapper.getControl() != null)
            {
                controlWrapper.getControl().setTag(controlWrapper);
            }
        }

        return controlWrapper;
    }

    public interface IAndroidCreateControl
    {
        void onCreateControl(JObject controlSpec, AndroidControlWrapper controlWrapper);
    }

    public void createControls(JArray controlList, final IAndroidCreateControl OnCreateControl)
    {
        final ControlWrapper parent = this;

        super.createControls(
                this.getBindingContext(), controlList, new ICreateControl()
                {
                    @Override
                    public void onCreateControl(
                            BindingContext bindingContext, JObject element
                                               )
                    {
                        AndroidControlWrapper controlWrapper = CreateControl(parent, bindingContext, element);
                        if (controlWrapper == null)
                        {
                            Log.w(TAG, String.format("WARNING: Unable to create control of type: %s", element.get("control").asString()));
                        }
                        else if (OnCreateControl != null)
                        {
                            if (controlWrapper.isVisualElement())
                            {
                                OnCreateControl.onCreateControl(element, controlWrapper);
                            }
                        }
                    }
                });
    }
}

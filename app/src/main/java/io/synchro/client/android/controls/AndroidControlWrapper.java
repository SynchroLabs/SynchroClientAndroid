package io.synchro.client.android.controls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

import io.synchro.client.android.*;
import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JValue;

/**
 * Created by blake on 3/2/15.
 */
public class AndroidControlWrapper extends ControlWrapper
{
    public static final String TAG = AndroidControlWrapper.class.getSimpleName();

    public static final Double DEFAULT_MARGIN = 5.0;

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

    protected boolean _widthSpecified = false;
    protected boolean _heightSpecified = false;

    protected int _defaultTextColor;
    protected Drawable _defaultBackground;

    public class SynchroButton extends Button
    {
        int _textColor = Color.WHITE;
        int _disabledColor = Color.GRAY;

        public SynchroButton(Context context, AttributeSet attrs, int defStyleAttr)
        {
            super(context, attrs, defStyleAttr);
            _textColor = this.getCurrentTextColor();
            int[] disabledState = { -android.R.attr.state_enabled };
            _disabledColor = this.getTextColors().getColorForState(disabledState, _disabledColor);

            // We always want our icon and/or text grouped and centered.  We have to left align the text to
            // the (possible) left drawable in order to then be able to center them in our onDraw() below.
            //
            setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        }

        public void updateIconAndTextColor()
        {
            int color = this.isEnabled() ? _textColor : _disabledColor;

            Drawable drawables[] = this.getCompoundDrawables();
            for (Drawable drawable : drawables)
            {
                if (drawable != null)
                {
                    drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            }

            super.setTextColor(color);
        }

        @Override
        public void setEnabled(boolean enabled)
        {
            super.setEnabled(enabled);
            this.updateIconAndTextColor();
        }

        @Override
        public void setTextColor(int color)
        {
            // We assume this means "set the normal/enabled text color"
            super.setTextColor(color);
            _textColor = color;
            this.updateIconAndTextColor();
        }

        public void setIcon(Drawable icon)
        {
            icon.mutate(); // We're going to have some fun with this guy later
            super.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            this.updateIconAndTextColor();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            // We want the icon and/or text grouped together and centered as a group.

            float buttonContentWidth = getWidth() - getPaddingLeft() - getPaddingRight();

            // In later versions of Android, an "all caps" transform is applied to buttons.  We need to get
            // the transformed text in order to measure it.
            //
            TransformationMethod method = getTransformationMethod();
            String buttonText = ((method != null) ? method.getTransformation(getText(), this) : getText()).toString();
            float textWidth = getPaint().measureText(buttonText);

            Drawable[] drawables = getCompoundDrawables();
            Drawable drawableLeft = drawables[0];
            int drawableWidth = (drawableLeft != null) ? drawableLeft.getIntrinsicWidth() : 0;
            int drawablePadding = ((textWidth > 0) && (drawableLeft != null)) ? getCompoundDrawablePadding() : 0;
            float bodyWidth = textWidth + drawableWidth + drawablePadding;
            canvas.translate((buttonContentWidth - bodyWidth) / 2, 0);

            super.onDraw(canvas);
        }
    }

    // Icon helpers
    //
    static public String getResourceNameFromIcon(String icon, String color, int sizeDp)
    {
        return "ic_" + icon + "_" + color + "_" + sizeDp + "dp";
    }

    static public String getResourceNameFromIcon(String icon, String color)
    {
        return getResourceNameFromIcon(icon, color, 36);
    }

    static public String getResourceNameFromIcon(String icon, int sizeDp)
    {
        return getResourceNameFromIcon(icon, "white", sizeDp);
    }

    static public String getResourceNameFromIcon(String icon)
    {
        // Backward compat for Civics - convert the old ic_ icons to the new names...
        if (icon == "ic_action_important")
        {
            icon = "star";
        }
        else if (icon == "ic_action_not_important")
        {
            icon = "star_border";
        }
        else if (icon.startsWith("ic_"))
        {
            // The user knows *exactly* what they want, so give it to them...
            return icon;
        }
        return getResourceNameFromIcon(icon, "white", 36);
    }

    static public Drawable getIconDrawable(Context context, String iconName)
    {
        Log.d(TAG, String.format("getIconDrawable(\"%s\")", iconName));

        // http://stackoverflow.com/questions/4427608/android-getting-resource-id-from-string
        //
        Field idField = null;
        try
        {
            idField = R.drawable.class.getDeclaredField(iconName);
        }
        catch (NoSuchFieldException e)
        {
            Log.wtf(TAG, e);
        }

        if (idField != null)
        {
            int iconResourceId = 0;

            try
            {
                iconResourceId = idField.getInt(idField);
            }
            catch (IllegalAccessException e)
            {
                Log.wtf(TAG, e);
            }
            if (iconResourceId > 0)
            {
                return context.getDrawable(iconResourceId);
            }
        }

        // If we didn't find the icon, return a placeholder (the "no" sign)
        //
        Log.w(TAG, String.format("getIconDrawable - icon not found: %s, using default", iconName));
        return context.getDrawable(R.drawable.ic_do_not_disturb_white_36dp);
    }
    //
    // End icon helpers

    public AndroidControlWrapper(
            AndroidPageView pageView, StateManager stateManager, ViewModel viewModel,
            BindingContext bindingContext, View control
                                )
    {
        super(stateManager, viewModel, bindingContext);
        _pageView = pageView;
        _control = control;
    }

    public AndroidControlWrapper(ControlWrapper parent, BindingContext bindingContext, JObject controlSpec)
    {
        super(parent, bindingContext, controlSpec);
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
        _widthSpecified = true;
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
        _heightSpecified = true;
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
            // The noinspection is because gravity has some strong typing and get...Alignment()
            // return just regular int. The right fix is probably to fix get...Alignment return
            // values to indicate they are strongly typed and not just crazy ints.
            //
            //noinspection ResourceType
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

    protected void updateGravityOnUiThread()
    {
        ((Activity) this.getControl().getContext()).runOnUiThread(new Runnable()
                                                                  {
                                                                      @Override
                                                                      public void run()
                                                                      {
                                                                          if (AndroidControlWrapper.this.getControl().getLayoutParams() instanceof LinearLayout.LayoutParams)
                                                                          {
                                                                              LinearLayout.LayoutParams linearLayoutParams = (LinearLayout.LayoutParams) AndroidControlWrapper.this
                                                                                      .getControl().getLayoutParams();
                                                                              if (linearLayoutParams != null)
                                                                              {
                                                                                  linearLayoutParams.gravity = _horizontalAlignment | _verticalAlignment;
                                                                                  _control.requestLayout();
                                                                              }
                                                                          }

                                                                      }
                                                                  });
    }

    protected int _verticalAlignment = Gravity.TOP;

    public int getVerticalAlignment()
    {
        return _verticalAlignment;
    }

    public void setVerticalAlignment(int verticalAlignment)
    {
        _verticalAlignment = verticalAlignment;
        updateGravityOnUiThread();
    }

    protected int _horizontalAlignment = Gravity.LEFT;

    public int getHorizontalAlignment()
    {
        return _horizontalAlignment;
    }

    public void setHorizontalAlignment(int horizontalAlignment)
    {
        _horizontalAlignment = horizontalAlignment;
        updateGravityOnUiThread();
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

    public Integer ToColor(JToken value, Integer defaultColor)
    {
        ColorARGB color = ControlWrapper.getColor(ToString(value, ""));
        if (color != null)
        {
            return color.getARGB();
        }
        else
        {
            return defaultColor;
        }
    }

    protected void applyFrameworkElementDefaults(View element)
    {
        this.applyFrameworkElementDefaults(element, true);
    }

    protected void applyFrameworkElementDefaults(View element, Boolean applyMargins)
    {
        if (applyMargins)
        {
            MarginThicknessSetter marginSetter = new MarginThicknessSetter(this);
            marginSetter.SetThicknessLeft((int)ToDeviceUnits(DEFAULT_MARGIN));
            marginSetter.SetThicknessTop((int)ToDeviceUnits(DEFAULT_MARGIN));
            marginSetter.SetThicknessRight((int)ToDeviceUnits(DEFAULT_MARGIN));
            marginSetter.SetThicknessBottom((int)ToDeviceUnits(DEFAULT_MARGIN));
        }
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
            JObject controlSpec, String attributeName, final ThicknessSetter thicknessSetter
                                        )
    {
        processElementProperty(controlSpec, attributeName + ".left", new ISetViewValue()
                               {
                                   @Override
                                   public void SetViewValue(JToken value)
                                   {
                                       thicknessSetter.SetThicknessLeft((int) ToDeviceUnits(value));
                                   }
                               });
        processElementProperty(controlSpec, attributeName + ".top", new ISetViewValue()
                               {
                                   @Override
                                   public void SetViewValue(JToken value)
                                   {
                                       thicknessSetter.SetThicknessTop((int) ToDeviceUnits(value));
                                   }
                               });
        processElementProperty(controlSpec, attributeName + ".right", new ISetViewValue()
                               {
                                   @Override
                                   public void SetViewValue(JToken value)
                                   {
                                       thicknessSetter.SetThicknessRight((int) ToDeviceUnits(value));
                                   }
                               });
        processElementProperty(controlSpec, attributeName + ".bottom", new ISetViewValue()
                               {
                                   @Override
                                   public void SetViewValue(JToken value)
                                   {
                                       thicknessSetter.SetThicknessBottom((int) ToDeviceUnits(value));
                                   }
                               });
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
                controlSpec, "horizontalAlignment", new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        setHorizontalAlignment(ToHorizontalAlignment(value, Gravity.LEFT));
                    }
                }
                              );

        processElementProperty(
                controlSpec, "verticalAlignment", new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        setVerticalAlignment(ToVerticalAlignment(value, Gravity.TOP));
                    }
                }
                              );

        processElementProperty(
                controlSpec, "height", new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        setHeight(value);
                    }
                }
                              );

        processElementProperty(
                controlSpec, "width", new ISetViewValue()
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
                controlSpec, "minheight", new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        getControl().setMinimumHeight((int) ToDeviceUnits(value));
                    }
                }
                              );

        processElementProperty(
                controlSpec, "minwidth", new ISetViewValue()
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
                controlSpec, "opacity", new ISetViewValue()
                {
                    @Override
                    public void SetViewValue(JToken value)
                    {
                        getControl().setAlpha((float) ToDouble(value, 0.0));
                    }
                }
                              );

        processElementProperty(
                controlSpec, "visibility", new AndroidUiThreadSetViewValue((Activity) getControl().getContext())
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
                controlSpec, "enabled", new AndroidUiThreadSetViewValue((Activity) getControl().getContext())
                {
                    @Override
                    public void UiThreadSetViewValue(JToken value)
                    {
                        getControl().setEnabled(ToBoolean(value, false));
                    }
                }
                              );


        processThicknessProperty(controlSpec, "margin", new MarginThicknessSetter(this));
        // Since some controls have to treat padding differently, the padding attribute is handled by the individual control classes

        if (!(this instanceof AndroidBorderWrapper) && !(this instanceof AndroidRectangleWrapper) &&
            !(this instanceof AndroidCanvasWrapper))
        {
            _defaultBackground = getControl().getBackground();
            processElementProperty(
                    controlSpec, "background", new ISetViewValue()
                    {
                        @Override
                        public void SetViewValue(JToken value)
                        {
                            Integer newColor = ToColor(value, null);
                            if (newColor != null)
                            {
                                getControl().setBackgroundColor(newColor);
                            }
                            else
                            {
                                getControl().setBackground(_defaultBackground);
                            }
                        }
                    }
                                  );
        }

        processFontAttribute(controlSpec, new AndroidFontSetter(this.getControl()));

        final TextView textView = (this.getControl() instanceof TextView) ? (TextView) this
                .getControl() : null;
        if (textView != null)
        {
            _defaultTextColor = textView.getCurrentTextColor();
            processElementProperty(
                    controlSpec, "color", "foreground", new AndroidUiThreadSetViewValue((Activity) textView.getContext())
                    {
                        @Override
                        public void UiThreadSetViewValue(JToken value)
                        {
                            Integer color = ToColor(value, null);
                            textView.setTextColor((color == null) ? _defaultTextColor : color);
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
            case "actionBar.item":
                controlWrapper = new AndroidActionWrapper(parent, bindingContext, controlSpec);
                break;
            case "actionBar.toggle":
                controlWrapper = new AndroidActionToggleWrapper(parent, bindingContext, controlSpec);
                break;
            case "border":
                controlWrapper = new AndroidBorderWrapper(parent, bindingContext, controlSpec);
                break;
            case "button":
                controlWrapper = new AndroidButtonWrapper(parent, bindingContext, controlSpec);
                break;
            case "canvas":
                controlWrapper = new AndroidCanvasWrapper(parent, bindingContext, controlSpec);
                break;
            case "edit":
                controlWrapper = new AndroidTextBoxWrapper(parent, bindingContext, controlSpec);
                break;
//            case "gridview":
//                controlWrapper = new AndroidGridViewWrapper(parent, bindingContext, controlSpec);
//                break;
            case "image":
                controlWrapper = new AndroidImageWrapper(parent, bindingContext, controlSpec);
                break;
            case "listbox":
                controlWrapper = new AndroidListBoxWrapper(parent, bindingContext, controlSpec);
                break;
            case "listview":
                controlWrapper = new AndroidListViewWrapper(parent, bindingContext, controlSpec);
                break;
            case "location":
                controlWrapper = new AndroidLocationWrapper(parent, bindingContext, controlSpec);
                break;
            case "password":
                controlWrapper = new AndroidTextBoxWrapper(parent, bindingContext, controlSpec);
                break;
            case "picker":
                controlWrapper = new AndroidPickerWrapper(parent, bindingContext, controlSpec);
                break;
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
            case "scrollview":
                controlWrapper = new AndroidScrollWrapper(parent, bindingContext, controlSpec);
                break;
            case "slider":
                controlWrapper = new AndroidSliderWrapper(parent, bindingContext, controlSpec);
                break;
            case "stackpanel":
                controlWrapper = new AndroidStackPanelWrapper(parent, bindingContext, controlSpec);
                break;
            case "text":
                controlWrapper = new AndroidTextBlockWrapper(parent, bindingContext, controlSpec);
                break;
            case "toggle":
                controlWrapper = new AndroidToggleSwitchWrapper(
                        parent, bindingContext, controlSpec
                );
                break;
            case "togglebutton":
                controlWrapper = new AndroidToggleButtonWrapper(
                        parent, bindingContext, controlSpec
                );
                break;
            case "webview":
                controlWrapper = new AndroidWebViewWrapper(parent, bindingContext, controlSpec);
                break;
            case "wrappanel":
                controlWrapper = new AndroidWrapPanelWrapper(parent, bindingContext, controlSpec);
                break;
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

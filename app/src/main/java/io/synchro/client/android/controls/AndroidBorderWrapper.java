package io.synchro.client.android.controls;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import io.synchro.client.android.*;
import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

/**
 * Created by blake on 3/2/15.
 */
public class AndroidBorderWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidBorderWrapper.class.getSimpleName();

    public class BorderPaddingThicknessSetter extends ThicknessSetter
    {
        protected View _control;
        protected int _paddingLeft = 0;
        protected int _paddingTop = 0;
        protected int _paddingRight = 0;
        protected int _paddingBottom = 0;
        protected int _inset = 0;

        public BorderPaddingThicknessSetter(View control)
        {
            _control = control;
            _paddingLeft = _control.getPaddingLeft();
            _paddingTop = _control.getPaddingTop();
            _paddingRight = _control.getPaddingRight();
            _paddingBottom = _control.getPaddingBottom();
        }

        public int getInset()
        {
            return _inset;
        }
        public void setInset(int value)
        {
            _inset = value;
            updatePadding();
        }

        protected void updatePadding()
        {
            _control.setPadding(_paddingLeft + _inset, _paddingTop + _inset, _paddingRight + _inset, _paddingBottom + _inset);
        }

        @Override
        public void SetThicknessLeft(int thickness)
        {
            _paddingLeft = thickness;
            updatePadding();
        }

        @Override
        public void SetThicknessTop(int thickness)
        {
            _paddingTop = thickness;
            updatePadding();
        }

        @Override
        public void SetThicknessRight(int thickness)
        {
            _paddingRight = thickness;
            updatePadding();
        }

        @Override
        public void SetThicknessBottom(int thickness)
        {
            _paddingBottom = thickness;
            updatePadding();
        }
    }

    LinearLayout _layout;
    int _padding   = 0;
    int _thickness = 0;

    AndroidSynchroRectDrawable _rect = new AndroidSynchroRectDrawable();

    protected void updateLayoutPadding()
    {
        _layout.setPadding(
                _padding + _thickness,
                _padding + _thickness,
                _padding + _thickness,
                _padding + _thickness
                          );
    }

    public AndroidBorderWrapper(
            ControlWrapper parent, BindingContext bindingContext, JObject controlSpec
                               )
    {
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, "Creating border element");

        _layout = new LinearLayout(((AndroidControlWrapper) parent).getControl().getContext());
        this._control = _layout;

        _layout.setBackgroundDrawable(_rect);
        _layout.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
                                          {
                                              @Override
                                              public void onLayoutChange(
                                                      View v, int left, int top, int right,
                                                      int bottom, int oldLeft, int oldTop,
                                                      int oldRight,
                                                      int oldBottom
                                                                        )
                                              {
                                                  _rect.setBounds(
                                                          0, 0, _layout.getWidth(),
                                                          _layout.getHeight()
                                                                 );
                                              }
                                          });

        applyFrameworkElementDefaults(_layout);

        // If border thickness or padding change, need to record value and update layout padding...
        //
        final BorderPaddingThicknessSetter borderThicknessSetter = new BorderPaddingThicknessSetter(this.getControl());
        processElementProperty(controlSpec, "border", new AndroidUiThreadSetViewValue((Activity) this.getControl().getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.SetStrokeColor(ToColor(value));
                                   }
                               });
        processElementProperty(controlSpec, "borderThickness", new AndroidUiThreadSetViewValue((Activity) this.getControl().getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _thickness = (int)ToDeviceUnits(value);
                                       _rect.SetStrokeWidth(_thickness);
                                       borderThicknessSetter.setInset(_thickness);
                                   }
                               });
        processElementProperty(controlSpec, "cornerRadius", new AndroidUiThreadSetViewValue((Activity) this.getControl().getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.setCornerRadius((float) ToDeviceUnits(value));
                                   }
                               });
        processElementProperty(controlSpec, "background", new AndroidUiThreadSetViewValue((Activity) this.getControl().getContext())
                               {
                                   @Override
                                   protected void UiThreadSetViewValue(JToken value)
                                   {
                                       _rect.SetFillColor(ToColor(value));
                                   }
                               });
        processThicknessProperty(controlSpec, "padding", borderThicknessSetter);

        // In theory we're only jamming one child in here (so it doesn't really matter whether the linear layout is
        // horizontal or vertical).
        //
        _layout.setOrientation(LinearLayout.VERTICAL);

        // Since the orientation is vertical, the item gravity will control the horizontal alignment of the item.
        // For vertical alignment, we need to set the gravity of the container itself (to specify how the container
        // should align the totality of its contents, which in this case is just the one item).  We default to centered,
        // but bind the child's verticalAlignment to the container gravity when the child is processed below.
        //
        _layout.setGravity(Gravity.CENTER_VERTICAL);

        if (controlSpec.get("contents") != null)
        {
            createControls((JArray)controlSpec.get("contents"),new IAndroidCreateControl()
                           {
                               @Override
                               public void onCreateControl(
                                       JObject controlSpec, AndroidControlWrapper controlWrapper
                                                          )
                               {
                                   controlWrapper.AddToLinearLayout(_layout, controlSpec);
                                   processElementProperty(
                                           controlSpec, "verticalAlignment",
                                           new AndroidUiThreadSetViewValue((Activity) AndroidBorderWrapper.this.getControl().getContext())
                                           {
                                               @Override
                                               protected void UiThreadSetViewValue(
                                                       JToken value
                                                                                  )
                                               {
                                                   _layout.setGravity(ToVerticalAlignment(value, Gravity.TOP));
                                               }
                                           });
                               }
                           });
        }
    }
}

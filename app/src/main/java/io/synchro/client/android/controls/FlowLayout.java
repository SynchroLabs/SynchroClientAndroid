package io.synchro.client.android.controls;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import io.synchro.client.android.*;
import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JToken;

/**
 * Created by blake on 3/2/15.
 */
// Here is an implementation of Android FlowLayout:
//
//    https://github.com/ApmeM/android-flowlayout
//
// This one is by Romain Guy on Google Code:
//
//    https://code.google.com/p/devoxx-schedule/source/browse/devoxx-android-client/src/net/peterkuterna/android/apps/devoxxsched/ui/widget/FlowLayout.java?name=422c381967&r=422c38196733ba3c54eb44418160e248ee1aea86
//
// The code below came from:
//
//    http://slodge.blogspot.no/2013/01/an-mono-for-android-wrappanelflowlayout.html
//
// And that code was based on: http://forums.xamarin.com/discussion/comment/156#Comment_156:
//

public class FlowLayout extends ViewGroup
{
    public static final String TAG = FlowLayout.class.getSimpleName();

    public FlowLayout(Context context)
    {
        super(context);
    }

    public boolean DebugDraw = false;

    protected int _orientation = LinearLayout.HORIZONTAL;

    public int getOrientation()
    {
        return _orientation;
    }

    public void setOrientation(int value)
    {
        _orientation = value;
        invalidate();
        requestLayout();
    }

    protected int _itemHeight = 0;

    public int getItemHeight()
    {
        return _itemHeight;
    }

    public void setItemHeight(int value)
    {
        _itemHeight = value;
        this.requestLayout();
    }

    protected int _itemWidth = 0;

    public int getItemWidth()
    {
        return _itemWidth;
    }

    public void setItemWidth(int value)
    {
        _itemWidth = value;
        this.requestLayout();
    }

    // The line elements have been position in the dimension in which the are running, but we can't position
    // them in the other dimension until we fill the line (and know the "thickness" of the line, as determined
    // by the size of the thickest element on the line).  So this function goes back and positions in the
    // dimension opposite of the running dimension based on the line thickness, the element margins, and the
    // element alignment (gravity).
    //
    protected void positionLineElements(
            List<View> lineContents, int linePosition, int lineThickness
                                       )
    {
        for (View lineMember : lineContents)
        {
            WrapLayoutParams lp = (WrapLayoutParams) lineMember.getLayoutParams();

            if (_orientation == LinearLayout.HORIZONTAL)
            {
                if ((lp.gravity & Gravity.TOP) == Gravity.TOP)
                {
                    lp.Y = getPaddingTop() + linePosition + lp.topMargin;
                }
                else if ((lp.gravity & Gravity.BOTTOM) == Gravity.BOTTOM)
                {
                    lp.Y = getPaddingTop() + linePosition + lineThickness - (lineMember
                            .getMeasuredHeight() + lp.bottomMargin);
                }
                else // Center - default
                {
                    lp.Y = getPaddingTop() + linePosition + (lineThickness - lineMember
                            .getMeasuredHeight()) / 2;
                }
            }
            else
            {
                if ((lp.gravity & Gravity.LEFT) == Gravity.LEFT)
                {
                    lp.X = getPaddingLeft() + linePosition + lp.leftMargin;
                }
                else if ((lp.gravity & Gravity.RIGHT) == Gravity.RIGHT)
                {
                    lp.X = getPaddingLeft() + linePosition + lineThickness - (lineMember
                            .getMeasuredWidth() + lp.rightMargin);
                }
                else // Center - default
                {
                    lp.X = getPaddingLeft() + linePosition + (lineThickness - lineMember
                            .getMeasuredWidth()) / 2;
                }
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int sizeWidth = MeasureSpec
                .getSize(widthMeasureSpec) - getPaddingRight() - getPaddingLeft();
        int sizeHeight = MeasureSpec
                .getSize(heightMeasureSpec) - getPaddingRight() - getPaddingLeft();

        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int size;
        int mode;

        if (_orientation == LinearLayout.HORIZONTAL)
        {
            size = sizeWidth;
            mode = modeWidth;
        }
        else
        {
            size = sizeHeight;
            mode = modeHeight;
        }

        int lineThickness = 0;
        int lineLength = 0;

        int linePosition = 0;

        int controlMaxLength = 0;
        int controlMaxThickness = 0;

        List<View> lineContents = new ArrayList<>();

        int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE)
            {
                continue;
            }

            WrapLayoutParams lp = (WrapLayoutParams) child.getLayoutParams();

            child.measure(
                    getChildMeasureSpec(
                            widthMeasureSpec,
                            getPaddingLeft() + getPaddingTop() + lp.leftMargin + lp.rightMargin,
                            lp.width
                                       ),
                    getChildMeasureSpec(
                            heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin,
                            lp.height
                                       )
                         );

            int childTotalWidth = _itemWidth != 0 ? _itemWidth : child
                    .getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childTotalHeight = _itemHeight != 0 ? _itemHeight : child
                    .getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            int childLength;    // Running dimension
            int childThickness; // Opposite dimension

            if (_orientation == LinearLayout.HORIZONTAL)
            {
                childLength = childTotalWidth;
                childThickness = childTotalHeight;
            }
            else
            {
                childLength = childTotalHeight;
                childThickness = childTotalWidth;
            }

            if ((mode != MeasureSpec.UNSPECIFIED) && ((lineLength + childLength) > size))
            {
                // New line...
                //
                this.positionLineElements(lineContents, linePosition, lineThickness);
                lineContents.clear();

                linePosition = linePosition + lineThickness;
                lineThickness = childThickness;
                lineLength = childLength;
            }
            else
            {
                // Continuation of current line...
                //
                lineThickness = Math.max(lineThickness, childThickness);
                lineLength += childLength;
            }

            lineContents.add(child);

            // The positioning below is complex because of the case where there is a fixed item size and the element
            // needs to be positioning in the running dimension within that fixed size (as opposed to just stacking it
            // next to the previous element, as happens without fixed element sizes).  In the case where there is not
            // a fixed element size, the child "total" size is the same as the measured size plus margins, meaning that
            // the math below results in the same position regardless of the gravity / math used.
            //
            if (_orientation == LinearLayout.HORIZONTAL)
            {
                if ((lp.gravity & Gravity.LEFT) == Gravity.LEFT)
                {
                    lp.X = getPaddingLeft() + lineLength - childTotalWidth + lp.leftMargin;
                }
                else if ((lp.gravity & Gravity.RIGHT) == Gravity.RIGHT)
                {
                    lp.X = getPaddingLeft() + lineLength - (child
                            .getMeasuredWidth() + lp.rightMargin);
                }
                else // Center - default
                {
                    lp.X = getPaddingLeft() + lineLength - childTotalWidth + ((childTotalWidth - child
                            .getMeasuredWidth()) / 2);
                }
            }
            else
            {
                if ((lp.gravity & Gravity.TOP) == Gravity.TOP)
                {
                    lp.Y = getPaddingTop() + lineLength - childTotalHeight + lp.topMargin;
                }
                else if ((lp.gravity & Gravity.BOTTOM) == Gravity.BOTTOM)
                {
                    lp.Y = getPaddingTop() + lineLength - (child
                            .getMeasuredHeight() + lp.bottomMargin);
                }
                else // Center - default
                {
                    lp.Y = getPaddingTop() + lineLength - childTotalHeight + ((childTotalHeight - child
                            .getMeasuredHeight()) / 2);
                }
            }

            controlMaxLength = Math.max(controlMaxLength, lineLength);
            controlMaxThickness = linePosition + lineThickness;
        }

        this.positionLineElements(lineContents, linePosition, lineThickness);
        lineContents.clear();

        if (_orientation == LinearLayout.HORIZONTAL)
        {
            setMeasuredDimension(
                    resolveSize(controlMaxLength, widthMeasureSpec),
                    resolveSize(controlMaxThickness, heightMeasureSpec)
                                );
        }
        else
        {
            setMeasuredDimension(
                    resolveSize(controlMaxThickness, widthMeasureSpec),
                    resolveSize(controlMaxLength, heightMeasureSpec)
                                );
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View child = getChildAt(i);
            WrapLayoutParams lp = (WrapLayoutParams) child.getLayoutParams();
            child.layout(
                    lp.X, lp.Y, lp.X + child.getMeasuredWidth(), lp.Y + child.getMeasuredHeight()
                        );
        }
    }

    protected boolean checkLayoutParams(ViewGroup.LayoutParams p)
    {
        return p instanceof WrapLayoutParams;
    }

    protected ViewGroup.LayoutParams generateDefaultLayoutParams()
    {
        return new WrapLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p)
    {
        if (p instanceof LinearLayout.LayoutParams)
        {
            return new WrapLayoutParams((LinearLayout.LayoutParams) p);
        }
        else if (p instanceof MarginLayoutParams)
        {
            return new WrapLayoutParams((MarginLayoutParams) p);
        }
        else
        {
            return new WrapLayoutParams(p);
        }
    }

    public class WrapLayoutParams extends LinearLayout.LayoutParams
    {
        public int X;
        public int Y;

        public WrapLayoutParams(int width, int height)
        {
            super(width, height);
        }

        public WrapLayoutParams(ViewGroup.LayoutParams layoutParams)
        {
            super(layoutParams);
        }

        public WrapLayoutParams(ViewGroup.MarginLayoutParams layoutParams)
        {
            super(layoutParams);
        }

        // Xamarin does't expose the copy constructor (which takes a LinearLayout.LayoutParams), so we call
        // the MarginLayoutParams base class constructor, then propagate the gravity and weight ourselves.
        //
        public WrapLayoutParams(LinearLayout.LayoutParams layoutParams)
        {
            super((ViewGroup.MarginLayoutParams) layoutParams);
            this.gravity = layoutParams.gravity;
            this.weight = layoutParams.weight;
        }

        public String toString()
        {
            return String.format("WrapLayoutParams pos(%d,%d), gravity: %d", X, Y, gravity);
        }

        public void SetPosition(int x, int y)
        {
            X = x;
            Y = y;
        }
    }

    static class AndroidWrapPanelWrapper extends AndroidControlWrapper
    {
        public static final String TAG = AndroidWrapPanelWrapper.class.getSimpleName();

        public AndroidWrapPanelWrapper(
                ControlWrapper parent, BindingContext bindingContext, JObject controlSpec
                                      )
        {
            super(parent, bindingContext);
            Log.d(TAG, "Creating wrap panel element");

            final FlowLayout layout = new FlowLayout(
                    ((AndroidControlWrapper) parent).getControl().getContext()
            );
            this._control = layout;

            applyFrameworkElementDefaults(layout);

            if (controlSpec.get("orientation") == null)
            {
                layout.setOrientation(LinearLayout.VERTICAL);
            }
            else
            {
                processElementProperty(
                        controlSpec.get("orientation"), new ISetViewValue()
                        {
                            @Override
                            public void SetViewValue(JToken value)
                            {
                                layout.setOrientation(ToOrientation(value, LinearLayout.VERTICAL));
                            }
                        }
                                      );
            }

            processElementProperty(
                    controlSpec.get("itemHeight"), new ISetViewValue()
                    {
                        @Override
                        public void SetViewValue(JToken value)
                        {
                            layout.setItemHeight((int) ToDeviceUnits(value));
                        }
                    }
                                  );

            processElementProperty(
                    controlSpec.get("itemWidth"), new ISetViewValue()
                    {
                        @Override
                        public void SetViewValue(JToken value)
                        {
                            layout.setItemWidth((int) ToDeviceUnits(value));
                        }
                    }
                                  );

            processThicknessProperty(
                    controlSpec.get("padding"), new PaddingThicknessSetter(this.getControl())
                                    );

            if (controlSpec.get("contents") != null)
            {
                createControls(
                        (JArray) controlSpec.get("contents"), new IAndroidCreateControl()
                        {
                            @Override
                            public void onCreateControl(
                                    JObject controlSpec, AndroidControlWrapper controlWrapper
                                                       )
                            {
                                controlWrapper.AddToLinearLayout(layout, controlSpec);
                            }
                        }
                              );

            }

            layout.forceLayout();
        }
    }
}

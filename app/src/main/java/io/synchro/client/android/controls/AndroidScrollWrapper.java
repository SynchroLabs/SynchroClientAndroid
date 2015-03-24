package io.synchro.client.android.controls;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import io.synchro.client.android.BindingContext;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.JArray;
import io.synchro.client.android.JObject;

/**
 * Created by blake on 3/23/15.
 */
public class AndroidScrollWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidScrollWrapper.class.getSimpleName();

    public AndroidScrollWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                               )
    {
        super(parent, bindingContext);
        Log.d(TAG, "Creating scroll element");

        FrameLayout scroller = null;

        // ScrollView scrolls vertically only.  For horizontal use HorizontalScrollView
        //
        // http://developer.android.com/reference/android/widget/ScrollView.html
        //
        // Vertical scroll is default...
        //
        int orientation = ToOrientation(controlSpec.get("orientation"), LinearLayout.VERTICAL);
        if (orientation == LinearLayout.VERTICAL)
        {
            scroller = new ScrollView(((AndroidControlWrapper)parent).getControl().getContext());
        }
        else
        {
            scroller = new HorizontalScrollView(((AndroidControlWrapper)parent).getControl().getContext());
        }

        scroller.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener()
                                              {
                                                  @Override
                                                  public void onChildViewAdded(
                                                          View parent, View child
                                                                              )
                                                  {
                                                      if (parent instanceof ScrollView)
                                                      {
                                                          ScrollView scrollView = (ScrollView) parent;
                                                          scrollView.setFillViewport(child.getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT);
                                                      }

                                                      if (parent instanceof HorizontalScrollView)
                                                      {
                                                          HorizontalScrollView hScrollView = (HorizontalScrollView) parent;

                                                          hScrollView.setFillViewport(child.getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT);
                                                      }
                                                  }

                                                  @Override
                                                  public void onChildViewRemoved(
                                                          View parent, View child
                                                                                )
                                                  {
                                                  }
                                              });

        _control = scroller;
        _control.setOverScrollMode(View.OVER_SCROLL_NEVER);

        applyFrameworkElementDefaults(_control);

        if (controlSpec.get("contents") != null)
        {
            createControls((JArray)controlSpec.get("contents"), new IAndroidCreateControl()
                           {
                               @Override
                               public void onCreateControl(
                                       JObject controlSpec, AndroidControlWrapper controlWrapper
                                                          )
                               {
                                   ((FrameLayout) _control).addView(controlWrapper.getControl());
                               }
                           });
        }
    }
}

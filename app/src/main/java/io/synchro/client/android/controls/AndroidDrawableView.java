package io.synchro.client.android.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

/**
 * Created by blake on 3/22/15.
 */
public class AndroidDrawableView extends View
{
    GradientDrawable _drawable;

    public AndroidDrawableView(Context context, GradientDrawable gradientDrawable)
    {
        super(context);
        _drawable = gradientDrawable;
        _drawable.setCallback(this);
    }

    @Override
    public void invalidateDrawable(Drawable who)
    {
        super.invalidateDrawable(who);
        this.invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        //logger.Debug("OnSizeChanged - w: {0} , h: {1}", w, h);
        super.onSizeChanged(w, h, oldw, oldh);
        _drawable.setBounds(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        _drawable.draw(canvas);
    }
}

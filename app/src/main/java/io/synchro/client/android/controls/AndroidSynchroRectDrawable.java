package io.synchro.client.android.controls;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

/**
 * Created by blake on 3/22/15.
 */
public class AndroidSynchroRectDrawable extends GradientDrawable
{
    int _strokeWidth = 0;
    float _radius = 0;

    int _fillColor;
    int _strokeColor;

    public AndroidSynchroRectDrawable()
    {
        super();

        this.setShape(RECTANGLE);
        this.setBounds(0, 0, 0, 0);
    }

    public void SetFillColor(Integer color)
    {
        _fillColor = (color != null) ? color : Color.TRANSPARENT;
        this.setColor(_fillColor);
    }

    public void SetStrokeWidth(int width)
    {
        _strokeWidth = width;
    }

    @Override
    public void setCornerRadius(float radius)
    {
        _radius = radius;
        super.setCornerRadius(_radius);
    }

    public void SetStrokeColor(Integer color)
    {
        _strokeColor = (color != null) ? color : Color.TRANSPARENT;
    }

    @Override
    public void draw(Canvas canvas)
    {
        // Since the stroke width and color can be set independantly, we update the stroke here before drawing...
        //
        this.setStroke(_strokeWidth, _strokeColor);
        super.draw(canvas);
    }
}

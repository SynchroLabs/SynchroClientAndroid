package io.synchro.client.android.controls;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Created by blake on 3/2/15.
 */
public class FlowLayout extends ViewGroup
{
    public FlowLayout(Context context)
    {
        super(context);
    }

    public int getOrientation()
    {
        return 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {

    }
}

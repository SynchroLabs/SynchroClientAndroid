package io.synchro.client.android.controls;

import android.view.View;

import io.synchro.client.android.*;

/**
 * Created by blake on 3/2/15.
 */
public class AndroidBorderWrapper extends AndroidControlWrapper
{
    public AndroidBorderWrapper(
            AndroidPageView pageView, StateManager stateManager,
            ViewModel viewModel,
            BindingContext bindingContext,
            View control
                               )
    {
        super(pageView, stateManager, viewModel, bindingContext, control);
    }
}

package io.synchro.client.android;

import android.content.Context;

/**
 * Created by blake on 12/24/14.
 */
public class SynchroAppTwoLineArrayAdapter extends TwoLineArrayAdapter<SynchroApp>
{
    public SynchroAppTwoLineArrayAdapter(
            Context context, SynchroApp[] synchroApps
                                        )
    {
        super(context, synchroApps);
    }

    @Override
    public String getLineOneText(SynchroApp synchroApp)
    {
        return synchroApp.getName() + " - " + synchroApp.getDescription();
    }

    @Override
    public String getLineTwoText(SynchroApp synchroApp)
    {
        return synchroApp.getEndpoint();
    }
}

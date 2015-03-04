package io.synchro.client.android;

/**
 * Created by blake on 3/2/15.
 */
public class AndroidPageView extends PageView
{
    public AndroidPageView(
            StateManager stateManager, ViewModel viewModel,
            IDoBackToMenu doBackToMenu
                          )
    {
        super(stateManager, viewModel, doBackToMenu);
    }

    @Override
    public ControlWrapper CreateRootContainerControl(
            JObject controlSpec
                                                    )
    {
        return null;
    }

    @Override
    public void ClearContent()
    {

    }

    @Override
    public void SetContent(ControlWrapper content)
    {

    }

    @Override
    public void ProcessMessageBox(
            JObject messageBox, StateManager.ICommandHandler onCommand
                                 )
    {

    }
}

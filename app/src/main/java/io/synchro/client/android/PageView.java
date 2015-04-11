package io.synchro.client.android;

import android.util.Log;

import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JValue;

/**
 * Created by blake on 3/2/15.
 */
public abstract class PageView
{
    public static final String TAG = PageView.class.getSimpleName();

    public interface ISetPageTitle
    {
        void setPageTitle(String title);
    }

    public interface ISetBackEnabled
    {
        void setBackEnabled(boolean backEnabled);
    }

    ISetPageTitle _setPageTitle;
    public void setSetPageTitle(ISetPageTitle setPageTitle)
    {
        _setPageTitle = setPageTitle;
    }

    ISetBackEnabled _setBackEnabled;
    public void setSetBackEnabled(ISetBackEnabled setBackEnabled)
    {
        _setBackEnabled = setBackEnabled;
    }

    public interface IDoBackToMenu
    {
        void doBackToMenu();
    }

    protected StateManager _stateManager;
    protected ViewModel _viewModel;
    protected IDoBackToMenu _doBackToMenu;

    // This is the top level container of controls for a page.  If the page specifies a single top level
    // element, then this represents that element.  If not, then this is a container control that we
    // created to wrap those elements (currently a vertical stackpanel).
    //
    // Derived classes have a similarly named _rootControlWrapper which represents the actual topmost
    // visual element, typically a scroll container, that is re-populated as page contents change, and
    // which has a single child, the _rootContainerControlWrapper (which will change as the active page
    // changes).
    //
    protected ControlWrapper _rootContainerControlWrapper;

    protected String onBackCommand = null;

    public PageView(StateManager stateManager, ViewModel viewModel, IDoBackToMenu doBackToMenu)
    {
        _stateManager = stateManager;
        _viewModel = viewModel;
        _doBackToMenu = doBackToMenu;
    }

    public abstract ControlWrapper CreateRootContainerControl(JObject controlSpec);
    public abstract void ClearContent();
    public abstract void SetContent(ControlWrapper content);

    public abstract void ProcessMessageBox(JObject messageBox, StateManager.ICommandHandler onCommand);

    public boolean HasBackCommand()
    {
        if (this._stateManager.IsBackSupported())
        {
            // Page-specified back command...
            //
            return true;
        }
        else if ((_doBackToMenu != null) && _stateManager.IsOnMainPath())
        {
            // No page-specified back command, launched from menu, and is main (top-level) page...
            //
            return true;
        }

        return false;
    }

    public boolean GoBack()
    {
        if (_stateManager.IsBackSupported())
        {
            Log.d(TAG, "Back navigation");
            _stateManager.sendBackRequestAsync();
            return true;
        }
        else if ((_doBackToMenu != null) && _stateManager.IsOnMainPath())
        {
            Log.d(TAG, "Back navigation - returning to menu");
            _rootContainerControlWrapper.Unregister();
            _doBackToMenu.doBackToMenu();
            return true;
        }
        else
        {
            Log.w(TAG, "OnBackCommand called with no back command, ignoring");
            return false; // Not handled
        }
    }

    public void ProcessPageView(JObject pageView)
    {
        if (_rootContainerControlWrapper != null)
        {
            _rootContainerControlWrapper.Unregister();
            ClearContent();
            _rootContainerControlWrapper = null;
        }

        if (this._setBackEnabled != null)
        {
            this._setBackEnabled.setBackEnabled(this.HasBackCommand());
        }

        String pageTitle = pageView.get("title").asString();
        if (pageTitle != null)
        {
            _setPageTitle.setPageTitle(pageTitle);
        }

        JArray elements = (JArray)pageView.get("elements");
        if (elements.size() == 1)
        {
            // The only element is the container of all page elements, so make it the root element, and populate it...
            //
            _rootContainerControlWrapper = CreateRootContainerControl((JObject)elements.get(0));
        }
        else if (elements.size() > 1)
        {
            // There is a collection of page elements, create a default container (vertical stackpanel), make it the root, and populate it...
            //
            JObject controlSpec = new JObject();

            controlSpec.put("control", new JValue("stackpanel"));
            controlSpec.put("orientation", new JValue("vertical"));
            controlSpec.put("contents", elements);

            _rootContainerControlWrapper = CreateRootContainerControl(controlSpec);
        }

        SetContent(_rootContainerControlWrapper);
    }
}

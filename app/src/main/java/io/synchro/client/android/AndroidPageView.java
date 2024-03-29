package io.synchro.client.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.synchro.client.android.controls.AndroidControlWrapper;
import io.synchro.json.JArray;
import io.synchro.json.JObject;

/**
 * Created by blake on 3/2/15.
 */
public class AndroidPageView extends PageView
{
    public static final String TAG = AndroidPageView.class.getSimpleName();

    Activity              _activity;
    AndroidControlWrapper _rootControlWrapper;
    List<AndroidActionBarItem> _actionBarItems = new ArrayList<>();

    public AndroidPageView(
            StateManager stateManager, ViewModel viewModel, Activity activity, ViewGroup panel,
            Boolean launchedFromMenu
                          )
    {
        super(stateManager, viewModel, launchedFromMenu);
        _activity = activity;
        _rootControlWrapper = new AndroidControlWrapper(this, _stateManager, _viewModel, _viewModel.getRootBindingContext(), panel);
    }

    public AndroidActionBarItem CreateAndAddActionBarItem()
    {
        AndroidActionBarItem actionBarItem = new AndroidActionBarItem(_activity.getApplicationContext());
        this._actionBarItems.add(actionBarItem);
        return actionBarItem;
    }

    public boolean OnCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, "Option menu created");
        if (_actionBarItems.size() > 0)
        {
            int pos = 0;
            for(AndroidActionBarItem actionBarItem : _actionBarItems)
            {
                actionBarItem.setMenuItem(menu.add(0, pos, pos, actionBarItem.getTitle()));
                pos++;
            }
            return true;
        }
        else // No items
        {
            return false;
        }
    }

    public boolean OnOptionsItemSelected(MenuItem item)
    {
        if ((item.getItemId() >= 0) && (item.getItemId() < _actionBarItems.size()))
        {
            AndroidActionBarItem actionBarItem = _actionBarItems.get(item.getItemId());
            Log.d(TAG, String.format("Action bar item selected - id: %d, title: %s", item.getItemId(), actionBarItem.getTitle()));
            if (actionBarItem.getOnItemSelected() != null)
            {
                actionBarItem.getOnItemSelected().OnItemSelected();
            }
            return true;
        }

        return false;
    }

    public boolean OnCommandBarUp(MenuItem item)
    {
        Log.d(TAG, "Command bar Up button pushed");
        this.GoBack();
        return true;
    }

    protected void HideSoftKeyboard()
    {
        Context ctx = _rootControlWrapper.getControl().getContext();
        InputMethodManager inputManager =  (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = this._activity.getCurrentFocus();
        if (view != null)
        {
            // In any sane Android universe, doing either ClearFocus() or hiding w/ ImplicitOnly should work,
            // and frankly, the fact that the control with focus is being removed from the view should do it
            // automatically, but in the real world, the kb stays up when the view is removed, and only the
            // shithammering below seems to work to dimiss the kb.
            //
            view.clearFocus();
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0); // HideSoftInputFlags.ImplicitOnly);
        }
    }

    @Override
    public ControlWrapper CreateRootContainerControl(JObject controlSpec)
    {
        return AndroidControlWrapper
                .CreateControl(_rootControlWrapper, _viewModel.getRootBindingContext(), controlSpec);
    }

    @Override
    public void ClearContent()
    {
        this.HideSoftKeyboard();
        this._actionBarItems.clear();
        this._activity.invalidateOptionsMenu();

        ViewGroup panel = (ViewGroup)_rootControlWrapper.getControl();

        ScrollView mainScroller = (panel instanceof ScrollView) ?  (ScrollView) panel : null;
        if (mainScroller != null)
        {
            mainScroller.scrollTo(0, 0);
        }

        panel.removeAllViews();
        _rootControlWrapper.getChildControls().clear();
    }

    @Override
    public void SetContent(ControlWrapper content)
    {
        AndroidControlWrapper controlWrapper = (AndroidControlWrapper) content;

        // Default scroll behavior had the effect of allowing the contained item to grow
        // unbounded when its content was sized as "match parent" (or "*").  So for example,
        // if you had a vertical stack panel with a height of "*", which contained a vertical
        // wrap panel that also had a height of "*", once the wrap panel filled the space, it
        // would continue to expand (growing the scroll content) instead of wrapping to the
        // scroll content area.
        //
        // Since there is no way to disable scrolling in a ScrollView (at least not such that
        // it will constrain its content size), we have to use a scroll view as the root only
        // when the contents is intended to scroll (meaning that the contents has an explicit
        // size or is sized as "wrap content".  Otherwise, we just use a stack panel that will
        // constrain the vertical size of the contents (and not scroll, obviously).
        //
        if (controlWrapper.getHeight() == ViewGroup.LayoutParams.MATCH_PARENT)
        {
            if (_rootControlWrapper.getControl() instanceof ScrollView)
            {
                LinearLayout newRootView = new LinearLayout(_activity);
                _activity.setContentView(newRootView);
                _rootControlWrapper = new AndroidControlWrapper(this, _stateManager, _viewModel, _viewModel.getRootBindingContext(), newRootView);
            }
        }
        else
        {
            if (_rootControlWrapper.getControl() instanceof LinearLayout)
            {
                ScrollView newRootView = new ScrollView(_activity);
                _activity.setContentView(newRootView);
                _rootControlWrapper = new AndroidControlWrapper(this, _stateManager, _viewModel, _viewModel.getRootBindingContext(), newRootView);
            }
        }

        ViewGroup panel = (ViewGroup)_rootControlWrapper.getControl();
        if (content != null)
        {
            View control = controlWrapper.getControl();
            if (panel instanceof ScrollView)
            {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) control.getLayoutParams();
                if (layoutParams != null)
                {
                    // We are putting a control with a margin into a top-level ScrollView.  The ScrollView does not
                    // support margin layout, so we convert the margin of the contents into a padding of the ScrollView
                    // to accomplish the same thing.
                    //
                    // Note that generally the top-level control will be a container and not have a margin.  Note also
                    // that this "margin as padding" will be applied at the time the content is set, so the applied margin
                    // will not be updated after that point (the margin property cannot be animated in this case).  This is
                    // really to handle the "Hello World" case of a standalone non-container top-level control that we don't
                    // want jammed into the upper left corner.
                    //
                    panel.setPadding(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
                }
            }
            panel.addView(control);
        }
        _rootControlWrapper.getChildControls().add(content);

        this._activity.getActionBar().setDisplayHomeAsUpEnabled(this.HasBackCommand());
        this._activity.invalidateOptionsMenu();
    }

    @Override
    public void ProcessMessageBox(
            JObject messageBox, final StateManager.ICommandHandler onCommand
                                 )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
        AlertDialog dialog = builder.create();
        String message = "";

        if (messageBox.get("message") != null)
        {
            message = PropertyValue.ExpandAsString((String)messageBox.get("message").asString(), _viewModel.getRootBindingContext());
        }
        dialog.setMessage(message);

        if (messageBox.get("title") != null)
        {
            dialog.setTitle(PropertyValue.ExpandAsString((String)messageBox.get("title").asString(), _viewModel.getRootBindingContext()));
        }

        if (messageBox.get("options") != null)
        {
            JArray options = (JArray)messageBox.get("options");
            if (options.size() > 0)
            {
                JObject option = (JObject)options.get(0);

                final String label = PropertyValue.ExpandAsString(option.get("label").asString(), _viewModel.getRootBindingContext());
                String command = null;
                if ((option.get("command") != null) && (option.get("command").asString() != null))
                {
                    command = PropertyValue.ExpandAsString(option.get("command").asString(), _viewModel.getRootBindingContext());
                }

                final String finalCommand = command;

                dialog.setButton(label, new DialogInterface.OnClickListener()
                                 {
                                     @Override
                                     public void onClick(DialogInterface dialog, int which)
                                     {
                                         Log.d(TAG, String.format("MessageBox Command invoked: %s", label));
                                         if (finalCommand != null)
                                         {
                                             Log.d(TAG, String.format("MessageBox command: %s", finalCommand));
                                             try
                                             {
                                                 onCommand.CommandHandler(finalCommand);
                                             }
                                             catch (IOException e)
                                             {
                                                 Log.wtf(TAG, e);
                                             }
                                         }

                                     }
                                 });
            }

            if (options.size() > 1)
            {
                JObject option = (JObject)options.get(1);

                final String label = PropertyValue.ExpandAsString(option.get("label").asString(), _viewModel.getRootBindingContext());
                String command = null;
                if (option.get("command") != null)
                {
                    command = PropertyValue.ExpandAsString(option.get("command").asString(), _viewModel.getRootBindingContext());
                }

                final String finalCommand = command;

                dialog.setButton2(label, new DialogInterface.OnClickListener()
                                  {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which)
                                      {
                                          Log.d(TAG, String.format("MessageBox Command invoked: %s", label));
                                          if (finalCommand != null)
                                          {
                                              Log.d(TAG, String.format("MessageBox command: %s", finalCommand));
                                              try
                                              {
                                                  onCommand.CommandHandler(finalCommand);
                                              }
                                              catch (IOException e)
                                              {
                                                  Log.wtf(TAG, e);
                                              }
                                          }
                                      }
                                  });


            }

            if (options.size() > 2)
            {
                JObject option = (JObject)options.get(2);

                final String label = PropertyValue.ExpandAsString(option.get("label").asString(), _viewModel.getRootBindingContext());
                String command = null;
                if (option.get("command").asString() != null)
                {
                    command = PropertyValue.ExpandAsString(option.get("command").asString(), _viewModel.getRootBindingContext());
                }

                final String finalCommand = command;

                dialog.setButton3(
                        label, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Log.d(TAG, String.format("MessageBox Command invoked: %s", label));
                                if (finalCommand != null)
                                {
                                    Log.d(
                                            TAG,
                                            String.format("MessageBox command: %s", finalCommand)
                                         );
                                    try
                                    {
                                        onCommand.CommandHandler(finalCommand);
                                    }
                                    catch (IOException e)
                                    {
                                        Log.wtf(TAG, e);
                                    }
                                }
                            }
                        }
                                 );


            }
        }
        else
        {
            // Not commands - add default "close"
            //
            dialog.setButton("Close", new DialogInterface.OnClickListener()
                             {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which)
                                 {
                                     Log.d(TAG, "MessageBox default close button clicked");
                                 }
                             });
        }

        dialog.show();
    }

    @Override
    public void ProcessLaunchUrl(String primaryUrl, String secondaryUrl)
    {
        try
        {
            _activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(primaryUrl)));
        }
        catch (ActivityNotFoundException e)
        {
            _activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(secondaryUrl)));
        }
    }
}

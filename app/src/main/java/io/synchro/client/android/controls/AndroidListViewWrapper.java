package io.synchro.client.android.controls;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import io.synchro.client.android.AndroidUiThreadSetViewValue;
import io.synchro.client.android.BindingContext;
import io.synchro.client.android.BindingHelper;
import io.synchro.client.android.CommandInstance;
import io.synchro.client.android.CommandName;
import io.synchro.client.android.ControlWrapper;
import io.synchro.client.android.IGetViewValue;
import io.synchro.json.JArray;
import io.synchro.json.JObject;
import io.synchro.json.JToken;
import io.synchro.json.JValue;
import io.synchro.client.android.ValueBinding;

/**
 * Created by blake on 3/28/15.
 */
public class AndroidListViewWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidListViewWrapper.class.getSimpleName();

    boolean _selectionChangingProgramatically = false;
    JToken _localSelection;
    int _listStart = 0;

    static String[] Commands = new String[]{CommandName.getOnItemClick().getAttribute(), CommandName.getOnSelectionChange().getAttribute()};

    public static class ListItemView extends RelativeLayout implements Checkable
    {
        View _contentView;
        boolean _checkable;
        CheckBox _checkBox = null;

        public ListItemView(Context context, View contentView, int viewType, boolean checkable)
        {
            super(context);

            _contentView = contentView;
            _checkable = checkable;

            this.setLayoutParams(
                    new ListView.LayoutParams(
                            ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, viewType
                    )
                                );

            RelativeLayout.LayoutParams contentLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            contentLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            this.addView(contentView, contentLayoutParams);

            if (_checkable)
            {
                // If you add any view that is focusable inside of a ListView row, it will make the row un-selectabled.
                // For more, see: http://wiresareobsolete.com/wordpress/2011/08/clickable-zones-in-listview-items/
                //
                // Turns out we don't want the checkbox to be clickable (or focusable) anyway, so no problemo.
                //
                _checkBox = new CheckBox(this.getContext());
                _checkBox.setClickable(false);
                _checkBox.setFocusable(false);
                RelativeLayout.LayoutParams checkboxLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                checkboxLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                checkboxLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                this.addView(_checkBox, checkboxLayoutParams);
            }
        }

        public View getContentView()
        {
            return _contentView;
        }

        @Override
        public void setChecked(boolean checked)
        {
            _checkBox.setChecked(checked);
        }

        @Override
        public boolean isChecked()
        {
            return _checkBox.isChecked();
        }

        @Override
        public void toggle()
        {
            _checkBox.toggle();
        }
    }

    public static class ListViewAdapter extends BaseAdapter
    {
        public static final String TAG = ListViewAdapter.class.getSimpleName();

        Context _context;

        AndroidControlWrapper _parentControl;
        JObject               _itemTemplate;
        List<BindingContext>  _itemContexts;
        boolean               _checkable;

        public ListViewAdapter(
                Context context, AndroidControlWrapper parentControl, JObject itemTemplate,
                boolean checkable
                              )
        {
            super();
            _context = context;
            _parentControl = parentControl;
            _itemTemplate = itemTemplate;
            _checkable = checkable;
        }

        public void SetContents(BindingContext bindingContext, String itemSelector)
        {
            _itemContexts = bindingContext.SelectEach(itemSelector);
        }

        public List<BindingContext> getBindingContexts()
        {
            return _itemContexts;
        }

        public BindingContext GetBindingContext(int position)
        {
            if ((_itemContexts != null) && (_itemContexts.get(position) != null))
            {
                return _itemContexts.get(position);
            }

            return null;
        }

        @Override
        public int getCount()
        {
            if (_itemContexts != null)
            {
                return _itemContexts.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position)
        {
            // Not really sure what the point of this is.  Presumably wrap actual content in a Java.Lang.Object, but
            // then who's going to be processing that?  I've never seen this method actually get called in practice.
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(
                int position, View convertView, ViewGroup parent
                           )
        {
            if ((_itemContexts != null) && (_itemContexts.get(position) != null))
            {
                Log.d(TAG, String.format("Getting view for item at position: %d", position));
                if (convertView != null)
                {
                    AndroidControlWrapper currentWrapper = _parentControl.getChildControlWrapper(((ListItemView)convertView).getContentView());
                    if (currentWrapper != null)
                    {
                        if (currentWrapper.getBindingContext().getBindingPath().equals(
                                _itemContexts.get(position).getBindingPath()
                                                                                      ))
                        {
                            // Android is kind of stupid about list item management.  For example, when list contents are reset, such
                            // as when list items are added, you will be asked to provide a view for each visible cell, including the
                            // ones that didn't change.  In our case, if the contents of a given item change, then any controls bound
                            // to those contents will update automatically.  So as long a the binding context path for a cell doesn't
                            // change, we don't need to genererate a new view (we can just hand back the existing view, which, as just
                            // stated, will manage its own updating).  This fixes the annoying flicker that used to happen when new
                            // items were added to the end of the list (because new cells were being generated for all the the existing
                            // list positions).
                            //
                            return convertView;
                        }
                        else
                        {
                            // We are going to generate a new view (below) to replace this view, so let's clean this view up...
                            //
                            currentWrapper.Unregister();
                        }
                    }
                }

                AndroidControlWrapper controlWrapper = AndroidControlWrapper.CreateControl(_parentControl, _itemContexts.get(position), _itemTemplate);

                // By specifying IgnoreItemViewType we are telling the ListView not to recycle views (convertView will always be null).  It might be
                // nice to try to take advantage of view recycling, but that presents somewhat of a challenge in terms of the way our data binding
                // works (the bound values will attempt to update their associated views at various points in the future).
                //
                int viewType = Adapter.IGNORE_ITEM_VIEW_TYPE;

                ListItemView listItemView = new ListItemView(parent.getContext(), controlWrapper.getControl(), viewType, _checkable);
                return listItemView;
            }
            return null;
        }
    }

    public AndroidListViewWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                                 )
    {
        super(parent, bindingContext, controlSpec);

        Log.d(TAG, "Creating listview element");

        final ListView listView = new ListView(
                ((AndroidControlWrapper) parent).getControl().getContext()
        );
        this._control = listView;


        int choiceMode = ListView.CHOICE_MODE_NONE;

        ListSelectionMode mode = ToListSelectionMode(processElementProperty(controlSpec, "select", null), ListSelectionMode.Single);
        switch (mode)
        {
            case Single:
                choiceMode = ListView.CHOICE_MODE_SINGLE;
                break;
            case Multiple:
                choiceMode = ListView.CHOICE_MODE_MULTIPLE;
                break;
        }

        ListViewAdapter adapter = new ListViewAdapter(((AndroidControlWrapper)parent).getControl().getContext(), this, (JObject)controlSpec.get("itemTemplate"), choiceMode != ListView.CHOICE_MODE_NONE);
        listView.setAdapter(adapter);

        listView.setChoiceMode(choiceMode);

        applyFrameworkElementDefaults(listView);

        if (controlSpec.get("header") != null)
        {
            JArray headerControlSpec = new JArray();

            headerControlSpec.add(controlSpec.get("header"));

            createControls(headerControlSpec, new IAndroidCreateControl()
                           {
                               @Override
                               public void onCreateControl(
                                       JObject controlSpec, AndroidControlWrapper controlWrapper
                                                          )
                               {
                                   controlWrapper.getControl().setLayoutParams(new ListView.LayoutParams(controlWrapper.getControl().getLayoutParams().width, controlWrapper.getControl().getLayoutParams().height));
                                   listView.addHeaderView(controlWrapper.getControl(), null, false);
                                   _listStart++;
                               }
                           });
        }

        if (controlSpec.get("footer") != null)
        {
            JArray headerControlSpec = new JArray();

            headerControlSpec.add(controlSpec.get("footer"));

            createControls(headerControlSpec, new IAndroidCreateControl()
                           {
                               @Override
                               public void onCreateControl(
                                       JObject controlSpec, AndroidControlWrapper controlWrapper
                                                          )
                               {
                                   controlWrapper.getControl().setLayoutParams(new ListView.LayoutParams(controlWrapper.getControl().getLayoutParams().width, controlWrapper.getControl().getLayoutParams().height));
                                   listView.addFooterView(controlWrapper.getControl(), null, false);
                               }
                           });
        }

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "items", Commands);
        ProcessCommands(bindingSpec, Commands);

        if (bindingSpec.get("items") != null)
        {
            processElementBoundValue(
                    "items",
                    bindingSpec.get("items").asString(),
                    new IGetViewValue()
                    {
                        @Override
                        public JToken GetViewValue()
                        {
                            return getListViewContents(listView);
                        }
                    },
                    new AndroidUiThreadSetViewValue((Activity) listView.getContext())
                    {
                        @Override
                        protected void UiThreadSetViewValue(JToken value)
                        {
                            AndroidListViewWrapper.this.setListViewContents(
                                    listView, GetValueBinding("items").getBindingContext()
                                                                           );
                        }
                    });
        }

        if (bindingSpec.get("selection") != null)
        {
            final String selectionItem = (bindingSpec.get("selectionItem") != null) ? bindingSpec.get("selectionItem").asString() : "$data";

            processElementBoundValue(
                    "selection",
                    bindingSpec.get("selection").asString(),
                    new IGetViewValue()
                    {
                        @Override
                        public JToken GetViewValue()
                        {
                            return getListViewSelection(listView, selectionItem);
                        }
                    },
                    new AndroidUiThreadSetViewValue((Activity) listView.getContext())
                    {
                        @Override
                        protected void UiThreadSetViewValue(JToken value)
                        {
                            AndroidListViewWrapper.this.setListViewSelection(
                                    listView, selectionItem, (JToken) value
                                                                            );
                        }
                    });
        }

        if (listView.getChoiceMode() != ListView.CHOICE_MODE_NONE)
        {
            // Have not witnessed these getting called (maybe they get called on kb or other non-touch interaction?).
            // At any rate, if there is any change to the selection, we need to know about it, and may need to add these,
            // but not safe to do so until we have an environment where they are testable.
            //
            // listView.ItemSelected += listView_ItemSelected;
            // listView.NothingSelected += listView_NothingSelected;
        }

        // Since we need to handle the item click in order to update the selection state anyway, we'll always add
        // the handler (whether or not there is an onItemClick command, which it will also handle if present)...
        //
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                                        {
                                            @Override
                                            public void onItemClick(
                                                    AdapterView<?> parent, View view, int position,
                                                    long id
                                                                   )
                                            {
                                                AndroidListViewWrapper.this.listView_ItemClick(parent, view, position, id);
                                            }
                                        });
        // setListViewHeightBasedOnChildren();
    }

    protected ListViewAdapter getListViewAdapter(ListView listView)
    {
        if (listView.getAdapter() instanceof HeaderViewListAdapter)
        {
            return (ListViewAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter();
        }

        return (ListViewAdapter)listView.getAdapter();
    }

    public JToken getListViewContents(ListView listbox)
    {
        Log.d(TAG, "Get listview contents - NOOP");
        throw new UnsupportedOperationException();
    }

    public void setListViewContents(ListView listView, BindingContext bindingContext)
    {
        Log.d(TAG, "Setting listview contents");

        _selectionChangingProgramatically = true;

        ListViewAdapter adapter = getListViewAdapter(listView);
        adapter.SetContents(bindingContext, "$data");
        adapter.notifyDataSetChanged();

        ValueBinding selectionBinding = GetValueBinding("selection");
        if (selectionBinding != null)
        {
            selectionBinding.UpdateViewFromViewModel();
        }
        else if (_localSelection != null)
        {
            // If there is not a "selection" value binding, then we use local selection state to restore the selection when
            // re-filling the list.
            //
            this.setListViewSelection(listView, "$data", _localSelection);
        }

        _selectionChangingProgramatically = false;
    }

    // To determine if an item should be selected, get an item from the list, get the ElementMetaData.BindingContext.  Apply any
    // selectionItem to the binding context, resolve that and compare it to the selection (selectionItem will always be provided
    // here, and will default to "$data").
    //
    public JToken getListViewSelection(ListView listView, String selectionItem)
    {
        ListViewAdapter adapter = getListViewAdapter(listView);
        List<BindingContext> selectedBindingContexts = new ArrayList<>();
        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
        for (int i = 0; i < checkedItems.size(); i++)
        {
            int key = checkedItems.keyAt(i);
            if (checkedItems.get(key))
            {
                selectedBindingContexts.add(adapter.GetBindingContext(key - _listStart));
            }
        }

        if (listView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE)
        {
            JArray array = new JArray();
            for (BindingContext bindingContext : selectedBindingContexts)
            {
                array.add(bindingContext.Select(selectionItem).GetValue().deepClone());
            }
            return array;
        }
        else if (listView.getChoiceMode() == ListView.CHOICE_MODE_SINGLE)
        {
            if (selectedBindingContexts.size() > 0)
            {
                // We need to clone the item so we don't destroy the original link to the item in the list (since the
                // item we're getting in SelectedItem is the list item and we're putting it into the selection binding).
                //
                return selectedBindingContexts.get(0).Select(selectionItem).GetValue().deepClone();
            }
            return new JValue(false); // This is a "null" selection
        }

        return null;
    }

    // This gets triggered when selection changes come in from the server (including when the selection is initially set),
    // and it also gets triggered when the list itself changes (including when the list contents are intially set).  So
    // in the initial list/selection set case, this gets called twice.  On subsequent updates it's possible that this will
    // be triggered by either a list change or a selection change from the server, or both.  There is no easy way currerntly
    // to detect the "both" case (without exposing a lot more information here).  We're going to go ahead and live with the
    // multiple calls.  It shouldn't hurt anything (they should produce the same result), it's just slightly inefficient.
    //
    public void setListViewSelection(ListView listView, String selectionItem, JToken selection)
    {
        _selectionChangingProgramatically = true;

        ListViewAdapter adapter = getListViewAdapter(listView);

        listView.clearChoices();

        for (int n = 0; n < adapter.getCount(); n++)
        {
            BindingContext bindingContext = adapter.GetBindingContext(n);
            if (selection instanceof JArray)
            {
                JArray array = (JArray) selection;
                for (JToken item : array)
                {
                    if (item.equals(bindingContext.Select(selectionItem).GetValue()))
                    {
                        listView.setItemChecked(n + _listStart, true);
                        break;
                    }
                }
            }
            else
            {
                if (selection.equals(bindingContext.Select(selectionItem).GetValue()))
                {
                    listView.setItemChecked(n + _listStart, true);
                }
            }
        }

        _selectionChangingProgramatically = false;
    }

        /*
        static bool isListViewItemChecked(ListView listView, int position)
        {
            bool isChecked = false;
            if (listView.ChoiceMode == ChoiceMode.Single)
            {
                isChecked = position == listView.CheckedItemPosition;
            }
            else if (listView.ChoiceMode == ChoiceMode.Multiple)
            {
                isChecked = listView.CheckedItemPositions.ValueAt(position);
            }
            return isChecked;
        }
        */

    void listView_ItemClick(AdapterView<?> parent, View view, int position,
                            long id)
    {
        ListView listView = (ListView)this.getControl();

        if (listView.getChoiceMode() != ListView.CHOICE_MODE_NONE)
        {
            ValueBinding selectionBinding = GetValueBinding("selection");
            if (selectionBinding != null)
            {
                updateValueBindingForAttribute("selection");
            }
            else if (!_selectionChangingProgramatically)
            {
                _localSelection = this.getListViewSelection(listView, "$data");
            }

        }

        if (!_selectionChangingProgramatically)
        {
            // Process commands
            //
            if (listView.getChoiceMode() == ListView.CHOICE_MODE_NONE)
            {
                CommandInstance command = GetCommand(CommandName.getOnItemClick());
                if (command != null)
                {
                    Log.d(TAG, String.format("ListView item clicked with command: %s", command));

                    ListItemView listItemView = (view instanceof ListItemView) ? (ListItemView) view : null;
                    if (listItemView != null)
                    {
                        // The item click command handler resolves its tokens relative to the item clicked.
                        //
                        View contentView = listItemView.getContentView();
                        ControlWrapper wrapper = this.getChildControlWrapper(contentView);
                        if (wrapper != null)
                        {
                            getStateManager().sendCommandRequestAsync(
                                    command.getCommand(),
                                    command.GetResolvedParameters(wrapper.getBindingContext()));
                        }
                    }
                }
            }
            else
            {
                CommandInstance command = GetCommand(CommandName.getOnSelectionChange());
                if (command != null)
                {
                    Log.d(TAG, String.format("ListView selection changed with command: %s", command));

                    if (listView.getChoiceMode() == ListView.CHOICE_MODE_SINGLE)
                    {
                        ListItemView listItemView = (view instanceof ListItemView) ? (ListItemView) view : null;
                        if (listItemView != null)
                        {
                            // The selection change command handler resolves its tokens relative to the item selected when in single select mode.
                            //
                            View contentView = listItemView.getContentView();
                            ControlWrapper wrapper = this.getChildControlWrapper(contentView);
                            if (wrapper != null)
                            {
                                getStateManager().sendCommandRequestAsync(
                                        command.getCommand(),
                                        command.GetResolvedParameters(wrapper.getBindingContext()));
                            }
                        }
                    }
                    else // ChoiceMode.Multiple
                    {
                        // The selection change command handler resolves its tokens relative to the list context when in multiple select mode.
                        //
                        getStateManager().sendCommandRequestAsync(
                                command.getCommand(),
                                command.GetResolvedParameters(this.getBindingContext()));
                    }
                }
            }
        }
    }
}

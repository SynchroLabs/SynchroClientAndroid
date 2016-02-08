package io.synchro.client.android.controls;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
 * Created by blake on 3/29/15.
 */
public class AndroidListBoxWrapper extends AndroidControlWrapper
{
    public static final String TAG = AndroidButtonWrapper.class.getSimpleName();

    boolean _selectionChangingProgramatically = false;
    JToken _localSelection;

    static String[] Commands = new String[]{CommandName.getOnItemClick().getAttribute(), CommandName.getOnSelectionChange().getAttribute()};

    public static class BindingContextListboxAdapter extends BaseAdapter
    {
        protected Context _context;
        protected List<View> _views = new ArrayList<>();

        protected List<AndroidPickerWrapper.BindingContextListItem> _listItems = new ArrayList<>();
        protected int _layoutResourceId;

        public BindingContextListboxAdapter(Context context, int itemLayoutResourceId)
        {
            _context = context;
            _layoutResourceId = itemLayoutResourceId;
        }

        public void SetContents(BindingContext bindingContext, String itemContent)
        {
            _listItems.clear();

            List<BindingContext> itemBindingContexts = bindingContext.SelectEach("$data");
            for (BindingContext itemBindingContext : itemBindingContexts)
            {
                _listItems.add(
                        new AndroidPickerWrapper.BindingContextListItem(
                                itemBindingContext, itemContent
                        )
                              );
            }
        }

        public AndroidPickerWrapper.BindingContextListItem GetItemAtPosition(int position)
        {
            return _listItems.get(position);
        }

        @Override
        public int getCount()
        {
            return _listItems.size();
        }

        @Override
        public Object getItem(int position)
        {
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
            AndroidPickerWrapper.BindingContextListItem item = _listItems.get(position);

            LayoutInflater inflater = LayoutInflater.from(_context);
            View view = (convertView != null) ? convertView :  inflater.inflate(
                    _layoutResourceId, parent, false
                                                                               );

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            if (text != null)
                text.setText(item.toString());

            if (!_views.contains(view))
                _views.add(view);

            return view;
        }
    }

    public AndroidListBoxWrapper(
            ControlWrapper parent,
            BindingContext bindingContext,
            JObject controlSpec
                                )
    {
        super(parent, bindingContext, controlSpec);
        Log.d(TAG, "Creating listbox element");

        final ListView listView = new ListView(((AndroidControlWrapper)parent).getControl().getContext());
        this._control = listView;

        int listTemplate = android.R.layout.simple_list_item_1; // Default for ChoiceMode.None

        ListSelectionMode mode = ToListSelectionMode(processElementProperty(controlSpec, "select", null), ListSelectionMode.Single);
        switch (mode)
        {
            case Single:
                listTemplate = android.R.layout.simple_list_item_single_choice;
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                break;
            case Multiple:
                listTemplate = android.R.layout.simple_list_item_multiple_choice;
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                break;
        }

        BindingContextListboxAdapter adapter = new BindingContextListboxAdapter(((AndroidControlWrapper)parent).getControl().getContext(), listTemplate);
        listView.setAdapter(adapter);

        setListViewHeightBasedOnChildren();

        applyFrameworkElementDefaults(listView);

        JObject bindingSpec = BindingHelper.GetCanonicalBindingSpec(controlSpec, "items", Commands);
        ProcessCommands(bindingSpec, Commands);

        if (bindingSpec.get("items") != null)
        {
            final String itemContent = (bindingSpec.get("itemContent") != null) ? bindingSpec.get("itemContent").asString() : "{$data}";

            processElementBoundValue(
                    "items",
                    bindingSpec.get("items").asString(),
                    new IGetViewValue
                            ()
                    {
                        @Override
                        public JToken GetViewValue()
                        {
                            return getListboxContents(listView);
                        }
                    },
                    new AndroidUiThreadSetViewValue((Activity) listView.getContext())
                    {
                        @Override
                        protected void UiThreadSetViewValue(JToken value)
                        {
                            AndroidListBoxWrapper.this.setListboxContents(
                                    listView, GetValueBinding("items").getBindingContext(),
                                    itemContent
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
                            return getListboxSelection(listView, selectionItem);
                        }
                    },
                    new AndroidUiThreadSetViewValue((Activity) listView.getContext())
                    {
                        @Override
                        protected void UiThreadSetViewValue(JToken value)
                        {
                            AndroidListBoxWrapper.this.setListboxSelection(
                                    listView, selectionItem, value
                                                                          );
                        }
                    });
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                                        {
                                            @Override
                                            public void onItemClick(
                                                    AdapterView<?> parent, View view, int position,
                                                    long id
                                                                   )
                                            {
                                                listView_ItemClick(parent, view, position, id);
                                            }
                                        });
    }

    // !!! This doesn't really work at all.  When it does the measure pass, the value reported is significantly smaller than the size
    //     actually rendered.  Even if this did work, we'd want to obey height, minheight, and maxheight.
    //
    public void setListViewHeightBasedOnChildren()
    {
        ListView listView = (ListView)_control;
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null)
        {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++)
        {
            View listItem = adapter.getView(i, null, listView);
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            listItem.measure(0, measureSpec);
            totalHeight += listItem.getMeasuredHeight() + ((CheckedTextView)listItem).getTotalPaddingBottom() + ((CheckedTextView)listItem).getTotalPaddingTop();
        }

        _height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() + 1));
        this.updateSize();
    }

    public JToken getListboxContents(ListView listView)
    {
        Log.d(TAG, "Getting listbox contents");
        JArray array = new JArray();
        for (int n = 0; n < listView.getCount(); n++)
        {
            array.add(new JValue(listView.getItemAtPosition(n).toString()));
        }
        return array;
    }

    public void setListboxContents(ListView listView, BindingContext bindingContext, String itemContent)
    {
        Log.d(TAG, "Setting listbox contents");

        _selectionChangingProgramatically = true;

        BindingContextListboxAdapter adapter = (BindingContextListboxAdapter)listView.getAdapter();
        adapter.SetContents(bindingContext, itemContent);
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
            this.setListboxSelection(listView, "$data", _localSelection);
        }

        _selectionChangingProgramatically = false;
    }

    public JToken getListboxSelection(ListView listView, String selectionItem)
    {
        BindingContextListboxAdapter adapter = (BindingContextListboxAdapter)listView.getAdapter();

        List<AndroidPickerWrapper.BindingContextListItem> selectedListItems = new ArrayList<>();
        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
        for (int i = 0; i < checkedItems.size(); i++)
        {
            int key = checkedItems.keyAt(i);
            if (checkedItems.get(key))
            {
                selectedListItems.add(adapter.GetItemAtPosition(key));
            }
        }

        if (listView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE)
        {
            JArray array = new JArray();
            for (AndroidPickerWrapper.BindingContextListItem listItem : selectedListItems)
            {
                array.add(listItem.GetSelection(selectionItem));
            }
            return array;
        }
        else if (listView.getChoiceMode() == ListView.CHOICE_MODE_SINGLE)
        {
            if (selectedListItems.size() > 0)
            {
                return selectedListItems.get(0).GetSelection(selectionItem);
            }
            return new JValue(false); // This is a "null" selection
        }

        return null;
    }

    public void setListboxSelection(ListView listView, String selectionItem, JToken selection)
    {
        _selectionChangingProgramatically = true;

        BindingContextListboxAdapter adapter = (BindingContextListboxAdapter)listView.getAdapter();

        listView.clearChoices();

        for (int n = 0; n < adapter.getCount(); n++)
        {
            AndroidPickerWrapper.BindingContextListItem listItem = adapter.GetItemAtPosition(n);
            if (selection instanceof JArray)
            {
                JArray array = (JArray) selection;
                for (JToken item : array)
                {
                    if (item.equals(listItem.GetSelection(selectionItem)))
                    {
                        listView.setItemChecked(n, true);
                        break;
                    }
                }
            }
            else
            {
                if (selection.equals(listItem.GetSelection(selectionItem)))
                {
                    listView.setItemChecked(n, true);
                }
            }
        }

        _selectionChangingProgramatically = false;
    }

    void listView_ItemClick(AdapterView<?> parent, View view, int position, long id)
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
                _localSelection = this.getListboxSelection(listView, "$data");
            }

        }

        if (!_selectionChangingProgramatically)
        {
            // Process commands
            //
            BindingContextListboxAdapter adapter = (BindingContextListboxAdapter)listView.getAdapter();

            if (listView.getChoiceMode() == ListView.CHOICE_MODE_NONE)
            {
                CommandInstance command = GetCommand(CommandName.getOnItemClick());
                if (command != null)
                {
                    Log.d(TAG, String.format("ListView item clicked with command: %s", command.getCommand()));

                    // The item click command handler resolves its tokens relative to the item clicked.
                    //
                    AndroidPickerWrapper.BindingContextListItem listItem = adapter.GetItemAtPosition(position);
                    if (listItem != null)
                    {
                        getStateManager().sendCommandRequestAsync(
                                command.getCommand(), command.GetResolvedParameters(
                                        listItem.getBindingContext()
                                                                                   )
                                                                 );
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
                        // The selection change command handler resolves its tokens relative to the item selected when in single select mode.
                        //
                        AndroidPickerWrapper.BindingContextListItem listItem = adapter.GetItemAtPosition(position);
                        if (listItem != null)
                        {
                            getStateManager().sendCommandRequestAsync(
                                    command.getCommand(), command.GetResolvedParameters(
                                            listItem.getBindingContext()
                                                                                       )
                                                                     );
                        }
                    }
                    else // ChoiceMode.Multiple
                    {
                        // The selection change command handler resolves its tokens relative to the list context when in multiple select mode.
                        //
                        getStateManager().sendCommandRequestAsync(
                                command.getCommand(), command.GetResolvedParameters(
                                        this.getBindingContext()
                                                                                   )
                                                                 );
                    }
                }
            }
        }
    }
}

package io.github.tonyguyot.acronym;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.github.tonyguyot.acronym.data.Acronym;

/**
 * Adapter class for the Recycler View displaying the results in
 * the history pane.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // the data set and its index
    private Map<String, Set<String>> mDataSet;
    private List<String> mDataIndex;

    // the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView definitions;

        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.history_item_name);
            definitions = (TextView) v.findViewById(R.id.history_item_definitions);
        }
    }

    // default constructor
    public HistoryAdapter() {
        mDataSet = new HashMap<>();
        mDataIndex = new ArrayList<>();
    }

    // update the dataset from a global list of definitions
    public void update(ArrayList<Acronym> flatList) {
        if (flatList != null) {
            for (Acronym item : flatList) {
                if (TextUtils.isEmpty(item.getName()) || TextUtils.isEmpty(item.getExpansion())) {
                    // should not happen, but in case...
                    continue;
                }
                if (!mDataSet.containsKey(item.getName())) {
                    // we have a new acronym
                    mDataSet.put(item.getName(), new TreeSet<String>());
                    mDataIndex.add(item.getName());
                }
                // add the expansion
                // the TreeSet will ensure that there is no duplicate
                mDataSet.get(item.getName()).add(item.getExpansion());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // replace the content of the view with a new element
        final String name = mDataIndex.get(position);
        final Set<String> content = mDataSet.get(name);
        final TextView definitionsText = holder.definitions;

        // put the name in the first textview
        // the name is always visible
        holder.name.setText(name);
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                definitionsText.setVisibility(View.VISIBLE);
            }
        });

        // put the definitions in the second textview
        // the definitions are invisible by default but becomes visible when
        // the user clicks on the name
        StringBuilder definitions = new StringBuilder();
        boolean start = true;
        for (String def : content) {
            if (start) {
                start = false;
            } else {
                definitions.append("\n");
            }
            definitions.append(def);
        }
        holder.definitions.setText(definitions);
        holder.definitions.setVisibility(View.GONE); // not displayed by default
        holder.definitions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                definitionsText.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }
}

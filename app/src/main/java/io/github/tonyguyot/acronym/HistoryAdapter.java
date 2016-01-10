/*
 * Copyright (C) 2016 Tony Guyot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.tonyguyot.acronym;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

    // icons for the view
    private final Drawable mExpandedDrawable;
    private final Drawable mCollapsedDrawable;

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

    // constructor
    public HistoryAdapter(Context context) {
        mDataSet = new HashMap<>();
        mDataIndex = new ArrayList<>();

        // define the icons
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            mExpandedDrawable = context.getDrawable(R.mipmap.arrow_down_float);
            mCollapsedDrawable = context.getDrawable(R.mipmap.arrow_up_float);
        } else {
            mExpandedDrawable = context.getResources().getDrawable(R.mipmap.arrow_down_float);
            mCollapsedDrawable = context.getResources().getDrawable(R.mipmap.arrow_up_float);
        }

    }

    // update the dataset from a global list of definitions
    public void update(ArrayList<Acronym> flatList) {
        clear();
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
        final TextView nameTextView = holder.name;
        final TextView definitionsTextView = holder.definitions;

        // put the name in the first textview
        // the name is always visible
        holder.name.setText(name);
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // the user has clicked on the name:
                // show the definitions (or hide them if they were
                // already displayed)
                toggleView(nameTextView, definitionsTextView);
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
                // the user has clicked on the definitions:
                // hide the definitions and display only the name
                collapseView(nameTextView, definitionsTextView);
            }
        });
    }

    // show name + definitions in the view
    private void expandView(TextView tvName, TextView tvDefinitions) {
        // show definitions
        tvDefinitions.setVisibility(View.VISIBLE);

        // change icon near name
        tvName.setCompoundDrawablesWithIntrinsicBounds(mExpandedDrawable, null, null, null);
    }

    // show name only in the view
    private void collapseView(TextView tvName, TextView tvDefinitions) {
        // hide definitions
        tvDefinitions.setVisibility(View.GONE);

        // change icon near name
        tvName.setCompoundDrawablesWithIntrinsicBounds(mCollapsedDrawable, null, null, null);
    }

    private void toggleView(TextView tvName, TextView tvDefinitions) {
        if (tvDefinitions.getVisibility() == View.GONE) {
            // the definitions are currently not displayed:
            // we want now to show them
            expandView(tvName, tvDefinitions);
        } else {
            // the defintions are currently displayed:
            // we want now to hide them
            collapseView(tvName, tvDefinitions);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void clear() {
        mDataSet.clear();
        mDataIndex.clear();
        notifyDataSetChanged();
    }
}

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
package io.github.tonyguyot.acronym.ui;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.tonyguyot.acronym.R;
import io.github.tonyguyot.acronym.data.Acronym;

/**
 * Adapter class for the Recycler View displaying the results in
 * the query pane.
 */
public class QueryAdapter extends RecyclerView.Adapter<QueryAdapter.ViewHolder> {

    // the data set
    private ArrayList<Acronym> mDataSet;

    // the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mDescription;
        public TextView mComment;

        public ViewHolder(View v) {
            super(v);
            mDescription = (TextView) v.findViewById(R.id.query_item_definition);
            mComment = (TextView) v.findViewById(R.id.query_item_comment);
        }
    }

    // default constructor
    public QueryAdapter() {
        mDataSet = new ArrayList<>();
    }

    // constructor
    public QueryAdapter(ArrayList<Acronym> dataSet) {
        mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_query, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // replace the content of the view with a new element
        final Acronym item = mDataSet.get(position);
        holder.mDescription.setText(item.getExpansion());
        if (!TextUtils.isEmpty(item.getComment())) {
            holder.mComment.setVisibility(View.VISIBLE);
            holder.mComment.setText(item.getComment());
        } else {
            holder.mComment.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public ArrayList<Acronym> getValues() {
        return mDataSet;
    }

    public void add(int pos, Acronym item) {
        mDataSet.add(pos, item);
        notifyItemInserted(pos);
    }

    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }
}

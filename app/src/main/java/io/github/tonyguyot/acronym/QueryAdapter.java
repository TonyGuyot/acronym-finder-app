package io.github.tonyguyot.acronym;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

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

    public void add(int pos, Acronym item) {
        mDataSet.add(pos, item);
        notifyItemInserted(pos);
    }

    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }
}

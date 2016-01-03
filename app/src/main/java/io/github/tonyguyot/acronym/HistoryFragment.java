package io.github.tonyguyot.acronym;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.tonyguyot.acronym.data.Acronym;
import io.github.tonyguyot.acronym.ui.DividerItemDecoration;

/**
 * A fragment representing a list of Items.
 */
public class HistoryFragment extends Fragment implements AbsListView.OnItemClickListener {

    // tag for logging information
    private static final String TAG = "AcronymHistoryFragment";

    // where we display information message in case no result to display
    private TextView mMessageText;

    // where we display the history
    private RecyclerView mRecyclerView;

    // the adapter for the list of results
    private HistoryAdapter mAdapter;

    // the progress indicator to show that search is being performed
    private ProgressBar mProgress;

    // define the broadcast receiver for the results of acronym listing
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResultReceived(intent);
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_acronym_definition, container, false);

        // retrieve the different UI items we need to interact with
        mMessageText = (TextView) view.findViewById(R.id.history_text);
        // TODO mProgress = (ProgressBar) view.findViewById(R.id.history_progress);

        // initialize the recycler view for the list of results
        mRecyclerView = (RecyclerView) view.findViewById(R.id.history_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);
        mAdapter = new HistoryAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, AcronymService.ListIntent.getIntentFilter());
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mReceiver);
        super.onPause();
    }

    public void refresh() {
        // start the service
        AcronymService.start(getContext());
    }

    // received notification about search result
    private void onResultReceived(Intent intent) {
        // display the new results
        hideInProgress();
        if (AcronymService.ListIntent.getResultStatus(intent) == Activity.RESULT_OK) {
            onResultSuccess(intent); // we received an answer with 0 or more results
        } else {
            onResultFailed(); // system failure, no answer received
        }
    }

    // received notification about acronym search success
    private void onResultSuccess(Intent intent) {
        ArrayList<Acronym> results = AcronymService.ListIntent.getResultList(intent);

        if (results != null) {
            Log.d(TAG, results.toString());

            // display the number of results in the status text view
            if (results.isEmpty()) {
                // no result found
                setMessageText(R.string.history_empty);
            } else {
                // one or more results found
                setResultList(results);
            }
        } else {
            // this should not happen here and will be treated as an error
            onResultFailed();
        }
    }

    // received notification about acronym list failed
    private void onResultFailed() {
        setMessageText(R.string.history_error);
    }

    // ------ HELPER METHODS -----

    // set the UI in the "search in progress" mode
    private void showInProgress() {
        // clear previous result
        // TODO mTvResultStatus.setText(R.string.query_in_progress);
        mAdapter.clear();

        // show the progress indicator
        // TODO mProgress.setVisibility(View.VISIBLE);
    }

    // set the UI in the "search completed" mode
    private void hideInProgress() {
        // hide the progress indicator
        // TODO mProgress.setVisibility(View.GONE);
    }

    // display an information message
    private void setMessageText(int resId) {
        mMessageText.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mMessageText.setText(resId);
    }

    // display the list of acronyms
    private void setResultList(ArrayList<Acronym> list) {
        mMessageText.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mAdapter.update(list);
    }
}

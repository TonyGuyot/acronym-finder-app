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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.tonyguyot.acronym.R;
import io.github.tonyguyot.acronym.data.Acronym;
import io.github.tonyguyot.acronym.presenter.AcronymService;

/**
 * A fragment representing a list of Items.
 */
public class HistoryFragment extends Fragment {

    // tag for logging information
    private static final String TAG = "AcronymHistoryFragment";

    // where we display information message in case no result to display
    private TextView mMessageText;

    // where we display the history
    private RecyclerView mRecyclerView;

    // the adapter for the list of results
    private HistoryAdapter mAdapter;

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
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // retrieve the different UI items we need to interact with
        mMessageText = (TextView) view.findViewById(R.id.history_text);

        // initialize the recycler view for the list of results
        mRecyclerView = (RecyclerView) view.findViewById(R.id.history_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);
        mAdapter = new HistoryAdapter(getActivity().getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);

        // define the callbacks for the buttons
        Button clearButton = (Button) view.findViewById(R.id.history_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearButtonClick();
            }
        });
        Button refreshButton = (Button) view.findViewById(R.id.history_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRefreshButtonClick();
            }
        });

        Log.d(TAG, "view has been created");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, AcronymService.ListIntent.getIntentFilter());
        refresh();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mReceiver);
        super.onPause();
    }

    public void refresh() {
        // start the service
        if (getActivity() == null) {
            Log.d(TAG, "Activity is null");
        } else {
            AcronymService.startListContentOfCache(getActivity());
        }
    }

    /////////////////////
    // Other callbacks
    /////////////////////

    // received notification about search result
    private void onResultReceived(Intent intent) {
        // display the new results
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
                mAdapter.clear(); // in case there was something in the list previously
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

    // user has clicked on the clear button
    private void onClearButtonClick() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.history_clear)
                .setMessage(R.string.history_confirm)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // user pressed YES => clear the cache
                        AcronymService.startClearCache(getContext());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // user pressed NO => do nothing
                    }
                })
                .setIcon(R.mipmap.ic_dialog_alert_holo_light)
                .show();
    }

    // user has clicked on the refresh button
    private void onRefreshButtonClick() {
        refresh();
    }

    // ------ HELPER METHODS -----

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
        mAdapter.clear();
        mAdapter.update(list);
    }
}

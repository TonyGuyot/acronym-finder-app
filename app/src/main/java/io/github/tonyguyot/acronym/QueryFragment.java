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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import io.github.tonyguyot.acronym.data.Acronym;
import io.github.tonyguyot.acronym.fragments.ViewPagerFragmentLifecycle;
import io.github.tonyguyot.acronym.ui.DividerItemDecoration;


/**
 * A simple {@link Fragment} subclass.
 */
public class QueryFragment extends Fragment
                            implements ViewPagerFragmentLifecycle {

    // tag for logging information
    private static final String TAG = "AcronymQueryFragment";

    // key for the saved instance
    private static final String KEY_ACRONYMS = "acronyms";

    // where the user types the acronym to search
    private TextView mTvQuery;

    // where we display if the search was successful
    private TextView mTvResultStatus;

    // the adapter for the list of results
    private QueryAdapter mAdapter;

    // the progress indicator to show that search is being performed
    private ProgressBar mProgress;

    // define the broadcast receiver for the results of acronym searches
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResultReceived(intent);
        }
    };

    public QueryFragment() {
        // Required empty public constructor
    }

    // ------ LIFECYCLE METHODS ------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Acronym[] savedValues = null;
        if (savedInstanceState != null) {
            // the application has been reloaded
            savedValues = (Acronym[]) savedInstanceState.getParcelableArray(KEY_ACRONYMS);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_query, container, false);

        // retrieve the different UI items we need to interact with
        mTvQuery = (TextView) view.findViewById(R.id.query_entry);
        mTvResultStatus = (TextView) view.findViewById(R.id.query_result);
        mProgress = (ProgressBar) view.findViewById(R.id.query_progress);

        // initialize the recycler view for the list of results
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.query_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
        if (savedValues == null) {
            mAdapter = new QueryAdapter();
        } else {
            mAdapter = new QueryAdapter(savedValues);
        }
        recyclerView.setAdapter(mAdapter);

        // define the callback for the button
        Button submitButton = (Button) view.findViewById(R.id.query_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitButtonClick();
            }
        });

        Log.d(TAG, "view has been created");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, AcronymService.ReplyIntent.getIntentFilter());
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        ArrayList<Acronym> values = mAdapter.getValues();
        if (values != null) {
            savedState.putParcelableArray(KEY_ACRONYMS,
                    values.toArray(new Acronym[values.size()]));
        }
    }

    /////////////////////////////////////////////////////////////
    // Callbacks from the ViewPagerFragmentLifecycle interface
    /////////////////////////////////////////////////////////////

    @Override
    public void onShowInViewPager() {
        // do nothing
    }

    @Override
    public void onHideInViewPager() {
        // if keyboard is currently displayed, we don't want it to saty open
        // when displaying another pane
        hideKeyboard();
    }

    /////////////////////
    // Other callbacks
    /////////////////////

    // called when the user clicks on the submit button
    // => start the AcronymService to retrieve the acronym expansions
    private void onSubmitButtonClick() {
        String acronymName = mTvQuery.getText().toString().trim().toUpperCase();
        // TODO remove any invalid character
        if (!TextUtils.isEmpty(acronymName)) {
            hideKeyboard();
            showInProgress();
            AcronymService.startRetrieveAcronym(getContext(), acronymName);
        } else {
            Log.d(TAG, "invalid data entry -> do nothing");
        }
    }

    // received notification about search result
    private void onResultReceived(Intent intent) {
        // display the new results
        hideInProgress();
        if (AcronymService.ReplyIntent.getResultStatus(intent) == Activity.RESULT_OK) {
           onResultSuccess(intent); // we received an answer with 0 or more results
        } else {
           onResultFailed(intent); // network failure, no answer received
        }
    }

    // received notification about acronym search success
    private void onResultSuccess(Intent intent) {
        Collection<Acronym> results = AcronymService.ReplyIntent.getResultList(intent);
        String acronym = AcronymService.ReplyIntent.getAcronymName(intent);

        if (results != null) {
            // display the number of results in the status text view
            Resources res = getResources();
            String text;
            if (results.isEmpty()) {
                // no result found
                text = String.format(res.getString(R.string.query_no_result), acronym);
            } else {
                // one or more results found
                int count = results.size();
                text = res.getQuantityString(R.plurals.query_n_results,
                        count, // to select which string we use (plural or not)
                        count, // to replace %d with number
                        acronym); // to replace %s with name
            }
            CharSequence styledText = Html.fromHtml(text); // retrieve HTML tags
            mTvResultStatus.setText(styledText);

            // display all the results in the list
            int pos = 0;
            for (Acronym item : results) {
                mAdapter.add(pos, item);
                pos++;
            }
        } else {
            // this should not happen here and will be treated as an error
            onResultFailed(intent);
        }
    }

    // received notification about acronym search failed
    private void onResultFailed(Intent intent) {
        if (AcronymService.ReplyIntent.isNetworkError(intent)) {
            mTvResultStatus.setText(R.string.query_error_server);
        } else if (AcronymService.ReplyIntent.isParsingError(intent)) {
            mTvResultStatus.setText(R.string.query_error_parse);
        } else if (AcronymService.ReplyIntent.isHttpError(intent)) {
            int httpCode = AcronymService.ReplyIntent.getHttpResponse(intent);
            Resources res = getResources();
            String text = String.format(res.getString(R.string.query_error_response), httpCode);
            mTvResultStatus.setText(text);
        } else {
            mTvResultStatus.setText(R.string.query_error_other);
        }
    }

    // ------ HELPER METHODS -----

    // set the UI in the "search in progress" mode
    private void showInProgress() {
        // clear previous result
        mTvResultStatus.setText(R.string.query_in_progress);
        mAdapter.clear();

        // show the progress indicator
        mProgress.setVisibility(View.VISIBLE);
    }

    // set the UI in the "search completed" mode
    private void hideInProgress() {
        // hide the progress indicator
        mProgress.setVisibility(View.GONE);
    }

    // remove the keyboard from view if it is displayed
    public void hideKeyboard() {
        Utils.hideKeyboard(getActivity(), mTvQuery.getWindowToken());
    }
}

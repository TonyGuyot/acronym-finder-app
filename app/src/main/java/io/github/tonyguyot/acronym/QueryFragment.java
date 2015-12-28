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
import android.widget.TextView;

import java.util.Collection;

import io.github.tonyguyot.acronym.data.Acronym;


/**
 * A simple {@link Fragment} subclass.
 */
public class QueryFragment extends Fragment {

    // tag for logging information
    private static final String TAG = "AcronymQueryFragment";

    // where the user types the acronym to search
    private TextView mTvQuery;

    // where we display if the search was successful
    private TextView mTvResultStatus;

    // the adapter for the list of results
    private QueryAdapter mAdapter;

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

        // TODO: retrieve the previous values
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_query, container, false);

        // retrieve the different UI items we need to interact with
        mTvQuery = (TextView) view.findViewById(R.id.query_entry);
        mTvResultStatus = (TextView) view.findViewById(R.id.query_result);

        // initialize the recycler view for the list of results
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.query_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new QueryAdapter();
        recyclerView.setAdapter(mAdapter);

        // define the callback for the button
        Button submitButton = (Button) view.findViewById(R.id.query_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitButtonClick();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, AcronymService.getIntentFilter());
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mReceiver);
        super.onPause();
    }

    // ------ CALLBACKS ------

    // called when the user clicks on the submit button
    // => start the AcronymService to retrieve the acronym expansions
    private void onSubmitButtonClick() {
        String acronym = mTvQuery.getText().toString();
        if (!TextUtils.isEmpty(acronym)) {
            Utils.hideKeyboard(getActivity(), mTvQuery.getWindowToken());
            AcronymService.start(getContext(), acronym);
        }
    }

    // received notification about search result
    private void onResultReceived(Intent intent) {

        // clear the previous results
        mAdapter.clear();

        // display the new results
        if (AcronymService.getResultStatus(intent) == Activity.RESULT_OK) {
           onResultSuccess(intent); // we received an answer with 0 or more results
        } else {
           onResultFailed(intent); // network failure, no answer received
        }
    }

    // received notification about acronym search success
    private void onResultSuccess(Intent intent) {
        Collection<Acronym> results = AcronymService.getResultList(intent);
        String acronym = AcronymService.getAcronymName(intent);

        if (results != null) {
            Log.d(TAG, results.toString());

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
        mTvResultStatus.setText(R.string.query_error);
    }
}

package io.github.tonyguyot.acronym;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

    private TextView mTvQuery;
    private TextView mTvResult;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_query, container, false);

        // retrieve the different UI items we need to interact with
        mTvQuery = (TextView) view.findViewById(R.id.query_entry);
        mTvResult = (TextView) view.findViewById(R.id.query_result);

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
           onResultSuccess(intent);
        } else {
           onResultFailed(intent);
        }
    }

    // received notification about acronym search success
    private void onResultSuccess(Intent intent) {
        Collection<Acronym> acronyms = AcronymService.getResultList(intent);
        if (acronyms != null) {
            mTvResult.setText(acronyms.toString());
            int pos = 0;
            for (Acronym item : acronyms) {
                mAdapter.add(pos, item);
                pos++;
            }
        } else {
            mTvResult.setText("null");
        }
    }

    // received notification about acronym search failed
    private void onResultFailed(Intent intent) {
        mTvResult.setText("error");
    }
}

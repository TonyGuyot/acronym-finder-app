package tonyguyot.github.io.acronym;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class QueryFragment extends Fragment {

    TextView mTvQuery;

    public QueryFragment() {
        // Required empty public constructor
    }

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

        // define the callback for the button
        Button submitButton = (Button) view.findViewById(R.id.query_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick();
            }
        });

        return view;
    }

    // ------ CALLBACKS ------

    // called when the user clicks on the submit button
    private void onButtonClick() {
        Utils.toast(getContext(), mTvQuery.getText().toString());
    }
}

package tonyguyot.github.io.acronym;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import tonyguyot.github.io.acronym.data.Acronym;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class AcronymService extends IntentService {

    // tag for logging information
    private static final String TAG = "AcronymService";

    // common tag for all cross-entities identifier
    private static final String PREFIX = "tonyguyot.github.io.acronym.";

    // expected action in the calling intent
    // there is only one supported action, but in order to illustrate
    // a possible multi-action service, an action tag is defined
    private static final String ACTION_GET_ACRONYM = PREFIX + "action.GET_ACRONYM";

    // expected parameter in the calling intent
    private static final String EXTRA_ACRONYM_NAME = PREFIX + "extra.ACRONYM_NAME";

    // notification id for the answer
    private static final String NOTIFICATION = PREFIX + "notification";

    // parameter for the response content (optional)
    private static final String EXTRA_ACRONYM_LIST = PREFIX + "extra.ACRONYM_LIST";

    // parameter for the response status (mandatory)
    private static final String EXTRA_RESULT_STATUS = PREFIX + "extra.RESULT_STATUS";

    // mandatory constructor for a service
    public AcronymService() {
        super("AcronymService");
    }

    // ------ STATIC HELPER METHODS ------

    /**
     * Starts this service with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void start(Context context, String acronym) {
        Intent intent = new Intent(context, AcronymService.class);
        intent.setAction(ACTION_GET_ACRONYM);
        intent.putExtra(EXTRA_ACRONYM_NAME, acronym);
        context.startService(intent);
    }

    // produce an intent filter for the broadcast receiver
    public static IntentFilter getIntentFilter() {
        return new IntentFilter(NOTIFICATION);
    }

    // extract the list of acronyms from the reply intent
    public static Collection<Acronym> getResultList(Intent intent) {
        if (NOTIFICATION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            Collection<Acronym> result = bundle.getParcelableArrayList(EXTRA_ACRONYM_LIST);
            return result;
        } else {
            Log.d(TAG, "Unexpected intent action: "+intent.getAction()+
                        "instead of: "+NOTIFICATION);
            return null;
        }
    }

    // extract the result status from the reply intent
    public static int getResultStatus(Intent intent) {
        if (NOTIFICATION.equals(intent.getAction())) {
            return intent.getIntExtra(EXTRA_RESULT_STATUS, -1);
        } else {
            Log.d(TAG, "Unexpected intent action: "+intent.getAction()+
                    "instead of: "+NOTIFICATION);
            return -1;
        }
    }

    // ------ LIFECYCLE METHODS ------

    // perform the action in a background thread
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (ACTION_GET_ACRONYM.equals(intent.getAction())) {
                doRetrieveAcronymDefinitions(intent.getStringExtra(EXTRA_ACRONYM_NAME));
            } else {
                Log.d(TAG, "Unknown action received");
            }
        } else {
            Log.d(TAG, "Null intent received");
        }
    }

    public void doRetrieveAcronymDefinitions(String acronym) {

        boolean success = true;
        if (acronym == null || acronym.isEmpty()) {
            Log.d(TAG, "Acronym is null or empty string");
            success = false;
        }

        ArrayList<Acronym> acronyms = null;
        if (success) {
            // first try to retrieve the information from the cache
            acronyms = retrieveFromCache(acronym);

            // if not found in cache or expired, access network
            if (acronyms == null) {
                acronyms = retrieveFromServer(acronym);
            }

            // TODO if retrieved from network, then add in cache
        }

        // broadcast result back to sender
        if (acronyms != null) {
            publishResults(acronyms, Activity.RESULT_OK);
        } else {
            publishResults(null, Activity.RESULT_CANCELED);
        }
    }

    // search the acronym in the cache and check that it is still valid
    private ArrayList<Acronym> retrieveFromCache(String acronym) {
        // TODO
        return null;
    }

    // retrieve the acronym from the server, performing an HTTP request
    private ArrayList<Acronym> retrieveFromServer(String acronym) {
        AcronymMediator mediator = new AcronymMediator();
        return mediator.retrieveAcronymDefinitions(acronym);
    }

    // publish the results using a local broadcast receiver
    private void publishResults(ArrayList<Acronym> acronyms, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(EXTRA_ACRONYM_LIST, acronyms);
        intent.putExtra(EXTRA_RESULT_STATUS, result);

        sendBroadcast(intent);
    }

}

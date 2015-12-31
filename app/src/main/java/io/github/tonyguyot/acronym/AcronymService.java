package io.github.tonyguyot.acronym;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import java.util.Collection;
import java.util.List;

import io.github.tonyguyot.acronym.data.Acronym;
import io.github.tonyguyot.acronym.data.AcronymList;
import io.github.tonyguyot.acronym.operations.AcronymCacheMediator;
import io.github.tonyguyot.acronym.operations.AcronymHttpMediator;

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

    // parameter for the response error information (mandatory if error)
    private static final String EXTRA_ERROR_CODE = PREFIX + "extra.ERROR_CODE";

    // additional error code in case of HTTP error
    private static final String EXTRA_HTTP_RESPONSE = PREFIX + "extra.HTTP_RESPONSE";

    // expiration period for the data in the cache (in milliseconds)
    private static final long EXPIRATION_PERIOD = 5*24*60*60*1000; // 5 days

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

    // extract the acronym name from the reply intent
    public static String getAcronymName(Intent intent) {
        if (NOTIFICATION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            return bundle.getString(EXTRA_ACRONYM_NAME);
        } else {
            Log.d(TAG, "Unexpected intent action: " + intent.getAction() +
                    "instead of: " + NOTIFICATION);
            return null;
        }
    }

    // extract the list of acronyms from the reply intent
    public static Collection<Acronym> getResultList(Intent intent) {
        if (NOTIFICATION.equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            return bundle.getParcelableArrayList(EXTRA_ACRONYM_LIST);
        } else {
            return null;
        }
    }

    // extract the result status from the reply intent
    public static int getResultStatus(Intent intent) {
        if (NOTIFICATION.equals(intent.getAction())) {
            return intent.getIntExtra(EXTRA_RESULT_STATUS, Activity.RESULT_CANCELED);
        } else {
            return Activity.RESULT_CANCELED;
        }
    }

    // extract error code from the reply intent
    public static boolean isNetworkError(Intent intent) {
        return NOTIFICATION.equals(intent.getAction())
            && intent.getIntExtra(EXTRA_ERROR_CODE, 0) == AcronymList.Status.STATUS_ERROR_NETWORK;
    }
    public static boolean isParsingError(Intent intent) {
        return NOTIFICATION.equals(intent.getAction())
                && intent.getIntExtra(EXTRA_ERROR_CODE, 0) == AcronymList.Status.STATUS_ERROR_PARSING;
    }
    public static boolean isHttpError(Intent intent) {
        return NOTIFICATION.equals(intent.getAction())
                && intent.getIntExtra(EXTRA_ERROR_CODE, 0) == AcronymList.Status.STATUS_ERROR_COMMUNICATION;
    }
    public static int getHttpResponse(Intent intent) {
        if (NOTIFICATION.equals(intent.getAction())) {
            return intent.getIntExtra(EXTRA_HTTP_RESPONSE, 200);
        } else {
            return 200;
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

        AcronymList results;
        boolean newData = false;
        if (success) {
            // first try to retrieve the information from the cache
            AcronymCacheMediator cache = new AcronymCacheMediator(getApplicationContext());
            results = cache.retrieveFromCache(acronym, EXPIRATION_PERIOD);

            // if not found in cache or expired, access network
            if (results.getContent() == null || results.isExpired()) {
                results = retrieveFromServer(acronym);
                newData = true;
            }

            // if retrieved from network, then add in cache
            if (newData) {
                cache.addToCache(results.getContent(), results.isExpired());
            }
        } else {
            results = new AcronymList();
            results.setStatus(AcronymList.Status.STATUS_INVALID_DATA);
        }

        // broadcast result back to sender
        if (results.getContent() != null) {
            publishResultsSuccess(acronym, results.getContent());
        } else {
            publishResultsFailure(acronym, results.getStatus(), results.getAdditionalStatus());
        }
    }

    // retrieve the acronym from the server, performing an HTTP request
    private AcronymList retrieveFromServer(String acronym) {
        AcronymList results = new AcronymList();
        AcronymHttpMediator mediator = new AcronymHttpMediator();
        AcronymHttpMediator.Response resp = mediator.retrieveAcronymDefinitions(acronym);
        results.setContent(resp.mResults);
        switch (resp.mStatus) {
            case AcronymHttpMediator.Response.NETWORK_ERROR:
                results.setStatus(AcronymList.Status.STATUS_ERROR_NETWORK);
                break;
            case AcronymHttpMediator.Response.PARSE_ERROR:
                results.setStatus(AcronymList.Status.STATUS_ERROR_PARSING);
                break;
            case AcronymHttpMediator.Response.HTTP_ERROR:
                results.setStatus(AcronymList.Status.STATUS_ERROR_COMMUNICATION);
                break;
        }
        return results;
    }

    // publish the results using a local broadcast receiver
    private void publishResultsSuccess(String acronym, List<Acronym> results) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(EXTRA_ACRONYM_NAME, acronym);
        intent.putExtra(EXTRA_ACRONYM_LIST, results.toArray());
        intent.putExtra(EXTRA_RESULT_STATUS, Activity.RESULT_OK);

        sendBroadcast(intent);
    }

    // publish the error code using a local broadcast receiver
    private void publishResultsFailure(String acronym, int errorCode,
                                       int additionalErrorCode) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(EXTRA_ACRONYM_NAME, acronym);
        intent.putExtra(EXTRA_ERROR_CODE, errorCode);
        if (errorCode == AcronymList.Status.STATUS_ERROR_COMMUNICATION) {
            intent.putExtra(EXTRA_HTTP_RESPONSE, additionalErrorCode);
        }
        intent.putExtra(EXTRA_RESULT_STATUS, Activity.RESULT_CANCELED);

        sendBroadcast(intent);
    }
}

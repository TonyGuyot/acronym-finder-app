package io.github.tonyguyot.acronym;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import io.github.tonyguyot.acronym.data.Acronym;
import io.github.tonyguyot.acronym.provider.AcronymProvider;

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

    // possible values for the error
    private static final int ERROR_CODE_NETWORK = -1;
    private static final int ERROR_CODE_PARSING = -2;
    private static final int ERROR_CODE_UNKNOWN = -3;
    // ...other error codes are HTTP error codes

    // expiration period for the data in the cache (in milliseconds)
    private static final long EXPIRATION_PERIOD = 5*24*60*60*1000; // 5 days

    // inner class for a response
    private class Result {
        ArrayList<Acronym> list;
        int errorCode;
        boolean hasExpired;
        Result() {
            list = null;
            errorCode = 0;
            hasExpired = false;
        }
    }

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
            && intent.getIntExtra(EXTRA_ERROR_CODE, ERROR_CODE_UNKNOWN) == ERROR_CODE_NETWORK;
    }
    public static boolean isParsingError(Intent intent) {
        return NOTIFICATION.equals(intent.getAction())
                && intent.getIntExtra(EXTRA_ERROR_CODE, ERROR_CODE_UNKNOWN) == ERROR_CODE_PARSING;
    }
    public static boolean isHttpError(Intent intent) {
        return NOTIFICATION.equals(intent.getAction())
                && intent.getIntExtra(EXTRA_ERROR_CODE, ERROR_CODE_UNKNOWN) > 0;
    }
    public static int getHttpResponse(Intent intent) {
        if (NOTIFICATION.equals(intent.getAction())) {
            return intent.getIntExtra(EXTRA_ERROR_CODE, ERROR_CODE_UNKNOWN);
        } else {
            return ERROR_CODE_UNKNOWN;
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

        Result results = new Result();
        boolean newData = false;
        if (success) {
            // first try to retrieve the information from the cache
            results = retrieveFromCache(acronym);

            // if not found in cache or expired, access network
            if (results.list == null) {
                results = retrieveFromServer(acronym);
                newData = true;
            }

            // if retrieved from network, then add in cache
            if (newData) {
                addToCache(results.list, results.hasExpired);
            }
        }

        // broadcast result back to sender
        if (results.list != null) {
            publishResultsSuccess(acronym, results.list);
        } else {
            publishResultsFailure(acronym, results.errorCode);
        }
    }

    // search the acronym in the cache and check that it is still valid
    private Result retrieveFromCache(String name) {
        Result results = new Result();
        long oldestDate = Long.MAX_VALUE;
        String[] projection = {
                AcronymProvider.Metadata.COLUMN_NAME,
                AcronymProvider.Metadata.COLUMN_DEFINITION,
                AcronymProvider.Metadata.COLUMN_COMMENT,
                AcronymProvider.Metadata.COLUMN_INSERTION_DATE,
        };
        String[] selectionArgs = {
                name,
        };
        Cursor cursor = getContentResolver().query(
                AcronymProvider.CONTENT_URI,
                projection,
                AcronymProvider.Metadata.COLUMN_NAME + "= ?", // selection
                selectionArgs,
                null); // sortOrder
        if (cursor == null) {
            Log.d(TAG, "Error when retrieving data from content provider");
        } else if (cursor.getCount() < 1) {
            // nothing found
            cursor.close();
        } else {
            results.list = new ArrayList<>();
            String expansion;
            String comment;
            long insertedDate;
            while (cursor.moveToNext()) {
                expansion = cursor.getString(cursor.getColumnIndex(AcronymProvider.Metadata.COLUMN_DEFINITION));
                comment = cursor.getString(cursor.getColumnIndex(AcronymProvider.Metadata.COLUMN_COMMENT));
                insertedDate = cursor.getLong(cursor.getColumnIndex(AcronymProvider.Metadata.COLUMN_INSERTION_DATE));
                results.list.add(new Acronym.Builder(name, expansion)
                        .comment(comment)
                        .create());
                oldestDate = Math.min(insertedDate, oldestDate);
            }
            cursor.close();
        }

        // check expiration date
        if (results.list != null) {
            if (oldestDate + EXPIRATION_PERIOD < System.currentTimeMillis()) {
                // data is too old
                results.list = null;
                results.hasExpired = true;
            }
        }

        return results;
    }

    // remove the acronym list from the cache
    private void removeFromCache(Collection<Acronym> acronyms) {
        int deleted = 0;
        for (Acronym acronym : acronyms) {
            deleted += deleteByName(acronym.getName());
        }
        Log.d(TAG, deleted + " element(s) deleted from content provider");
    }

    // delete elements from the content provider
    private int deleteByName(String name) {
        String[] selectionArgs = { name };
        return getContentResolver().delete(
                AcronymProvider.CONTENT_URI,
                AcronymProvider.Metadata.COLUMN_NAME + "= ?",
                selectionArgs);
    }

    // add the acronym list to the cache
    private void addToCache(Collection<Acronym> acronyms, boolean doDeletePrevious) {

        if (acronyms == null) {
            return;
        }

        // delete previous items
        if (doDeletePrevious) {
            removeFromCache(acronyms);
        }

        // add the new ones
        for (Acronym acronym : acronyms) {
            addElement(acronym);
        }
    }

    // add one element to the content provider
    private void addElement(Acronym acronym) {
        // create the values
        ContentValues values = new ContentValues();
        values.put(AcronymProvider.Metadata.COLUMN_NAME, acronym.getName());
        values.put(AcronymProvider.Metadata.COLUMN_DEFINITION, acronym.getExpansion());
        if (!TextUtils.isEmpty(acronym.getComment())) {
            values.put(AcronymProvider.Metadata.COLUMN_COMMENT, acronym.getComment());
        }
        values.put(AcronymProvider.Metadata.COLUMN_INSERTION_DATE, System.currentTimeMillis());

        // insert the values
        getContentResolver().insert(AcronymProvider.CONTENT_URI, values);
        Log.d(TAG, "inserted " + acronym.getName() + "(" + acronym.getExpansion() + ")");
    }

    // retrieve the acronym from the server, performing an HTTP request
    private Result retrieveFromServer(String acronym) {
        Result results = new Result();
        AcronymHttpMediator mediator = new AcronymHttpMediator();
        AcronymHttpMediator.Response resp = mediator.retrieveAcronymDefinitions(acronym);
        results.list = resp.mResults;
        switch (resp.mStatus) {
            case AcronymHttpMediator.Response.NETWORK_ERROR:
                results.errorCode = ERROR_CODE_NETWORK;
                break;
            case AcronymHttpMediator.Response.PARSE_ERROR:
                results.errorCode = ERROR_CODE_PARSING;
                break;
            case AcronymHttpMediator.Response.HTTP_ERROR:
                results.errorCode = resp.mHttpResponse;
                break;
        }
        return results;
    }

    // publish the results using a local broadcast receiver
    private void publishResultsSuccess(String acronym, ArrayList<Acronym> results) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(EXTRA_ACRONYM_NAME, acronym);
        intent.putExtra(EXTRA_ACRONYM_LIST, results);
        intent.putExtra(EXTRA_RESULT_STATUS, Activity.RESULT_OK);

        sendBroadcast(intent);
    }

    // publish the error code using a local broadcast receiver
    private void publishResultsFailure(String acronym, int errorCode) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(EXTRA_ACRONYM_NAME, acronym);
        intent.putExtra(EXTRA_ERROR_CODE, errorCode);
        intent.putExtra(EXTRA_RESULT_STATUS, Activity.RESULT_CANCELED);

        sendBroadcast(intent);
    }
}

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
package io.github.tonyguyot.acronym.presenter;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

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
    private static final String PREFIX = "io.github.tonyguyot.acronym.";

    // expiration period for the data in the cache (in milliseconds)
    private static final long EXPIRATION_PERIOD = 5*24*60*60*1000; // 5 days

    // ------ STATIC HELPER INNER CLASSES FOR THE INTENTS ------

    // provide useful methods to deal with the calling intent.
    // the calling intent has the following structure:
    //   * action: ACTION_GET_ACRONYM -> ask to retrieve one acronym definition
    //   * action: ACTION_GET_ACRONYMS -> retrieve all acronyms from cache
    //   * action: ACTION_CLEAR_CACHE -> clear all acronyms in the cache
    //   * extra: EXTRA_ACRONYM_NAME -> name of the acronym to retrieve
    public static class CallingIntent {

        // expected action in the calling intent
        private static final String ACTION_GET_ACRONYM = PREFIX + "action.GET_ACRONYM";
        private static final String ACTION_GET_ACRONYMS = PREFIX + "action.GET_ACRONYMS";
        private static final String ACTION_CLEAR_CACHE = PREFIX + "action.CLEAR_CACHE";

        // expected parameter in the calling intent
        private static final String EXTRA_ACRONYM_NAME = PREFIX + "extra.ACRONYM_NAME";

        // check that the intent is a calling intent
        private static boolean checkIntent(Intent intent) {
            return ACTION_GET_ACRONYM.equals(intent.getAction())
                    || ACTION_GET_ACRONYMS.equals(intent.getAction())
                    || ACTION_CLEAR_CACHE.equals(intent.getAction());
        }

        // create a new calling intent to perform the acronym search operation
        public static Intent makeIntent(Context context, String acronymName) {
            Intent intent = new Intent(context, AcronymService.class);
            intent.setAction(ACTION_GET_ACRONYM);
            intent.putExtra(EXTRA_ACRONYM_NAME, acronymName);
            return intent;
        }

        // create a new calling intent to perform the acronym history list operation
        public static Intent makeIntent(Context context) {
            Intent intent = new Intent(context, AcronymService.class);
            intent.setAction(ACTION_GET_ACRONYMS);
            return intent;
        }

        // create a new calling intent to perform the cache clear operation
        public static Intent makeCacheClearIntent(Context context) {
            Intent intent = new Intent(context, AcronymService.class);
            intent.setAction(ACTION_CLEAR_CACHE);
            return intent;
        }

        // extract the acronym name from the calling intent
        public static String getAcronymName(Intent intent) {
            if (checkIntent(intent)) {
                Bundle bundle = intent.getExtras();
                return bundle.getString(EXTRA_ACRONYM_NAME);
            } else {
                return null;
            }
        }
    }


    // provide useful methods to deal with the reply intent.
    // the reply intent is used to report an answer when the action "search
    // a given acronym" has been sent.
    // the reply intent has the following structure:
    //   * action: ACTION_NOTIFICATION -> notify a result
    //   * extra: EXTRA_ACRONYM_NAME -> name of the processed acronym
    //   * extra: EXTRA_ACRONYM_LIST -> list of retrieved acronym definitions
    //   * extra: EXTRA_RESULT_STATUS -> Android status of the result (OK, Cancelled)
    //   * extra: EXTRA_ERROR_CODE -> if error, indicates which one
    //   * extra: EXTRA_HTTP_RESPONSE -> if HTTP error, indicates which one
    public static class ReplyIntent {

        // notification id for the answer
        private static final String ACTION_NOTIFICATION = PREFIX + "action.notification";

        // same parameter as the calling intent
        private static final String EXTRA_ACRONYM_NAME = PREFIX + "extra.ACRONYM_NAME";

        // parameter for the response content (optional)
        private static final String EXTRA_ACRONYM_LIST = PREFIX + "extra.ACRONYM_LIST";

        // parameter for the response status (mandatory)
        private static final String EXTRA_RESULT_STATUS = PREFIX + "extra.RESULT_STATUS";

        // parameter for the response error information (mandatory if error)
        private static final String EXTRA_ERROR_CODE = PREFIX + "extra.ERROR_CODE";

        // additional error code in case of HTTP error
        private static final String EXTRA_HTTP_RESPONSE = PREFIX + "extra.HTTP_RESPONSE";

        // check that the intent is a reply intent
        private static boolean checkIntent(Intent intent) {
            return (ACTION_NOTIFICATION.equals(intent.getAction()));
        }

        // produce an intent filter for the broadcast receiver
        public static IntentFilter getIntentFilter() {
            return new IntentFilter(ACTION_NOTIFICATION);
        }

        // create a new reply intent to notify success
        public static Intent makeIntentSuccess(String acronymName, ArrayList<Acronym> results) {
            Intent intent = new Intent(ACTION_NOTIFICATION);
            intent.putExtra(EXTRA_ACRONYM_NAME, acronymName);
            intent.putExtra(EXTRA_ACRONYM_LIST, results);
            intent.putExtra(EXTRA_RESULT_STATUS, Activity.RESULT_OK);
            return intent;
        }

        // create a new reply intent to notify failure
        public static Intent makeIntentFailure(String acronymName, int errorCode,
                                               int additionalErrorCode) {
            Intent intent = new Intent(ACTION_NOTIFICATION);
            intent.putExtra(EXTRA_ACRONYM_NAME, acronymName);
            intent.putExtra(EXTRA_ERROR_CODE, errorCode);
            if (errorCode == AcronymList.Status.STATUS_ERROR_COMMUNICATION) {
                intent.putExtra(EXTRA_HTTP_RESPONSE, additionalErrorCode);
            }
            intent.putExtra(EXTRA_RESULT_STATUS, Activity.RESULT_CANCELED);
            return intent;
        }

        // extract the acronym name from the reply intent
        public static String getAcronymName(Intent intent) {
            if (checkIntent(intent)) {
                Bundle bundle = intent.getExtras();
                return bundle.getString(EXTRA_ACRONYM_NAME);
            } else {
                return null;
            }
        }

        // extract the list of acronyms from the reply intent
        public static Collection<Acronym> getResultList(Intent intent) {
            if (checkIntent(intent)) {
                Bundle bundle = intent.getExtras();
                return bundle.getParcelableArrayList(EXTRA_ACRONYM_LIST);
            } else {
                return null;
            }
        }

        // extract the result status from the reply intent
        public static int getResultStatus(Intent intent) {
            if (checkIntent(intent)) {
                return intent.getIntExtra(EXTRA_RESULT_STATUS, Activity.RESULT_CANCELED);
            } else {
                return Activity.RESULT_CANCELED;
            }
        }

        // extract error code from the reply intent
        public static boolean isInvalidDataError(Intent intent) {
            return checkIntent(intent)
                    && intent.getIntExtra(EXTRA_ERROR_CODE, 0)
                        == AcronymList.Status.STATUS_INVALID_DATA;
        }
        public static boolean isNetworkError(Intent intent) {
            return checkIntent(intent)
                    && intent.getIntExtra(EXTRA_ERROR_CODE, 0)
                        == AcronymList.Status.STATUS_ERROR_NETWORK;
        }
        public static boolean isParsingError(Intent intent) {
            return checkIntent(intent)
                    && intent.getIntExtra(EXTRA_ERROR_CODE, 0)
                        == AcronymList.Status.STATUS_ERROR_PARSING;
        }
        public static boolean isHttpError(Intent intent) {
            return checkIntent(intent)
                    && intent.getIntExtra(EXTRA_ERROR_CODE, 0)
                        == AcronymList.Status.STATUS_ERROR_COMMUNICATION;
        }
        public static int getHttpResponse(Intent intent) {
            if (checkIntent(intent)) {
                return intent.getIntExtra(EXTRA_HTTP_RESPONSE, 200);
            } else {
                return 200;
            }
        }
    }

    // provide useful methods to deal with the list intent.
    // the list intent is used to report an answer when the action "search
    // all acronyms in the cache" has been sent.
    // the list intent has the following structure:
    //   * action: ACTION_HISTORY -> notify a result
    //   * extra: EXTRA_ACRONYM_LIST -> list of retrieved acronym definitions
    //   * extra: EXTRA_RESULT_STATUS -> Android status of the result (OK, Cancelled)
    public static class ListIntent {

        // notification id for the answer
        private static final String ACTION_HISTORY = PREFIX + "action.history";

        // parameter for the response content (optional)
        private static final String EXTRA_ACRONYM_LIST = PREFIX + "extra.ACRONYM_LIST";

        // parameter for the response status (mandatory)
        private static final String EXTRA_RESULT_STATUS = PREFIX + "extra.RESULT_STATUS";

        // check that the intent is a reply intent
        private static boolean checkIntent(Intent intent) {
            return (ACTION_HISTORY.equals(intent.getAction()));
        }

        // produce an intent filter for the broadcast receiver
        public static IntentFilter getIntentFilter() {
            return new IntentFilter(ACTION_HISTORY);
        }

        // create a new intent to notify a result
        public static Intent makeIntent(ArrayList<Acronym> results) {
            Intent intent = new Intent(ACTION_HISTORY);
            if (results == null) {
                // an error occurred
                intent.putExtra(EXTRA_RESULT_STATUS, Activity.RESULT_CANCELED);
            } else {
                intent.putExtra(EXTRA_ACRONYM_LIST, results);
                intent.putExtra(EXTRA_RESULT_STATUS, Activity.RESULT_OK);
            }
            return intent;
        }

        // extract the list of acronyms from the reply intent
        public static ArrayList<Acronym> getResultList(Intent intent) {
            if (checkIntent(intent)) {
                Bundle bundle = intent.getExtras();
                return bundle.getParcelableArrayList(EXTRA_ACRONYM_LIST);
            } else {
                return null;
            }
        }

        // extract the result status from the reply intent
        public static int getResultStatus(Intent intent) {
            if (checkIntent(intent)) {
                return intent.getIntExtra(EXTRA_RESULT_STATUS, Activity.RESULT_CANCELED);
            } else {
                return Activity.RESULT_CANCELED;
            }
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
    public static void startRetrieveAcronym(Context context, String acronymName) {
        Intent intent = CallingIntent.makeIntent(context, acronymName);
        context.startService(intent);
    }

    public static void startListContentOfCache(Context context) {
        Intent intent = CallingIntent.makeIntent(context);
        context.startService(intent);
    }

    public static void startClearCache(Context context) {
        Intent intent = CallingIntent.makeCacheClearIntent(context);
        context.startService(intent);
    }

    // ------ LIFECYCLE METHODS ------

    // perform the action in a background thread
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            switch (intent.getAction()) {

                // retrieve a given acronym
                case CallingIntent.ACTION_GET_ACRONYM:

                    // retrieve the name & sanitize it
                    String acronymName = CallingIntent.getAcronymName(intent);
                    String sanitizedAcronymName = sanitizeName(acronymName);
                    if (TextUtils.isEmpty(sanitizedAcronymName)) {
                        // no need to perform the operation
                        publishResultsFailure(acronymName, AcronymList.Status.STATUS_INVALID_DATA, 0);
                    } else {

                        // perform the operation
                        AcronymList results = doRetrieveAcronymDefinitions(sanitizedAcronymName);

                        // broadcast result back to sender
                        if (results.getContent() != null) {
                            publishResultsSuccess(sanitizedAcronymName, results.getContent());
                        } else {
                            publishResultsFailure(sanitizedAcronymName, results.getStatus(), results.getAdditionalStatus());
                        }
                    }
                    break;

                // retrieve all acronyms
                case CallingIntent.ACTION_GET_ACRONYMS:
                    // perform the operation
                    AcronymList list = doRetrieveAllAcronymDefinitions();

                    // broadcast result back to sender
                    Intent reply = ListIntent.makeIntent(list.getContent());
                    sendBroadcast(reply);
                    break;

                // clear all acronyms in the cache
                case CallingIntent.ACTION_CLEAR_CACHE:
                    // perform the operation
                    doClearCache();

                    // broadcast result (= an empty list) back to sender
                    reply = ListIntent.makeIntent(new ArrayList<Acronym>());
                    sendBroadcast(reply);
                    break;

                default:
                    Log.d(TAG, "Unknown action received");
            }
        } else {
            Log.d(TAG, "Null intent received");
        }
    }

    ////////////////////
    // Helper methods
    ////////////////////

    /**
     * Ensure that the name does not contain illegal characters.
     * This will help to prevent any SQL injection attempts from malicious
     * users.
     */
    private String sanitizeName(String name) {
        if (name != null) {
            // keep only letters, numbers and some punctuations
            return name.replaceAll("[^A-Za-z0-9._-]", "");
        }
        return null;
    }

    // retrieve all definitions of a given acronym
    // from the cache or from the Acronym server if not in the cache
    public AcronymList doRetrieveAcronymDefinitions(String acronymName) {

        boolean success = true;
        if (acronymName == null || acronymName.isEmpty()) {
            Log.d(TAG, "Acronym is null or empty string");
            success = false;
        }

        AcronymList results;
        boolean newData = false;
        if (success) {
            // first try to retrieve the information from the cache
            AcronymCacheMediator cache = new AcronymCacheMediator(getApplicationContext());
            results = cache.retrieveFromCache(acronymName, EXPIRATION_PERIOD);

            // if not found in cache or expired => access network
            boolean noContent = results.getContent() == null || results.getContent().isEmpty();
            boolean isExpired = results.isExpired();
            if (noContent || isExpired) {
                AcronymHttpMediator mediator = new AcronymHttpMediator();
                results = mediator.retrieveFromServer(acronymName);
                newData = true;
            }

            // if retrieved from network, then add in cache
            if (newData) {
                cache.addToCache(results.getContent(), isExpired);
            }
        } else {
            results = new AcronymList();
            results.setStatus(AcronymList.Status.STATUS_INVALID_DATA);
        }
        return results;
    }

    // report all acronyms definitions found in the cache
    public AcronymList doRetrieveAllAcronymDefinitions() {
        AcronymCacheMediator cache = new AcronymCacheMediator(getApplicationContext());
        return cache.retrieveAllFromCache();
    }

    // clear all the elements in the cache
    public void doClearCache() {
        AcronymCacheMediator cache = new AcronymCacheMediator(getApplicationContext());
        cache.removeAllFromCache();
    }

    // publish the results using a local broadcast receiver
    private void publishResultsSuccess(String acronymName, ArrayList<Acronym> results) {
        Intent intent = ReplyIntent.makeIntentSuccess(acronymName, results);
        sendBroadcast(intent);
    }

    // publish the error code using a local broadcast receiver
    private void publishResultsFailure(String acronymName, int errorCode,
                                       int additionalErrorCode) {
        Intent intent = ReplyIntent.makeIntentFailure(acronymName, errorCode, additionalErrorCode);
        sendBroadcast(intent);
    }
}

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
package io.github.tonyguyot.acronym.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import io.github.tonyguyot.acronym.data.Acronym;
import io.github.tonyguyot.acronym.data.AcronymList;
import io.github.tonyguyot.acronym.provider.AcronymProvider;

/**
 * This class provides the actual cache-related actions
 * which are possible on an acronym.
 * The cache is actually implemented with a content provider
 * backed by an SQLite database.
 */
public class AcronymCacheMediator {

    // tag for logging information
    private static final String TAG = "AcronymCacheMediator";

    // the application context
    private Context mContext;

    // constructor
    public AcronymCacheMediator(Context context) {
        mContext = context;
    }

    // search all acronyms in the cache
    public AcronymList retrieveAllFromCache() {
        return retrieveFromCache(null, -1L);
    }

    // search the acronym in the cache and check that it is still valid
    public AcronymList retrieveFromCache(String acronymName, long expirationPeriod) {
        AcronymList results = new AcronymList();

        // prepare parameters
        long oldestDate = Long.MAX_VALUE;
        String[] projection = {
                AcronymProvider.Metadata.COLUMN_NAME,
                AcronymProvider.Metadata.COLUMN_DEFINITION,
                AcronymProvider.Metadata.COLUMN_COMMENT,
                AcronymProvider.Metadata.COLUMN_INSERTION_DATE,
        };
        String selection = AcronymProvider.Metadata.COLUMN_NAME + "= ?";
        String[] selectionArgs = {
                acronymName,
        };
        if (TextUtils.isEmpty(acronymName)) {
            selection = null;
            selectionArgs = null;
        }

        // perform the query
        Cursor cursor = mContext.getContentResolver().query(
                AcronymProvider.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null); // sortOrder

        // process results
        if (cursor == null) {
            Log.d(TAG, "Error when retrieving data from content provider");
            results.setStatus(AcronymList.Status.STATUS_ERROR_SYSTEM);
        } else if (cursor.getCount() < 1) {
            // nothing found
            Log.d(TAG, "No result found for: " + acronymName);
            results.setContent(new ArrayList<Acronym>());
            cursor.close();
        } else {
            Log.d(TAG, cursor.getCount() + " results found for: " + acronymName);
            ArrayList<Acronym> list = new ArrayList<>();
            String name;
            String expansion;
            String comment;
            long insertedDate;
            while (cursor.moveToNext()) {
                name = cursor.getString(cursor.getColumnIndex(AcronymProvider.Metadata.COLUMN_NAME));
                expansion = cursor.getString(cursor.getColumnIndex(AcronymProvider.Metadata.COLUMN_DEFINITION));
                comment = cursor.getString(cursor.getColumnIndex(AcronymProvider.Metadata.COLUMN_COMMENT));
                insertedDate = cursor.getLong(cursor.getColumnIndex(AcronymProvider.Metadata.COLUMN_INSERTION_DATE));
                list.add(new Acronym.Builder(name, expansion)
                        .comment(comment)
                        .create());
                oldestDate = Math.min(insertedDate, oldestDate);
                Log.d(TAG, "found " + name + " (" + expansion + ") from content provider");
            }
            cursor.close();
            results.setContent(list);
        }

        // check expiration date
        if (expirationPeriod > 0L) {
            if (results.getContent() != null && !results.getContent().isEmpty()) {
                if (oldestDate + expirationPeriod < System.currentTimeMillis()) {
                    // data is too old
                    Log.d(TAG, "mark data as expired");
                    results.setAsExpired();
                }
            }
        }

        return results;
    }

    // remove the acronym list from the cache
    public void removeFromCache(Collection<Acronym> acronyms) {
        int deleted = 0;
        Set<String> deletedNames = new TreeSet<>();
        for (Acronym acronym : acronyms) {
            if (!deletedNames.contains(acronym.getName())) {
                deleted += deleteByName(acronym.getName());
                deletedNames.add(acronym.getName());
            }
        }
        Log.d(TAG, deleted + " element(s) deleted from content provider");
    }

    // remove everything from the cache
    public void removeAllFromCache() {
        int deleted = mContext.getContentResolver().delete(
                AcronymProvider.CONTENT_URI,
                null, // no selection = everything
                null); // no selection args
        Log.d(TAG, deleted + " element(s) deleted from content provider");
    }

    // delete elements from the content provider
    private int deleteByName(String name) {
        String[] selectionArgs = { name };
        return mContext.getContentResolver().delete(
                AcronymProvider.CONTENT_URI,
                AcronymProvider.Metadata.COLUMN_NAME + "= ?",
                selectionArgs);
    }

    // add the acronym list to the cache
    public void addToCache(Collection<Acronym> acronyms, boolean doDeletePrevious) {

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
        mContext.getContentResolver().insert(AcronymProvider.CONTENT_URI, values);
        Log.d(TAG, "inserted " + acronym.getName() + " (" + acronym.getExpansion() + ")");
    }
}

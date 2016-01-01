package io.github.tonyguyot.acronym.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.tonyguyot.acronym.AcronymService;
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

    // search the acronym in the cache and check that it is still valid
    public AcronymList retrieveFromCache(String name, long expirationPeriod) {
        AcronymList results = new AcronymList();

        // prepare parameters
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

        // perform the query
        Cursor cursor = mContext.getContentResolver().query(
                AcronymProvider.CONTENT_URI,
                projection,
                AcronymProvider.Metadata.COLUMN_NAME + "= ?", // selection
                selectionArgs,
                null); // sortOrder

        // process results
        if (cursor == null) {
            Log.d(TAG, "Error when retrieving data from content provider");
            // TODO: set an error as response
        } else if (cursor.getCount() < 1) {
            // nothing found
            // TODO: return empty list
            cursor.close();
        } else {
            ArrayList<Acronym> list = new ArrayList<>();
            String expansion;
            String comment;
            long insertedDate;
            while (cursor.moveToNext()) {
                expansion = cursor.getString(cursor.getColumnIndex(AcronymProvider.Metadata.COLUMN_DEFINITION));
                comment = cursor.getString(cursor.getColumnIndex(AcronymProvider.Metadata.COLUMN_COMMENT));
                insertedDate = cursor.getLong(cursor.getColumnIndex(AcronymProvider.Metadata.COLUMN_INSERTION_DATE));
                list.add(new Acronym.Builder(name, expansion)
                        .comment(comment)
                        .create());
                oldestDate = Math.min(insertedDate, oldestDate);
            }
            cursor.close();
            results.setContent(list);
        }

        // check expiration date
        if (results.getContent() != null) {
            if (oldestDate + expirationPeriod < System.currentTimeMillis()) {
                // data is too old
                results.setAsExpired();
            }
        }

        return results;
    }

    // remove the acronym list from the cache
    public void removeFromCache(Collection<Acronym> acronyms) {
        int deleted = 0;
        for (Acronym acronym : acronyms) {
            deleted += deleteByName(acronym.getName());
        }
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
        Log.d(TAG, "inserted " + acronym.getName() + "(" + acronym.getExpansion() + ")");
    }
}

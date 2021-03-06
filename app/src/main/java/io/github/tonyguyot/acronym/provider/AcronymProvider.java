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
package io.github.tonyguyot.acronym.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import io.github.tonyguyot.acronym.database.AcronymDatabaseHelper;
import io.github.tonyguyot.acronym.database.AcronymTable;

public class AcronymProvider extends ContentProvider {

    // inner class to define constants about the content provider
    public static class Metadata {
        public static final String COLUMN_NAME = AcronymTable.COLUMN_NAME;
        public static final String COLUMN_DEFINITION = AcronymTable.COLUMN_DEFINITION;
        public static final String COLUMN_COMMENT = AcronymTable.COLUMN_COMMENT;
        public static final String COLUMN_INSERTION_DATE = AcronymTable.COLUMN_INSERTION_DATE;
    }

    // database
    private AcronymDatabaseHelper mDatabase;

    // URI
    private static final String SCHEME = "content://";
    private static final String AUTHORITY = "io.github.tonyguyot.acronym.provider";
    private static final String PATH = "acronym";
    public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + "/" + PATH);

    // UriMatcher
    private static final int MATCH_ACRONYMS = 1;
    private static final int MATCH_ACRONYM_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, PATH, MATCH_ACRONYMS);
        sUriMatcher.addURI(AUTHORITY, PATH + "/#", MATCH_ACRONYM_ID);
    }

    // constructor
    public AcronymProvider() {
    }

    @Override
    public boolean onCreate() {
        mDatabase = new AcronymDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // build the query
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(AcronymTable.TABLE_ACRONYM);

        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case MATCH_ACRONYMS:
                break;
            case MATCH_ACRONYM_ID:
                queryBuilder.appendWhere(AcronymTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // execute the query
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db,
                projection,
                selection,
                selectionArgs,
                null, // groupBy
                null, // having
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        long id = 0;
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case MATCH_ACRONYMS:
                id = db.insert(AcronymTable.TABLE_ACRONYM, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(PATH + "/" + id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        int rowsUpdated = 0;

        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case MATCH_ACRONYMS:
                rowsUpdated = db.update(AcronymTable.TABLE_ACRONYM,
                        values, selection, selectionArgs);
                break;
            case MATCH_ACRONYM_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(AcronymTable.TABLE_ACRONYM,
                            values, AcronymTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = db.update(AcronymTable.TABLE_ACRONYM, values,
                            AcronymTable.COLUMN_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        int rowsDeleted = 0;

        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case MATCH_ACRONYMS:
                rowsDeleted = db.delete(AcronymTable.TABLE_ACRONYM,
                        selection, selectionArgs);
                break;
            case MATCH_ACRONYM_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(AcronymTable.TABLE_ACRONYM,
                            AcronymTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(AcronymTable.TABLE_ACRONYM,
                            AcronymTable.COLUMN_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}

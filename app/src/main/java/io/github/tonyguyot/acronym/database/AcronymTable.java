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
package io.github.tonyguyot.acronym.database;

import android.database.sqlite.SQLiteDatabase;

/**
 *
 */
public class AcronymTable {

    // Table name
    public static final String TABLE_ACRONYM = "acronym";

    // Column names
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DEFINITION = "definition";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_INSERTION_DATE = "inserted";

    // SQL commands
    public static final String TABLE_CREATION_CMD =
            "create table " + TABLE_ACRONYM + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_NAME + " text not null, " +
            COLUMN_DEFINITION + " text not null, " +
            COLUMN_COMMENT + " text, " +
            COLUMN_INSERTION_DATE + " integer not null);";
    public static final String TABLE_DELETION_CMD =
            "drop table if exists " + TABLE_ACRONYM;

    // methods for operations on the database

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_CREATION_CMD);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            database.execSQL(TABLE_DELETION_CMD);
            onCreate(database);
        }
    }
}

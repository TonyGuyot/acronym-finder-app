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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 */
public class AcronymDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "acronym.db";
    private static final int DATABASE_VERSION = 1;

    // constructor
    public AcronymDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // this method is called during the creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        AcronymTable.onCreate(database);
    }

    // this method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        AcronymTable.onUpgrade(database, oldVersion, newVersion);
    }
}

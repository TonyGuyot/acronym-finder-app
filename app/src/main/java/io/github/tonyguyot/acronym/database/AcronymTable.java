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
        database.execSQL(TABLE_DELETION_CMD);
        onCreate(database);
    }
}

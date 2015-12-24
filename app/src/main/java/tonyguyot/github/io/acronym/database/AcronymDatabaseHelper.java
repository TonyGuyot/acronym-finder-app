package tonyguyot.github.io.acronym.database;

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

package mc.android.fhooe.at.geofencemonitoring.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by chris on 18.12.2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = DatabaseHelper.class.getSimpleName();

    // Table Name
    public static final String TABLE_GEOFENCES = "db_geofences";
    // Collumns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME= "na";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_RAD = "rad";
    public static final String COLUMN_EXP = "exp";
    public static final String COLUMN_TRA = "tra";
    public static final String COLUMN_TIME = "tim"; //timestap data


    private static final String DATABASE_NAME = "geofences.db";
    private static final int DATABASE_VERSION = 1;

    // create DATABASE sql Statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_GEOFENCES + "(" + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_NAME + " string , "
            + COLUMN_LAT + " integer , "
            + COLUMN_LNG + " integer , "
            + COLUMN_RAD + " integer , "
            + COLUMN_EXP + " long , "
            + COLUMN_TRA + " long , "
            + COLUMN_TIME + " long );";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,
              "Upgrading database from version " + oldVersion + " to "
                      + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOFENCES);
        onCreate(db);
    }
}

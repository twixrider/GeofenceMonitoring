package mc.android.fhooe.at.geofencemonitoring.data;

import com.google.android.gms.location.Geofence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import mc.android.fhooe.at.geofencemonitoring.helper.DatabaseHelper;
import mc.android.fhooe.at.geofencemonitoring.helper.GeofenceUtils;

/**
 * Created by chris on 18.12.2014.
 */
public class GeofenceDatasource {



    // Database fields
    public static SQLiteDatabase mDatabase;
    public static DatabaseHelper mDBHelper;
    private String[] allColumns = { DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_NAME, DatabaseHelper.COLUMN_LAT,
            //DatabaseHelper.COLUMN_LNG, DatabaseHelper.COLUMN_RAD, DatabaseHelper.COLUMN_EXP, DatabaseHelper.COLUMN_TRA };
            DatabaseHelper.COLUMN_LNG, DatabaseHelper.COLUMN_RAD, DatabaseHelper.COLUMN_EXP, DatabaseHelper.COLUMN_TRA, DatabaseHelper.COLUMN_TIME };


    public GeofenceDatasource(Context _context) {
        mDBHelper = new DatabaseHelper(_context);
    }

    public void open() throws SQLException {
        mDatabase = mDBHelper.getWritableDatabase();
    }

    public void close() {
        mDBHelper.close();
    }

    public SimpleGeofence createGeofence(SimpleGeofence _fence) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, _fence.getName());
        values.put(DatabaseHelper.COLUMN_LAT, _fence.getLatitude());
        values.put(DatabaseHelper.COLUMN_LNG, _fence.getLongitude());
        values.put(DatabaseHelper.COLUMN_RAD, _fence.getRadius());
        values.put(DatabaseHelper.COLUMN_EXP, Geofence.NEVER_EXPIRE);
        values.put(DatabaseHelper.COLUMN_TRA, Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_EXIT);
        values.put(DatabaseHelper.COLUMN_TIME, _fence.getTime());

        long insertId = mDatabase.insert(DatabaseHelper.TABLE_GEOFENCES, null,
                                        values);
        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_GEOFENCES,
                                       allColumns, DatabaseHelper.COLUMN_ID + " = " + insertId, null,
                                       null, null, null);
        cursor.moveToFirst();
        SimpleGeofence simpleGeofence = cursorToGeofence(cursor);
        cursor.close();
        return simpleGeofence;
    }

    /**
     * Delete geofence with id.
     * @param _id id which needs to get deleted.
     */
    public void deleteGeofenceWithId(int _id) {
        mDatabase.delete(DatabaseHelper.TABLE_GEOFENCES, DatabaseHelper.COLUMN_ID + " = " + _id, null);
    }

    public void deleteGeofence(SimpleGeofence _fence) {
        long id = _fence.getId();
        mDatabase.delete(DatabaseHelper.TABLE_GEOFENCES, DatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    /**
     * Returns a List with Geofences all geofences in it.
     * @return a List with SimpleGeofence Objects.
     */
    public List<SimpleGeofence> getAllFences() {

        List<SimpleGeofence> simpleGeofences = new ArrayList<SimpleGeofence>();

        Cursor cursor = mDatabase.query(DatabaseHelper.TABLE_GEOFENCES,
                                       allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SimpleGeofence fence = cursorToGeofence(cursor);
            simpleGeofences.add(fence);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return simpleGeofences;
    }

    /**
     * get the number of all fences which are passed today
     * @return: num of fences which are passed.
     */
    public int getPassedFenceNum() {
        List<SimpleGeofence> simpleGeofences = getAllFences();
        int passedFences = 0;
        Date dateNow = new Date(System.currentTimeMillis());
        long timenow = dateNow.getTime();
        for(SimpleGeofence fence : simpleGeofences) {
            if((timenow/1000) - (fence.getTime()/1000) < GeofenceUtils.DIFFERENCE_SECONDS) { //1 day in seconds = 86400, values/1000 because they are in milli
                passedFences++;
            }
        }
        return passedFences;
    }

    /**
     * Creates a SimpleGeofence object from the database object.
     * @param cursor the database object
     * @return SimpleGeofence Object.
     */
    private SimpleGeofence cursorToGeofence(Cursor cursor) {
        SimpleGeofence fence = new SimpleGeofence();
        fence.setId(cursor.getInt(0));
        fence.setName(cursor.getString(1));
        fence.setLatitude(cursor.getDouble(2));
        fence.setLongitude(cursor.getDouble(3));
        fence.setRadius(cursor.getInt(4));
        return fence;
    }

    /**
     * Database updaste
     * @param _id String array with all Ids saved in.
     * @param _timestamp current timestamp in milliseconds
     */
    public void updateRowById(String[] _id, long _timestamp) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TIME, _timestamp);

        //set in DB
        for(int i = 0; i < _id.length; i++) {
            String where = mDBHelper.COLUMN_ID + " = " + _id[i];
            mDatabase.update(mDBHelper.TABLE_GEOFENCES, values, where, null);
        }
    }

}

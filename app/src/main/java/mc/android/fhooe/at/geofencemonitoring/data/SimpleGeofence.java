package mc.android.fhooe.at.geofencemonitoring.data;

import com.google.android.gms.location.Geofence;

import android.database.Cursor;

import java.sql.Date;

import mc.android.fhooe.at.geofencemonitoring.ui.MainActivity;

/**
 * Created by chris on 18.12.2014.
 *
 * A Simple Geofence Object with Lat, Long, Radius,...
 */
public class SimpleGeofence {

    // Instance variables
    private int mId;
    private String mName;
    private double mLatitude;
    private double mLongitude;
    private int mRadius;
    private long mExpirationDuration;
    private int mTransitionType;
    private long mTimestamp;

    public SimpleGeofence() {
        setExpirationDuration(Geofence.NEVER_EXPIRE);
        setTransitionType(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
    }

    /**
     * @param latitude   Latitude of the Geofence's center.
     * @param longitude  Longitude of the Geofence's center.
     * @param radius     Radius of the geofence circle.
     * @param transition Type of Geofence transition.
     */
    public SimpleGeofence(String _name, double latitude, double longitude,
            int radius, long expiration, int transition) {
        // Set the instance fields from the constructor
        this.mName = _name;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mExpirationDuration = expiration;
        this.mTransitionType = transition;
        this.mTimestamp = 0;
    }

    // Instance field getters
    public int getId() {
        return mId;
    }

    public String getName() { return mName; }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public int getRadius() {
        return mRadius;
    }

    public long getExpirationDuration() {
        return mExpirationDuration;
    }

    public int getTransitionType() {
        return mTransitionType;
    }

    public void setId(int _id) {
        mId = _id;
    }

    public void setName(String _name) {mName = _name; }

    public void setLatitude(double _latitude) {
        mLatitude = _latitude;
    }

    public void setLongitude(double _longitude) {
        mLongitude = _longitude;
    }

    public void setRadius(int _radius) {
        mRadius = _radius;
    }

    public void setExpirationDuration(long _expirationDuration) {
        mExpirationDuration = _expirationDuration;
    }

    public void setTransitionType(int _transitionType) {
        mTransitionType = _transitionType;
    }

    //get time from DB!!!
    public long getTime() {
        String selectQuery = "SELECT " + MainActivity.mDbHelper.COLUMN_TIME + " FROM " + MainActivity.mDbHelper.TABLE_GEOFENCES +
                " WHERE " + MainActivity.mDbHelper.COLUMN_ID + " = '" + getId() + "';";
        Cursor c = MainActivity.mDbHelper.getWritableDatabase().rawQuery(selectQuery, new String[] {});
        if(c.moveToFirst()) {
            mTimestamp = c.getLong(c.getColumnIndex(MainActivity.mDbHelper.COLUMN_TIME));
        }
        c.close();
        //MainActivity.mDbHelper.getWritableDatabase().execSQL(selectQuery);
        return mTimestamp;
    }


    /**
     * Creates a Location Services Geofence object from a
     * SimpleGeofence.
     *
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        // Build a new Geofence object
        return new Geofence.Builder()
                .setRequestId(String.valueOf(getId()))
                .setTransitionTypes(mTransitionType)
                .setCircularRegion(getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(mExpirationDuration)
                .build();
    }

    /**
     * Creates a String presentaion of a Simple Geofence.
     * @return String from the Simplegeofence.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        Date date = new Date(getTime());
        //buff.append(getId() + "| Lat: " + getLatitude() + " Lng: " + getLongitude() + " Rad: " + getRadius());
        buff.append(getId() + " (" + getName() + ")  Lat: " + getLatitude() + " Lng: " + getLongitude() + " Last Enter: " + date.toString());

        return buff.toString();
    }
}

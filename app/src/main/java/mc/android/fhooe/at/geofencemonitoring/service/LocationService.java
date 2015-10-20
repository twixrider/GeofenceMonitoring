package mc.android.fhooe.at.geofencemonitoring.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mc.android.fhooe.at.geofencemonitoring.ui.LocationActivity;
import mc.android.fhooe.at.geofencemonitoring.R;
import mc.android.fhooe.at.geofencemonitoring.data.MyLocation;

/**
 * Created by chris on 16.10.2014.
 */
public class LocationService extends Service {

    // Variables which get set while service is running
    long mStartTime;
    long mStopTime;
    int mLocationCount = 0; // how many different locations are counted;
    List<MyLocation> mLocationList = new ArrayList<MyLocation>();

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // in Meters
    private static final long MIN_TIME_BETWEEN_UPDATES = 10000; // in Milliseconds

    private NotificationManager mNotiMgr;
    private int mID = 12345;

    protected LocationManager mLocationMgr;

    @Override
    public IBinder onBind(Intent _intent) {
        return null;
    }

    /**
     * This method gets called from the application,...
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartTime = System.currentTimeMillis();
        initService();
        initNotification();
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        return START_STICKY; // keep the service running until it gets
    }

    /**
     * This method gets called from the application,...
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mStopTime = System.currentTimeMillis();
        long diffTime = mStopTime - mStartTime;
        int seconds = (int) (diffTime / 1000) % 60;
        int minutes = (int) ((diffTime / (1000 * 60)) % 60);
        int hours = (int) ((diffTime / (1000 * 60 * 60)) % 24);

        mNotiMgr.cancel(mID);

        Toast.makeText(this, "Service Stopped \n runtime: " + hours + "h " + minutes + "m " + seconds + "s \n Locations found: " + mLocationCount,
                       Toast.LENGTH_SHORT).show();

        StringBuffer buff = new StringBuffer();
        buff.append("Locations found: \n");
        int i = 0;
        for(MyLocation loc : mLocationList) {
            buff.append(i +" | Lat: " + loc.mLat + " Lng: " + loc.mLng + "\n");
            i++;
        }
        LocationActivity.mListTv.setText(buff.toString());
        mLocationCount = 0;
    }

    private void initService() {
        mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener());
    }

    /**
     * Creates a Statusbar- icon
     */
    private void initNotification() {
        Context context = getApplicationContext();
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher);

        Intent intent = new Intent(context, LocationActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, mID , intent, 0);
        builder.setContentIntent(pIntent);
        mNotiMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notif = builder.build();
        mNotiMgr.notify(mID, notif);
    }

    /**
     * Creates a Toastmessage with the Latitude and Longitude from the current Position.
     */
    protected void showCurrentLocation() {
        Location location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            String message;
            if (mLocationCount < 1) {
                message = String.format("First Location \n Lat: " + location.getLatitude() + "\n Lon: " + location.getLongitude());
            } else {
                message = String.format("New Location \n Lat: " + location.getLatitude() + "\n Lon: " + location.getLongitude());
            }
            MyLocation loc = new MyLocation(location.getLatitude(), location.getLongitude());
            mLocationList.add(loc);
            mLocationCount++;
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            showCurrentLocation();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String msg = "Provider status changed"; //e.g no gps connection
            Toast.makeText(LocationService.this, msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(LocationService.this, "Provider enabled by the user. GPS turned on", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(LocationService.this, "Provider disabled by the user. GPS turned off", Toast.LENGTH_SHORT).show();
        }
    }
}

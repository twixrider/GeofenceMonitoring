package mc.android.fhooe.at.geofencemonitoring.ui;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import mc.android.fhooe.at.geofencemonitoring.GeofenceRemover;
import mc.android.fhooe.at.geofencemonitoring.GeofenceRequester;
import mc.android.fhooe.at.geofencemonitoring.R;
import mc.android.fhooe.at.geofencemonitoring.data.GeofenceDatasource;
import mc.android.fhooe.at.geofencemonitoring.data.SimpleGeofence;
import mc.android.fhooe.at.geofencemonitoring.helper.DatabaseHelper;
import mc.android.fhooe.at.geofencemonitoring.helper.GeofenceUtils;

/**
 * Created by chris on 18.12.2014.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private final String TAG = LocationActivity.class.getSimpleName();

    private boolean mUnlocked = false;
    // Internal List of Geofence objects
    public static GeofenceDatasource mDatasource;
    public static DatabaseHelper mDbHelper;

    // Geofence Members
    GeofenceRequester mGeofenceRequester;
    GeofenceRemover mGeofenceRemover;
    SimpleGeofence mSimpleGeofence;
    // Store a list of geofences to add
    List<Geofence> mCurrentGeofences;

    // Store the list of geofences to remove
    private List<String> mGeofenceIdsToRemove;

    // Defines the allowable request types.
    // Store the current request
    private GeofenceUtils.REQUEST_TYPE mRequestType;

    // Store the current type of removal
    private GeofenceUtils.REMOVE_TYPE mRemoveType;

    /*
     * An instance of an inner class that receives broadcasts from listeners and from the
     * IntentService that receives geofence transition events
     */
    private GeofenceSampleReceiver mBroadcastReceiver;

    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

    // UI fields
    Crouton mCrouton;
    private EditText mLat;
    private EditText mLng;
    private EditText mRad;
    private EditText mName;
    private Button mAddBt;
    private Button mShowBt;
    private Button mDelBt;
    private Button mOpenServiceBt;
    private TextView mStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new GeofenceSampleReceiver();

        // Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

        // Action for broadcast Intents that report that geofence transition is done
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_TRANSITION);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();

        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(this);

        // Instantiate a Geofence remover
        mGeofenceRemover = new GeofenceRemover(this);

        // Attach to the main UI
        setContentView(R.layout.activity_main);

        // Database
        mDatasource = new GeofenceDatasource(getBaseContext());
        mDatasource.open();
        mDbHelper = new DatabaseHelper(getBaseContext());
        // get Items from Main Layoutfor UI
        mLat = (EditText) findViewById(R.id.activity_main_et_lat);
        mLng = (EditText) findViewById(R.id.activity_main_et_lng);
        mRad = (EditText) findViewById(R.id.activity_main_et_rad);
        mName = (EditText) findViewById(R.id.activity_main_et_name);
        mAddBt = (Button) findViewById(R.id.activity_main_bt_add);
        mAddBt.setOnClickListener(this);
        mShowBt = (Button) findViewById(R.id.activity_main_bt_show);
        mShowBt.setOnClickListener(this);
        mDelBt = (Button) findViewById(R.id.activity_main_bt_del);
        mDelBt.setOnClickListener(this);
        mOpenServiceBt = (Button) findViewById(R.id.activity_main_bt_start_service);
        mOpenServiceBt.setOnClickListener(this);
        mStatus = (TextView) findViewById(R.id.activity_main_tv_status);
    }

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * GeofenceRemover and GeofenceRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     * calls
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to add geofences
                        if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Toggle the request flag and send a new request
                            mGeofenceRequester.setInProgressFlag(false);

                            // Restart the process of adding the current geofences
                            mGeofenceRequester.addGeofences(mCurrentGeofences);

                            // If the request was to remove geofences
                         } else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType) {

                            // Toggle the removal flag and send a new removal request
                            mGeofenceRemover.setInProgressFlag(false);

                            // If the removal was by Intent
                            if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

                                // Restart the removal of all geofences for the PendingIntent
                                mGeofenceRemover.removeGeofencesByIntent(
                                        mGeofenceRequester.getRequestPendingIntent());

                                // If the removal was by a List of geofence IDs
                            } else {
                                // Restart the removal of the geofence list
                                mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
                            }
                        }
                        break;

                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to resolve the problem.
                        Log.i(TAG, "Google Play Services is unable to resolve the problem");
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.i(TAG, "Unknown Requestcode received: " + requestCode);
                break;
        }
    }

    @Override
    protected void onResume() {
        mDatasource.open();
        super.onResume();
        // Register the broadcast receiver to receive status updates
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
        /*
         * Get existing geofences if stored in Database. If no values
         * exist, null is returned.
         */
        List<SimpleGeofence> allFences = new ArrayList<SimpleGeofence>();
        allFences = mDatasource.getAllFences();

        if (allFences.size() > 0) {
            mSimpleGeofence = allFences.get(allFences.size() - 1);
            for (SimpleGeofence fence : allFences) {
                mCurrentGeofences.add(fence.toGeofence());
            }
        }

        int numPassedFences = mDatasource.getPassedFenceNum();

        if (numPassedFences >= allFences.size() - 1 && allFences.size() >= GeofenceUtils.MIN_NUM_PASSED_FENCES) {
            mStatus.setText("UNLOCKED");
            mStatus.setBackgroundColor(Color.GREEN);
            mUnlocked = true;
        } else {
            mStatus.setText("Today passed: " + numPassedFences + "/ " + allFences.size());
            mStatus.setBackgroundColor(Color.TRANSPARENT);
            mUnlocked = false;
        }
    }

    /**
     * API for other external access!
     * @return true if a user is able to authenticate, false if not.
     */
    private boolean unlocked() {
        return mUnlocked;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
//        if (checkInputFields()) {
//
//            SimpleGeofence fence = new SimpleGeofence(mName.getText().toString(),
//                                                      Double.valueOf(mLat.getText().toString()),
//                                                      Double.valueOf(mLng.getText().toString()),
//                                                      Integer.valueOf(mRad.getText().toString()),
//                                                      Geofence.NEVER_EXPIRE,
//                                                      (Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT));
//            // Add to Database
//            mDatasource.createGeofence(fence);
//        }
    }

    @Override
    protected void onDestroy() {
        mDatasource.close();
        super.onDestroy();
    }

    /*
     * For Button Events
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == findViewById(R.id.activity_main_bt_add).getId()) {
            Log.i(TAG, "ADD Button Clicked");
            onRegister(v);
            // Create error Crouton
            Configuration config = new Configuration.Builder().setDuration(Configuration.DURATION_SHORT).build();
            mCrouton = Crouton.makeText(this, "Geofence Added!", Style.INFO);
            mCrouton.setConfiguration(config).show();
            //Clear Text
            mLat.setText("");
            mLng.setText("");
            mRad.setText("");
            mName.setText("");
        } else if (v.getId() == findViewById(R.id.activity_main_bt_show).getId()) {
            Log.i(TAG, "SHOW Button Clicked");
            Intent i = new Intent(getBaseContext(), ShowGeofences.class);
            startActivity(i);
        } else if (v.getId() == findViewById(R.id.activity_main_bt_del).getId()) {
            Log.i(TAG, "DEL Button Clicked");
            onUnregister(v);

            mLat.setText("");
            mLng.setText("");
            mRad.setText("");
            mName.setText("");

            // DEL from Database
            for (SimpleGeofence fence : mDatasource.getAllFences()) {
                mDatasource.deleteGeofenceWithId(fence.getId());
            }
            mCurrentGeofences.clear();
            // Create error Crouton
            Configuration config = new Configuration.Builder().setDuration(Configuration.DURATION_SHORT).build();
            mCrouton = Crouton.makeText(this, "All Fences Deleted", Style.INFO);
            mCrouton.setConfiguration(config).show();
        } else if (v.getId() == findViewById(R.id.activity_main_bt_start_service).getId()) {
            Log.i(TAG, "OPEN Button Clicked");
            Intent i = new Intent(getBaseContext(), LocationActivity.class);
            startActivity(i);
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean serviceConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
            Log.d(TAG, "Google Play Services Availabel");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            // Create error Crouton
            Configuration config = new Configuration.Builder().setDuration(Configuration.DURATION_SHORT).build();
            mCrouton = Crouton.makeText(this, "Google Play Services not Available", Style.ALERT);
            mCrouton.setConfiguration(config).show();
            return false;
        }
    }

    /**
     * Check all the input values and flag those that are incorrect
     *
     * @return true if all the widget values are correct; otherwise false
     */
    private boolean checkInputFields() {
        // Start with the input validity flag set to true
        boolean inputOK = true;

        /*
         * Latitude, longitude, and radius values can't be empty. If they are, highlight the input
         * field in red and put a Toast message in the UI. Otherwise set the input field highlight
         * to black, ensuring that a field that was formerly wrong is reset.
         */
        if (TextUtils.isEmpty(mLat.getText())) {
            mLat.setBackgroundColor(getResources().getColor(R.color.LIGHT_RED));
            // Set the validity to "invalid" (false)
            inputOK = false;
        } else {

            mLat.setBackgroundColor(Color.GRAY);
        }

        if (TextUtils.isEmpty(mLng.getText())) {
            mLng.setBackgroundColor(getResources().getColor(R.color.LIGHT_RED));
            // Set the validity to "invalid" (false)
            inputOK = false;
        } else {

            mLng.setBackgroundColor(Color.GRAY);
        }
        if (TextUtils.isEmpty(mRad.getText())) {
            mRad.setBackgroundColor(getResources().getColor(R.color.LIGHT_RED));
            // Set the validity to "invalid" (false)
            inputOK = false;
        } else {
            mRad.setBackgroundColor(Color.GRAY);
        }
        if (TextUtils.isEmpty(mName.getText())) {
            mName.setBackgroundColor(getResources().getColor(R.color.LIGHT_RED));
            // Set the validity to "invalid" (false)
            inputOK = false;
        } else {
            mName.setBackgroundColor(Color.GRAY);
        }

        /*
         * If all the input fields have been entered, test to ensure that their values are within
         * the acceptable range. The tests can't be performed until it's confirmed that there are
         * actual values in the fields.
         */
        if (inputOK) {

            /*
             * Get values from the latitude, longitude, and radius fields.
             */
            double lat1 = Double.valueOf(mLat.getText().toString());
            double lng1 = Double.valueOf(mLng.getText().toString());
            float rd1 = Float.valueOf(mRad.getText().toString());

            /*
             * Test latitude and longitude for minimum and maximum values. Highlight incorrect
             * values and set a Toast in the UI.
             */

            if (lat1 > GeofenceUtils.MAX_LATITUDE || lat1 < GeofenceUtils.MIN_LATITUDE) {
                mLat.setBackgroundColor(Color.RED);
                Toast.makeText(this, "Latitude Val invalid", Toast.LENGTH_LONG).show();
                // Set the validity to "invalid" (false)
                inputOK = false;
            } else {
                mLat.setBackgroundColor(Color.GRAY);
            }

            if ((lng1 > GeofenceUtils.MAX_LONGITUDE) || (lng1 < GeofenceUtils.MIN_LONGITUDE)) {
                mLng.setBackgroundColor(Color.RED);
                Toast.makeText(this, "Longitude Val invalid", Toast.LENGTH_LONG).show();

                // Set the validity to "invalid" (false)
                inputOK = false;
            } else {
                mLng.setBackgroundColor(Color.GRAY);
            }

            if (rd1 < GeofenceUtils.MIN_RADIUS) {
                mRad.setBackgroundColor(Color.RED);
                Toast.makeText(this, "Radius Val Invalid", Toast.LENGTH_LONG).show();

                // Set the validity to "invalid" (false)
                inputOK = false;
            } else {

                mRad.setBackgroundColor(Color.GRAY);
            }
        }
        // If everything passes, the validity flag will still be true, otherwise it will be false.
        return inputOK;
    }

    /**
     * Called when the user clicks the "Register geofences" button.
     * Get the geofence parameters for each geofence and add them to
     * a List. Create the PendingIntent containing an Intent that
     * Location Services sends to this app's broadcast receiver when
     * Location Services detects a geofence transition. Send the List
     * and the PendingIntent to Location Services.
     */
    public void onRegister(View view) {
        /*
         * Record the request as an ADD. If a connection error occurs,
         * the app can automatically restart the add request if Google Play services
         * can fix the error
         */
        mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!serviceConnected()) {
            return;
        }

        /*
         * Check that the input fields have values and that the values are with the
         * permitted range
         */
        if (!checkInputFields()) {
            return;
        }

        /*
         * Create a version of geofence 1 that is "flattened" into individual fields. This
         * allows it to be stored in SharedPreferences.
         */
        SimpleGeofence fence = new SimpleGeofence(mName.getText().toString(),
                                                  Double.valueOf(mLat.getText().toString()),
                                                  Double.valueOf(mLng.getText().toString()),
                                                  Integer.valueOf(mRad.getText().toString()),
                                                  Geofence.NEVER_EXPIRE,
                                                  (Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT));
        // Add to Database
        mDatasource.createGeofence(fence);

        /*
         * Add Geofence objects to a List. toGeofence()
         * creates a Location Services Geofence object from a
         * flat object
         */
        mCurrentGeofences.add(fence.toGeofence());
        Log.i(TAG, "Current geofences");
        for (Geofence i : mCurrentGeofences) {
            Log.i(TAG, "ID:" + i.getRequestId());
        }

        // Start the request. Fail if there's already a request in progress
        try {
            // Try to add geofences
            mGeofenceRequester.addGeofences(mCurrentGeofences);
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.add_geofences_already_requested_error,
                           Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called when the user clicks the "Remove geofences" button
     *
     * @param view The view that triggered this callback
     */
    public void onUnregister(View view) {
        /*
         * Remove all geofences set by this app. To do this, get the
         * PendingIntent that was added when the geofences were added
         * and use it as an argument to removeGeofences(). The removal
         * happens asynchronously; Location Services calls
         * onRemoveGeofencesByPendingIntentResult() (implemented in
         * the current Activity) when the removal is done
         */
        mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());


        /*
         * Record the removal as remove by Intent. If a connection error occurs,
         * the app can automatically restart the removal if Google Play services
         * can fix the error
         */
        // Record the type of removal
        mRemoveType = GeofenceUtils.REMOVE_TYPE.INTENT;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!serviceConnected()) {
            return;
        }

        // Try to make a removal request
        try {
        /*
         * Remove the geofences represented by the currently-active PendingIntent. If the
         * PendingIntent was removed for some reason, re-create it; since it's always
         * created with FLAG_UPDATE_CURRENT, an identical PendingIntent is always created.
         */
            mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());

        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.remove_geofences_already_requested_error,
                           Toast.LENGTH_LONG).show();
        }

    }

    // ################### INNER CLASS- BROADCAST RECEIVER #####################################

    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class GeofenceSampleReceiver extends BroadcastReceiver {

        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

                // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                            ||
                            TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

                // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);
                // The Intent contained an invalid action
            } else {
                Log.i(TAG, "Invalid Intent Action: " + action);
                Toast.makeText(context, "Invalid Intent Action: " + action, Toast.LENGTH_LONG).show();
            }
        }


        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent  The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {

        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent  The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */
            //int triggerdId = intent.getIntExtra(GeofenceUtils.EXTRA_GEOFENCE_ID, 0);
            String triggerdIds = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_ID);
            String[] ids = triggerdIds.split(",");
            Date tempTime = new Date(System.currentTimeMillis());
            long timestamp = tempTime.getTime(); //time in milliseconds
            mDatasource.updateRowById(ids, timestamp);
        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
}

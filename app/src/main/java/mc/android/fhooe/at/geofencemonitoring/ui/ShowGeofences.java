package mc.android.fhooe.at.geofencemonitoring.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


import java.util.List;

import mc.android.fhooe.at.geofencemonitoring.R;
import mc.android.fhooe.at.geofencemonitoring.data.SimpleGeofence;

/**
 * Created by chris on 18.12.2014.
 */
public class ShowGeofences extends Activity {

    private final String TAG = ShowGeofences.class.getSimpleName();
    private TextView mList;
    private List<SimpleGeofence> mGeofenceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_geofences);
        mList = (TextView) findViewById(R.id.activity_show_geofences_tv_list);
        fillList();
    }

    /**
     * Creates a list with all Geofences from the Database.
     */
    private void fillList() {
        mGeofenceList = null;
        mGeofenceList = MainActivity.mDatasource.getAllFences();
        StringBuffer buff = new StringBuffer();
        buff.append("Geofences: \n");
        for(SimpleGeofence fence : mGeofenceList) {
            buff.append(fence.toString() + "\n");
        }
        mList.setText(buff.toString());
    }
}

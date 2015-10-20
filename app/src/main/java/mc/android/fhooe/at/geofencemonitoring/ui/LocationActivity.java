package mc.android.fhooe.at.geofencemonitoring.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import mc.android.fhooe.at.geofencemonitoring.R;
import mc.android.fhooe.at.geofencemonitoring.service.LocationService;


public class LocationActivity extends Activity {

    Button mStartBt;
    Button mStopBt;
    public static TextView mListTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListTv.setText("");
    }

    /**
     * Initiates the button and the Textview.
     */
    private void initActivity() {
        mStartBt = (Button) findViewById(R.id.bt_activity_main_start);
        mStartBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(getBaseContext(), LocationService.class)); // Start the service
            }
        });
        mStopBt = (Button) findViewById(R.id.bt_activity_main_stop);
        mStopBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(getBaseContext(), LocationService.class));
            }
        });
        mListTv = (TextView) findViewById(R.id.tv_activtiy_main_list);
    }

}

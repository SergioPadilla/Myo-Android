package com.piser.myo;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.scanner.ScanActivity;

public class MyoActivity extends AppCompatActivity {

    public final static String TAG = "Myo Activity";

    private Hub hub;
    private AbstractDeviceListener listener;
    private TextView centerlabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myo);

        centerlabel = (TextView) findViewById(R.id.centerlabel);

        hub = Hub.getInstance();

        if (!hub.init(this)) {
            Log.e(TAG, "Could not initialize the Hub.");
            finish();
        }
        else {
            // Disable standard Myo locking policy. All poses will be delivered.
            hub.setLockingPolicy(Hub.LockingPolicy.NONE);
            //startConnectActivity();
            Hub.getInstance().attachToAdjacentMyo();
            listener = get_listener();
        }
    }

    private void startConnectActivity() {
        /**
         * Init activity to connect Myo device
         */
        startActivity(new Intent(MyoActivity.this, ScanActivity.class));
    }

    private AbstractDeviceListener get_listener() {
        /**
         * Get the listener to myo device
         */
        return new AbstractDeviceListener() {
            @Override
            public void onConnect(Myo myo, long timestamp) {
                Toast.makeText(getApplicationContext(), "Myo Connected!", Toast.LENGTH_SHORT).show();
                centerlabel.setText("Myo Connected!");
                centerlabel.setTextColor(ContextCompat.getColor(MyoActivity.this,
                        android.R.color.holo_blue_dark));
            }

            @Override
            public void onDisconnect(Myo myo, long timestamp) {
                Toast.makeText(getApplicationContext(), "Myo Disconnected!", Toast.LENGTH_SHORT).show();
                centerlabel.setText("Myo Disconnected!");
                centerlabel.setTextColor(ContextCompat.getColor(MyoActivity.this,
                        android.R.color.holo_red_dark));
            }

            @Override
            public void onPose(Myo myo, long timestamp, Pose pose) {
                Toast.makeText(getApplicationContext(), "Pose: " + pose.toString(), Toast.LENGTH_SHORT).show();

                //TODO: Do something awesome.
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hub.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hub.addListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hub.removeListener(listener);
    }
}

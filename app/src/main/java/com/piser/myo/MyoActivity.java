package com.piser.myo;

import android.app.FragmentManager;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.piser.myo.Youtube.DeveloperKey;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

public class MyoActivity extends AppCompatActivity {

    public final static String TAG = "Myo Activity";

    private Hub hub;
    private AbstractDeviceListener listener;

    private TextView connected_label;
    private TextView arm_label;
    private TextView pose_label;
    private YouTubePlayerFragment youTubePlayerFragment;
    private YouTubePlayer player; // control video

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myo);

        connected_label = (TextView) findViewById(R.id.connectedlabel);
        arm_label = (TextView) findViewById(R.id.arm_label);
        pose_label = (TextView) findViewById(R.id.pose_label);
        youTubePlayerFragment = (YouTubePlayerFragment)getFragmentManager()
                .findFragmentById(R.id.youtube_fragment);

        hub = Hub.getInstance();

        if (!hub.init(this)) {
            Log.e(TAG, "Could not initialize the Hub.");
            finish();
        }
        else {
            // Disable standard Myo locking policy. All poses will be delivered.
            hub.setLockingPolicy(Hub.LockingPolicy.NONE);
            // Capture the Myo nearest automatically
            //Hub.getInstance().attachToAdjacentMyo();
            // scan Myo by activity
            startConnectActivity();
            // Create listener for Myo
            listener = get_listener();

            createYoutubeFragment();
        }
    }

    private void createYoutubeFragment() {
        YouTubePlayerFragment youTubeFragment = YouTubePlayerFragment.newInstance();

        youTubeFragment.initialize(DeveloperKey.DEVELOPER_KEY,
                new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                YouTubePlayer youTubePlayer, boolean b) {
                player = youTubePlayer;
                player.cueVideo("RrGqlGxRIn0");
                player.play();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                YouTubeInitializationResult
                                                        youTubeInitializationResult) {
                Toast.makeText(getApplicationContext(), "YouTubePlayer.onInitializationFailure(): "
                        + youTubeInitializationResult.toString(), Toast.LENGTH_LONG).show();
            }
        });
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
                Toast.makeText(getApplicationContext(), "Myo Connected!",
                        Toast.LENGTH_SHORT).show();
                connected_label.setText("Myo Connected!");
                connected_label.setTextColor(ContextCompat.getColor(MyoActivity.this,
                        android.R.color.holo_blue_dark));
            }

            @Override
            public void onDisconnect(Myo myo, long timestamp) {
                Toast.makeText(getApplicationContext(), "Myo Disconnected!",
                        Toast.LENGTH_SHORT).show();
                connected_label.setText("Myo Disconnected!");
                connected_label.setTextColor(ContextCompat.getColor(MyoActivity.this,
                        android.R.color.holo_red_dark));
            }

            @Override
            public void onPose(Myo myo, long timestamp, Pose pose) {
                pose_label.setText("Pose: " + pose.toString());

                if(pose == Pose.REST) {
                    // Mano normal
                }
                else if(pose == Pose.DOUBLE_TAP) {
                    // Movimiento pinza doble (dificil de reconocer)

                }
                else if(pose == Pose.WAVE_IN) {
                    // Mano hacia dentro

                }
                else if(pose == Pose.WAVE_OUT) {
                    // Mano hacia fuera

                }
                else if(pose == Pose.FIST) {
                    // Cerrar puño
                    if(player != null) {
                        player.pause();
                    }
                }
                else if(pose == Pose.FINGERS_SPREAD) {
                    // Abrir mano y dedos

                }

                //TODO: Do something awesome.
            }

            @Override
            public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
                arm_label.setText(myo.getArm() == Arm.LEFT ? "Arm left" : "Arm right");
            }

            @Override
            public void onArmUnsync(Myo myo, long timestamp) {
                arm_label.setText("Arm not Detected");
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

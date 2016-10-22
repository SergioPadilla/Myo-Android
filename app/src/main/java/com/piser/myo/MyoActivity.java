package com.piser.myo;

/**
 * Created by sergiopadilla on 11/10/16.
 */

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.media.*;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import com.piser.myo.Utils.Chapters.Chapter;
import com.piser.myo.Utils.Chapters.ChaptersAdapterList;
import com.piser.myo.Utils.Youtube.DeveloperKey;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MyoActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    /**
     * Youtube
     */
    private static final int RECOVERY_REQUEST = 1;
    private static final String VIDEOID = "kWzgRHC5apY";
    private YouTubePlayerView youTubeView;
    private YouTubePlayer player;
    private boolean fullscreen = false;
    private MediaPlayer playerControl;
    private AudioManager audioManager;

    /**
     * Myo
     */
    private Hub hub;
    private AbstractDeviceListener listener;
    public final static String TAG = "Myo Activity";
    private TextView connected_label;
    private TextView arm_label;
    private TextView pose_label;
    private TextView x_orientation_label;
    private TextView y_orientation_label;
    private TextView z_orientation_label;
    private float roll;
    private float pitch;
    private float yaw;
    boolean oneTime;


    /**
     * Drawer layout controllers
     */
    private boolean drawer_open;
    private DrawerLayout drawer_layout;
    private LinearLayout left_layout;
    private ListView season;
    private ChaptersAdapterList chapters;
    private int position_selected;
    private boolean block_selector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        left_layout = (LinearLayout) findViewById(R.id.left_drawer);
        season = (ListView) findViewById(R.id.season_list);
        chapters = new ChaptersAdapterList(this);
        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(DeveloperKey.DEVELOPER_KEY, this);
        connected_label = (TextView) findViewById(R.id.connectedlabel);
        arm_label = (TextView) findViewById(R.id.arm_label);
        pose_label = (TextView) findViewById(R.id.pose_label);
        x_orientation_label = (TextView) findViewById(R.id.x_orientation_label);
        y_orientation_label = (TextView) findViewById(R.id.y_orientation_label);
        z_orientation_label = (TextView) findViewById(R.id.z_orientation_label);

        hub = Hub.getInstance();
        drawer_open = false;
        season.setAdapter(chapters);
        position_selected = 0;
        block_selector = false;

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        oneTime=true;

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
        }
    }

    private void closeSelector() {
        drawer_layout.closeDrawer(left_layout);
        drawer_open = false;
    }

    private void openSelector() {
        drawer_layout.openDrawer(left_layout);
        drawer_open = true;
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
                    if(player!= null && !drawer_open) {
                        player.setFullscreen(true);
                        fullscreen = true;
                    }
                }
                else if(pose == Pose.WAVE_IN) {
                    if(drawer_open) {
                        closeSelector();
                    }
                }
                else if(pose == Pose.WAVE_OUT) {
                    if(!drawer_open) {
                        openSelector();
                    }
                }
                else if(pose == Pose.FIST) {
                    // Cerrar puño
                    if(!drawer_open) {
                        if (player != null) {
                            player.pause();
                        }
                    } else {
                        block_selector = true; // block the selection of video for security
                        Chapter chapter = (Chapter) season.getItemAtPosition(position_selected);
                        String video_id = chapter.getId();
                        if(player != null) {
                            player.loadVideo(video_id);
                            closeSelector();
                        }
                        block_selector = false;
                    }
                }
                else if(pose == Pose.FINGERS_SPREAD) {
                    // Abrir mano y dedos
                    if(player != null && !drawer_open) {
                        player.play();
                    }
                }
            }

            @Override
            public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
                arm_label.setText(myo.getArm() == Arm.LEFT ? "Arm left" : "Arm right");
            }

            @Override
            public void onArmUnsync(Myo myo, long timestamp) {
                arm_label.setText("Arm not Detected");
            }

            @Override
            public void onOrientationData(Myo myo, long timestamp, Quaternion rotation){
                // Calcula los angulos de Euler
                // (roll: eje morro cola (x)) (pitch: eje ala (y)) (yaw: eje perpenticular al objeto (z))
                float roll = ((-1)*(float) Math.toDegrees(Quaternion.roll(rotation)));
                float pitch = ((-1)*((float) Math.toDegrees(Quaternion.pitch(rotation)))) + 60;
                float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

                // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
                x_orientation_label.setText("Xº: "+String.valueOf(roll));
                y_orientation_label.setText("Yº: "+String.valueOf(pitch));
                z_orientation_label.setText("Zº: "+String.valueOf(yaw));

                if(drawer_open && !block_selector) { //block selector for security
                    int dif = 120 / chapters.getCount();
                    int pos = (int) pitch / dif;
                    if(pos >= 0 && pos < chapters.getCount()) {
                        View focused = chapters.getView(pos, null, season);
                        season.setSelection(pos);
                        for (int j = 0; j < season.getChildCount(); j++) {
                            View view = season.getChildAt(j);
                            if (focused.getId() == view.getId()) {
                                view.setFocusable(true);
                                view.setBackgroundColor(Color.parseColor("#3b60d7"));
                                position_selected = pos;
                            } else {
                                view.setFocusable(false);
                                view.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            }
                        }
                    }
                }
                else if(!drawer_open) {
                    if (roll > 80 && oneTime) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1, AudioManager.FLAG_SHOW_UI);
                        oneTime = false;

                    } else if (roll < -10 && oneTime) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 1, AudioManager.FLAG_SHOW_UI);
                        oneTime = false;
                    } else if (roll > 0 && roll < 40) {
                        oneTime = true;
                    }
                }
            }
        };
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.player = player;
        this.player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
            @Override
            public void onFullscreen(boolean b) {
                fullscreen = b;
            }
        });
        if (!wasRestored) {
            player.loadVideo(VIDEOID);
            //player.play();
        }
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(DeveloperKey.DEVELOPER_KEY, this);
        }
    }

    protected Provider getYouTubePlayerProvider() {
        return youTubeView;
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

    @Override
    public void onBackPressed() {
        if(fullscreen)
            player.setFullscreen(false);
        else
            super.onBackPressed();
    }
}

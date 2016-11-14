package com.piser.myo;

/**
 * Created by sergiopadilla on 11/10/16.
 */

import android.content.Intent;
import android.content.res.Configuration;
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

public class MyoActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    /**
     * Youtube
     */
    private static final int RECOVERY_REQUEST = 1;
    private static final String VIDEOID = "kWzgRHC5apY";
    private YouTubePlayerView youTubeView;
    private YouTubePlayer player;
    private boolean fullscreen = false;
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
    private boolean drawer_open; // Control if the layout is open or close
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

        /**
         * Init view component
         */
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

        /**
         * Init drawer layout controllers
         */
        drawer_open = false;
        season.setAdapter(chapters);
        position_selected = 0;
        block_selector = false;

        /**
         * Init audio controler
         */
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        oneTime=true;

        /**
         * Init MYO controllers
         */
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
        }
    }

    private void closeSelector() {
        /**
         * Close the left drawer (selector of chapters)
         */
        drawer_layout.closeDrawer(left_layout);
        drawer_open = false;
    }

    private void openSelector() {
        /**
         * Open the left drawer (selector of chapters)
         */
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
                /**
                 * When myo is connected, show it in a label
                 */
                connected_label.setText("Myo Connected!");
                connected_label.setTextColor(ContextCompat.getColor(MyoActivity.this,
                        android.R.color.holo_blue_dark));
            }

            @Override
            public void onDisconnect(Myo myo, long timestamp) {
                /**
                 * When myo is disconnected, show it in a label
                 */
                connected_label.setText("Myo Disconnected!");
                connected_label.setTextColor(ContextCompat.getColor(MyoActivity.this,
                        android.R.color.holo_red_dark));
            }

            @Override
            public void onPose(Myo myo, long timestamp, Pose pose) {
                /**
                 * This interface in called when Myo detect some pose
                 */
                pose_label.setText("Pose: " + pose.toString());

                if(pose == Pose.REST) {
                    // Mano normal
                }
                else if(pose == Pose.DOUBLE_TAP) {
                    /**
                     * Double tap put the video in full screen
                     */
                    // Movimiento pinza doble (dificil de reconocer)
//                    if(player!= null && !drawer_open) {
//                        player.setFullscreen(true);
//                        fullscreen = true;
//                    }
                }
                else if(pose == Pose.WAVE_IN) {
                    /**
                     * This gesture close the selector only if it's open
                     */
                    if(drawer_open) { // the if structure, give user more security gestures
                        closeSelector();
                    }
                }
                else if(pose == Pose.WAVE_OUT) {
                    /**
                     * This gesture close the selector only if it's close
                     */
                    if(!drawer_open) { // the if structure, give user more security gestures
                        openSelector();
                    }
                }
                else if(pose == Pose.FIST) {
                    // Cerrar puño
                    /**
                     * This gesture have two functionalities depend on the left drawer
                     *    1. If drawer is open, select the chapter and play it
                     *    2. If drawer is close, pause the video that is playing
                     */
                    if(!drawer_open) {
                        if (player != null) {
                            player.pause();
                        }
                    } else {
                        block_selector = true; // block the chapter's seleection for security
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
                    /**
                     * Play video only if the left drawer is close
                     */
                    if(player != null && !drawer_open) {
                        player.play();
                    }
                }
            }

            @Override
            public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
                /**
                 * Show in the label, the arm where myo is wearing
                 */
                arm_label.setText(myo.getArm() == Arm.LEFT ? "Arm left" : "Arm right");
            }

            @Override
            public void onArmUnsync(Myo myo, long timestamp) {
                /**
                 * Util to detect if myo is disconnected
                 */
                arm_label.setText("Arm not Detected");
            }

            @Override
            public void onOrientationData(Myo myo, long timestamp, Quaternion rotation){
                /**
                 * This interface is called to give us the orientation of Myo
                 */
                // Calcula los angulos de Euler
                // (roll: eje morro cola (x)) (pitch: eje ala (y)) (yaw: eje perpenticular al objeto (z))
                float roll = ((-1)*(float) Math.toDegrees(Quaternion.roll(rotation)));
                // pitch plus 60 allow us to set 0 when the arm is in the top
                float pitch = ((-1)*((float) Math.toDegrees(Quaternion.pitch(rotation)))) + 60;
                float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

                // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
                x_orientation_label.setText("Xº: "+String.valueOf(roll));
                y_orientation_label.setText("Yº: "+String.valueOf(pitch));
                z_orientation_label.setText("Zº: "+String.valueOf(yaw));

                if(drawer_open && !block_selector) { //block selector for security
                    /**
                     * We select a range between 0º and 120º in the y-axis
                     * Then, we set a range for each chapter in the list (dif)
                     * In this way, we select the chapter changing its background, focusing it and
                     * save its position.
                     *
                     * Important: each time, we have to put the rest child background in the
                     * original color
                     */
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
                    /**
                     * Only when the selector is closed, we can to set Volumen to the video
                     * That is contolled by the X-axis
                     * We set a range between -10º and 80º
                     *
                     * Important: oneTime allow us control the volume, in this way, we
                     * increase/reduce the volumen only 1 when the gesture is captured, and
                     * init again when capture the rest position (beetwen 0º and 40º)
                     */
                    if (roll > 80 && oneTime) {
                        // arm spin more than 80º so increase the volumen in 1
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1,
                                AudioManager.FLAG_SHOW_UI);
                        oneTime = false;
                    } else if (roll < -10 && oneTime) {
                        // arm spin less than -10º so reduce the volumen in 1
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 1,
                                AudioManager.FLAG_SHOW_UI);
                        oneTime = false;
                    } else if (roll > 0 && roll < 40) {
                        // arm in rest, allow you to change the volumen again
                        oneTime = true;
                    }
                }
            }
        };
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        /**
         * This interface is called by youtube api when it's initialized correctly
         */
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
        /**
         * This interface is called by youtube api when it's initialized incorrectly
         */
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * Get the activity result of youtube api
         */
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(DeveloperKey.DEVELOPER_KEY, this);
        }
    }

    protected Provider getYouTubePlayerProvider() {
        // Neccesary for yotube api
        return youTubeView;
    }

    /***********************************************************************************************
     * Activity's Methods
     ***********************************************************************************************/
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        /**
         * We need it to control the fullscreen of video
         */
        super.onConfigurationChanged(newConfig);
    }
}

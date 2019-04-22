package inc.humanoid.spysee;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LogPrinter;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class FullscreenActivity extends AppCompatActivity implements SensorEventListener {


    private SensorManager mSensorManager;
    private Display mDisplay;
    private Sensor mAccelerometer;

    private WebView webview;
    private TextView tx1, tx2, tx3;
    private View joystickBase, pointer, mContentView;
    private SeekBar topBar, leftBar;
    private ImageButton tuneButton, reorientButton, hideAllButton, refreshButton, cameraLockButton,
            modesButton, geoLocateButton, qualitySetButton, objectDetectionButton, faceRecogButton,
            videoRecButton, selfDestructButton;
    private LinearLayout ctrlList;
    private Animations Animate;
    private SpySeeSync motionSync;
    private SpySeeSync camSync;
    private SpySeeSync specialSync;
    private SpySeeSync emergencySync;

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mContentView = findViewById(R.id.relative_layout);
        hideUI();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mWindowManager != null) {
            mDisplay = mWindowManager.getDefaultDisplay();
        }
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        startSensor();
       // tx1 = findViewById(R.id.textView1);
        //tx2 = findViewById(R.id.textView2);
        //tx3 = findViewById(R.id.textView3);

        ctrlList = (LinearLayout) findViewById(R.id.ctrl_list);

        tuneButton = (ImageButton) findViewById(R.id.tune_button);
        reorientButton = (ImageButton) findViewById(R.id.reorient_button);
        hideAllButton = (ImageButton) findViewById(R.id.hide_all_button);
        refreshButton = (ImageButton) findViewById(R.id.refresh_button);
        //cameraLockButton = findViewById(R.id.camera_lock_button);
        //modesButton = findViewById(R.id.modes_button);
       // geoLocateButton = findViewById(R.id.geo_locate_button);
       // qualitySetButton = findViewById(R.id.quality_set_button);
        objectDetectionButton = (ImageButton) findViewById(R.id.object_detection_button);
        faceRecogButton = (ImageButton) findViewById(R.id.face_recog_button);
        //videoRecButton = findViewById(R.id.video_rec_button);
        //selfDestructButton = findViewById(R.id.self_destruct_button);

        leftBar = (SeekBar) findViewById(R.id.left_bar);
       // topBar = findViewById(R.id.top_bar);
        leftBar.setProgressDrawable(getDrawable(R.drawable.power_seekbar));
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        motionCtrlPort = Integer.valueOf(settings.getString("motionCtrl_port", ""));
        camCtrlPort = Integer.parseInt(settings.getString("camCtrl_port", ""));
        specialCtrlPort = Integer.parseInt(settings.getString("specialCtrl_port", ""));
        emergencyCtrlPort = Integer.parseInt(settings.getString("emergencyCtrl_port", ""));

        webview = (WebView) findViewById(R.id.web_view);
        loadVideo();

        joystickBase = findViewById(R.id.joystick_base);
        pointer = findViewById(R.id.pointer);
        joystickBase.setTranslationZ(40);
        pointer.setTranslationZ(41);
        joystickBase.setVisibility(View.INVISIBLE);
        pointer.setVisibility(View.INVISIBLE);

        //topBar.setTranslationY(-82-13);
       // leftBar.setTranslationX(-98.4f);
        //leftBar.setProgressDrawable(getDrawable(R.drawable.power_seekbar));

        Animate = new Animations();

        webview.setOnTouchListener(Webview);

        leftBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                leftBarData = progress;
                leftBarDataFromUser = fromUser;
//                tx1.setText(String.valueOf(leftBarData));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
       /*
        topBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                topBarData = progress;
                topBarDataFromUser = fromUser;
                tx2.setText(String.valueOf(progress));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
*/


        try {
            IPAddress = InetAddress.getByName(settings.getString("ip_address", ""));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        connect();
    }

    View.OnTouchListener Webview = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent joystickEvent) {
            Animate.menuAnimator(joystickEvent);
            return joystickControl(joystickEvent);
        }
    };


//-----------------------------------------Control Buttons---------------------------------------//

    @SuppressWarnings("FieldCanBeLocal")
    private boolean firstClickOnHideAllButton = true;
    private float hideAllButtonY;

    public void tuneButton(View v) {
        Intent intent = new Intent(FullscreenActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
    public void refreshButton(View v) {
        reloadVideo();
        disconnect();
        connect();
    }
    public void hideAllButton(View v) {
        if (firstClickOnHideAllButton) {
            hideAllButtonY = hideAllButton.getY();
            firstClickOnHideAllButton = false;
        }
        Animate.hideAllAnimator();

    }
   /*
    public void cameraLockButton(View v) {
        Animate.cameraLockAnimator();
    }
   */
   boolean ob = false;
   public void objectDetectionButtonButton(View v) {
       if(!ob){
           ob= true;

           emergencySync.send();
       }
       else{
           ob = false;
       }
   }

    public void reorientButton(View v) {
        Snackbar bar = Snackbar.make(v, R.string.snakBarText, 3000);
        ViewGroup contentLay = (ViewGroup) bar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        ProgressBar item = new ProgressBar(getApplicationContext());
        contentLay.addView(item);
        bar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                refAngleX = angleX;
                refAngleY = angleY;
            }
        });
        bar.show();
    }

    private boolean state1 = false;
    public void debugButton1(View v) {
        /*
        if (!state1) {
            motionHandler = new Handler();
            motionHandler.post(motionCtrlRun);
            Log.d("FA","Button1 @Motion Enabled");
            state1 = true;
        } else {
            if(motionHandlerState){
                motionHandler.removeCallbacks(motionCtrlRun);
                Log.d("FA","Button1 @Motion Disabled");
                motionHandlerState=false;
            }
            state1 = false;
        }
        */
    }
    private boolean state2 = false;
    public void debugButton2(View v) {
        /*
        if (!state2) {
            camHandler = new Handler();
            camHandler.post(camCtrlRun);
            Log.d("FA","Button2 @Arm Enabled");
            state2 = true;
        } else {
            if(camHandlerState){
                camHandler.removeCallbacks(camCtrlRun);
                Log.d("FA","Button2 @Arm Disabled");
                camHandlerState=false;
            }
            state2 = false;
        }
        //startComm(2110);
        */
    }
    private boolean state3 = false;
    public void debugButton3(View v) {
        if (!state3) {
            stopSensor();
            state3 = true;
        } else {
            startSensor();
            state3 = false;
        }

    }


//-----------------------------------------Joystick Control---------------------------------------//

    private float x1, y1;
    private boolean touchstate;

    private boolean joystickControl(MotionEvent event) {
        int index;
        switch (MotionEventCompat.getActionMasked(event)) {
            case (MotionEvent.ACTION_DOWN):

                touchstate = true;
                index = event.findPointerIndex(event.getPointerId(0));
                joystickBase.setVisibility(View.VISIBLE);
                pointer.setVisibility(View.VISIBLE);
                x1 = event.getX(index) - ((pointer.getWidth()) / 2);
                y1 = event.getY(index) - ((pointer.getHeight()) / 2);
                joystickBase.animate()
                        .x(event.getX(index) - (joystickBase.getWidth() / 2))
                        .y(event.getY(index) - (joystickBase.getWidth() / 2))
                        .setDuration(0)
                        .start();
                pointer.animate()
                        .x(event.getX(index) - ((pointer.getWidth()) / 2))
                        .y(event.getY(index) - ((pointer.getHeight()) / 2))
                        .setDuration(0)
                        .start();
                break;
            case (MotionEvent.ACTION_MOVE):
                //if(event.getPointerCount()>0)
                index = event.findPointerIndex(event.getPointerId(0));
                float X = event.getX(index) - ((pointer.getWidth()) / 2);
                float Y = event.getY(index) - ((pointer.getHeight()) / 2);
                float a = X - x1;
                float b = Y - y1;
                float c = (joystickBase.getWidth() * 5) / 10;
                float d = (float) (Math.pow((Math.pow(a, 2) + Math.pow(b, 2)), 0.5));
                float e = (float) (Math.cos(Math.atan(b / a)) * (d - c));
                float f = (float) (Math.sin(Math.atan(b / a)) * (d - c));
                if (d > c) {
                    if (a < 0) {
                        pointer.animate()
                                .x(X + e)
                                .y(Y + f)
                                .setDuration(0)
                                .start();
                    } else {
                        pointer.animate()
                                .x(X - e)
                                .y(Y - f)
                                .setDuration(0)
                                .start();
                    }
                } else {
                    pointer.animate()
                            .x(X)
                            .y(Y)
                            .setDuration(0)
                            .start();
                }
                motionDataUpdater(a, b);
                break;
            // if (event.getPointerCount() == 2) {
            //   int index = event.findPointerIndex(event.getPointerId(1));
            //   Log.d("FA", String.format("X: %s", String.valueOf(event.getX(index))));

            //}
            case (MotionEvent.ACTION_UP):
                touchstate = false;
                joystickBase.setVisibility(View.INVISIBLE);
                pointer.setVisibility(View.INVISIBLE);
                motionDataUpdater(0, 0);
                break;
        }
        return true;
    }
    //  TODO use of touchstate??


//---------------------------------------GyroSensor Handler---------------------------------------//

    private float angleX;
    private float angleY;
    private float refAngleX, refAngleY;

    public void startSensor() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }
    public void stopSensor() {
        mSensorManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_GRAVITY)
            return;
        camDataUpdater(event.values[0], event.values[1], event.values[2]);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


//------------------------------------------Video Handler-----------------------------------------//

    private void loadVideo() {
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webview.loadUrl("http://"
                + settings.getString("ip_address", "")
                + ":"
                + settings.getString("video_port", "")
                + "/"
                + settings.getString("video_address", ""));
    }
    public void reloadVideo() {
        webview.reload();
    }
    //  TODO Reload should consider the settings changes.
    //  TODO WebView reload at resume/restart after destroy on pause/stop.


//-------------------------------------------Animations-------------------------------------------//

    private int leftBarData;
    private int lastLeftBarData;
    private boolean leftBarDataFromUser;
    private int topBarData;
    private int lastTopBarData;
    private boolean topBarDataFromUser;
    private boolean hideAll = false;
    private boolean cameraLockStatus = false;

    private class Animations {

        private int duration = 500;
        private float xLeft = leftBar.getX();
        private float xRight = ctrlList.getX();
        //private float yTop = topBar.getY();

        private void setDuration(int duration) {
            this.duration = duration;
        }

        private void menuAnimator(MotionEvent event) {

            if (!hideAll) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    scrollViewAnimator(true);
                    //topBarAnimator(true);
                    leftBarAnimator(true);
                } else if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                    scrollViewAnimator(false);
                    //topBarAnimator(false);
                    leftBarAnimator(false);
                }
            } else {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    buttonAnimator(true, hideAllButton);
                    leftBarAnimator(true);
                } else if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                    buttonAnimator(false, hideAllButton);
                    leftBarAnimator(false);
                }
            }
        }

        private void hideAllAnimator() {
            if (!hideAll) {
                //topBarAnimator(true);
                leftBar.animate()
                        .setDuration(duration)
                        .x((float) (xLeft - (leftBar.getHeight() * 1.2)))
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                leftBar.setProgressDrawable(getDrawable(R.drawable.power_seekbar));
                            }
                        })
                        .start()
                ;
                buttonAnimator(true, tuneButton);
                buttonAnimator(true, reorientButton);
                buttonAnimator(true, refreshButton);
                //buttonAnimator(true, cameraLockButton);
                //buttonAnimator(true, modesButton);
                //buttonAnimator(true, geoLocateButton);
                //buttonAnimator(true, qualitySetButton);
                buttonAnimator(true, objectDetectionButton);
                buttonAnimator(true, faceRecogButton);
                //buttonAnimator(true, videoRecButton);
                //buttonAnimator(true, selfDestructButton);

                hideAllButton.animate()
                        .setDuration(duration)
                        .x(xRight + hideAllButton.getWidth())
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                hideAllButton.setY(0);
                                hideAllButton.setImageDrawable(getDrawable(R.drawable.ic_all_hide));
                                buttonAnimator(false, hideAllButton);
                            }
                        })
                        .start()
                ;
                hideAll = true;
            } else {
                //topBarAnimator(false);
                leftBar.animate()
                        .setDuration(duration)
                        .x(xLeft)
                        .withStartAction(new Runnable() {
                            @Override
                            public void run() {
                                leftBar.setProgressDrawable(getDrawable(R.drawable.seekbar));
                                barAnimator(0, lastLeftBarData);
                            }
                        })
                        .start()
                ;
                hideAllButton.animate()
                        .setDuration(duration)
                        .x(xRight + hideAllButton.getWidth())
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                hideAllButton.setImageDrawable(getDrawable(R.drawable.ic_all_visible));
                                hideAllButton.setY(hideAllButtonY);
                                buttonAnimator(false, tuneButton);
                                buttonAnimator(false, reorientButton);
                                buttonAnimator(false, hideAllButton);
                                buttonAnimator(false, refreshButton);
                                //buttonAnimator(false, cameraLockButton);
                                //buttonAnimator(false, modesButton);
                                //buttonAnimator(false, geoLocateButton);
                                //buttonAnimator(false, qualitySetButton);
                                buttonAnimator(false, objectDetectionButton);
                                buttonAnimator(false, faceRecogButton);
                                //buttonAnimator(false, videoRecButton);
                                //buttonAnimator(false, selfDestructButton);
                            }
                        })
                        .start()
                ;
                hideAll = false;
            }

        }

        /*private void cameraLockAnimator() {
            if (!cameraLockStatus) {
                cameraLockStatus = true;
                Log.d("FA", "cameraLockButton " + cameraLockStatus);
                topBarAnimator(false);

                //leftBar.setTranslationX(+98.4f);
                leftBarAnimator(false);
            } else {
                topBarAnimator(true);
                //leftBarAnimator(false);
                cameraLockStatus = false;
                Log.d("FA", "cameraLockButton " + cameraLockStatus);
            } */

        private void scrollViewAnimator(boolean hide) {
            if (!hide) {
                ctrlList.animate()
                        .setDuration(duration)
                        .x(xRight)
                        .start()
                ;
            } else {
                ctrlList.animate()
                        .setDuration(duration)
                        .x(xRight + ctrlList.getWidth())
                        .start()
                ;
            }
        }

        private void buttonAnimator(boolean hide, ImageButton button) {
            if (!hide) {
                button.animate()
                        .setDuration(duration)
                        .x(xRight)
                        .start()
                ;
            } else {
                button.animate()
                        .setDuration(duration)
                        .x(xRight + button.getWidth())
                        .start()
                ;
            }
        }
/*
        private void topBarAnimator(boolean hide) {
            if (cameraLockStatus) {
                if (!hide) {
                    topBar.animate()
                            .setDuration(duration)
                            .y(13)
                            .start()
                    ;
                    //Log.d("FA","topBarAnimator@ topbary: "+ topBar.getY());
                } else {
                    topBar.animate()
                            .setDuration(duration)
                            .y(-topBar.getHeight())
                            .start()
                    ;
                    // Log.d("FA","topBarAnimator@ topbary: "+ topBar.getY());
                }
            }
        }
  */
        private void leftBarAnimator(boolean hide) {

                if (!hide) {
                    barAnimator(leftBarData, lastLeftBarData);
                    leftBar.animate()
                            .setDuration(duration / 2)
                            .x((float) (xLeft - (leftBar.getHeight() * 1.2)))
                            .start()
                    ;
                } else {
                    lastLeftBarData = leftBarData;
                    barAnimator(lastLeftBarData, 30);
                    leftBar.animate()
                            .setDuration(duration / 2)
                            .x(xLeft)
                            .start()
                    ;
                }

        }

        private void barAnimator(int from, int to) {
            ValueAnimator barAnim = ValueAnimator.ofArgb(from, to);
            barAnim.setDuration(500);
            barAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    leftBar.setProgress((Integer) animation.getAnimatedValue());
                }
            });
            barAnim.start();
        }

    }
    private void hideUI() {
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

//-----------------------------------------Data Updaters------------------------------------------//

    private static int motionControllerStopCommand = 101;
    private static int motionControllerStartCommand = 102;
    private static int specialControllerStopCommand = 103;
    private static int specialControllerStartCommand = 104;
    private static int camControllerStopCommand =105;
    private static int camControllerStartCommand = 106;
    private static int motionDataUpdaterStopCommand =107;
    private static int motionDataUpdaterStartCommand = 108;
    private static int camDataUpdaterStopCommand = 109;
    private static int camDataUpdaterStartCommand = 110;
    private static int ultraSonicDataUpdateStopCommand = 111;
    private static int ultraSonicDataUpdateStartCommand =112;
    private static int gyroDataUpdaterStopCommand = 113;
    private static int gyroDataUpdaterStartCommand = 114;
    private static int gpsDataUpdaterStopCommand = 115;
    private static int gpsDataUpdaterStartCommand =116;
    private static int specialDataUpdaterStopCommand = 117;
    private static int specialDataUpdaterStartCommand = 118;
    private static int videoSteamerStopCommand = 119;
    private static int videoSteamerStartCommand =120;
    private static int objectDetectorStopCommand = 121;
    private static int objectDetectorStartCommand = 122;

    private int txMotionData;
    private int txCamData;
    private int txSpecialData;
    private int txEmergencyData;

    private int rxMotionData;
    private int rxCamData;
    private int rxSpecialData;
    private int rxEmergencyData;

    private void motionDataUpdater(float x, float y) {
        if (touchstate) {
            int front;
            int rear;
            front = rxMotionData/1000;
            rear = rxMotionData%1000;
            int joystickData = (int) Math.round((Math.atan(y / x)) * 180 / Math.PI);
            if (x == 0 && y == 0)
                joystickData = 90;
            else if (x >= 0 && y < 0)
                joystickData = 0 - joystickData;
            else if (x > 0 && y >= 0)
                joystickData = 360 - joystickData;
            else
                joystickData = 180 - joystickData;
            if (leftBarDataFromUser)
                txMotionData = (1000 * joystickData) + leftBarData;
            else
                txMotionData = (1000 * joystickData);
        } else
            txMotionData = 90000;
//        tx3.setText(String.valueOf(txMotionData));
    }
    private void camDataUpdater(float mSensorX, float mSensorY, float mSensorZ) {
        if (mSensorX > 9.805f) mSensorX = 9.8067f;
        if (mSensorY > 9.805f) mSensorY = 9.8067f;
        if (mSensorZ > 9.805f) mSensorZ = 9.8067f;
        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_90:
                angleX = (int) Math.round((Math.asin(mSensorX / 9.8067f) * 180 / Math.PI));
                angleY = (int) Math.round((Math.asin(mSensorY / 9.8067f) * 180 / Math.PI));
                break;
            case Surface.ROTATION_270:
                angleX = (int) Math.round(-(Math.asin(mSensorX / 9.8067f) * 180 / Math.PI));
                angleY = (int) Math.round(-(Math.asin(mSensorY / 9.8067f) * 180 / Math.PI));
                break;
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_180:
                break;
        }
        //angleZ = (int) Math.round((Math.asin(mSensorZ/9.8067f)*180/Math.PI));

        int range = 20;
        float camAngle = (int) (((angleX - refAngleX + range) / (range * 2)) * 180);
        float headAngle = (int) (((angleY - refAngleY + range) / (range * 2)) * 180);
        if (camAngle > 145) camAngle = 145;
        if (camAngle < 80) camAngle = 80;
        if (headAngle > 179) headAngle = 179;
        if (headAngle < 1) headAngle = 1;
        txCamData = (int) ((headAngle * 1000) + camAngle);
        //tx1.setText(String.format("X: %s", String.valueOf(angleX)));
        //tx2.setText(String.format("Y: %s", String.valueOf(angleY)));
        //tx3.setText(String.format("Z: %s", String.valueOf(angleZ)));
    }
    private void specialDataUpdtaer() {
        txSpecialData = 1021;
    }
    private void emergencyDataUpdtaer() {

    }


//-----------------------------------------Communication------------------------------------------//

    private int motionCtrlPort;
    private int camCtrlPort;
    private int specialCtrlPort;
    private int emergencyCtrlPort;

    private boolean motionSyncState = false;
    private boolean camSyncState = false;

    public int j = 255;
    public String sendBit(int port) {
        if (port == motionCtrlPort) {
            return "" + txMotionData;
        } else if (port == camCtrlPort) {
            return "" + txCamData;
        } else if (port == specialCtrlPort) {
            return "" + txSpecialData;
        } else if (port == emergencyCtrlPort) {
            return "" + txEmergencyData;
        } else return "";
    }
    private void receiveBit(String rxData, int from) {
        if (from == motionCtrlPort) {
            rxMotionData = Integer.parseInt(rxData);
        } else if (from == camCtrlPort) {
            rxCamData = Integer.parseInt(rxData);
        } else if (from == specialCtrlPort) {
            rxSpecialData = Integer.parseInt(rxData);
        } else if (from == emergencyCtrlPort) {
            rxEmergencyData = Integer.parseInt(rxData);
        }
        //Log.d("FA", "txMD:" + txMotionData + " rxMD:" + rxMotionData +" txMD:" + txCamData + " rxMD:" + rxCamData +         " txMD:" + txSpecialData + " rxMD:" + rxSpecialData +         " txMD:" + txEmergencyData + " rxMD:" + rxEmergencyData);
    }
    public void commStateUpdate(String comState) {
        //.setText(comState);
    }
    private InetAddress IPAddress;
    private static final int COMM_DELAY_TIME = 1;

    private void connect() {
        motionSync = new SpySeeSync(motionCtrlPort);
        camSync = new SpySeeSync(camCtrlPort);
        specialSync = new SpySeeSync(emergencyCtrlPort);
        emergencySync = new SpySeeSync(specialCtrlPort);
        motionSync.start();
        camSync.start();
        // specialSync.send();
        // emergencySync.send();
    }
    private class SpySeeSync {
        int ctrlPort;
        DatagramSocket dSocket;
        Runnable dataSendLoop;
        Runnable layerloop;
        Handler dataLoopHandler;
        boolean connected = false;
        Thread commThread;
        boolean faltuKa;

        SpySeeSync(int ctrlPort) {
            this.ctrlPort = ctrlPort;
            try {
                dSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        private void start() {

            layerloop = new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    if (!dSocket.isClosed()) {
                        dataLoopHandler = new Handler();
                        if (dSocket.isConnected())
                            faltuKa= true;
                            dataSendLoop = new Runnable() {
                                @Override
                                public void run() {
                                    if (!dSocket.isClosed()) {
                                        //Log.d("FA", "loop2@: Started:");
                                        try {
                                            String data = sendBit(ctrlPort);
                                            byte[] txDataBytes = data.getBytes();
                                            int txDataLength = data.length();
                                            byte[] rxDataBytes = new byte[1024];
                                            DatagramPacket packet = new DatagramPacket(txDataBytes, txDataLength, IPAddress, ctrlPort);
                                            dSocket.send(packet);
                                            if (!connected) commStateUpdate("Connecting...");
                                            packet = new DatagramPacket(rxDataBytes, rxDataBytes.length);
                                            dSocket.receive(packet);
                                            if (!connected) {
                                                commStateUpdate("Connected");
                                                connected = true;
                                            }
                                            receiveBit(new String(packet.getData(), 0, packet.getLength()), ctrlPort);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            commStateUpdate("Error in Connection");
                                        }
                                        dataLoopHandler.postDelayed(dataSendLoop, COMM_DELAY_TIME);
                                    }
                                }
                            };
                        dataLoopHandler.post(dataSendLoop);
                    }
                    Looper.loop();
                }
            };
            commThread = new Thread(layerloop);
            commThread.start();
        }
        private void send() {
            if (!dSocket.isClosed()) {
                try {
                    String data = sendBit(ctrlPort);
                    byte[] txDataBytes = data.getBytes();
                    int txDataLength = data.length();
                    byte[] rxDataBytes = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(txDataBytes, txDataLength, IPAddress, ctrlPort);
                    dSocket.send(packet);
                    if (!connected) commStateUpdate("Connecting...");
                    packet = new DatagramPacket(rxDataBytes, rxDataBytes.length);
                    dSocket.receive(packet);
                    receiveBit(new String(packet.getData(), 0, packet.getLength()), ctrlPort);
                } catch (IOException e) {
                    e.printStackTrace();
                    commStateUpdate("Error in Connection");
                } finally {
                    dSocket.close();
                }
            }
        }
        private void interrupt() {
            dSocket.close();
            commStateUpdate("Disconnected");
            Log.d("Com", "interrupt@  Socket closed" + String.valueOf(ctrlPort));
            dataLoopHandler.removeCallbacks(dataSendLoop);
            commThread.interrupt();
        }
    }
    private void disconnect() {
        if (motionSyncState) {
            motionSync.interrupt();
            motionSyncState = false;
        }
        if(camSyncState){
            camSync.interrupt();
            camSyncState =false;
        }
    }

    private void resetAll(){

    }

//----------------------------------------Override methods----------------------------------------//

    @Override
    protected void onStop() {
        super.onStop();
        webview.destroy();
        disconnect();
        stopSensor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webview.destroy();
        disconnect();
        stopSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webview.reload();
        hideUI();
        startSensor();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadVideo();
        startSensor();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //connect();
    }


}

/*
  private void leftBarAnimator(boolean hide) {
            if (!hideAll && cameraLockStatus) {
                if (!hide) {
                    barAnimator(leftBarData, lastLeftBarData);
                    leftBar.animate()
                            .setDuration(duration / 2)
                            .x((float) (xLeft + (leftBar.getHeight() * 1.2)))
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    leftBar.setProgressDrawable(getDrawable(R.drawable.seekbar));
                                    leftBar.animate()
                                            .setDuration(duration / 2)
                                            .x(xLeft)
                                            .start()
                                    ;
                                }
                            })
                            .start()
                    ;
                } else {
                    lastLeftBarData = leftBarData;
                    barAnimator(lastLeftBarData, 30);
                    leftBar.animate()
                            .setDuration(duration / 2)
                            .x(xLeft)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    leftBar.setProgressDrawable(getDrawable(R.drawable.power_seekbar));
                                    leftBar.animate()
                                            .setDuration(duration / 2)
                                            .x((float) (xLeft + (leftBar.getHeight() * 1.2)))
                                            .start()
                                    ;
                                }
                            })
                            .start()
                    ;
                }
            } else {
                if (hide) {
                    barAnimator(0, 30);
                    leftBar.animate()
                            .setDuration(duration / 2)
                            .x((float) (xLeft + (leftBar.getHeight() * 1.2)))
                            .start()
                    ;
                    Log.d("FA","leftBarAnimator@ topbary: "+ leftBar.getX());
                } else {
                    leftBar.animate()
                            .setDuration(duration)
                            .x(xLeft)
                            .start()
                    ;
                    Log.d("FA","leftBarAnimator@ topbary: "+ leftBar.getX());
                }
            }
        }

 */
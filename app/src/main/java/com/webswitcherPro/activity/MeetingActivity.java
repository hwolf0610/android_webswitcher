package com.webswitcherPro.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.opentok.android.Session;
import com.opentok.android.Connection;

import com.opentok.android.Stream;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Subscriber;
import com.opentok.android.OpentokError;
import com.opentok.android.SubscriberKit;
import com.tokbox.android.tutorials.webswitcherpro.R;
import com.webswitcherPro.Service.BackgroundService;
import com.webswitcherPro.Service.ForegroundService;
import com.webswitcherPro.Service.MyBackgroundService;
import com.webswitcherPro.Utils.Constant;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.widget.ProgressBar;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pub.devrel.easypermissions.AppSettingsDialog;

public class MeetingActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks,
        WebServiceCoordinator.Listener,
        Session.SessionListener,
        PublisherKit.PublisherListener, PublisherKit.AudioLevelListener,
        SubscriberKit.SubscriberListener,SubscriberKit.AudioLevelListener,
        Session.SignalListener {

    private Intent serviceIntent;

    private static final String LOG_TAG = MeetingActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    static final String SIGNAL_NEW_SUBSCRIBER = "newSubscriber";
    static final String SIGNAL_SWITCH_LANG = "switchLang";
    static final String SIGNAL_SUBSCRIBER_RESPONSE = "responseSubscriberInfos";
    static final String SIGNAL_REQUEST_MY_INFOS = "requestSubscriberInfos";
    static final String SIGNAL_INTERPRETER_MIC_STATUS = "interpreter_microphone_event";

    static HashMap<String, HashMap<String, String>> allSubscribers = new HashMap<>();
    static HashMap<String, Subscriber> allSubscribersObjects = new HashMap<>();

    ProgressBar publisherBar = null;
    ProgressBar subscriberBar = null;

    Boolean PUBLISHER_MIC    = false;

    static Button logoutBtn;

    static String API_KEY = "";
    static String SESSION_ID = "";
    static String TOKEN = "";
    static String CONFERENCE_LANGUAGES = "";
    static String USER_TYPE = "";
    static String LANG_IN = "";
    static String LANG_OUT = "";
    static String NICKNAME = "";

    static String MY_STREAM_ID = "";

    // Microphone Btn define
    ImageButton microphoneButton = null;

    private Session mSession;
    private Publisher mPublisher = null;
    private Subscriber mSubscriber = null;
    final Context context = this;

    public Spinner listeningSpinner;
    public static HashMap<String, String> languagesArray = new HashMap<>();
    ArrayList<String> LanguagesName;
    JSONObject jsonobject;
    JSONArray jsonarray;
    String eachName;
    String eachAbbr2;
    public TextView microphoneStatusLabel;
    ArrayAdapter<String> adapterListening;
    Timer t ;
    TimerTask doAsynchronousTask;
    final Handler handler = new Handler();
    Timer timer = new Timer();

    private FrameLayout spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

//        Intent startServiceIntent = new Intent(MeetingActivity.this, MyBackgroundService.class);
//        startService(startServiceIntent);

//        if(Constant.FLAG == true){
//            microphoneStatusLabel.setText("On Air");
//            PUBLISHER_MIC = Constant.FLAG;
//            mPublisher.setPublishAudio(true);
//            microphoneButton.setBackgroundResource(R.drawable.microphone_on);
//            String info_data = "id=" + MY_STREAM_ID + ",name=" + NICKNAME + ",audio_lang=" + LANG_OUT + ",user_type=" + USER_TYPE + ",div_id= ,microphone=1";
//            mSession.sendSignal(SIGNAL_SUBSCRIBER_RESPONSE, info_data);
//            Toast.makeText(this, "OnCreate!!!", Toast.LENGTH_LONG).show();
//        }



        // App version
        TextView appVersion = findViewById(R.id.appVersion);
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
            appVersion.setText("Version:" + String.valueOf(info.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        assert extras != null;
        API_KEY     = extras.getString("MEETING_API_KEY");
        SESSION_ID  = extras.getString("MEETING_SESSION_ID");
        TOKEN       = extras.getString("MEETING_TOKEN");
        CONFERENCE_LANGUAGES = extras.getString("MEETING_CONFERENCE_LANGUAGES");
        USER_TYPE   = extras.getString("MEETING_USER_TYPE");
        NICKNAME    = extras.getString("MEETING_NICKNAME");
        LANG_IN     = extras.getString("MEETING_LANG_IN");
        LANG_OUT    = extras.getString("MEETING_LANG_OUT");

        logoutBtn = findViewById(R.id.logoutBtn);

        // Print Labels
        TextView langOutLabel = findViewById(R.id.langOutLabel);
        //TextView langOutLabelText  = findViewById(R.id.langOutLabelText);
        TextView nicknameLabel = findViewById(R.id.nicknameLabel);
        microphoneStatusLabel = findViewById(R.id.microphoneStatus);

        // Add Spinner "Loading"
        spinner = findViewById(R.id.spinnerLayout);
        spinner.setVisibility(View.VISIBLE);
        logoutBtn.setClickable(false);

        //listeningSpinner.setSelection(LANG_IN);
        langOutLabel.setText(LANG_OUT.toUpperCase());
        nicknameLabel.setText(NICKNAME.toUpperCase());

        // Microphone button
        microphoneButton = findViewById(R.id.microphoneButton);

        // Progress bar section
        publisherBar  = findViewById(R.id.publisher_volume);
        subscriberBar = findViewById(R.id.subscriber_volume);
        listeningSpinner = findViewById(R.id.listeningSpinner);

        try {
            LanguagesName = new ArrayList<>();
            JSONObject jsnobject = new JSONObject(CONFERENCE_LANGUAGES);
            jsonarray = jsnobject.getJSONArray("languages");

            for (int i = 0; i < jsonarray.length(); i++) {
                jsonobject = jsonarray.getJSONObject(i);
                eachName = jsonobject.optString("name");
                eachAbbr2 = jsonobject.optString("abbr2");
                languagesArray.put(eachName, eachAbbr2);
                String displayValue = eachName.substring(0, 1).toUpperCase() + eachName.substring(1);

                if(eachName != null && LANG_IN.equals(eachAbbr2))
                {
                    LanguagesName.add(displayValue);
                }
            }

            for (int i = 0; i < jsonarray.length(); i++) {
                jsonobject = jsonarray.getJSONObject(i);
                eachName = jsonobject.optString("name");
                eachAbbr2 = jsonobject.optString("abbr2");
                languagesArray.put(eachName, eachAbbr2);
                String displayValue = eachName.substring(0, 1).toUpperCase() + eachName.substring(1);

                if(eachName != null && !LANG_IN.equals(eachAbbr2))
                {
                    LanguagesName.add(displayValue);
                }
            }

            adapterListening = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, LanguagesName);
            adapterListening.setDropDownViewResource(R.layout.spinner_dropdown_item);
            listeningSpinner.setAdapter(adapterListening);

            listeningSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    ((TextView) parentView.getChildAt(0)).setTextColor(getResources().getColor(R.color.ws_background_black_clr));
                    String selectedItemText = (String) parentView.getItemAtPosition(position);
                    LANG_IN = languagesArray.get(selectedItemText.toLowerCase());

                    if (mSession != null) {
                        // RESET LANGUAGE ALGORITHM
                        resetAllSubscriberLanguages();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterListening = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, LanguagesName);
        adapterListening.setDropDownViewResource(R.layout.spinner_dropdown_item);
        listeningSpinner.setAdapter(adapterListening);

        if(!(USER_TYPE.equals("viewer")))
        {
            microphoneButton.setVisibility(View.VISIBLE);
            publisherBar.setVisibility(View.VISIBLE);
            microphoneStatusLabel.setVisibility(View.VISIBLE);
            //PUBLISHER_MIC    = true;
            //langOutLabel.setVisibility(View.VISIBLE);
            //langOutLabelText.setVisibility(View.VISIBLE);
        }

        // Logout Button (confirmation)
        logoutBtn.setBackgroundColor(Color.parseColor("#e8262f"));
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("Webswitcher");
                alertDialogBuilder
                        .setMessage("Are you sure want to exit the conference ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                mSession.unpublish(mPublisher);
                                mSession.disconnect();
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        requestPermissions();

    }

     /* Activity lifecycle methods */
    @Override
    protected void onPause()
    {

        //Log.d(LOG_TAG, "onPause");
//        if(Constant.FLAG == true){
//            microphoneStatusLabel.setText("On Air");
//            PUBLISHER_MIC = Constant.FLAG;
//            mPublisher.setPublishAudio(true);
//            microphoneButton.setBackgroundResource(R.drawable.microphone_on);
//            String info_data = "id=" + MY_STREAM_ID + ",name=" + NICKNAME + ",audio_lang=" + LANG_OUT + ",user_type=" + USER_TYPE + ",div_id= ,microphone=1";
//            mSession.sendSignal(SIGNAL_SUBSCRIBER_RESPONSE, info_data);
//            Toast.makeText(this, "onPause!!!", Toast.LENGTH_LONG).show();
//        }



        super.onPause();

        if (mSession != null) {
            mSession.onPause();
        }

    }

    @Override
    protected void onResume()
    {

        //Log.d(LOG_TAG, "onResume");
//        if(Constant.FLAG == true){
//            microphoneStatusLabel.setText("On Air");
//            PUBLISHER_MIC = Constant.FLAG;
//            mPublisher.setPublishAudio(true);
//            microphoneButton.setBackgroundResource(R.drawable.microphone_on);
//            String info_data = "id=" + MY_STREAM_ID + ",name=" + NICKNAME + ",audio_lang=" + LANG_OUT + ",user_type=" + USER_TYPE + ",div_id= ,microphone=1";
//            mSession.sendSignal(SIGNAL_SUBSCRIBER_RESPONSE, info_data);
//            Toast.makeText(this, "OnResume!!!", Toast.LENGTH_LONG).show();
//        }




        super.onResume();

        if (mSession != null) {
            mSession.onResume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms)
    {

        //Log.d(LOG_TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        Log.d(LOG_TAG, "");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms)
    {

        //Log.d(LOG_TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setRationale(getString(R.string.rationale_ask_again))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel))
                    .setRequestCode(RC_SETTINGS_SCREEN_PERM)
                    .build()
                    .show();
        }
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {

        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {

            initializeSession(API_KEY, SESSION_ID, TOKEN);

        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }

    private void initializeSession(String apiKey, String sessionId, String token)
    {

        mSession = new Session.Builder(this, apiKey, sessionId).build();
        mSession.setSessionListener(this);
        mSession.setSignalListener(this);
        mSession.connect(token);
    }

    /* Web Service Coordinator delegate methods */

    @Override
    public void onSessionConnectionDataReady(String apiKey, String sessionId, String token)
    {

        //Log.d(LOG_TAG, "ApiKey: "+apiKey + " SessionId: "+ sessionId + " Token: "+token);
        initializeSession(apiKey, sessionId, token);
    }

    @Override
    public void onWebServiceCoordinatorError(Exception error)
    {

        Toast.makeText(this, "Web Service error: " + error.getMessage(), Toast.LENGTH_LONG).show();
        finish();

    }

    /* Session Listener methods */

    @Override
    public void onConnected(Session session)
    {
        //Log.d(LOG_TAG, "onConnected: Connected to session: "+session.getSessionId());
        final String currentSess = session.getSessionId();
        doAsynchronousTask = new TimerTask() {

            @Override
            public void run() {

                handler.post(new Runnable() {
                    public void run() {
                        sendHttpRequest(currentSess);
                    }
                });

            }

        };
        timer.schedule(doAsynchronousTask, 0, 10000);

        spinner.setVisibility(View.GONE);
        logoutBtn.setVisibility(View.VISIBLE);

        if(!(USER_TYPE.equals("viewer"))){
            // initialize Publisher and set this object to listen to Publisher events
            mPublisher = new Publisher.Builder(this).videoTrack(false).build();
            mPublisher.setPublishAudio(false);
            mPublisher.setPublisherListener(this);
            mPublisher.setAudioLevelListener(new PublisherKit.AudioLevelListener() {
                @Override
                public void onAudioLevelUpdated(PublisherKit publisher, float audioLevel)
                {
                if (PUBLISHER_MIC)
                {
                    int currentLevel = Math.round(audioLevel * 100);
                    publisherBar.setProgress(currentLevel);
                }
                else
                {
                    publisherBar.setProgress(0);
                }
                }
            });
            mSession.publish(mPublisher);
        }
    }

    @Override
    public void onDisconnected(Session session)
    {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Webswitcher")
                .setMessage("Are you sure you want to exit conference?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
        Log.d(LOG_TAG, "onDisconnected: Disconnected from session: "+session.getSessionId());
        publisherBar.setProgress(0);
        subscriberBar.setProgress(0);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        System.exit(0);
        finish();
    }

    @Override
    public void onStreamReceived(Session session, Stream stream)
    {

        //Log.d(LOG_TAG, "onStreamReceived: New Stream Received "+stream.getStreamId() + " in session: "+session.getSessionId());
        mSubscriber = new Subscriber.Builder(this, stream).build();
        //mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        mSubscriber.setSubscriberListener(this);
        mSession.subscribe(mSubscriber);
        mSubscriber.setSubscribeToAudio(true);

        mSubscriber.setAudioLevelListener(new SubscriberKit.AudioLevelListener() {
            @Override
            public void onAudioLevelUpdated(SubscriberKit subscriber, float audioLevel) {
                int currentLevel = Math.round(audioLevel * 100 ) ;
                subscriberBar.setProgress(currentLevel);
            }
        });

        if (!allSubscribersObjects.containsKey(stream.getStreamId())) {
            allSubscribersObjects.put(stream.getStreamId(),mSubscriber);
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {

        //Log.d(LOG_TAG, "onStreamDropped: Stream Dropped: "+stream.getStreamId() +" in session: "+session.getSessionId());

        if (mSubscriber != null) {
            mSubscriber = null;
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError)
    {
        //Log.e(LOG_TAG, "onError: "+ opentokError.getErrorDomain() + " : " + opentokError.getErrorCode() + " - "+opentokError.getMessage() + " in session: "+ session.getSessionId());

        showOpenTokError(opentokError);
    }

    /* Publisher Listener methods */

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream)
    {
        //Log.d(LOG_TAG, "onStreamCreated: Publisher Stream Created. Own stream "+ stream.getStreamId());
        MY_STREAM_ID = stream.getStreamId();
        sendMyInfos(MY_STREAM_ID);
        getSubscribersInfos();
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream)
    {

        Log.d(LOG_TAG, "onStreamDestroyed: Publisher Stream Destroyed. Own stream "+stream.getStreamId());
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError)
    {
        //Log.e(LOG_TAG, "onError: "+opentokError.getErrorDomain() + " : " + opentokError.getErrorCode() +  " - "+opentokError.getMessage());
        showOpenTokError(opentokError);
    }

    @Override
    public void onConnected(SubscriberKit subscriberKit)
    {
        //Log.d(LOG_TAG, "onConnected: Subscriber connected. Stream: "+subscriberKit.getStream().getStreamId());
        sendMyInfos(MY_STREAM_ID);
        getSubscribersInfos();
    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit)
    {
        Log.d(LOG_TAG, "Subscriber disconnected. Stream: "+subscriberKit.getStream().getStreamId());
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError)
    {

        //Log.e(LOG_TAG, "onError: "+opentokError.getErrorDomain() + " : " + opentokError.getErrorCode() +  " - "+opentokError.getMessage());

        showOpenTokError(opentokError);
    }

    private void showOpenTokError(OpentokError opentokError)
    {

        Toast.makeText(this, opentokError.getErrorDomain().name() +": " +opentokError.getMessage() + " Please, see the logcat.", Toast.LENGTH_LONG).show();
        finish();
    }

    public void onSignalReceived(Session session, String type, String data, Connection connection)
    {
        String myConnectionId = session.getConnection().getConnectionId();

        if (connection != null && !connection.getConnectionId().equals(myConnectionId)) {
            //Log.d(LOG_TAG, "Connection: "+ connection + " Data: " + data);
            // Signal received from another client
            if (!TextUtils.isEmpty(data)){
                switch (type) {
                    // New subscriber
                    case SIGNAL_NEW_SUBSCRIBER:
                        newSubscriberSignal(data);
                        break;

                    // Subscriber change language
                    case SIGNAL_SWITCH_LANG:
                        langSwitchSignal(data);
                        break;

                    case SIGNAL_INTERPRETER_MIC_STATUS:
                        interpreterMicrophoneSwitchSignal(data);
                        break;

                    // Subscriber response to request
                    case SIGNAL_SUBSCRIBER_RESPONSE:
                        subscriberInfos(data);
                        break;

                    // Others request my infos
                    case SIGNAL_REQUEST_MY_INFOS:
                        sendMyInfos(MY_STREAM_ID);
                        break;

                    case "requestPresenterShareScreen":
                        Log.d(LOG_TAG, "EMPTY");
                        break;
                }
            }
        }
    }

    // When new subscriber connecting
    private void newSubscriberSignal(String signal)
    {
        if(signal.contains(",")) {
            String[] splitSignal = signal.split(",");

            String subscriberId = "";
            String[] split_subscriberId = splitSignal[0].split("=");
            if(1 < split_subscriberId.length) {
                subscriberId = split_subscriberId[1];
            }

            String subscriberNickname = "";
            String[] split_subscriberNickname = splitSignal[1].split("=");
            if(1 < split_subscriberNickname.length) {
                subscriberNickname = split_subscriberNickname[1];
            }

            String subscriberLanguage = "";
            String[] split_subscriberLanguage = splitSignal[2].split("=");
            if(1 < split_subscriberLanguage.length) {
                subscriberLanguage = split_subscriberLanguage[1];
            }

            String subscriberType = "";
            String[] split_subscriberType = splitSignal[3].split("=");
            if(1 < split_subscriberType.length) {
                subscriberType = split_subscriberType[1];
            }

            String subscriberSource = "";
            String[] split_subscriberSource = splitSignal[4].split("=");
            if(1 < split_subscriberSource.length) {
                subscriberSource = split_subscriberSource[1];
            }

            String subscriberMicrophone = "0";
            String[] split_subscriberMicrophone = splitSignal[5].split("=");
            if(1 < split_subscriberMicrophone.length) {
                subscriberMicrophone = split_subscriberMicrophone[1];
            }
            if (!allSubscribers.containsKey(subscriberId)) {
                allSubscribers.put(subscriberId, new HashMap<String, String>());
            }
            allSubscribers.get(subscriberId).put("subscriberNickname", subscriberNickname);
            allSubscribers.get(subscriberId).put("subscriberLanguage", subscriberLanguage);
            allSubscribers.get(subscriberId).put("subscriberType", subscriberType);
            allSubscribers.get(subscriberId).put("subscriberSource", subscriberSource);
            allSubscribers.get(subscriberId).put("subscriberMicrophone", subscriberMicrophone);

            resetAllSubscriberLanguages();
        }
    }

    // When subscriber change his language
    private void langSwitchSignal(String signal)
    {
        if(signal.contains(",")) {
            String[] splitSignal = signal.split(",");

            String subscriberId = "";
            String[] split_subscriberId = splitSignal[0].split("=");
            if(1 < split_subscriberId.length) {
                subscriberId = split_subscriberId[1];
            }

            String subscriberNewLang = "";
            String[] split_subscriberNewLang = splitSignal[2].split("=");
            if(1 < split_subscriberNewLang.length) {
                subscriberNewLang = split_subscriberNewLang[1];
            }

            if (!allSubscribers.containsKey(subscriberId)) {
                allSubscribers.put(subscriberId, new HashMap<String, String>());
            }
            allSubscribers.get(subscriberId).put("subscriberLanguage", subscriberNewLang);
            resetAllSubscriberLanguages();
        }
    }

    // When subscriber change his language
    private void interpreterMicrophoneSwitchSignal(String signal)
    {
        if(signal.contains(",")) {
            String[] splitSignal = signal.split(",");

            String subscriberId = "";
            String[] split_subscriberId = splitSignal[0].split("=");
            if(1 < split_subscriberId.length) {
                subscriberId = split_subscriberId[1];
            }

            String subscriberMicrophoneStatus = "0";
            String[] split_subscriberMicrophoneStatus = splitSignal[1].split("=");
            if(1 < split_subscriberMicrophoneStatus.length) {
                subscriberMicrophoneStatus = split_subscriberMicrophoneStatus[1];
            }

            if (!allSubscribers.containsKey(subscriberId)) {
                allSubscribers.put(subscriberId, new HashMap<String, String>());
            }
            allSubscribers.get(subscriberId).put("subscriberMicrophone", subscriberMicrophoneStatus);
            resetAllSubscriberLanguages();
        }
    }

    // Get info of subscriber
    private void subscriberInfos(String signal)
    {
        if(signal.contains(",")) {
            String[] splitSignal = signal.split(",");

            String subscriberId = "";
            String[] split_subscriberId = splitSignal[0].split("=");
            if(1 < split_subscriberId.length) {
                subscriberId = split_subscriberId[1];
            }

            String subscriberNickname = "";
            String[] split_subscriberNickname = splitSignal[1].split("=");
            if(1 < split_subscriberNickname.length) {
                subscriberNickname = split_subscriberNickname[1];
            }

            String subscriberLanguage = "";
            String[] split_subscriberLanguage = splitSignal[2].split("=");
            if(1 < split_subscriberLanguage.length) {
                subscriberLanguage = split_subscriberLanguage[1];
            }

            String subscriberType = "";
            String[] split_subscriberType = splitSignal[3].split("=");
            if(1 < split_subscriberType.length) {
                subscriberType = split_subscriberType[1];
            }

            String subscriberMicrophone = "0";
            if(splitSignal.length > 5) {
                String[] split_subscriberMicrophone = splitSignal[5].split("=");
                if (1 < split_subscriberMicrophone.length) {
                    subscriberMicrophone = split_subscriberMicrophone[1];
                }
            }
            if (!allSubscribers.containsKey(subscriberId)) {
                allSubscribers.put(subscriberId, new HashMap<String, String>());
            }

            allSubscribers.get(subscriberId).put("subscriberNickname", subscriberNickname);
            allSubscribers.get(subscriberId).put("subscriberLanguage", subscriberLanguage);
            allSubscribers.get(subscriberId).put("subscriberType", subscriberType);
            allSubscribers.get(subscriberId).put("subscriberMicrophone", subscriberMicrophone);

            resetAllSubscriberLanguages();
        }
    }

    private void resetAllSubscriberLanguages()
    {
        // Check if any available interpreter (subscriber that have language not Floor is considered interpreter)
        // Check also if their microphone is ON
        Boolean Interpreter_ready = false;
        if(!allSubscribersObjects.isEmpty()) {
            for (Map.Entry each : allSubscribersObjects.entrySet()) {
                if (allSubscribers.containsKey(each.getKey())) {
                    String eachInterpreterLang = allSubscribers.get(each.getKey()).get("subscriberLanguage");
                    String eachInterpreterMicrophone = allSubscribers.get(each.getKey()).get("subscriberMicrophone");

                    if (eachInterpreterLang.equals(LANG_IN) && eachInterpreterMicrophone.equals("1")) {
                        Interpreter_ready = true;
                    }
                }
            }

            // Loop all subscriber and reset their audio
            for (Map.Entry each : allSubscribersObjects.entrySet()) {
                if (allSubscribers.containsKey(each.getKey())) {
                    Subscriber eachSubscriber = (Subscriber) each.getValue();
                    String eachSubscriberLang = allSubscribers.get(each.getKey()).get("subscriberLanguage");

                    // If lang is not FLOOR, and there's no Interpreter, we must hear Floor
                    // Check if Interpreters are online and their microphone is ON
                    eachSubscriber.setSubscribeToAudio(false);
                    if
                            (
                            (eachSubscriberLang.equals(LANG_IN) && !LANG_IN.equals("fl") && Interpreter_ready) ||
                                    (eachSubscriberLang.equals("fl") && !LANG_IN.equals("fl") && !Interpreter_ready) ||
                                    (eachSubscriberLang.equals("fl") && LANG_IN.equals("fl"))
                            ) {
                        // Hear this subscriber!
                        eachSubscriber.setSubscribeToAudio(true);
                        //Log.e(LOG_TAG, each + " : I MUST HEAR");
                    }
                    //Log.e(LOG_TAG, " SUBSCRIBER VOLUME: " + eachSubscriber.getSubscribeToAudio());
                }
            }
        }
    }

    // Send signal to get already subscribers
    private void getSubscribersInfos()
    {
        mSession.sendSignal("requestSubscriberInfos", "NO DATA");
    }

    // Send my infos to others subscribers
    private void sendMyInfos(String streamId)
    {
        String microphone_status = "1";

        if(!PUBLISHER_MIC) {
            microphone_status = "0";
        }

        String info_data = "id=" + streamId + ",name=" + NICKNAME + ",audio_lang=" + LANG_OUT + ",user_type=" + USER_TYPE + ",div_id= ,microphone=" + microphone_status;
        mSession.sendSignal(SIGNAL_SUBSCRIBER_RESPONSE, info_data);
    }

    @Override
    public void onAudioLevelUpdated(SubscriberKit subscriber, float audioLevel) {
        int currentLevel = Math.round(audioLevel) * 100;
        subscriberBar.setProgress(currentLevel);
        //Log.d(LOG_TAG, "AudioLevel: "+ currentLevel);
    }

    @Override
    public void onAudioLevelUpdated(PublisherKit publisher, float audioLevel) {
        Log.d(LOG_TAG, "");
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    public void microphone_click(View view){
//        Intent startServiceIntent = new Intent(MeetingActivity.this, MyBackgroundService.class);
//        startService(startServiceIntent);

//        Intent startServiceIntent2 = new Intent(MeetingActivity.this, ForegroundService.class);
//        startService(startServiceIntent2);

        startService();
        if(PUBLISHER_MIC)
        {
            stopService();
            microphoneStatusLabel.setText("Muted");
            PUBLISHER_MIC = false;
            mPublisher.setPublishAudio(false);
            microphoneButton.setBackgroundResource(R.drawable.microphone_off);
            String info_data = "id=" + MY_STREAM_ID + ",name=" + NICKNAME + ",audio_lang=" + LANG_OUT + ",user_type=" + USER_TYPE + ",div_id= ,microphone=0";
            mSession.sendSignal(SIGNAL_SUBSCRIBER_RESPONSE, info_data);
        }
        else
        {
            startService();
            microphoneStatusLabel.setText("On Air");
            PUBLISHER_MIC = true;
            mPublisher.setPublishAudio(true);
            microphoneButton.setBackgroundResource(R.drawable.microphone_on);
            String info_data = "id=" + MY_STREAM_ID + ",name=" + NICKNAME + ",audio_lang=" + LANG_OUT + ",user_type=" + USER_TYPE + ",div_id= ,microphone=1";
            mSession.sendSignal(SIGNAL_SUBSCRIBER_RESPONSE, info_data);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Webswitcher")
                .setMessage("Are you sure you want to exit conference?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                        mSession.unpublish(mPublisher);
                        mSession.disconnect();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    public void sendHttpRequest(String currentSession){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.webswitcher.com/android/ping/" + currentSession + '/' + MY_STREAM_ID ;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //System.out.printf(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG,"sending error");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        if(keyCode == KeyEvent.KEYCODE_BACK)
//        {
//            Log.d("Test", "Back button pressed!");
//            Toast.makeText(this, "Back button pressed!", Toast.LENGTH_LONG).show();
//        }
//        else if(keyCode == KeyEvent.KEYCODE_HOME)
//        {
//            Log.d("Test", "Home button pressed!");
//            Toast.makeText(this, "Home button pressed!", Toast.LENGTH_LONG).show();
//        }
//        return super.onKeyDown(keyCode, event);
//    }


    @Override
    protected void onStop() {
//        Toast.makeText(this, "Your app run with Background Mode !!!", Toast.LENGTH_LONG).show();
//        serviceIntent = BackgroundService.createIntent(this);
        Log.e("background", "background");

//        Intent startServiceIntent = new Intent(MeetingActivity.this, MyBackgroundService.class);
//        startService(startServiceIntent);

        super.onStop();
    }



}

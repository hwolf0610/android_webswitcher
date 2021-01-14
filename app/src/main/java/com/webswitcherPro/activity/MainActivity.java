package com.webswitcherPro.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.tokbox.android.tutorials.webswitcherpro.R;

import android.widget.TextView;


// For Ai request
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static org.webrtc.NetworkMonitor.isOnline;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.pm.PackageManager;
import android.os.PowerManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static String API_KEY = "";
    public static String SESSION_ID = "";
    public static String TOKEN = "";
    public static String CONFERENCE_LANGUAGES = "";
    public static String USER_TYPE = "";

    public static Boolean SESSION_EXIST = false;
    public static final String CHAT_SERVER_URL = "https://api.webswitcher.com/android";
    public static final String SESSION_INFO_ENDPOINT = CHAT_SERVER_URL + "/session/";

    public AlertDialog.Builder builder;
    private static final String TAG = "Accessibility";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();

        boolean runinBack = checkAllowRunInBackground(context);
        boolean inwhietlist = isInDozeWhiteList(context);

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


        /*Toast.makeText(this,
                "PackageName = " + info.packageName + "\nVersionCode = "
                        + info.versionCode + "\nVersionName = "
                        + info.versionName + "\nPermissions = " + info.permissions, Toast.LENGTH_SHORT).show();*/

        // If app is not in white list, request permission
        if(!inwhietlist)
        {
            requestChangeBatteryOptimizations();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else
        {
            builder = new AlertDialog.Builder(MainActivity.this);
        }

        if (!isOnline())
        {
            builder.setTitle("Internet Connection Problem")
                    .setMessage("Internet not available, Cross check your internet connectivity and try again!")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    // Logout Button
    public void check_login(View view) {

        EditText tokenInput = (EditText) findViewById(R.id.tokenInput);
        String tokenValue = tokenInput.getText().toString();
        if(!tokenValue.equals(null) && !(tokenValue.equals("")))
        {
            // Check Token, and get Session info if done
            getSessionInfos(tokenValue);
        }
        else
        {
            // display error: Please enter a Token
            builder.setTitle("Token Missing")
                    .setMessage("Enter your TOKEN before continue!")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }
    public Boolean getSessionInfos(String tokenValue) {
        String SESSION_URL = SESSION_INFO_ENDPOINT + tokenValue;

        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                SESSION_URL,
                null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            API_KEY = response.getString("apiKey");
                            SESSION_ID = response.getString("sessionId");
                            TOKEN = response.getString("token");
                            CONFERENCE_LANGUAGES = "{\"languages\":" + response.getString("languages") + "}";
                            USER_TYPE = response.getString("user_type");

                            if(
                                    API_KEY.equals(null) || API_KEY.equals("")
                                            || SESSION_ID.equals(null) || SESSION_ID.equals("")
                                            || TOKEN.equals(null) || TOKEN.equals("")
                                            || USER_TYPE.equals(null) || USER_TYPE.equals("") || USER_TYPE.equals("interpreter")
                                    )
                            {
                                builder.setTitle("INVALID TOKEN")
                                        .setMessage("Your TOKEN is invalid. Please enter a valid TOKEN.")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                            else
                            {
                                Intent intent = new Intent(MainActivity.this, ConfigureActivity.class);
                                Bundle extras = new Bundle();
                                extras.putString("MESSAGE_API_KEY", API_KEY);
                                extras.putString("MESSAGE_SESSION_ID", SESSION_ID);
                                extras.putString("MESSAGE_TOKEN", TOKEN);
                                extras.putString("MESSAGE_CONFERENCE_LANGUAGES", CONFERENCE_LANGUAGES);
                                extras.putString("MESSAGE_USER_TYPE", USER_TYPE);
                                intent.putExtras(extras);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                                Context context = getApplicationContext();
                                String packageName = context.getPackageName();

                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                                    if (pm.isIgnoringBatteryOptimizations(packageName)) {
                                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                                    } else {
                                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                        intent.setData(Uri.parse("package:" + packageName));
                                    }
                                }
                                startActivity(intent);
                                finish();
                            }
                        }
                        catch (JSONException error) {
                            //Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
                            builder.setTitle("Web Server Error")
                                    .setMessage(error.getMessage())
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
                builder.setTitle("Web Server Error")
                        .setMessage(error.getMessage())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }));
        return SESSION_EXIST;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isInDozeWhiteList (Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        PowerManager powerManager = context.getSystemService(PowerManager.class);
        return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static boolean checkAllowRunInBackground (Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return true;
        try {
            Field field = AppOpsManager.class.getField("OP_RUN_IN_BACKGROUND");
            field.setAccessible(true);
            Method checkOpNoThrow = AppOpsManager.class.getMethod("checkOpNoThrow",
                    int.class,
                    int.class, String.class);
            int mode = (int)checkOpNoThrow.invoke(context.getSystemService(AppOpsManager.class),
                    field.getInt(AppOpsManager.class)
                    , context.getPackageManager()
                            .getPackageUid(context.getPackageName(),
                                    PackageManager.GET_DISABLED_COMPONENTS), context.getPackageName());
            if (mode == AppOpsManager.MODE_ERRORED) {
                Log.e(TAG, "ERRORED");
                return true;
            } else {
                Log.e(TAG, "Mode: " + mode);
                return mode != AppOpsManager.MODE_IGNORED;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private void requestChangeBatteryOptimizations ()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            }
            else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
            }
            startActivity(intent);
        }
    }

}

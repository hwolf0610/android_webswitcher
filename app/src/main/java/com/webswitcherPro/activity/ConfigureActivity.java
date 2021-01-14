package com.webswitcherPro.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.tokbox.android.tutorials.webswitcherpro.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigureActivity extends AppCompatActivity {

    private static final String LOG_TAG = ConfigureActivity.class.getSimpleName();

    public static String CONFIGURE_API_KEY = "";
    public static String CONFIGURE_SESSION_ID = "";
    public static String CONFIGURE_TOKEN = "";
    public static String CONFIGURE_CONFERENCE_LANGUAGES = "";
    public static String CONFIGURE_USER_TYPE = "";
    String CONFIGURE_LANG_OUT = "";
    String CONFIGURE_LANG_IN = "";
    public Spinner outgoingLangInput;
    public Spinner incomingLangInput;
    public static HashMap<String, String> languagesArray = new HashMap<>();

    JSONArray jsonarray;
    JSONObject jsonobject;
    ArrayList<String> LanguagesName;
    ArrayAdapter adapter;
    ArrayAdapter<String> adapterOut;
    String eachName;
    String eachAbbr2;

    public AlertDialog.Builder configureBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            configureBuilder = new AlertDialog.Builder(ConfigureActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else
        {
            configureBuilder = new AlertDialog.Builder(ConfigureActivity.this);
        }
        TextView outgoingLangLabel = findViewById(R.id.outgoingLangLabel);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        assert extras != null;
        CONFIGURE_API_KEY = extras.getString("MESSAGE_API_KEY");
        CONFIGURE_SESSION_ID = extras.getString("MESSAGE_SESSION_ID");
        CONFIGURE_TOKEN = extras.getString("MESSAGE_TOKEN");
        CONFIGURE_CONFERENCE_LANGUAGES = extras.getString("MESSAGE_CONFERENCE_LANGUAGES");
        CONFIGURE_USER_TYPE = extras.getString("MESSAGE_USER_TYPE");

        outgoingLangInput= findViewById(R.id.outgoingLangInput);
        incomingLangInput= findViewById(R.id.incomingLangInput);

        try {
            LanguagesName = new ArrayList<>();
            JSONObject jsnobject = new JSONObject(CONFIGURE_CONFERENCE_LANGUAGES);
            jsonarray = jsnobject.getJSONArray("languages");
            
            for (int i = 0; i < jsonarray.length(); i++) {
                jsonobject = jsonarray.getJSONObject(i);
                eachName = jsonobject.optString("name");
                eachAbbr2 = jsonobject.optString("abbr2");
                String displayValue = eachName.substring(0, 1).toUpperCase() + eachName.substring(1);

                if(eachName != null)
                {
                    languagesArray.put(eachName, eachAbbr2);
                    LanguagesName.add(displayValue);
                }
            }

            adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, LanguagesName);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            incomingLangInput.setAdapter(adapter);

            incomingLangInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    //((TextView) parentView.getChildAt(0)).setTextColor(getResources().getColor(R.color.ws_background_black_clr));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            adapterOut = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, LanguagesName);
            adapterOut.setDropDownViewResource(R.layout.spinner_dropdown_item);
            outgoingLangInput.setAdapter(adapterOut);

            outgoingLangInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    ((TextView) parentView.getChildAt(0)).setTextColor(getResources().getColor(R.color.ws_background_black_clr));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //if(!(CONFIGURE_USER_TYPE.equals("viewer")))
        if(CONFIGURE_USER_TYPE.equals("interpreter"))
        {
            //Log.d(LOG_TAG, "USER TYPE IS: " + CONFIGURE_USER_TYPE);
            outgoingLangLabel.setVisibility(View.VISIBLE);
            outgoingLangInput.setVisibility(View.VISIBLE);

        }

    }

    // Logout Button
    public void start_meeting(View view) {

        // Get Nickname
        EditText nicknameInput = findViewById(R.id.nicknameInput);
        String CONFIGURE_NICKNAME = nicknameInput.getText().toString();

        // Get Incoming Lang
        Spinner incomingLangInput= findViewById(R.id.incomingLangInput);
        String selectedLangIn = incomingLangInput.getSelectedItem().toString().toLowerCase();
        if(languagesArray.containsKey(selectedLangIn))
        {
            CONFIGURE_LANG_IN = languagesArray.get(selectedLangIn);
        }
        else
        {
            CONFIGURE_LANG_IN = "";
        }

        // Get Outgoing Lang
        Spinner outgoingLangInput= findViewById(R.id.outgoingLangInput);
        String selectedLangOut = outgoingLangInput.getSelectedItem().toString().toLowerCase();
        if(languagesArray.containsKey(selectedLangOut))
        {
            CONFIGURE_LANG_OUT = languagesArray.get(selectedLangOut);
        }
        else
        {
            CONFIGURE_LANG_OUT = "";
        }

        if(!CONFIGURE_NICKNAME.equals(null) && !(CONFIGURE_NICKNAME.equals(""))) {
            //Intent intent = new Intent(ConfigureActivity.this, MeetingActivity.class);
            Intent intent = new Intent(this, MeetingActivity.class);
            Bundle extras = new Bundle();
            extras.putString("MEETING_API_KEY", CONFIGURE_API_KEY);
            extras.putString("MEETING_SESSION_ID", CONFIGURE_SESSION_ID);
            extras.putString("MEETING_TOKEN", CONFIGURE_TOKEN);
            extras.putString("MEETING_CONFERENCE_LANGUAGES", CONFIGURE_CONFERENCE_LANGUAGES);
            extras.putString("MEETING_USER_TYPE", CONFIGURE_USER_TYPE);
            extras.putString("MEETING_NICKNAME", CONFIGURE_NICKNAME);
            extras.putString("MEETING_LANG_IN", CONFIGURE_LANG_IN);
            extras.putString("MEETING_LANG_OUT", CONFIGURE_LANG_OUT);
            intent.putExtras(extras);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        else
        {
            configureBuilder.setTitle("Nickname Missing")
                    .setMessage("Please enter a Nickname before continue!")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

}

package org.hcilab.projects.nlogx.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.Entity.Country;
import org.hcilab.projects.nlogx.service.AppDatabase;
import org.hcilab.projects.nlogx.Entity.RegexFilterEntity;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class RegexEditorActivity extends AppCompatActivity {

    private EditText regexInput, testMessage, formatInput;
    private TextView regexResult, formattedResult, appNameTxtView;
    private Button toggleAdvanced, speakButton, saveButton, testButton;
    private LinearLayout advancedSection;
    private AppDatabase db;
    private String packageName;
    private String appName;
    private boolean isAdvancedVisible = false;

    private SharedPreferences sharedPreferences;
    private TextToSpeech tts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regex_editor);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d("[DEBUG]", "TextToSpeech onInit status: " + status);

                prepareTextToSpeech();
            }
        });
        db = AppDatabase.getInstance(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        regexInput = findViewById(R.id.regex_input);
        testMessage = findViewById(R.id.test_message);
        formatInput = findViewById(R.id.format_input);
        regexResult = findViewById(R.id.regex_result);
        formattedResult = findViewById(R.id.formatted_result);
        toggleAdvanced = findViewById(R.id.toggle_advanced);
        speakButton = findViewById(R.id.speak_button);
        saveButton = findViewById(R.id.save_button);
        advancedSection = findViewById(R.id.advanced_section);
        appNameTxtView = findViewById(R.id.app_title);
        testButton = findViewById(R.id.test_regex_button);


        Log.d("[DEBUG]", "Shared preference from " + this.getClass().getName());

        logSharedPreferences();

        //Log.d("[DEBUG", "Shared preference name Regex activity: " + )
        packageName = getIntent().getStringExtra("package_name");
        appName = getIntent().getStringExtra("app_name");

        regexInput.setText("(-?\\d{1,3}(?:,\\d{3})*\\sVND).*?(\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2})");
        testMessage.setText("Số dư TK VCB 001004090571 -473,684 VND lúc 17-04-2023 16:55:20. Số dư 530,250 Ref Ecom.EW23041745294945.MOMO.0975391014.CashIn.62a2acf4e077");

        appNameTxtView.setText(String.format(getString(R.string.set_regex_for), appName));
        // Toggle advanced view
        toggleAdvanced.setOnClickListener(v -> {
            isAdvancedVisible = !isAdvancedVisible;
            advancedSection.setVisibility(isAdvancedVisible ? View.VISIBLE : View.GONE);
            toggleAdvanced.setText(isAdvancedVisible ? "Hide Advanced" : "Show Advanced");
        });

        // Test message live preview (add a text listener)
        testMessage.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { updatePreview(); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        regexInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { updatePreview(); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        formatInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { updatePreview(); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Speak formatted text
        speakButton.setOnClickListener(v -> {
            //prepareTextToSpeech();
            String toSpeak = formattedResult.getText().toString();

            String utteranceId = UUID.randomUUID().toString();
            tts.speak(toSpeak, TextToSpeech.QUEUE_ADD, null, utteranceId);
            // TODO: Use TextToSpeech to speak the text
        });

        // Save config
        saveButton.setOnClickListener(v -> {
            String pattern = regexInput.getText().toString();
            String formatedSpeech = formatInput.getText().toString();
            new Thread(() ->{
                RegexFilterEntity newFilter = new RegexFilterEntity(packageName, appName, pattern, formatedSpeech);

                db.regexFilterDao().insertOrUpdate(newFilter);
                runOnUiThread(() -> Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show());

            }).start();

            finish();
        });

        testButton.setOnClickListener(v -> {
            String pattern = regexInput.getText().toString();
            String sample = testMessage.getText().toString();

            try {
                Pattern regex = Pattern.compile(pattern);
                Matcher matcher = regex.matcher(sample);

                if(matcher.find()) {
                    String strMatchFormat = "Total matched: " + matcher.groupCount() + "\n";
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        strMatchFormat += String.format("\t [%d] %s \n", i, matcher.group(i));
                    }
                    regexResult.setText(strMatchFormat);

                    Toast.makeText(RegexEditorActivity.this, "Capture group count: " + matcher.groupCount(), Toast.LENGTH_SHORT).show();
                } else {
                    regexResult.setText("No match");
                }
            } catch (PatternSyntaxException e){
                regexResult.setText("Invalid Regex: " + e.getMessage());
            }

        });

        setSavedRegex();



    }


    private void logSharedPreferences() {
        Log.d("[DEBUG]", "===== SharedPreferences Contents =====");
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("[DEBUG]", entry.getKey() + ": " + entry.getValue());
        }
        Log.d("[DEBUG]", "=====================================");
    }
    private void prepareTextToSpeech(){

        String countryName = sharedPreferences.getString(Const.PREF_SPEECH_LANG, "");
        Log.d("[DEBUG]", "Find save country speech language: " + countryName);
        Country country = Const.currentAvailableLocale.get(countryName);
        Locale currentLanguage = country == null ? Const.DEFAULT_LOCALE
                                                : country.getLocale();

        Log.d("[DEBUG]", "Locale: " + currentLanguage);

        int result = tts.setLanguage(currentLanguage);
        if (result == TextToSpeech.LANG_MISSING_DATA) {

            Toast.makeText(this, "Missing language data", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {

            Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            return;
        }
        else {

            Locale currentLocale = tts.getVoice().getLocale();
            Toast.makeText(this, "Language " + currentLocale, Toast.LENGTH_SHORT).show();

        }

    }
    private void setSavedRegex(){
        new Thread(() -> {
            RegexFilterEntity filter = db.regexFilterDao().getByPackage(packageName);
            if (filter != null){
                runOnUiThread(() -> {
                    regexInput.setText(filter.getRegexPattern());
                    formatInput.setText(filter.getFormatedSpeech());
                });
            }
        }).start();
    }
    private void updatePreview() {
        String patternStr = regexInput.getText().toString();
        String inputText = testMessage.getText().toString();
        String formatStr = formatInput.getText().toString();

        try {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(inputText);

            if (matcher.find()) {
                StringBuilder matchBuilder = new StringBuilder();
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    matchBuilder.append("[").append(i).append("] = ").append(matcher.group(i)).append("\n");
                }
                regexResult.setText(matchBuilder.toString());

                // Replace [1], [2], ... in format string
                String formatted = formatStr;
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    formatted = formatted.replace("[" + i + "]", matcher.group(i));
                }
                formattedResult.setText(formatted);

            } else {
                regexResult.setText("No match found");
                formattedResult.setText("");
            }
        } catch (Exception e) {
            regexResult.setText("Invalid regex: " + e.getMessage());
            formattedResult.setText("");
        }
    }
}

//
//public class RegexEditorActivity extends AppCompatActivity {
//    private EditText editRegex, editSample;
//    private TextView textResult, textAppName;
//    private Button btnTest, btnSave;
//
//    private AppDatabase db;
//    private String packageName;
//    private String appName;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_regex_editor);
//
//        Toast.makeText(this, "Regex activity", Toast.LENGTH_SHORT).show();
//        editRegex = findViewById(R.id.editRegex);
//        editSample = findViewById(R.id.editSampleText);
//        textResult = findViewById(R.id.textResult);
//        textAppName = findViewById(R.id.textAppName);
//        btnTest = findViewById(R.id.btnTest);
//        btnSave = findViewById(R.id.btnSave);
//
//        db = AppDatabase.getInstance(this);
//
//        packageName = getIntent().getStringExtra("package_name");
//        appName = getIntent().getStringExtra("app_name");
//
//        editRegex.setText("(-?\\d{1,3}(?:,\\d{3})*\\sVND).*?(\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2})");
//        editSample.setText("Số dư TK VCB 001004090571 -473,684 VND lúc 17-04-2023 16:55:20. Số dư 530,250 Ref Ecom.EW23041745294945.MOMO.0975391014.CashIn.62a2acf4e077");
//
//
//        textAppName.setText(String.format(getString(R.string.set_regex_for), appName));
//        new Thread(() -> {
//            RegexFilterEntity RegexFilterEntity;
//            RegexFilterEntity = db.regexFilterDao().getByPackage(packageName);
//            if (RegexFilterEntity != null){
//                editRegex.setText(RegexFilterEntity.getRegexPattern());
//
//            }
//            try {
//
//
//                String result = RegexFilterEntity.getRegexPattern();
//                Log.d("[DEBUG]", "Return getRegexPattern: " + result);
//
//            } catch (Exception e){
//                Log.d("[DEBUG]", e.getMessage());
//            }
//
//        }).start();
//
//
//
//        new Thread(() -> {
//            RegexFilterEntity filter = db.regexFilterDao().getByPackage(packageName);
//            if (filter != null){
//                runOnUiThread(() -> editRegex.setText(filter.getRegexPattern()));
//            }
//        }).start();
//
//        btnTest.setOnClickListener(v -> {
//            String pattern = editRegex.getText().toString();
//            String sample = editSample.getText().toString();
//
//            try {
//                Pattern regex = Pattern.compile(pattern);
//                Matcher matcher = regex.matcher(sample);
//
//                if(matcher.find()) {
//                    String strMatchFormat = "Total matched: " + matcher.groupCount() + "\n";
//                    for (int i = 1; i <= matcher.groupCount(); i++) {
//                        strMatchFormat = String.format("\t [%d] %s \n", i, matcher.group(i));
//                    }
//                    textResult.setText(strMatchFormat);
//
//                    Toast.makeText(RegexEditorActivity.this, "Capture group count: " + matcher.groupCount(), Toast.LENGTH_SHORT).show();
//                } else {
//                    textResult.setText("No match");
//                }
//            } catch (PatternSyntaxException e){
//                textResult.setText("Invalid Regex: " + e.getMessage());
//            }
//
//        });
//
//        btnSave.setOnClickListener(v -> {
//            String pattern = editRegex.getText().toString();
//
//            new Thread(() ->{
//                RegexFilterEntity newFilter = new RegexFilterEntity(packageName, appName, pattern);
//
//                db.regexFilterDao().insertOrUpdate(newFilter);
//                runOnUiThread(() -> Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show());
//
//            }).start();
//
//            finish();
//        });
//
//
//
//    }
//}

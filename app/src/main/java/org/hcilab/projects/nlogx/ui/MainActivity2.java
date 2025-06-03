package org.hcilab.projects.nlogx.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import org.hcilab.projects.nlogx.R;
import org.hcilab.projects.nlogx.misc.Const;
import org.hcilab.projects.nlogx.Entity.Country;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity2 extends AppCompatActivity {
    Spinner spinner;
    EditText textToSpeech;
    TextToSpeech tts;
    Locale selectedLang;
    Button speak_btn;
    Boolean shouldFilterLanguage = Boolean.FALSE;
    Map<String,Country> languagesForCountries = new HashMap<>();
    SharedPreferences sharedPreferences;

    ArrayList<Country> supportedLocale;
    TextView textViewSpinner;
    Dialog dialog;

    String sharedPrefName;
    int sharedPrefMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //provide size to arrayList
        //supportedLocale=new ArrayList<>();
        //call this spinner function
        funCustomSpinner();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //spinner = findViewById(R.id.speech_lang);
        textToSpeech = findViewById(R.id.text_to_speak);
        speak_btn = findViewById(R.id.speak_btn);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d("[DEBUG]", "TextToSpeech onInit status: " + status);


            }
        });

        supportedLocale = getSupportedLocale(tts);
        supportedLocale.add(new Country(Const.DEFAULT_LOCALE, Const.DEFAULT_LOCALE.getDisplayName()));
        supportedLocale.add(new Country(Locale.FRANCE, Locale.FRANCE.getDisplayName()));

        ArrayAdapter<Country> adapter = new ArrayAdapter<Country>(this, R.layout.simple_spinner, R.id.txt_bundle, supportedLocale);
        adapter.setDropDownViewResource(R.layout.simple_spinner);

        speak_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String countryName = sharedPreferences.getString(Const.PREF_SPEECH_LANG, "");
                Log.d("[DEBUG]", "Set current language status: " + countryName);

                Locale currentLanguage = Const.currentAvailableLocale.get(countryName).getLocale();
                Log.d("[DEBUG]", "Set current locale: " + currentLanguage);

                int result = tts.setLanguage(currentLanguage);
                Log.d("[DEBUG]", "Set current language status: " + result);

                Log.d("[DEBUG]", "Language " + currentLanguage.getCountry());
                Toast.makeText(view.getContext(), "Language " + currentLanguage, Toast.LENGTH_SHORT).show();
                String toSpeak = textToSpeech.getText().toString();
                Toast.makeText(view.getContext(), textToSpeech.getText(), Toast.LENGTH_SHORT).show();
                String utteranceId = UUID.randomUUID().toString();
                tts.speak(toSpeak, TextToSpeech.QUEUE_ADD, null, utteranceId);
            }
        });
        shouldFilterLanguage = true;

        Intent intent= getIntent();
        Bundle b = intent.getExtras();

        if(b!=null)
        {
            sharedPrefName = b.getString("sharedPrefName", "");
            sharedPrefMode = b.getInt("sharedPrefMode", 0);
            Log.d("[DEBUG]", "sharedPrefName: " + sharedPrefName);
            Log.d("[DEBUG]", "sharedPrefMode: " + sharedPrefMode);
        }
        //sharedPreferences = this.getSharedPreferences(sharedPrefName, sharedPrefMode);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.d("[DEBUG]", "Shared preference from " + this.getClass().getName());

        logSharedPreferences();
        TextView text_view_for_dropdown = this.findViewById(R.id.text_view_for_dropdown);
        Country country = Const.currentAvailableLocale.get(sharedPreferences.getString(Const.PREF_SPEECH_LANG, ""));
        if (country != null)
        {
            text_view_for_dropdown.setText(country.getName());
        }
    }

    public ArrayList<Country> getSupportedLocale(TextToSpeech tts){
        Locale[] locales = Locale.getAvailableLocales();
        Log.d("[DEBUG]", "Total Locale: " + locales.length);
        ArrayList<Country> localeList = new ArrayList<Country>();
        for (Locale locale : locales) {
            if (locale.getCountry() != ""){
                Country country = new Country(locale, locale.getDisplayName());
                localeList.add(country);
                languagesForCountries.put(locale.toString(), country);
            }

        }


        return localeList;
    }

    private void logSharedPreferences() {
        Log.d("[DEBUG]", "===== SharedPreferences Contents =====");
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("[DEBUG]", entry.getKey() + ": " + entry.getValue());
        }
        Log.d("[DEBUG]", "=====================================");
    }
    public void funCustomSpinner() {
        //we are adding values in arraylist


        //provide id to textview and set onClick lister
        textViewSpinner = findViewById(R.id.text_view_for_dropdown);
        textViewSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(MainActivity2.this);
                //set  (our custom layout for dialog)
                dialog.setContentView(R.layout.layout_searchable_spinner);

                //set transparent background
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                //show dialog
                dialog.show();

                //initialize and assign variable
                EditText editText = dialog.findViewById(R.id.editText_of_searchableSpinner);
                ListView listView = dialog.findViewById(R.id.listView_of_searchableSpinner);


                //array adapter
                ArrayAdapter<Country> arrayAdapter = new ArrayAdapter<>(MainActivity2.this,
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, supportedLocale);
                listView.setAdapter(arrayAdapter);
                //Textwatcher for change data after every text type by user
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //filter arraylist
                        arrayAdapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                // listview onitem click listener
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Country country = (Country) arrayAdapter.getItem(i);
                        selectedLang = country.getLocale();
                        int result = tts.setLanguage(selectedLang);
                        Context context = adapterView.getContext();
                        if (result == TextToSpeech.LANG_MISSING_DATA) {

                            Toast.makeText(context, "Missing language data", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {

                            Toast.makeText(context, "Language not supported", Toast.LENGTH_SHORT).show();
                            return;
                        } else {

                            Locale currentLanguage = tts.getVoice().getLocale();
                            Toast.makeText(context, "Language " + currentLanguage, Toast.LENGTH_SHORT).show();

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(Const.PREF_SPEECH_LANG, currentLanguage.toString());
                            editor.apply();
                        }
                        textViewSpinner.setText(arrayAdapter.getItem(i).getName());
                        Toast.makeText(MainActivity2.this, "Selected:" + arrayAdapter.getItem(i), Toast.LENGTH_SHORT).show();
                        //dismiss dialog after choose
                        dialog.dismiss();

//                        if (shouldFilterLanguage){
//                            supportedLocale = getSupportedLocale(tts);
//                            shouldFilterLanguage = false;
//                            Log.d("[DEBUG]", "Filter languages: " + supportedLocale.size());
//
//                        }
                    }
                });
            }
        });
    }
}
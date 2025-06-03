package org.hcilab.projects.nlogx.misc;

//import org.hcilab.projects.nlogx.BuildConfig;

import org.hcilab.projects.nlogx.Entity.Country;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Const {

	public static final boolean DEBUG = true;
	public static final long VERSION  = 0;
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	// Feature flags
	public static final boolean ENABLE_ACTIVITY_RECOGNITION = true;
	public static final boolean ENABLE_LOCATION_SERVICE     = true;

	// Preferences shown in the UI
	public static final String PREF_STATUS  = "pref_status";
	public static final String PREF_BROWSE  = "pref_browse";
	public static final String PREF_TEXT    = "pref_text";
	public static final String PREF_ONGOING = "pref_ongoing";
	public static final String PREF_ABOUT   = "pref_about";
	public static final String PREF_VERSION = "pref_version";
	public static final String PREF_TTS_ENABLED = "PREF_TTS_ENABLED";
	public static final String PREF_SPEECH_LANG = "pref_speech_lang";
	public static final String PREF_APP_FILTER = "pref_app_filter";
	// Preferences not shown in the UI
	public static final String PREF_LAST_ACTIVITY  = "pref_last_activity";
	public static final String PREF_LAST_LOCATION  = "pref_last_location";

	public static final Map<String, Country> currentAvailableLocale = getAvailableLocale();
	private static Map<String, Country> getAvailableLocale(){
		Locale[] locales = Locale.getAvailableLocales();
		Map<String,Country> languagesForCountries = new HashMap<String, Country>();
		for (Locale locale : locales) {
			if (!locale.getCountry().isEmpty()){
				Country country = new Country(locale, locale.getDisplayName());
				languagesForCountries.put(locale.toString(), country);
			}
		}
		return languagesForCountries;
	}

}
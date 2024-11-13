package com.emopass.antitheftalarm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.emopass.antitheftalarm.model.LstPkgNameTaskInfo;
import java.util.ArrayList;
import java.util.List;

public class PreferencesHelper {

    private static SharedPreferences sharedPreferences;
    private static final String NAME = "MyPref";
    private static final String FIRST_TIME_SETUP_COMPLETE = "first time setup complete";
    public static final String ENABLE_KIDZONE = "enable kidzone";
    private static final String LIST_APP_KID_ZONE = "list app kid zone";
    public static final String SETTING_VALUE_SOUND = "setting value sound";
    public static final String SETTING_VALUE_TIMER = "setting value timer";
    public static final String SETTING_VALUE_TONE = "setting value alram tone";
    public static final String PATTERN_CODE = "pattern code";
    public static final String FINGERPRINT_UNLOCK = "fingerprint unlock";
    public static final String PIN_CODE = "pin code";
    public static final String INTRUDER_SELFIE_ENTRIES = "intruder selfie entries";
    public static final String INTRUDER_SELFIE = "intruder selfie ";
    public static final String HIDE_PATTERN = "hide pattern ";
    public static final String FULL_CHARGER_ALARM = "full charger alarm";
    public static final String NOTI_CHARGER_CONNECTED = "notifi charger connected";
    public static final String VIBRATE_DURING_ALARM = "vibrate during alarm";
    public static final String FLASH_DURING_ALARM = "flash during alarm";
    public static final String DETECTION_TYPE = "detection type";
    public static final String CHECKING_PERMISSION = "checking permission";

    public static final String LANGUAGE_UNIQUE_CODE = "language code";

    public static void init(Context mContext) {
        sharedPreferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor editor() {
        return sharedPreferences.edit();
    }

    public static void putBoolean(String key, boolean value) {
        editor().putBoolean(key, value).apply();
    }

    public static void putString(String key, String value) {
        editor().putString(key, value).apply();
    }

    public static void putInt(String key, int value) {
        editor().putInt(key, value).apply();
    }

    public static void putLong(String key, long value) {
        editor().putLong(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defaultvalue) {
        return sharedPreferences.getBoolean(key, defaultvalue);
    }

    public static String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public static int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public static List<String> getListAppKidZone() {
        String data = sharedPreferences.getString(LIST_APP_KID_ZONE, "");
        Gson gson = new Gson();
        LstPkgNameTaskInfo object = gson.fromJson(data, LstPkgNameTaskInfo.class);
        return object != null ? object.getLst() : new ArrayList<>();
    }

    public static String getLanguage() {
        return getString(LANGUAGE_UNIQUE_CODE);
    }

    public static void setLanguage(String lang) {
        sharedPreferences.edit().putString(LANGUAGE_UNIQUE_CODE, lang).apply();
    }

    public static void setListAppKidZone(List<String> lstApp) {
        Gson gson = new Gson();
        String data = gson.toJson(new LstPkgNameTaskInfo(lstApp));
        sharedPreferences.edit().putString(LIST_APP_KID_ZONE, data).apply();
    }

    public static void setFirstTimeSetupComplete(boolean isCompleted) {
        putBoolean(FIRST_TIME_SETUP_COMPLETE, isCompleted);
    }

    public static boolean isFirstTimeSetupComplete() {
        return getBoolean(FIRST_TIME_SETUP_COMPLETE, false);
    }

    public static void setCheckingPermission(boolean isChecking) {
        putBoolean(CHECKING_PERMISSION, isChecking);
    }

    public static boolean isCheckingPermission() {
        return getBoolean(CHECKING_PERMISSION, false);
    }

    public static void setPatternCode(String patternCode) {
        sharedPreferences.edit().putString(PATTERN_CODE, patternCode).apply();
        // set pin code empty
        sharedPreferences.edit().putString(PIN_CODE, "").apply();
    }

    public static String getPatternCode() {
        return sharedPreferences.getString(PATTERN_CODE, "");
    }

    public static void setPinCode(String pinCode) {
        sharedPreferences.edit().putString(PIN_CODE, pinCode).apply();
        // set pattern code empty
        sharedPreferences.edit().putString(PATTERN_CODE, "").apply();
    }

    public static String getPinCode() {
        return sharedPreferences.getString(PIN_CODE, "");
    }

    public static boolean isPatternCode() {
        if (TextUtils.isEmpty(getPinCode())) {
            return true;
        } else return !TextUtils.isEmpty(getPatternCode());
    }
}

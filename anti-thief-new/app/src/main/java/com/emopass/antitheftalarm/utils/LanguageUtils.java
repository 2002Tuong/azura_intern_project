package com.emopass.antitheftalarm.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.model.LanguageModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageUtils {
    private static LanguageUtils instance = null;
    private Locale curLocale = null;
    private final List<LanguageModel> supportedLanguageList = new ArrayList<>();

    public static LanguageUtils getInstance() {
        if(instance == null) {
            instance = new LanguageUtils();
        }
        return instance;
    }

    private LanguageUtils() {
        supportedLanguageList.add(new LanguageModel("en", R.string.lang_english, R.drawable.icon_flag_england, false));
        supportedLanguageList.add(new LanguageModel("hi", R.string.lang_hindi, R.drawable.icon_flag_india, false));
        supportedLanguageList.add(new LanguageModel("es", R.string.lang_spanish, R.drawable.icon_flag_es, false));
        supportedLanguageList.add(new LanguageModel("pt", R.string.lang_portugal, R.drawable.icon_flag_portugal, false));
        supportedLanguageList.add(new LanguageModel("fr", R.string.lang_french, R.drawable.icon_flag_france, false));
        supportedLanguageList.add(new LanguageModel("ko", R.string.lang_korean, R.drawable.icon_flag_korea, false));
        supportedLanguageList.add(new LanguageModel("ja", R.string.lang_japanese, R.drawable.icon_flag_japan, false));
        supportedLanguageList.add(new LanguageModel("vi", R.string.lang_vietnamese, R.drawable.icon_flag_vietnam, false));
        supportedLanguageList.add(new LanguageModel("zh", R.string.lang_chinese, R.drawable.icon_flag_chinese, false));
        supportedLanguageList.add(new LanguageModel("ru", R.string.lang_russian, R.drawable.icon_flag_russian, false));
        supportedLanguageList.add(new LanguageModel("ar", R.string.lang_arabic, R.drawable.icon_flag_saudi_arabia, false));
        supportedLanguageList.add(new LanguageModel("bn", R.string.lang_bengali, R.drawable.icon_flag_bangladesh, false));
        supportedLanguageList.add(new LanguageModel("ur", R.string.lang_urdu, R.drawable.icon_flag_urdu, false));
        supportedLanguageList.add(new LanguageModel("de", R.string.lang_german, R.drawable.icon_flag_germany, false));
        supportedLanguageList.add(new LanguageModel("mr", R.string.lang_marathi, R.drawable.icon_flag_india, false));
        supportedLanguageList.add(new LanguageModel("te", R.string.lang_telugu, R.drawable.icon_flag_india, false));
        supportedLanguageList.add(new LanguageModel("tr", R.string.lang_turkish, R.drawable.icon_flag_turkey, false));
        supportedLanguageList.add(new LanguageModel("ta", R.string.lang_tamil, R.drawable.icon_flag_india, false));
        supportedLanguageList.add(new LanguageModel("it", R.string.lang_italian, R.drawable.icon_flag_italy, false));
        supportedLanguageList.add(new LanguageModel("th", R.string.lang_thai, R.drawable.icon_flag_thailand, false));
    }

    public List<LanguageModel> getSupportedLanguages() {
        String deviceLanguage = getDeviceLanguage();
        int languageDevicePosition = -1;
        for (int i = 0; i < supportedLanguageList.size(); i++) {
            if(deviceLanguage.equals(supportedLanguageList.get(i).getLanguageCode())) {
                languageDevicePosition = i;
                break;
            }
        }

        List<LanguageModel> supportedDeviceLanguageList = new ArrayList<>();
        if(languageDevicePosition < 2) {
            supportedDeviceLanguageList = supportedLanguageList;
        } else {
            LanguageModel deviceLanguageModel =  supportedLanguageList.get(languageDevicePosition);
            supportedLanguageList.remove(languageDevicePosition);
            supportedLanguageList.add(2, deviceLanguageModel);
            supportedDeviceLanguageList = supportedLanguageList;
        }

        return  supportedDeviceLanguageList;
    }

    private void saveLocale(String lang) {
        PreferencesHelper.setLanguage(lang);
    }

    public void loadLocale(Context context) {
        String languageCode = PreferencesHelper.getLanguage();
        if(languageCode.isEmpty()) {
            Configuration configuration = new Configuration();
            Locale defaultLocale = Locale.getDefault();
            Locale.setDefault(defaultLocale);
            configuration.setLocale(defaultLocale);
            context.getResources().updateConfiguration(
                    configuration, context.getResources().getDisplayMetrics()
            );
        } else {
            changeLang(languageCode, context);
        }
    }

    public void changeLang(String languageCode, Context context) {
        if(languageCode.isEmpty()) {
            return;
        }
        curLocale =new Locale(languageCode);
        saveLocale(languageCode);
        if(curLocale != null) {
            Locale.setDefault(curLocale);
        }
        Configuration configuration = new Configuration();
        configuration.setLocale(curLocale);
        context.getResources().updateConfiguration(
                configuration, context.getResources().getDisplayMetrics()
        );
    }

    private String getDeviceLanguage() {
        Locale systemLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
        return  systemLocale.getLanguage();
    }
}

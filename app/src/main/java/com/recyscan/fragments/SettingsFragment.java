package com.recyscan.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.recyscan.R;
import com.recyscan.utils.Constants;
import com.recyscan.utils.LocaleHelper;
import com.recyscan.utils.ThemeHelper;

/**
 * Settings Fragment - Dark mode, language, notifications, city.
 */
public class SettingsFragment extends Fragment {

    private MaterialSwitch switchDarkMode, switchNotifications;
    private TextView tvSelectedCity, tvSelectedLanguage, tvAppVersion;
    private SharedPreferences prefs;

    private final String[] cities = {"Berlin","Hamburg","München","Köln","Frankfurt","Stuttgart","Düsseldorf","Leipzig","Dortmund","Essen","Bremen","Dresden","Hannover","Nürnberg"};
    private final String[] languages = {"Deutsch", "English", "العربية"};
    private final String[] languageCodes = {"de", "en", "ar"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        tvSelectedCity = view.findViewById(R.id.tv_selected_city);
        tvSelectedLanguage = view.findViewById(R.id.tv_selected_language);
        tvAppVersion = view.findViewById(R.id.tv_app_version);
        tvAppVersion.setText(getString(R.string.app_version) + " 1.1.0");

        switchDarkMode.setChecked(ThemeHelper.isDarkModeEnabled(requireContext()));
        switchNotifications.setChecked(prefs.getBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, true));
        tvSelectedCity.setText(prefs.getString(Constants.PREF_CITY, "Berlin"));
        
        String curLang = LocaleHelper.getLanguage(requireContext());
        String curLangDisplay = "Deutsch";
        if (curLang.equals("en")) curLangDisplay = "English";
        else if (curLang.equals("ar")) curLangDisplay = "العربية";
        tvSelectedLanguage.setText(curLangDisplay);

        switchDarkMode.setOnCheckedChangeListener((b, checked) -> ThemeHelper.toggleDarkMode(requireContext()));
        switchNotifications.setOnCheckedChangeListener((b, checked) -> prefs.edit().putBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, checked).apply());

        view.findViewById(R.id.row_city).setOnClickListener(v -> {
            String cur = prefs.getString(Constants.PREF_CITY, "Berlin");
            int sel = 0;
            for (int i = 0; i < cities.length; i++) if (cities[i].equals(cur)) { sel = i; break; }
            new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.select_city)
                .setSingleChoiceItems(cities, sel, (d, w) -> { prefs.edit().putString(Constants.PREF_CITY, cities[w]).apply(); tvSelectedCity.setText(cities[w]); d.dismiss(); })
                .setNegativeButton(R.string.cancel, null).show();
        });

        view.findViewById(R.id.row_language).setOnClickListener(v -> {
            String cur = LocaleHelper.getLanguage(requireContext());
            int sel = 0;
            for (int i = 0; i < languageCodes.length; i++) if (languageCodes[i].equals(cur)) { sel = i; break; }
            new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.select_language)
                .setSingleChoiceItems(languages, sel, (d, w) -> {
                    String selectedCode = languageCodes[w];
                    if (!selectedCode.equals(cur)) {
                        LocaleHelper.setLocale(requireContext(), selectedCode);
                        requireActivity().recreate();
                    }
                    d.dismiss();
                })
                .setNegativeButton(R.string.cancel, null).show();
        });
    }
}

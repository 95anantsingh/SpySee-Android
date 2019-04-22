package inc.humanoid.spysee;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            preference.setSummary(stringValue);
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SettingsFragment.class.getName().equals(fragmentName);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            setHasOptionsMenu(false);
            bindPreferenceSummaryToValue(findPreference("wifi_ssid"));
           // bindPreferenceSummaryToValue(findPreference("wifi_password"));
            bindPreferenceSummaryToValue(findPreference("ip_address"));
            bindPreferenceSummaryToValue(findPreference("video_port"));
            bindPreferenceSummaryToValue(findPreference("video_address"));
            bindPreferenceSummaryToValue(findPreference("motionCtrl_port"));
            bindPreferenceSummaryToValue(findPreference("camCtrl_port"));
            bindPreferenceSummaryToValue(findPreference("specialCtrl_port"));
            bindPreferenceSummaryToValue(findPreference("emergencyCtrl_port"));
        }
    }}
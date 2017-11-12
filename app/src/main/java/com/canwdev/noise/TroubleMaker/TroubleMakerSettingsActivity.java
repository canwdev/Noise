package com.canwdev.noise.TroubleMaker;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.canwdev.noise.AppCompatPreferenceActivity;
import com.canwdev.noise.R;
import com.canwdev.noise.util.Conf;

public class TroubleMakerSettingsActivity extends AppCompatPreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        preference.setSummary(stringValue);

        return true;
    };


    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is changed,
     * its summary (line of text below the preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is dependent on the type of preference.
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        addPreferencesFromResource(R.xml.tm_preferences_settings);

        bindPreferenceSummaryToValue(findPreference(Conf.pTmMaxDb));
        bindPreferenceSummaryToValue(findPreference(Conf.pTmIntervalDelay));
        bindPreferenceSummaryToValue(findPreference(Conf.pTmEnReferenceAmp));
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                //startActivity(new Intent(this, TroubleMakerSettingsActivity.class));
                //OR USING finish();
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

}

package local.googlehomeru;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public final class SettingsActivity extends Activity {
    private EditText targetLanguage;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(this);
        title.setText(R.string.settings_title);
        title.setTextSize(20);
        layout.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView description = new TextView(this);
        description.setText(R.string.settings_description);
        layout.addView(description, spacedParams());

        targetLanguage = new EditText(this);
        targetLanguage.setSingleLine(true);
        targetLanguage.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        targetLanguage.setHint(HookTargetPolicy.DEFAULT_TARGET_LANGUAGE);
        targetLanguage.setText(getSharedPreferences(
                LanguageSettings.PREFERENCES_NAME, MODE_WORLD_READABLE).getString(
                LanguageSettings.TARGET_LANGUAGE_KEY,
                HookTargetPolicy.DEFAULT_TARGET_LANGUAGE));
        layout.addView(targetLanguage, spacedParams());

        Button save = new Button(this);
        save.setText(R.string.save_language);
        save.setOnClickListener(view -> saveLanguage());
        layout.addView(save, spacedParams());
        setContentView(layout);
    }

    private void saveLanguage() {
        String value = targetLanguage.getText().toString().trim();
        if (!HookTargetPolicy.isValidTargetLanguage(value)) {
            targetLanguage.setError(getString(R.string.invalid_language));
            return;
        }

        boolean saved = getSharedPreferences(
                LanguageSettings.PREFERENCES_NAME, MODE_WORLD_READABLE).edit()
                .putString(LanguageSettings.TARGET_LANGUAGE_KEY, value)
                .commit();
        Toast.makeText(this, saved ? R.string.language_saved : R.string.language_save_failed,
                Toast.LENGTH_LONG).show();
    }

    private LinearLayout.LayoutParams spacedParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) (16 * getResources().getDisplayMetrics().density);
        return params;
    }
}

package com.pcchin.studyassistant;

import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences("com.pcchin.studyassistant", MODE_PRIVATE);
        if (sharedPref.getBoolean("isDark", false)) {
            setContentView(R.layout.activity_about_dark);
        } else {
            setContentView(R.layout.activity_about);
        }

        // Set version
        TextView textView = findViewById(R.id.m2_version);
        textView.setText(String.format("%s%s", getString(R.string.m_version), BuildConfig.VERSION_NAME));

        // Set license text
        Spanned license;
        String licenseText = GeneralFunctions.getReadTextFromAssets(this, "license.txt");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            license = Html.fromHtml(licenseText, Html.FROM_HTML_MODE_LEGACY); // Adds hyperlink to text
        } else {
            license = Html.fromHtml(licenseText);
        }
        TextView licenseView = findViewById(R.id.m2_license);
        licenseView.setTextSize(18);
        licenseView.setText(license);
        licenseView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

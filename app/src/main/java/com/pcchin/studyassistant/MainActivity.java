package com.pcchin.studyassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.pcchin.studyassistant.notes.NotesSelectActivity;
import com.pcchin.studyassistant.project.ProjectSelectActivity;

public class MainActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences("com.pcchin.studyassistant", MODE_PRIVATE);
        if (sharedPref.getBoolean("isDark", false)) {
            setContentView(R.layout.activity_main_dark);
        } else {
            setContentView(R.layout.activity_main);
        }

        // Set button listeners
        findViewById(R.id.m1_notes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NotesSelectActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.m1_projects).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProjectSelectActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.m1_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Press back to exit
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 1500);
        }
    }

}

package com.pcchin.studyassistant.project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pcchin.studyassistant.R;

public class ProjectInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ProjectSelectActivity.class);
        startActivity(intent);
    }
}

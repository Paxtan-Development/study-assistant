package com.pcchin.studyassistant.project.status;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pcchin.studyassistant.R;

public class ProjectStatusEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_status_edit);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ProjectStatusActivity.class);
        startActivity(intent);
    }
}

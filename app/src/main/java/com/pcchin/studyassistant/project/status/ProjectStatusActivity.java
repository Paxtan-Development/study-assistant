package com.pcchin.studyassistant.project.status;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.project.ProjectInfoActivity;

public class ProjectStatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_status);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ProjectInfoActivity.class);
        startActivity(intent);
    }
}

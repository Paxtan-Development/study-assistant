package com.pcchin.studyassistant.notes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pcchin.studyassistant.R;

public class NotesViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_view);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, NotesSubjectActivity.class);
        startActivity(intent);
    }
}

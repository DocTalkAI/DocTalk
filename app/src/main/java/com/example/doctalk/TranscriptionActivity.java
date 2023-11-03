package com.example.doctalk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TranscriptionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcription);

        // Retrieve the transcribed text from the intent's extra
        String transcription = getIntent().getStringExtra("transcription");

        // Display the transcribed text in your activity's layout
        TextView textView = findViewById(R.id.transcriptionTextView);
        textView.setText(transcription);
    }
}

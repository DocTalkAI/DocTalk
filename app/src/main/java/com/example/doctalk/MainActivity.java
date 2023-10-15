package com.example.doctalk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1001;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startRecordingButton = findViewById(R.id.button);
        Button stopRecordingButton = findViewById(R.id.stop);

        storage = FirebaseStorage.getInstance();
        // Create a reference to the Firebase Storage bucket where you want to upload the audio
        StorageReference storageRef = storage.getReference();

        // Request runtime permissions for audio recording
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION);
        }

        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        // Permission is granted, start recording
                        startRecording();
                    } else {
                        // Permission is not granted, you can show a message or request permission again
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION);
                    }
                }
            }
        });

        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    // Stop recording and upload
                    stopRecording();
                }
            }
        });
    }

    private void startRecording() {
        if (!isRecording) {
            // Set up the media recorder and audio file path using the app's external files directory
            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            if (externalFilesDir != null) {
                audioFilePath = externalFilesDir.getAbsolutePath() + "/audio.m4a"; // Use .m4a format
            } else {
                Toast.makeText(this, "External storage is not available", Toast.LENGTH_SHORT).show();
                return;
            }

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // Specify .m4a format
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audioFilePath);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
                isRecording = true; // Update the recording state
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopRecording() {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
                isRecording = false; // Update the recording state
                // Upload the recorded audio to Firebase
                uploadAudioToFirebase(audioFilePath);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error stopping recording", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadAudioToFirebase(String filePath) {
        // Specify the file type when uploading to Firebase Storage
        StorageReference audioRef = storage.getReference().child("audio/audio.m4a");

        audioRef.putFile(Uri.fromFile(new File(filePath)))
                .addOnSuccessListener(taskSnapshot -> {
                    // Audio file uploaded successfully
                    Toast.makeText(this, "Audio uploaded to Firebase", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle upload failure, e.g., display an error message
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isRecording) {
                    startRecording();
                }
            } else {
                Toast.makeText(this, "Permission denied. Cannot record audio.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
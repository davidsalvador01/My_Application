package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.R;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String error;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        Intent intent = getIntent();
        error = intent.getStringExtra("error");

        String err = generateText(error);

        TextView textView = findViewById(R.id.textView3);
        textView.setText(err);
    }

    private String generateText(String error){
        if (error.contains("CouldNotFindSpotifyApp")){
            return error + ".\nThe Spotify app is not installed on the device.";
        }
        if (error.contains("NotLoggedInException")){
            return error + ".\nNo one is logged in to the Spotify app on this device.";
        }
        if (error.contains("UserNotAuthorizedException")){
            return error + ".\nUser did not authorize this client of App Remote to use Spotify on the users behalf.";
        }
        if (error.contains("UnsupportedFeatureVersionException")){
            return error + ".\nSpotify app can't support requested features. User should update Spotify app.";
        }
        if (error.contains("AuthenticationFailedException")){
            return error + ".\nPartner app failed to authenticate with Spotify.";
        }
        return error;
    }

    public void button_inicio(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
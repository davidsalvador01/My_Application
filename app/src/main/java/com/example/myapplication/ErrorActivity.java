package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String error;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        Intent intent = getIntent();
        error = intent.getStringExtra("error");
        Log.d("Error recibido: ", ""+ error);

        String err = generateText(error);

        TextView textView = findViewById(R.id.textView3);
        textView.setText(err);
    }

    private String generateText(String error){
        if (error.contains("CouldNotFindSpotifyApp")){
            String s = error + ".\nThe Spotify app is not installed on the device.";
            return s;
        }
        if (error.contains("NotLoggedInException")){
            String s = error + ".\nNo one is logged in to the Spotify app on this device.";
            return s;
        }
        if (error.contains("UserNotAuthorizedException")){
            String s = error + ".\nUser did not authorize this client of App Remote to use Spotify on the users behalf.";
            return s;
        }
        if (error.contains("UnsupportedFeatureVersionException")){
            String s = error + ".\nSpotify app can't support requested features. User should update Spotify app.";
            return s;
        }
        if (error.contains("AuthenticationFailedException")){
            String s = error + ".\nPartner app failed to authenticate with Spotify.";
            return s;
        }
        return error;
    }

    public void button_inicio(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
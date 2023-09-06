package com.example.myapplication.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.db.DbHelper;
import com.example.myapplication.db.DbUsers;
import com.example.myapplication.typedefs.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.Task;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LoginActivity extends AppCompatActivity {
    private FitnessOptions fitnessOptions;
    private GoogleSignInAccount account;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private boolean connected_to_fit = false;
    private String id_user;
    private String location;
    private String[] choiceItems = {"Outdoor", "Indoor"};
    private int selectedOption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Resources resources = this.getResources();
        InputStream inputStreamSongs = resources.openRawResource(R.raw.songs);
        InputStream inputStreamArtists = resources.openRawResource(R.raw.artists);
        InputStream inputStreamSongArtists = resources.openRawResource(R.raw.song_artists);
        InputStream inputStreamGenres = resources.openRawResource(R.raw.genres);
        InputStream inputStreamSongGenres = resources.openRawResource(R.raw.song_genres);

        DbHelper dbHelper = new DbHelper(LoginActivity.this, inputStreamSongs,
                inputStreamArtists, inputStreamSongArtists,
                inputStreamGenres, inputStreamSongGenres);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.releaseReference();
        db.close();
        dbHelper.close();
    }


    public void connect_google_fit(View view) {
        if(!connected_to_fit){
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();

            connect.launch(signInIntent);
        } else {
            Toast.makeText(getApplicationContext(), " You are already connected to Google Fit " ,
                    Toast.LENGTH_SHORT).show();
        }

    }

    ActivityResultLauncher<Intent> connect = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error, Failed to connect " +
                            "con la cuenta de Google", Toast.LENGTH_SHORT).show();
                }
            });


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            checkGoogleFitPermissions();
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            this.account = account;
            TextView textViewEmail = findViewById(R.id.textViewEmail);
            textViewEmail.setText("Welcome, " + account.getEmail());

            String imageUrl = loadPhoto();

            checkAndAddUser(account.getDisplayName(), account.getEmail(), imageUrl);

            Toast.makeText(getApplicationContext(), " Successfully connected to Google Fit " ,
                    Toast.LENGTH_SHORT).show();

            chooseLocation();
            connected_to_fit = true;

        } catch (ApiException e) {
            Log.w("Google", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private String loadPhoto(){
        if (account.getPhotoUrl() != null) {
            ImageView imageView = findViewById(R.id.imageProfile);
            Picasso.get().load(account.getPhotoUrl().toString()).into(imageView);
            return account.getPhotoUrl().toString();
        }
        return "null";
    }

    private String generateIdUser(String email){
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            byte[] hashBytes = digest.digest(email.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkAndAddUser(String name, String email, String photoUrl){
        DbUsers dbUsers = new DbUsers(this);
        User u = dbUsers.searchUserByEmail(email);

        if(u == null){
            String id = generateIdUser(email);
            if (id != null){
                dbUsers.setUser(id, name, email, photoUrl);
            }
            id_user = id;
        }
        else {
            id_user = u.getId();
        }
        dbUsers.close();
    }

    public void checkGoogleFitPermissions() {

        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_CADENCE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_BASAL_METABOLIC_RATE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();

        account = getGoogleAccount();

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    LoginActivity.this,
                    MY_PERMISSIONS_REQUEST,
                    account,
                    fitnessOptions);
        }
    }
    private GoogleSignInAccount getGoogleAccount() {
        return GoogleSignIn.getAccountForExtension(LoginActivity.this, fitnessOptions);
    }

    public void connect_spotify(View view) {
        if (!connected_to_fit) {
            Toast.makeText(getApplicationContext(), "You must first connect to Google Fit", Toast.LENGTH_SHORT).show();
        }
        else {
            if(location == null){
                location = choiceItems[selectedOption];
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("source", "LoginActivity");
            intent.putExtra("id_user", id_user);
            intent.putExtra("location", location);
            startActivity(intent);
        }
    }

    private void chooseLocation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        selectedOption = 0;

        builder.setTitle("Choose location")
                .setSingleChoiceItems(choiceItems, selectedOption, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedOption = which;
                    }
                })
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        location = choiceItems[selectedOption];
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }


}
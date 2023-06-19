package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.db.DbHelper;
import com.example.myapplication.db.DbSongs;
import com.example.myapplication.db.DbUsers;
import com.example.myapplication.typedefs.Song;
import com.example.myapplication.typedefs.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.Task;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {
    private FitnessOptions fitnessOptions;
    private GoogleSignInAccount account;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private boolean connected_to_fit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Resources resources = this.getResources();
        InputStream inputStreamSongs = resources.openRawResource(R.raw.songs);
        InputStream inputStreamArtists = resources.openRawResource(R.raw.artists);
        InputStream inputStreamSongArtists = resources.openRawResource(R.raw.song_artists);
        InputStream inputStreamGenres = resources.openRawResource(R.raw.genres);
        InputStream inputStreamSongGenres = resources.openRawResource(R.raw.song_genres);

        DbHelper dbHelper = new DbHelper(MainActivity2.this, inputStreamSongs,
                inputStreamArtists, inputStreamSongArtists,
                inputStreamGenres, inputStreamSongGenres);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db != null){
            Log.d("DB", "Base de datos creada con éxito");
        } else {
            Log.d("DB", "Ha habido un error al crear la base de datos");
        }




    }

    /*public void connect_google_fit(View view) {
        // checkGoogleFitPermission();
        // connected_to_fit = true;
        // Toast.makeText(getApplicationContext(), "Conectado correctamente a Google Fit", Toast.LENGTH_SHORT).show();
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();


        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        //startActivityForResult(signInIntent, 000000);


        connected_to_fit = true;
    }


    ActivityResultLauncher<Intent> connect = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {


                    // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
                    if (result.getResultCode() == 000000) {
                        // The Task returned from this call is always completed, no need to attach
                        // a listener.
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                }
            });*/
    public void connect_google_fit(View view) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        connect.launch(signInIntent);
    }

    ActivityResultLauncher<Intent> connect = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error, No se ha podido conectar " +
                            "con la cuenta de Google", Toast.LENGTH_SHORT).show();
                }
            });


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            checkGoogleFitPermission();
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            this.account = account;
            TextView textViewEmail = findViewById(R.id.textViewEmail);
            textViewEmail.setText("Bienvenido, " + account.getEmail());

            String imageUrl = loadPhoto();

            checkAndAddUser(account.getEmail(), account.getDisplayName(), imageUrl);

            Toast.makeText(getApplicationContext(), " Conectado correctamente a Google Fit " ,
                    Toast.LENGTH_SHORT).show();
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
            connected_to_fit = true;
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google", "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
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
            // Crear una instancia del algoritmo de hash (en este caso, SHA-256)
            MessageDigest digest = MessageDigest.getInstance("MD5");

            // Calcular el hash del string
            byte[] hashBytes = digest.digest(email.getBytes());

            // Convertir el hash a representación hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String hash = hexString.toString();
            return  hash;
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
        }
    }

    public void checkGoogleFitPermission() {
        // Scopes read and write we either read or write datatype from sensor

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
                    MainActivity2.this,
                    MY_PERMISSIONS_REQUEST,
                    account,
                    fitnessOptions);
        }
    }
    private GoogleSignInAccount getGoogleAccount() {
        return GoogleSignIn.getAccountForExtension(MainActivity2.this, fitnessOptions);
    }

    public void connect_spotify(View view) {
        if (connected_to_fit == false) {
            Toast.makeText(getApplicationContext(), "Debes conectarte antes a Google Fit", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
        }
    }

}

package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.example.myapplication.db.DbSongs;
import com.example.myapplication.typedefs.Song;
import com.google.android.gms.auth.api.signin.GoogleSignIn;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;



public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;

    private static final String CLIENT_ID = "888de3e282484572a3cab7ccc088d1f9";
    private static final String REDIRECT_URI = "https://response/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private String mAccessToken;
    private float bpm_actual = -1;

    private boolean inicio = true;
    private boolean reproduciendo = true;

    ArrayList<String> selectedItems = new ArrayList();

    boolean [] checkedItems = { false, false, false, false, false, false, false };


    String track_id_playing = "";
    String track_id_ant_ejec = "";

    ArrayList<Song> songs = new ArrayList<>();
    ArrayList<Song> songs_played = new ArrayList<>();


    private FitnessOptions fitnessOptions;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();


        DbSongs dbSongs = new DbSongs(this);
        songs = dbSongs.getSongs();
        Collections.shuffle(songs);


        /* ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        MainActivity.this.onActivityResult(REQUEST_CODE, result.getResultCode(), result.getData());
                    }
                });*/

        startAuthenticationFlow();

        Timer timer = new Timer();
        Handler handler = new Handler(Looper.getMainLooper());

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Coloca aquí el código de la función que quieres ejecutar
                        readHeartRateData();
                        Log.d("MainActivity", "Función ejecutada, ha pasado 60 segundos");
                    }
                });
            }
        };
        timer.schedule(task, 0, 60000);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    private void startAuthenticationFlow(){
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }
    private void startAppRemoteConnection(String accessToken) {
        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        connected();

                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpotifyAppRemote.getPlayerApi().pause();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    mAccessToken = response.getAccessToken();
                    Log.d("MainActivity", "Access token: " + mAccessToken);
                    startAppRemoteConnection(mAccessToken);
                    break;
                case ERROR:
                    Log.e("MainActivity", "Auth error: " + response.getError());
                    break;
                default:
                    Log.d("MainActivity", "Auth result: " + response.getType());
            }
        }
    }

    private void connected() {
        readHeartRateData();

        if ( songs.size() != 0) {
            Song song = songs.remove(0);
            track_id_playing = song.getUri_spotify();
            mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + track_id_playing);
            mSpotifyAppRemote.getPlayerApi().queue("spotify:track:" + songs.remove(0).getUri_spotify());
        }

        // Subscribe to PlayerState
            mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity: Repr", track.name + " by " + track.artist.name);
                        if (inicio == true) {
                            track_id_ant_ejec = track.uri;
                            inicio = false;
                            Log.d("MainActivity: Id ant ejec", track.name + " by " + track.artist.name);
                        }
                        TextView textViewSong = findViewById(R.id.labelTrackName);
                        TextView textViewArtist = findViewById(R.id.labelArtistName);
                        textViewSong.setText(track.name);
                        textViewArtist.setText(track.artist.name);
                        if ( ("spotify:track:"+track_id_playing ).equals(track.uri) == false
                            && track_id_ant_ejec == ""){
                            String [] fields = track.uri.split(":");
                            DbSongs dbSongs = new DbSongs(this);
                            Song s = dbSongs.getSongBySpotifyUri(fields[2]);
                            songs_played.add(0, s);
                            track_id_playing = s.getUri_spotify();
                            Log.d("MainActivity", "Ha cambiado la cancion");
                            mSpotifyAppRemote.getPlayerApi().queue("spotify:track:" + songs.remove(0).getUri_spotify());
                        }
                        if (track_id_ant_ejec != ""){
                            track_id_ant_ejec = "";
                        }


                        mSpotifyAppRemote.getImagesApi().getImage(track.imageUri).setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                            @Override public void onResult(Bitmap bitmap) {
                                ImageView imageView = findViewById(R.id.imageView);
                                imageView.setImageBitmap(bitmap); } });
                    }
                });
    }

    public void button_reproducir(View view) {
        Button button_play = findViewById(R.id.button_reproducir);

        if ( this.reproduciendo == false) {
            mSpotifyAppRemote.getPlayerApi().resume();
            this.reproduciendo = true;
            button_play.setBackgroundResource(R.drawable.pausa);
        }
        else {
            mSpotifyAppRemote.getPlayerApi().pause();
            this.reproduciendo = false;
            button_play.setBackgroundResource(R.drawable.reproducir);
        }
    }

    public void button_sig(View view){
        if ( songs.size() != 0) {
            DbSongs dbSongs = new DbSongs(this);
            Song song_ant = dbSongs.getSongBySpotifyUri(track_id_playing);
            songs_played.add(0, song_ant);
            Song song_playing = songs.remove(0);
            track_id_playing = song_playing.getUri_spotify();
            mSpotifyAppRemote.getPlayerApi().play("spotify:track:"+track_id_playing);
        }
        else {
            mSpotifyAppRemote.getPlayerApi().skipNext();
        }
        Button button_play = findViewById(R.id.button_reproducir);
        if ( this.reproduciendo == false) {
            this.reproduciendo = true;
            button_play.setBackgroundResource(R.drawable.pausa);
        }
    }

    public void button_ant(View view) {
        if (songs_played.size() != 0){
            DbSongs dbSongs = new DbSongs(this);
            Song song_ant = dbSongs.getSongBySpotifyUri(track_id_playing);
            songs.add(0, song_ant);
            Song song_playing = songs_played.remove(0);
            track_id_playing = song_playing.getUri_spotify();
            mSpotifyAppRemote.getPlayerApi().play("spotify:track:"+song_playing.getUri_spotify());
        }
        else  {
            mSpotifyAppRemote.getPlayerApi().skipPrevious();
        }


        Button button_play = findViewById(R.id.button_reproducir);
        if ( this.reproduciendo == false) {
            this.reproduciendo = true;
            button_play.setBackgroundResource(R.drawable.pausa);
        }
    }


    public void bpm_aleat(View view){
        TextView textViewBpm = findViewById(R.id.textViewBpm);
        Random rand = new Random();
        float numeroAleatorio = rand.nextFloat() * 140.0f + 65.0f;

        update_songs(numeroAleatorio);
        textViewBpm.setText("Bpms actuales: "+numeroAleatorio);


    }

    public void update_songs(Float bpm){
        this.bpm_actual = bpm;
        songs.clear();
        DbSongs dbSongs = new DbSongs(this);
        songs = dbSongs.getSongsByBpm(bpm);
        Collections.shuffle(songs);
        // int id_json = chooseJson(bpm);
        // readJSONFromResource(this, id_json);
    }

    private void readHeartRateData() {
        Log.d("Main", "voy a leer datos (heart rate)");
        // Obtén la fecha específica
        // Obtener la fecha y hora actual
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Obtener el minuto actual
        int currentMinute = currentDateTime.getMinute();
        int currentHour = currentDateTime.getHour();

        // Imprimir el minuto actual
        System.out.println("Hora actual: " + currentHour+":"+ currentMinute + ", "+
                (currentDateTime.getYear()+"/"+ currentDateTime.getMonthValue()+"/"+ currentDateTime.getDayOfMonth()));

        LocalDate localDate = LocalDate.of(currentDateTime.getYear(), currentDateTime.getMonthValue(), currentDateTime.getDayOfMonth());
        // Crea un objeto ZonedDateTime utilizando la fecha específica y la zona horaria deseada
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, LocalTime.of(currentHour, currentMinute), ZoneId.systemDefault());

        // Obtiene el tiempo en forma de long
        long startTimeLong = zonedDateTime.toInstant().toEpochMilli() - 300000;
        long endTimeLong = zonedDateTime.toInstant().toEpochMilli();
        Log.d("Main", "Start Time: " + startTimeLong +" , End Time: "+ endTimeLong);

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(startTimeLong, endTimeLong, TimeUnit.MILLISECONDS)
                .bucketByActivityType(1, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                .readData(readRequest)
                .addOnSuccessListener (response -> {
                    // The aggregate query puts datasets into buckets, so convert to a
                    // single list of datasets
                    Log.d("HeartRate", "HeartRate: "+ response);
                    for (Bucket bucket : response.getBuckets()) {
                        for (DataSet dataSet : bucket.getDataSets()) {
                            dumpDataSet(dataSet);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.d("TAG", "There was an error reading data from Google Fit", e));

    }

    private void dumpDataSet(DataSet dataSet) {
        if (dataSet.isEmpty()) {
            Log.d("MainActivity", "El conjunto de datos está vacío");
            return;
        }

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i("TAG", "Data returned Times "+dp.getStartTime(TimeUnit.NANOSECONDS)
                    +" -> " +dp.getEndTime(TimeUnit.NANOSECONDS));
            Log.i("TAG","Data point:");
            Log.i("TAG","\tType: ${dp.dataType.name}"+ dp.getDataType().getName());
            Log.i("TAG","\tStart: ${dp.getStartTimeString()}");
            Log.i("TAG","\tEnd: ${dp.getEndTimeString()}");
            boolean flag_average = false;
            for (Field field : dp.getDataType().getFields()) {
                Log.i("TAG","\tField: "+ field.getName() +", Value: "+ dp.getValue(field));
                TextView textViewBpm = findViewById(R.id.textViewBpm);
                if (field.getName().equals("average") && !Float.isNaN(dp.getValue(field).asFloat())){
                    textViewBpm.setText("Bpms actuales " + dp.getValue(field) );
                    bpm_actual = dp.getValue(field).asFloat();
                    flag_average = true;
                    update_songs(bpm_actual);
                }
                if (flag_average == false && field.getName().equals("max")){
                    textViewBpm.setText("Bpms actuales " + dp.getValue(field) );
                    bpm_actual = dp.getValue(field).asFloat();
                    update_songs(bpm_actual);
                }
            }
        }
    }

    public void filterSongs(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String [] choiceItems = {"Pop", "Rock", "Hip Hop", "Reggaeton", "EDM", "Rap", "Trap"};
        ArrayList<String> antSelectedItems = new ArrayList<>();
        for (String s: selectedItems) {
            antSelectedItems.add(s);
        }

        final boolean[] antcheckedItems = Arrays.copyOf(checkedItems, checkedItems.length);

        builder.setTitle("Filter Songs")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(choiceItems, checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {

                                checkedItems[which] = isChecked;
                                if (!selectedItems.contains(choiceItems[which])) {
                                    // Else, if the item is already in the array, remove it
                                    selectedItems.add(choiceItems[which]);
                                } else {
                                    selectedItems.remove(choiceItems[which]);
                                }
                                for (int i = 0; i < antcheckedItems.length; i++) {
                                    System.out.println("AntChecked " + i + ": " + antcheckedItems[i]);
                                }
                            }
                        })
                .setPositiveButton("Guardar Cambios", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the selectedItems results somewhere
                        // or return them to the component that opened the dialog
                        if (selectedItems.size() == 0){
                            songs.clear();
                            DbSongs dbSongs = new DbSongs(MainActivity.this);
                            songs = dbSongs.getSongs();
                            Collections.shuffle(songs);
                        } else {
                            DbSongs dbSongs = new DbSongs(MainActivity.this);
                            songs.clear();
                            for (String genre:selectedItems) {
                                ArrayList<Song> tempSongs = dbSongs.getSongsByGenre(genre.toLowerCase());
                                songs.addAll(tempSongs);
                            }
                            Collections.shuffle(songs);

                        }

                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        selectedItems.clear();
                        for (String s: antSelectedItems) {
                            selectedItems.add(s);
                        }

                        for (int i = 0; i < antcheckedItems.length; i++) {
                            checkedItems[i] = antcheckedItems[i];
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}


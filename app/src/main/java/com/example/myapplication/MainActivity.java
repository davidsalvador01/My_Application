
package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import com.example.myapplication.db.DbBpmMeasurement;
import com.example.myapplication.db.DbRatings;
import com.example.myapplication.db.DbSessions;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private boolean like = false;
    private String id_user;
    private int id_session;
    private String location;
    private int dailyTotalSteps;
    private float totalCalories;
    private String source = "";
    private String bpmMode = "";


    ArrayList<String> selectedItems = new ArrayList();

    boolean [] checkedItems = { false, false, false, false, false, false, false };


    String track_id_playing = "";
    String track_id_ant_ejec = "";

    ArrayList<Song> songs = new ArrayList<>();
    ArrayList<Song> songs_played = new ArrayList<>();


    private FitnessOptions fitnessOptions;

    private boolean selectedPopularity = false;
    private boolean selectedRating = false;
    private boolean selectedContextRecommend = false;
    private ArrayList<String> selectedGenres = new ArrayList<>();
    private ArrayList<String> selectedDecades = new ArrayList<>();





    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        Intent intent = getIntent();
        source = intent.getStringExtra("source");
        Log.d("SOURCE", ""+ source);
        if (source.equals("LoginActivity")){
            Log.d("SOURCE LoginActivity", ""+ source);
            id_user = intent.getStringExtra("id_user");
            location = intent.getStringExtra("location");

            LocalDateTime currentDateTime = LocalDateTime.now();
            Log.d("TIME", ""+ currentDateTime);
            String[] parts = currentDateTime.toString().split("T");

            String date = parts[0];
            String time = parts[1];

            DbSessions dbSessions = new DbSessions(this);
            id_session = dbSessions.setSession(date, id_user, location, time, "");

            DbSongs dbSongs = new DbSongs(this);
            songs = dbSongs.getSongs();
            Collections.shuffle(songs);
            dbSongs.close();
        }

        if(source.equals("RecommenderActivity")){
            id_user = intent.getStringExtra("id_user");
            location = intent.getStringExtra("location");
            bpmMode = intent.getStringExtra("bpmMode");
            if (bpmMode.contains("Desactivar")){
                bpm_actual=-1;
                bpmMode = "desactivar_bpms";
            }
            if (bpmMode.contains("Invertir")){
                bpmMode = "invertir_bpms";
            }
            selectedPopularity = intent.getBooleanExtra("selectedPopularity", false);
            selectedRating = intent.getBooleanExtra("selectedRating", false);
            selectedContextRecommend = intent.getBooleanExtra("selectedContextRecommend", false);
            selectedGenres = intent.getStringArrayListExtra("selectedGenres");
            selectedDecades = intent.getStringArrayListExtra("selectedDecades");
            Log.d("SOURCE RecommenderActivity bpmMode", source+", "+ bpmMode);
            Log.d("SOURCE RecommenderActivity", ""+ selectedDecades + ", " + selectedGenres +
            ", " + selectedPopularity + ", " + selectedRating);

            filterAndSortSongs();

        }

        startAuthenticationFlow();

        Timer timer = new Timer();
        Handler handler = new Handler(Looper.getMainLooper());

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(!bpmMode.equals("desactivar_bpms")){
                            readHeartRateData();
                            readStepsCount();
                            readCalories();
                            Log.d("MainActivity", "Función ejecutada, ha pasado 60 segundos");
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, 60000);
    }

    private float invertBpms(float bpm){
        float bpm_inv = 0;
        if (bpm <= 95) {
            if(bpm < 75) {
                bpm_inv = bpm + 120;
            } else {
                bpm_inv = bpm + 100;
            }
        } else if(bpm > 95 && bpm <= 125){
            bpm_inv = bpm + 31;
        } else if(bpm > 125 && bpm <= 155){
            bpm_inv = bpm - 31;
        } else if(bpm > 155){
            if(bpm <= 185) {
                bpm_inv = bpm - 90;
            } else if(bpm > 185 && bpm < 195) {
                bpm_inv = bpm - 100;
            } else {
                bpm_inv = bpm - 110;
            }
        }
        return bpm_inv;
    }

    private void getContextRecommendSongs(){
        float bpm = 0;
        if (bpmMode.equals("invertir_bpms")){
            bpm = invertBpms(bpm_actual);
        } else if(bpmMode.equals("desactivar_bpms")){
            bpm_actual = -1;
        } else {
            bpm = bpm_actual;
        }
        DbSongs dbSongs = new DbSongs(this);
        songs.clear();
        songs = dbSongs.favoriteSongs();
        ArrayList<String> arrayListArtists = dbSongs.mostStreamedArtists(location);
        for (String artist: arrayListArtists) {
            ArrayList<Song> temp;
            if(bpm_actual == -1) {
                temp = dbSongs.getSongsByArtist(artist);
            } else {
                temp = dbSongs.getSongsByArtistAndBpm(artist, bpm);
            }
            songs.addAll(temp);
        }
        ArrayList<String> arrayListGenres = dbSongs.mostStreamedGenres(location);
        for (String genre: arrayListGenres) {
            ArrayList<Song> temp;
            if(bpm_actual == -1) {
                temp = dbSongs.getSongsByGenre(genre);
            } else {
                temp = dbSongs.getSongsByGenreAndBpm(genre, bpm);
            }
            songs.addAll(temp);
        }
        ArrayList<Integer> arrayListYears = dbSongs.mostStreamedYears(location);
        for (int year: arrayListYears) {
            ArrayList<Song> temp;
            if(bpm_actual == -1) {
                temp = dbSongs.getSongsByYear(year);
            } else {
                temp = dbSongs.getSongsByYearAndBpm(year, bpm);
            }
            songs.addAll(temp);
        }
        Collections.shuffle(songs);
        dbSongs.close();
    }

    private void filterAndSortSongs(){
        float bpm = 0;
        if (bpmMode.equals("invertir_bpms")){
            bpm = invertBpms(bpm_actual);
        } else if(bpmMode.equals("desactivar_bpms")){
            bpm_actual = -1;
        } else {
            bpm = bpm_actual;
        }
        String selectCondition = createSelectGenreDecade(selectedGenres, selectedDecades);
        DbSongs dbSongs = new DbSongs(this);
        if(selectedPopularity == true){
            if(bpm_actual == -1){
                songs = dbSongs.getSongsByConditionOrderByPopularity(selectCondition);
            }
            else {
                songs = dbSongs.getSongsByConditionAndBpmOrderByPopularity(selectCondition, bpm);
            }
        }
        else if(selectedRating == true){
            if(bpm_actual == -1){
                songs = dbSongs.getSongsByConditionOrderByRating(selectCondition);            }
            else {
                songs = dbSongs.getSongsByConditionAndBpmOrderByRating(selectCondition, bpm);
            }
        } else if(selectedContextRecommend == true) {
            getContextRecommendSongs();
        } else {
            if(bpm_actual == -1){
                songs = dbSongs.getSongsByCondition(selectCondition);
            }
            else {
                songs = dbSongs.getSongsByConditionAndBpm(selectCondition, bpm);
            }

            Collections.shuffle(songs);
        }
        if(songs.size() == 0) {
            if(bpm_actual == -1){
                songs = dbSongs.getSongs();
            }
            else {
                songs = dbSongs.getSongsByBpm(bpm_actual);
            }

            Collections.shuffle(songs);
        }
        dbSongs.close();
    }

    private String createSelectGenreDecade(ArrayList<String> selectedGenres,
    ArrayList<String> selectedDecades){
        String s="";
        if(selectedGenres.size()>0){
            for (int index = 0; index < selectedGenres.size(); index++) {
                String genre = selectedGenres.get(index).toLowerCase();
                if (index == 0) {
                    s = s + "g.name='" + genre.toLowerCase() + "'";
                } else {
                    s = s+ " OR g.name='"+ genre.toLowerCase() + "'";
                }
            }
        }
        if(selectedDecades.size()>0){
            for (int index = 0; index < selectedDecades.size(); index++) {
                int decade = formatDecade(selectedDecades.get(index));
                int decade_end = decade + 10;
                if (index == 0 && s.equals("")) {
                    s = s + "(s.release_year >="+ decade +" AND s.release_year <="+decade_end+")";
                } else {
                    s = s+ " OR (s.release_year >="+ decade +" AND s.release_year <="+decade_end+")";
                }
            }
        }
        return s;
    }

    private int formatDecade(String decade){
        int decada;
        switch (decade) {
            case "1950s":
                decada = 1950;
                break;
            case "1960s":
                decada = 1960;
                break;
            case "1970s":
                decada = 1970;
                break;
            case "1980s":
                decada = 1980;
                break;
            case "1990s":
                decada = 1990;
                break;
            case "2000s":
                decada = 2000;
                break;
            case "2010s":
                decada = 2010;
                break;
            case "2020s":
                decada = 2020;
                break;
            default:
                decada = 2023;
                break;
        }
        return decada;
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
                    // Iniciar una pantalla de error
                    Intent intent2 = new Intent(this, ErrorActivity.class);
                    Log.d("Error enviado: ", ""+ response.getError());
                    intent2.putExtra("error", response.getError());
                    startActivity(intent2);
                    finish();
                    break;
                default:
                    Log.d("MainActivity", "Auth result: " + response.getType());
            }
        }
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
                        //if(throwable.getClass().toString() == "SpotifyConnectionTerminatedException"){
                          //  SpotifyAppRemote.connect(this, connectionParams, this);
                        //}
                        Log.e("MainActivity, error ", throwable.getClass().toString(), throwable);
                        Intent intent2 = new Intent(MainActivity.this, ErrorActivity.class);
                        Log.d("Error enviado: ", ""+ throwable.getMessage());
                        intent2.putExtra("error", throwable.toString());
                        startActivity(intent2);
                        finish();
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    public void button_exit(View view){
        onDestroy();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        System.exit(0);
        // this.finishAffinity(); -> finaliza toda la app
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSpotifyAppRemote.getPlayerApi().pause();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);

        LocalDateTime currentDateTime = LocalDateTime.now();
        String[] parts = currentDateTime.toString().split("T");

        String time = parts[1];
        DbSessions dbSessions = new DbSessions(this);
        dbSessions.updateEndSession(id_session, time);
        setResult(Activity.RESULT_CANCELED);

        finish();
    }


    private void connected() {
        readHeartRateData();

        if ( songs.size() != 0) {
            Song song = songs.remove(0);
            track_id_playing = song.getUri_spotify();
            mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + track_id_playing);
            if(!source.equals("RecommenderActivity")){
                mSpotifyAppRemote.getPlayerApi().queue("spotify:track:" + songs.remove(0).getUri_spotify());
            }
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
                            Song song_ant = dbSongs.getSongBySpotifyUri(track_id_playing);

                            if (s != null && song_ant != null) {
                                DbSessions dbSessions = new DbSessions(this);
                                dbSessions.setSongSession(song_ant.getId(), id_session);
                                dbSessions.close();
                                songs_played.add(0, song_ant);
                                track_id_playing = s.getUri_spotify();
                                Log.d("MainActivity", "Ha cambiado la cancion");
                                mSpotifyAppRemote.getPlayerApi().queue("spotify:track:" + songs.remove(0).getUri_spotify());
                            }
                            dbSongs.close();

                        }
                        setLikeButton();
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
            setLikeButton();
            dbSongs.close();
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
            setLikeButton();
            dbSongs.close();
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


    public void update_songs(Float bpm){
        this.bpm_actual = bpm;

        songs.clear();

        DbSongs dbSongs = new DbSongs(this);
        filterAndSortSongs();
        dbSongs.close();

        Collections.shuffle(songs);
        Log.d("Main", "voy a actualizar bpms (heart rate)");
        setBpmMeasurement();
    }

    private void readHeartRateData() {
        Log.d("Main", "voy a leer datos (heart rate)");
        // Obtén la fecha específica
        // Obtener la fecha y hora actual
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Obtener el minuto actual
        int currentMinute = currentDateTime.getMinute();
        int currentHour = currentDateTime.getHour();


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
                if (!bpmMode.equals("desactivar_bpms") && field.getName().equals("average")
                        && !Float.isNaN(dp.getValue(field).asFloat())){
                    textViewBpm.setText("Bpms actuales " + dp.getValue(field) );
                    bpm_actual = dp.getValue(field).asFloat();
                    flag_average = true;
                    update_songs(bpm_actual);
                }
                if (!bpmMode.equals("desactivar_bpms") && flag_average == false
                        && field.getName().equals("max")){
                    textViewBpm.setText("Bpms actuales " + dp.getValue(field) );
                    bpm_actual = dp.getValue(field).asFloat();
                    update_songs(bpm_actual);
                }
            }
        }
    }

    public void setBpmMeasurement(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        Log.d("TIME", ""+ currentDateTime);
        String[] parts = currentDateTime.toString().split("T");
        String time = parts[1];

        DbBpmMeasurement dbBpmMeasurement = new DbBpmMeasurement(this);
        dbBpmMeasurement.setBpm(time, id_session, bpm_actual);
        dbBpmMeasurement.close();
    }

    public void filterSongs(View view){
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        Intent intent = new Intent(this, RecommenderActivity.class);
        intent.putExtra("id_user", id_user);
        intent.putExtra("location", location);
        intent.putExtra("bpmMode", bpmMode);
        intent.putExtra("selectedGenres", selectedGenres);
        intent.putExtra("selectedDecades", selectedDecades);
        intent.putExtra("selectedContextRecommend", selectedContextRecommend);
        intent.putExtra("selectedRating", selectedRating);
        intent.putExtra("selectedPopularity", selectedPopularity);

        startActivity(intent);
        finish();
    }

    public void likeDislike(View view){
        Button button_play = findViewById(R.id.button_like);

        if ( this.like == false) {
            this.like = true;
            button_play.setBackgroundResource(R.drawable.heart_color);
            DbRatings dbRatings = new DbRatings(this);
            DbSongs dbSongs = new DbSongs(this);
            Song s = dbSongs.getSongBySpotifyUri(track_id_playing);
            dbRatings.setRating(like, s.getId(), id_user);
            dbSongs.close();
            dbRatings.close();
        }
        else {
            this.like = false;
            button_play.setBackgroundResource(R.drawable.heart);
            DbRatings dbRatings = new DbRatings(this);
            DbSongs dbSongs = new DbSongs(this);
            Song s = dbSongs.getSongBySpotifyUri(track_id_playing);
            dbRatings.setRating(like, s.getId(), id_user);
            dbSongs.close();
            dbRatings.close();
        }
    }

    public void setLikeButton(){
        Button button_play = findViewById(R.id.button_like);
        DbRatings dbRatings = new DbRatings(this);
        DbSongs dbSongs = new DbSongs(this);
        Song s = dbSongs.getSongBySpotifyUri(track_id_playing);
        int rating = dbRatings.getRating(s.getId(), id_user);
        if (rating == 1){
            this.like = true;
            button_play.setBackgroundResource(R.drawable.heart_color);
        }
        else {
            this.like = false;
            button_play.setBackgroundResource(R.drawable.heart);
        }
        dbSongs.close();
        dbRatings.close();
    }

    public void detailTrainingScreen(View view){
        readCalories();
        readStepsCount();
        Intent intent = new Intent(this, DetailTrainingActivity.class);
        intent.putExtra("totalCalories", totalCalories);
        intent.putExtra("dailyTotalSteps", dailyTotalSteps);
        intent.putExtra("bpm_actual", bpm_actual);
        startActivity(intent);
    }

    private void readStepsCount() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                                if (dataSet.isEmpty()) {
                                    dailyTotalSteps = 0;
                                } else {
                                    dailyTotalSteps = (int) total;
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d( "There was a problem getting the step count.", ""+ e);
                            }
                        });
    }

    private void readCalories() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        totalCalories = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("There was a problem getting the calorie count.", e.toString());
                    }
                });
    }

}


package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;


import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Arrays;

public class RecommenderActivity extends AppCompatActivity {
    private String id_user;
    private int id_session;
    private String location;
    private ArrayList<String> selectedGenres = new ArrayList();

    private boolean [] checkedGenres = { false, false, false, false, false, false, false };
    String [] choiceItemsGenres = {"Pop", "Rock", "Hip Hop", "Reggaeton", "EDM", "Rap", "Trap"};

    private ArrayList<String> selectedDecades = new ArrayList();

    private boolean [] checkedDecades = { false, false, false, false, false, false, false, false};
    String [] choiceItemsDecades = {"1950s", "1960s", "1970s", "1980s",
            "1990s", "2000s", "2010s", "2020s"};
    private boolean selectedRating = false;
    private boolean selectedPopularity = false;
    private boolean selectedContextRecommend = false;
    private String bpmMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommender);

        Intent intent = getIntent();
        id_user = intent.getStringExtra("id_user");
        id_session = intent.getIntExtra("id_session", -1);
        location = intent.getStringExtra("location");
        selectedRating = intent.getBooleanExtra("selectedRating", false);
        selectedPopularity = intent.getBooleanExtra("selectedPopularity", false);
        selectedContextRecommend = intent.getBooleanExtra("selectedContextRecommend", false);
        selectedGenres = intent.getStringArrayListExtra("selectedGenres");
        selectedDecades = intent.getStringArrayListExtra("selectedDecades");
        Log.d("Info recibida", "decades: " + selectedDecades +
                ", genres: " + selectedGenres + ", rating: " + selectedRating +
                ", popularity: " + selectedPopularity + ", context: " + selectedContextRecommend);
        setInitialSwitches();
        buildSpinner();
    }

    private void setInitialSwitches(){
        if (selectedRating){
            Switch switch_rating = findViewById(R.id.switch_rating);
            switch_rating.setChecked(true);
        }
        if (selectedPopularity){
            Switch switch_popularity = findViewById(R.id.switch_popularity);
            switch_popularity.setChecked(true);
        }
        if (selectedContextRecommend){
            Switch switch_context = findViewById(R.id.switch_context_recommend);
            switch_context.setChecked(true);
        }
        if (selectedDecades.size()>0){
            Switch switch_popularity = findViewById(R.id.switch_decade);
            switch_popularity.setChecked(true);
            TextView textViewDecades = findViewById(R.id.textViewDecadesSelected);
            textViewDecades.setText("The selected decades are: " + selectedDecades);
            for (String decade:selectedDecades) {
                int indice = -1;
                for (int i=0; i < choiceItemsDecades.length; i++) {
                    if(decade.equals(choiceItemsDecades[i])){
                        indice=i;
                    }
                }
                if(indice != -1){
                    checkedDecades[indice] = true;
                }
            }
        }
        if (selectedGenres.size()>0){
            Switch switch_genre = findViewById(R.id.switch_genre);
            switch_genre.setChecked(true);
            TextView textViewGenres = findViewById(R.id.textViewGenresSelected);
            textViewGenres.setText("The selected genres are: " + selectedGenres);
            for (String genre:selectedGenres) {
                int indice = -1;
                for (int i=0; i < choiceItemsGenres.length; i++) {

                    if(genre.equals(choiceItemsGenres[i])){
                        Log.d("Genre", ""+ choiceItemsGenres[i] + ", "+genre);
                        indice=i;
                    }
                }
                if(indice != -1){
                    checkedGenres[indice] = true;
                }
            }
        }

    }

    public void switch_genre(View view){
        Switch switch_genre = findViewById(R.id.switch_genre);
        TextView textViewGenre = findViewById(R.id.textViewGenresSelected);
        Switch switch_context_recommend = findViewById(R.id.switch_context_recommend);
        if (switch_genre.isChecked()) {
            textViewGenre.setText("Activated");
            dialogGenres();
        } else {
            textViewGenre.setText("Deactivated");

        }
        if (switch_context_recommend.isChecked()){
            switch_context_recommend.setChecked(false);
            selectedContextRecommend = false;
        }

    }

    private void dialogGenres(){
        for (String s: selectedGenres) {
            Log.d("Click", ""+s);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ArrayList<String> antSelectedItems = new ArrayList<>();
        for (String s: selectedGenres) {
            antSelectedItems.add(s);
            Log.d("Anterior Genres", ""+s);
        }


        final boolean[] antcheckedItems = Arrays.copyOf(checkedGenres, checkedGenres.length);

        builder.setTitle("Filter Songs")
                .setMultiChoiceItems(choiceItemsGenres, checkedGenres,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {

                                checkedGenres[which] = isChecked;
                                Log.d("Click", ""+choiceItemsGenres[which]+ ","+isChecked);
                                if (!selectedGenres.contains(choiceItemsGenres[which])) {
                                    selectedGenres.add(choiceItemsGenres[which]);
                                }
                                else {
                                    selectedGenres.remove(choiceItemsGenres[which]);
                                }
                                for (String s: selectedGenres) {
                                    Log.d("Click", ""+s);
                                }

                            }
                        })
                .setPositiveButton("Save Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        TextView textViewGenre = findViewById(R.id.textViewGenresSelected);
                        textViewGenre.setText("The selected genres are: " + selectedGenres);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        selectedGenres.clear();
                        for (String s: antSelectedItems) {
                            selectedGenres.add(s);
                        }

                        for (int i = 0; i < antcheckedItems.length; i++) {
                            checkedGenres[i] = antcheckedItems[i];
                        }
                        TextView textViewGenre = findViewById(R.id.textViewGenresSelected);
                        textViewGenre.setText("The selected genres are: " + selectedGenres);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void switch_decade(View view){
        Switch switch_genre = findViewById(R.id.switch_decade);
        TextView textViewDecade = findViewById(R.id.textViewDecadesSelected);
        Switch switch_context_recommend = findViewById(R.id.switch_context_recommend);
        if (switch_genre.isChecked()) {
            textViewDecade.setText("Activated");
            dialogDecades();
        } else {
            textViewDecade.setText("Deactivated");
        }
        if (switch_context_recommend.isChecked()){
            switch_context_recommend.setChecked(false);
            selectedContextRecommend = false;
        }

    }

    private void dialogDecades() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        ArrayList<String> antSelectedDecades = new ArrayList<>();
        for (String s: selectedDecades) {
            antSelectedDecades.add(s);
        }

        final boolean[] antcheckedItems = Arrays.copyOf(checkedDecades, checkedDecades.length);

        builder.setTitle("Filter Songs")
                .setMultiChoiceItems(choiceItemsDecades, checkedDecades,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {

                                checkedDecades[which] = isChecked;
                                if (!selectedDecades.contains(choiceItemsDecades[which])) {
                                    selectedDecades.add(choiceItemsDecades[which]);
                                } else {
                                    selectedDecades.remove(choiceItemsDecades[which]);
                                }

                            }
                        })
                .setPositiveButton("Save Changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        TextView textViewDecade = findViewById(R.id.textViewDecadesSelected);
                        textViewDecade.setText("The selected decades are: " + selectedDecades);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        selectedDecades.clear();
                        for (String s: antSelectedDecades) {
                            selectedDecades.add(s);
                        }

                        for (int i = 0; i < antcheckedItems.length; i++) {
                            checkedDecades[i] = antcheckedItems[i];
                        }
                        TextView textViewGenre = findViewById(R.id.textViewGenresSelected);
                        textViewGenre.setText("The selected decades are: " + selectedDecades);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void switch_rating(View view){
        Switch switch_rating = findViewById(R.id.switch_rating);
        Switch switch_popularity = findViewById(R.id.switch_popularity);
        Switch switch_context_recommend = findViewById(R.id.switch_context_recommend);

        if (switch_rating.isChecked()) {
            selectedRating = true;
        } else {
            selectedRating = false;
        }

        if (switch_popularity.isChecked()){
            switch_popularity.setChecked(false);
            selectedPopularity = false;
        }
        if (switch_context_recommend.isChecked()){
            switch_context_recommend.setChecked(false);
            selectedContextRecommend = false;
        }

    }

    public void switch_context_recommendation(View view){
        Switch switch_context_recommend = findViewById(R.id.switch_context_recommend);
        Switch switch_rating = findViewById(R.id.switch_rating);
        Switch switch_popularity = findViewById(R.id.switch_popularity);
        Switch switch_genre = findViewById(R.id.switch_genre);
        Switch switch_decade = findViewById(R.id.switch_decade);
        if (switch_context_recommend.isChecked()) {
            selectedContextRecommend = true;
            selectedGenres.clear();
            selectedDecades.clear();
            for(int i=0; i<checkedGenres.length; i++){
                checkedGenres[i] = false;
                checkedDecades[i] = false;
            }
        } else {
            selectedContextRecommend = false;
        }

        if (switch_rating.isChecked()){
            switch_rating.setChecked(false);
            selectedRating = false;
        }
        if (switch_popularity.isChecked()){
            switch_popularity.setChecked(false);
            selectedPopularity = false;
        }
        if (switch_genre.isChecked()){
            switch_genre.setChecked(false);
            TextView textViewGenre = findViewById(R.id.textViewGenresSelected);
            textViewGenre.setText("The selected genres are: " + selectedGenres);

        }
        if (switch_decade.isChecked()){
            switch_decade.setChecked(false);
            TextView textViewDecade = findViewById(R.id.textViewDecadesSelected);
            textViewDecade.setText("The selected decades are: " + selectedDecades);

        }
    }

    public void switch_popularity(View view){
        Switch switch_context_recommend = findViewById(R.id.switch_context_recommend);
        Switch switch_rating = findViewById(R.id.switch_rating);
        Switch switch_popularity = findViewById(R.id.switch_popularity);
        if (switch_popularity.isChecked()) {
            selectedPopularity = true;
        } else {
            selectedPopularity = false;
        }

        if (switch_rating.isChecked()){
            switch_rating.setChecked(false);
            selectedRating = false;
        }
        if (switch_context_recommend.isChecked()){
            switch_context_recommend.setChecked(false);
            selectedContextRecommend = false;
        }
    }

    public void save_changes(View view){
        checkSwitches();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("source", "RecommenderActivity");
        intent.putExtra("selectedDecades", selectedDecades);
        intent.putExtra("selectedGenres", selectedGenres);
        intent.putExtra("selectedPopularity", selectedPopularity);
        intent.putExtra("selectedRating", selectedRating);
        intent.putExtra("selectedContextRecommend", selectedContextRecommend);
        intent.putExtra("location", location);
        intent.putExtra("id_user", id_user);
        intent.putExtra("id_session", id_session);
        intent.putExtra("bpmMode", bpmMode);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        checkSwitches();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("source", "RecommenderActivity");
        intent.putExtra("selectedDecades", selectedDecades);
        intent.putExtra("selectedGenres", selectedGenres);
        intent.putExtra("selectedPopularity", selectedPopularity);
        intent.putExtra("selectedRating", selectedRating);
        intent.putExtra("selectedContextRecommend", selectedContextRecommend);
        intent.putExtra("location", location);
        intent.putExtra("id_user", id_user);
        intent.putExtra("id_session", id_session);
        intent.putExtra("bpmMode", bpmMode);
        startActivity(intent);
    }

    public void button_back(View view){
        onBackPressed();
    }

    private void buildSpinner(){
        Spinner spinner = findViewById(R.id.spinner2);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Adapt Bpms");
        arrayList.add("Reverse Bpms");
        arrayList.add("Disable Tracking Bpms");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                arrayList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bpmMode = (String) spinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                bpmMode = "Adaptar Bpms";
            }
        });
    }

    public void checkSwitches(){
        Switch switch_context_recommend = findViewById(R.id.switch_context_recommend);
        Switch switch_rating = findViewById(R.id.switch_rating);
        Switch switch_popularity = findViewById(R.id.switch_popularity);
        Switch switch_genre = findViewById(R.id.switch_genre);
        Switch switch_decade = findViewById(R.id.switch_decade);
        if (!switch_genre.isChecked()) {
            selectedGenres.clear();
        }
        if (!switch_decade.isChecked()) {
            selectedDecades.clear();
        }
        if (!switch_rating.isChecked()){
            selectedRating = false;
        }
        if (!switch_popularity.isChecked()){
            selectedPopularity = false;
        }
        if (!switch_context_recommend.isChecked()){
            selectedContextRecommend = false;
        }
    }

}
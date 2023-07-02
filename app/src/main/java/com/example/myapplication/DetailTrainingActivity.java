package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.db.DbBpmMeasurement;
import com.example.myapplication.typedefs.BpmMeasurement;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DetailTrainingActivity extends AppCompatActivity {
    private int dailyTotalSteps;
    private float totalCalories;
    private float bpm_actual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_training);

        Intent intent = getIntent();
        dailyTotalSteps = intent.getIntExtra("dailyTotalSteps", -1);
        totalCalories = intent.getFloatExtra("totalCalories", -1);
        bpm_actual = intent.getFloatExtra("bpm_actual", -1);
        Log.d("Detail", " steps: " + dailyTotalSteps + " , totalCalories: " +
                totalCalories);

        LocalDateTime currentDateTime = LocalDateTime.now();
        String[] parts = currentDateTime.toString().split("T");
        String date = parts[0];

        TextView textViewCalories = findViewById(R.id.textViewCalories);
        textViewCalories.setText("Total de calorías consumidas: \n" + totalCalories);
        TextView textViewSteps = findViewById(R.id.textViewSteps);
        textViewSteps.setText("Total de pasos dados: \n" + dailyTotalSteps);
        TextView textViewDate = findViewById(R.id.textViewDate);
        textViewDate.setText("" + date);
        buildChart();

    }

    private void buildChart(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        String[] parts = currentDateTime.toString().split("T");
        String date = parts[0];

        DbBpmMeasurement dbBpmMeasurement = new DbBpmMeasurement(this);
        ArrayList<BpmMeasurement> bpms = dbBpmMeasurement.getBpmsFromDate(date);

        dbBpmMeasurement.close();

        List<Entry> values = new ArrayList<>();
        float promedio = 0;
        int cont = 0;
        float min = Integer.MAX_VALUE;
        float max = Integer.MIN_VALUE;
        for (BpmMeasurement bpm_measurement: bpms) {
            values.add(new Entry(cont, bpm_measurement.getHeart_rate()));
            cont++;
            if (bpm_measurement.getHeart_rate() < min) {
                min = bpm_measurement.getHeart_rate();
            }
            if (bpm_measurement.getHeart_rate() > max) {
                max = bpm_measurement.getHeart_rate();
            }
            promedio += bpm_measurement.getHeart_rate();
        }
        promedio = promedio / bpms.size();
        TextView textViewDate = findViewById(R.id.textViewHeartRate);
        textViewDate.setText("Media en el dia: " + promedio + " bpms");

        LineChart mChart = findViewById(R.id.chart);
        mChart.setTouchEnabled(true);
        mChart.setPinchZoom(true);

        YAxis leftAxis = mChart.getAxisLeft();

// Establecer el rango mínimo y máximo del eje Y
        float minYValue = min - 40; // Valor mínimo deseado
        float maxYValue = max + 40; // Valor máximo deseado
        leftAxis.setAxisMinimum(minYValue);
        leftAxis.setAxisMaximum(maxYValue);

        // Obtener el eje Y de la derecha del gráfico y deshabilitar la visualización de las etiquetas
        YAxis yAxisRight = mChart.getAxisRight();
        yAxisRight.setDrawLabels(false);

// También puedes deshabilitar la visualización de las líneas de división del eje Y de la derecha si lo deseas
        yAxisRight.setDrawGridLines(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawLabels(false);

// También puedes deshabilitar la visualización de las líneas de división del eje X si lo deseas
        xAxis.setDrawGridLines(false);

        // Crear un conjunto de datos de la línea y configurar su apariencia
        LineDataSet dataSet = new LineDataSet(values, "BPM");
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);



        // Crear una lista de conjuntos de datos y agregar el conjunto de datos de la línea
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        // Crear un objeto LineData con la lista de conjuntos de datos
        LineData lineData = new LineData(dataSets);

        // Establecer los datos en el gráfico
        mChart.setData(lineData);

        // Actualizar la visualización del gráfico
        mChart.invalidate();
    }

    public void button_back(View view){
        // Llamar a finish() en el lugar adecuado para regresar a la actividad anterior
        finish();

    }
}
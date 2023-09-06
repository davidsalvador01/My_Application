package com.example.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;

import com.example.myapplication.R;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_training);

        Intent intent = getIntent();
        dailyTotalSteps = intent.getIntExtra("dailyTotalSteps", -1);
        totalCalories = intent.getFloatExtra("totalCalories", -1);

        LocalDateTime currentDateTime = LocalDateTime.now();
        String[] parts = currentDateTime.toString().split("T");
        String date = parts[0];

        TextView textViewCalories = findViewById(R.id.textViewCalories);
        textViewCalories.setText("Total calories: \n" + totalCalories);
        TextView textViewSteps = findViewById(R.id.textViewSteps);
        textViewSteps.setText("Total steps: \n" + dailyTotalSteps);
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
        textViewDate.setText("Average heart rate: " + promedio + " bpms");

        LineChart mChart = findViewById(R.id.chart);
        mChart.setTouchEnabled(true);
        mChart.setPinchZoom(true);

        YAxis leftAxis = mChart.getAxisLeft();

        float minYValue = min - 40;
        float maxYValue = max + 40;
        leftAxis.setAxisMinimum(minYValue);
        leftAxis.setAxisMaximum(maxYValue);

        YAxis yAxisRight = mChart.getAxisRight();
        yAxisRight.setDrawLabels(false);

        yAxisRight.setDrawGridLines(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawLabels(false);

        xAxis.setDrawGridLines(false);

        LineDataSet dataSet = new LineDataSet(values, "BPM");
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawValues(false);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);

        mChart.setData(lineData);

        mChart.invalidate();
    }

    public void button_back(View view){
        finish();
    }
}
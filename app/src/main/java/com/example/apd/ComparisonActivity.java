package com.example.apd;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ComparisonActivity extends AppCompatActivity {

    Spinner spinnerAppliance, spinnerRating1, spinnerRating2;
    LinearLayout ratingCard, comparisonResultBox;
    Button compareBtn;
    TextView comparisonTitle;
    BarChart barChart;

    String BASE_URL = "http://192.168.239.113:8080/arnab_backend_api";
    ArrayList<String> applianceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        spinnerAppliance = findViewById(R.id.spinnerAppliance);
        spinnerRating1 = findViewById(R.id.spinnerRating1);
        spinnerRating2 = findViewById(R.id.spinnerRating2);
        ratingCard = findViewById(R.id.ratingCard);
        comparisonResultBox = findViewById(R.id.comparisonResultBox);
        compareBtn = findViewById(R.id.compareBtn);
        barChart = findViewById(R.id.barChart);
        comparisonTitle = findViewById(R.id.comparisonTitle);
        ImageView backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        fetchAppliances();
        ArrayAdapter<String> applianceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, applianceList);
        applianceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAppliance.setAdapter(applianceAdapter);

        String[] ratings = {"1 Star", "2 Star", "3 Star", "4 Star", "5 Star"};
        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ratings);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating1.setAdapter(ratingAdapter);
        spinnerRating2.setAdapter(ratingAdapter);

        spinnerAppliance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos != 0 && ratingCard.getVisibility() == View.GONE) {
                    Animation slideUp = AnimationUtils.loadAnimation(ComparisonActivity.this, R.anim.slide_up1);
                    ratingCard.setVisibility(View.VISIBLE);
                    ratingCard.startAnimation(slideUp);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        compareBtn.setOnClickListener(v -> {
            String appliance = spinnerAppliance.getSelectedItem().toString();
            String rating1 = spinnerRating1.getSelectedItem().toString();
            String rating2 = spinnerRating2.getSelectedItem().toString();

            if (appliance.equals("Select Appliance")) {
                Toast.makeText(this, "Please select an appliance", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rating1.equals(rating2)) {
                Toast.makeText(this, "Please select two different star ratings", Toast.LENGTH_SHORT).show();
                return;
            }

            if (comparisonResultBox.getVisibility() == View.GONE) {
                Animation slideUp = AnimationUtils.loadAnimation(ComparisonActivity.this, R.anim.slide_up);
                comparisonResultBox.setVisibility(View.VISIBLE);
                comparisonResultBox.startAnimation(slideUp);
            }

            new Thread(() -> {
                try {
                    String url1 = BASE_URL + "/energy?appliance=" + appliance + "&rating=" + rating1.split(" ")[0] + "-star";
                    String url2 = BASE_URL + "/energy?appliance=" + appliance + "&rating=" + rating2.split(" ")[0] + "-star";

                    double energy1 = fetchEnergy(url1);
                    double energy2 = fetchEnergy(url2);

                    runOnUiThread(() -> showComparison(rating1, rating2, energy1, energy2));

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }

    private double fetchEnergy(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = br.readLine();
        return Double.parseDouble(response.trim());
    }

    private void showComparison(String rating1, String rating2, double energy1, double energy2) {
        comparisonTitle.setText("Energy Comparison: " + rating1 + " vs " + rating2);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) energy1));
        entries.add(new BarEntry(1f, (float) energy2));

        BarDataSet dataSet = new BarDataSet(entries, "Energy Units/Day");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        barChart.setData(data);
        barChart.getXAxis().setDrawLabels(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextSize(12f);
        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);
        barChart.animateY(1000);
        barChart.invalidate();

        TextView dailyText = new TextView(this);
        dailyText.setText("\u2705 Daily Energy:\n\u2022 " + rating1 + ": " + energy1 + " units/day\n\u2022 " + rating2 + ": " + energy2 + " units/day");
        dailyText.setTextColor(Color.parseColor("#180166"));
        dailyText.setTextSize(14f);
        dailyText.setTypeface(null, Typeface.NORMAL);
        dailyText.setPadding(0, 20, 0, 10);

        TextView monthlyText = new TextView(this);
        monthlyText.setText("\u2705 Monthly Energy:\n\u2022 " + rating1 + ": " + String.format("%.2f", energy1 * 30) + " units/month\n\u2022 " + rating2 + ": " + String.format("%.2f", energy2 * 30) + " units/month");
        monthlyText.setTextColor(Color.parseColor("#180166"));
        monthlyText.setTextSize(14f);

        double diff = energy1 - energy2;
        double perc = (diff / energy2) * 100;

        TextView diffText = new TextView(this);
        diffText.setText("\uD83D\uDD0D " + rating1 + " consumes " + String.format("%.1f", Math.abs(perc)) + "% " + (perc > 0 ? "more" : "less") + " than " + rating2);
        diffText.setTextColor(Color.parseColor("#180166"));
        diffText.setTextSize(14f);
        diffText.setTypeface(null, Typeface.BOLD);
        diffText.setPadding(0, 10, 0, 0);

        comparisonResultBox.removeAllViews();
        comparisonResultBox.addView(comparisonTitle);
        comparisonResultBox.addView(barChart);
        comparisonResultBox.addView(dailyText);
        comparisonResultBox.addView(monthlyText);
        comparisonResultBox.addView(diffText);
    }

    void fetchAppliances() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/appliances");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) json.append(line);

                JSONArray arr = new JSONArray(json.toString());
                applianceList.clear();
                applianceList.add("Select Appliance");
                for (int i = 0; i < arr.length(); i++) {
                    applianceList.add(arr.getString(i));
                }

                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, applianceList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerAppliance.setAdapter(adapter);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ComparisonActivity.this, "Failed to fetch appliances", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}

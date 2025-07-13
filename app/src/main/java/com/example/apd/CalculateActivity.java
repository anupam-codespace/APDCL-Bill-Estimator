package com.example.apd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class CalculateActivity extends AppCompatActivity {

    Spinner spinnerAppliance, spinnerRating, spinnerMonth;
    EditText inputQuantity;
    Button btnDone, btnAdd, btnAppliancesSelected;
    ImageView backArrow;

    String BASE_URL = "http://192.168.239.113:8080/arnab_backend_api";
    HashMap<String, HashMap<String, String>> mp = new HashMap<>();
    ArrayList<String> applianceList = new ArrayList<>();
    ArrayList<String> ratingsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);
        Intent intent1 = getIntent();
        String category=intent1.getStringExtra("category");

        // Initialize views
        spinnerAppliance = findViewById(R.id.spinnerAppliance);
        spinnerRating = findViewById(R.id.spinnerRating);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        inputQuantity = findViewById(R.id.inputQuantity);
        btnDone = findViewById(R.id.btnDone);
        btnAdd = findViewById(R.id.btnAdd);
        backArrow = findViewById(R.id.backButton);
        btnAppliancesSelected = findViewById(R.id.btnAppliancesSelected);

        // Hide summary button initially
        btnAppliancesSelected.setVisibility(View.GONE);

        // Back navigation
        backArrow.setOnClickListener(view -> {
            view.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                Intent intent = new Intent(CalculateActivity.this, MainActivity2.class);
                startActivity(intent);
                finish();
            }).start();
        });

        // Set ratings and months
        String[] ratings = {"1", "2", "3", "4", "5"};
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        ratingsList.addAll(java.util.Arrays.asList(ratings));
        spinnerRating.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ratingsList));
        spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));

        // DONE button
        btnDone.setOnClickListener(v -> {
            String appliance = spinnerAppliance.getSelectedItem().toString();
            String rating = spinnerRating.getSelectedItem().toString();
            String quantity = inputQuantity.getText().toString();

            if (quantity.isEmpty()) {
                Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CalculateActivity.this, previewPage.class);
                intent.putExtra("map", mp);
                intent.putExtra("category", category);
                startActivityForResult(intent, 100);
                return;
            }

            if (!mp.containsKey(appliance)) {
                mp.put(appliance, new HashMap<>());
            }
            mp.get(appliance).put(rating, quantity);

            Toast.makeText(this, appliance + " (" + rating + ") x" + quantity + " Done ✅"+category, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CalculateActivity.this, previewPage.class);
            intent.putExtra("map", mp);
            intent.putExtra("category", category);
            startActivityForResult(intent, 100);
        });

        // ADD button
        btnAdd.setOnClickListener(v -> {
            String appliance = spinnerAppliance.getSelectedItem().toString();
            String rating = spinnerRating.getSelectedItem().toString();
            String quantity = inputQuantity.getText().toString();

            if (quantity.isEmpty()) {
                Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!mp.containsKey(appliance)) {
                mp.put(appliance, new HashMap<>());
            }
            mp.get(appliance).put(rating, quantity);

            Toast.makeText(this, appliance + " (" + rating + ") x" + quantity + " added! ✅", Toast.LENGTH_SHORT).show();

            // Update Appliance Selected count
            btnAppliancesSelected.setVisibility(View.VISIBLE);
            btnAppliancesSelected.setText(String.format("Appliance Selected %02d", mp.size()));

            // Reset fields
            spinnerAppliance.setSelection(0);
            spinnerRating.setSelection(0);
            inputQuantity.setText("");
        });

        // Appliance summary (go to previewPage)
        btnAppliancesSelected.setOnClickListener(v -> {
            Intent intent = new Intent(CalculateActivity.this, previewPage.class);
            intent.putExtra("map", mp);
            intent.putExtra("category", category);
            startActivityForResult(intent, 100); // use startActivityForResult to get updated map
        });

        // Fetch appliance list
        fetchAppliances();
    }

    // 👇 Receive updated map when returning from previewPage
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            HashMap<String, HashMap<String, String>> updatedMap =
                    (HashMap<String, HashMap<String, String>>) data.getSerializableExtra("map");

            if (updatedMap != null) {
                mp = updatedMap;

                // Update appliance count on button
                if (mp.size() > 0) {
                    btnAppliancesSelected.setVisibility(View.VISIBLE);
                    btnAppliancesSelected.setText(String.format("Appliance Selected %02d", mp.size()));
                } else {
                    btnAppliancesSelected.setVisibility(View.GONE);
                }
            }
        }
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
                runOnUiThread(() -> Toast.makeText(CalculateActivity.this, "Failed to fetch appliances", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}

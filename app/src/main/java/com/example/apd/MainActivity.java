package com.example.apd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Spinner categorySpinner;
    Button nextButton;
    String BASE_URL = "http://192.168.239.113:8080/arnab_backend_api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        categorySpinner = findViewById(R.id.consumerCategorySpinner);
        nextButton = findViewById(R.id.nextButton);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        fetchCategory();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Animation on button click
                v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();

                    // Get selected item
                    String selected = categorySpinner.getSelectedItem().toString();
                    Toast.makeText(MainActivity.this, "Category Selected: " + selected + " ✅", Toast.LENGTH_SHORT).show();

                    // Start next activity
                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                    intent.putExtra("category", selected);
                    startActivity(intent);
                }).start();
            }
        });
    }

    void fetchCategory() {
        try {
            URL url = new URL(BASE_URL + "/category");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) json.append(line);

            JSONArray arr = new JSONArray(json.toString());
            ArrayList<String> categories = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                categories.add(arr.getString(i));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);

        } catch (Exception e) {
            Toast.makeText(this, "Failed to fetch category", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}

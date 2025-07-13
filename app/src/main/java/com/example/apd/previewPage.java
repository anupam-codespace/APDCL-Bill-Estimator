package com.example.apd;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class previewPage extends AppCompatActivity {

    private LinearLayout applianceContainer;
    public HashMap<String, HashMap<String, String>> mp = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_page);
        Intent intent2 = getIntent();
        String category=intent2.getStringExtra("category");

        applianceContainer = findViewById(R.id.appliance_container);
        ImageView backArrow = findViewById(R.id.back_button);
        Button calculateButton = findViewById(R.id.calculate_button);
        ArrayList<String> rate=new ArrayList<>();

        // Back button
        backArrow.setOnClickListener(view -> {
            view.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("map", mp);
                setResult(RESULT_OK, resultIntent);
                finish();
            }).start();
        });

        // Receive appliance map
        Intent intent = getIntent();
        mp = (HashMap<String, HashMap<String, String>>) intent.getSerializableExtra("map");

        if (mp != null) {
            for (Map.Entry<String, HashMap<String, String>> entry : mp.entrySet()) {
                String applianceName = entry.getKey();
                HashMap<String, String> data = entry.getValue();

                if (data != null && !data.isEmpty()) {
                    Map.Entry<String, String> ratingQty = data.entrySet().iterator().next();
                    String rating = ratingQty.getKey();
                    String qtyStr = ratingQty.getValue();

                    int quantity = 1;
                    try {
                        quantity = Integer.parseInt(qtyStr);
                    } catch (Exception ignored) {}

                    addApplianceRow(applianceName, quantity, rating);
                }
            }
        }

        // Calculate button
        calculateButton.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction(() -> {

                        new Thread(() -> {
                            double totalEnergyPerDay = 0;
                            HashMap<String, Double> monthlyEnergyMap = new HashMap<>();

                            try {
                                for (Map.Entry<String, HashMap<String, String>> entry : mp.entrySet()) {
                                    String appliance = entry.getKey();
                                    HashMap<String, String> ratingQuantityMap = entry.getValue();

                                    if (ratingQuantityMap != null && !ratingQuantityMap.isEmpty()) {
                                        Map.Entry<String, String> inner = ratingQuantityMap.entrySet().iterator().next();
                                        String rating = inner.getKey();
                                        String quantityStr = inner.getValue();

                                        int quantity;
                                        try {
                                            quantity = Integer.parseInt(quantityStr);
                                        } catch (NumberFormatException nfe) {
                                            throw new Exception("Invalid quantity for " + appliance + ": " + quantityStr);
                                        }

                                        String urlStr = "http://192.168.239.113:8080/arnab_backend_api/energy?appliance=" +
                                                appliance + "&rating=" + rating + "-star";
                                        rate.add(rating);

                                        java.net.URL url = new java.net.URL(urlStr);
                                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                                        conn.setRequestMethod("GET");

                                        java.io.BufferedReader br = new java.io.BufferedReader(
                                                new java.io.InputStreamReader(conn.getInputStream()));

                                        String response = br.readLine();
                                        if (response == null || response.trim().isEmpty()) {
                                            throw new Exception("No response from backend for " + appliance);
                                        }

                                        double energyPerUnitPerDay;
                                        try {
                                            energyPerUnitPerDay = Double.parseDouble(response.trim());
                                        } catch (NumberFormatException e) {
                                            throw new Exception("Invalid backend value for " + appliance + ": " + response);
                                        }

                                        double dailyEnergy = energyPerUnitPerDay * quantity;
                                        double monthlyEnergy = dailyEnergy * 30;

                                        totalEnergyPerDay += dailyEnergy;
                                        monthlyEnergyMap.put(appliance, monthlyEnergy);
                                    }
                                }

                                double totalMonthlyEnergy = totalEnergyPerDay * 30;

                                runOnUiThread(() -> {
                                    StringBuilder result = new StringBuilder();
                                    result.append("\uD83D\uDD0B Energy Consumption:\n");
                                    for (Map.Entry<String, Double> entry : monthlyEnergyMap.entrySet()) {
                                        result.append("\u2022 ").append(entry.getKey())
                                                .append(": ")
                                                .append(String.format("%.2f", entry.getValue()))
                                                .append(" units/month\n");
                                    }
                                    result.append("\n\uD83D\uDCCA Total Monthly: ")
                                            .append(String.format("%.2f", totalMonthlyEnergy)).append(" units");

                                    Toast.makeText(previewPage.this, result.toString(), Toast.LENGTH_LONG).show();

                                    Intent intent1 = new Intent(previewPage.this, ResultPageActivity.class);
                                    intent1.putExtra("map1", monthlyEnergyMap);
                                    intent1.putExtra("total", totalMonthlyEnergy);
                                    intent1.putExtra("category", category);
                                    intent1.putExtra("rating",rate);
                                    startActivity(intent1);
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(() ->
                                        Toast.makeText(previewPage.this, "\u274C Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                            }

                        }).start();

                    }).start()
            ).start();
        });
    }

    private void addApplianceRow(String name, int quantity, String rating) {
        View row = LayoutInflater.from(this).inflate(R.layout.row_layout_item, applianceContainer, false);

        ImageView icon = row.findViewById(R.id.appliance_icon);
        TextView label = row.findViewById(R.id.appliance_name);
        TextView quantityText = row.findViewById(R.id.appliance_quantity);
        ImageButton minus = row.findViewById(R.id.minus_button);
        ImageButton plus = row.findViewById(R.id.plus_button);
        ImageButton delete = row.findViewById(R.id.delete_button);

        label.setText(name);
        quantityText.setText(String.format("%02d", quantity));

        String iconName = "ic_" + name.toLowerCase().replace(" ", "_").replace("-", "_");
        int iconResId = getResources().getIdentifier(iconName, "drawable", getPackageName());

        if (iconResId != 0) {
            icon.setImageResource(iconResId);
        } else {
            icon.setImageResource(R.drawable.ic_default_appliance);
        }

        plus.setOnClickListener(v -> {
            int qty = Integer.parseInt(quantityText.getText().toString()) + 1;
            quantityText.setText(String.format("%02d", qty));
            if (mp.containsKey(name)) {
                mp.get(name).put(rating, String.valueOf(qty));
            }
        });

        minus.setOnClickListener(v -> {
            int qty = Integer.parseInt(quantityText.getText().toString()) - 1;
            if (qty <= 0) {
                applianceContainer.removeView(row);
                mp.remove(name);
            } else {
                quantityText.setText(String.format("%02d", qty));
                if (mp.containsKey(name)) {
                    mp.get(name).put(rating, String.valueOf(qty));
                }
            }
        });

        delete.setOnClickListener(v -> {
            applianceContainer.removeView(row);
            mp.remove(name);
        });

        applianceContainer.addView(row);
    }
}

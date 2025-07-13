package com.example.apd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent1 = getIntent();
        String category=intent1.getStringExtra("category");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> {
            v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                intent.putExtra("category", category);
                startActivity(intent);
                finish();
            }).start();
        });

        LinearLayout btnCalculate = findViewById(R.id.btnCalculateBill);
        btnCalculate.setOnClickListener(v -> {
            v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                Toast.makeText(MainActivity2.this, "Calculate Bill Clicked ✅", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity2.this, CalculateActivity.class);
                intent.putExtra("category", category);
                startActivity(intent);
            }).start();
        });

        LinearLayout btnCalculate1 = findViewById(R.id.btnCharges);
        btnCalculate1.setOnClickListener(v -> {
            v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                Toast.makeText(MainActivity2.this, "Calculate Charge Clicked ✅", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity2.this, ComparisonActivity.class);
                startActivity(intent);
            }).start();
        });

        LinearLayout btnCalculate2 = findViewById(R.id.btnTips);
        btnCalculate2.setOnClickListener(v -> {
            v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                Toast.makeText(MainActivity2.this, "Calculate Tip Clicked ✅", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity2.this, TipsActivity.class);
                startActivity(intent);
            }).start();
        });

        LinearLayout btnCalculate3 = findViewById(R.id.btnWebsite);
        btnCalculate3.setOnClickListener(v -> {
            v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse("https://www.apdcl.org/website/"));
                startActivity(intent);
            }).start();
        });


    }
}

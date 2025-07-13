package com.example.apd;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ScrollView;

public class TipsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);

        // Animate scroll content
        ScrollView scrollView = findViewById(R.id.scrollTips);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        scrollView.startAnimation(fadeIn);

        // Animate back arrow with slight scale on click
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(view -> {
            view.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100);
                Intent intent = new Intent(TipsActivity.this, MainActivity2.class);
                startActivity(intent);
                finish();
            }).start();
        });
    }
}
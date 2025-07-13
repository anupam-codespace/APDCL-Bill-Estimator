package com.example.apd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends Activity {

    private static final int SPLASH_DURATION = 3000; // full 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Logo
        ImageView logoImageView = findViewById(R.id.logoImageView);

        // Circles in wave order
        ImageView bgCircleSmall = findViewById(R.id.bgCircleSmall);
        ImageView bgCircleMid = findViewById(R.id.bgCircleMid);
        ImageView bgCircleLarge = findViewById(R.id.bgCircleLarge);


        // Load animations
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        Animation wave1 = AnimationUtils.loadAnimation(this, R.anim.bg_wave_zoom1);
        Animation wave2 = AnimationUtils.loadAnimation(this, R.anim.bg_wave_zoom2);
        Animation wave3 = AnimationUtils.loadAnimation(this, R.anim.bg_wave_zoom3);


        // Start in sequence (wave effect)
        bgCircleSmall.startAnimation(wave1);
        bgCircleMid.startAnimation(wave2);
        bgCircleLarge.startAnimation(wave3);


        // Logo animation
        logoImageView.startAnimation(logoAnim);

        // Splash duration
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}
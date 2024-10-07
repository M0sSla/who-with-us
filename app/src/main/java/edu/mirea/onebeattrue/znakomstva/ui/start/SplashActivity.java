package edu.mirea.onebeattrue.znakomstva.ui.start;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

import edu.mirea.onebeattrue.znakomstva.databinding.ActivitySplashBinding;
import edu.mirea.onebeattrue.znakomstva.ui.auth.Login;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000;
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);


        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        // Создание анимации появления текста
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000); // Длительность анимации в миллисекундах

        // Применение анимации к тексту
        binding.logo.startAnimation(fadeIn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Здесь происходит переход на LoginActivity
                Intent intent = new Intent(SplashActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DELAY);
    }
}

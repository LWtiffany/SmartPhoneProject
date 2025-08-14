package hku.cs.hkutopia;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500; // 2.5 seconds
    private ImageView appLogo;
    private TextView appName;
    private TextView appSlogan;
    private ConstraintLayout logoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        appLogo = findViewById(R.id.appLogo);
        appName = findViewById(R.id.appName);
        appSlogan = findViewById(R.id.appSlogan);
        logoContainer = findViewById(R.id.logoContainer);

        // Set initial states for animation
        appLogo.setScaleX(0.1f);
        appLogo.setScaleY(0.1f);
        appLogo.setAlpha(0f);
        appName.setAlpha(0f);
        appName.setTranslationY(50f);
        appSlogan.setAlpha(0f);
        appSlogan.setTranslationY(50f);

        // Start animations with improved smoothness
        startLogoAnimation();

        // Navigate to MainActivity after animations
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            } catch (Exception e) {
                Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        }, SPLASH_DELAY);
    }

    private void startLogoAnimation() {
        // Improved logo animation with smoother transitions
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(appLogo, View.SCALE_X, 0.1f, 1.0f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(appLogo, View.SCALE_Y, 0.1f, 1.0f);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(appLogo, View.ALPHA, 0f, 1f);

        AnimatorSet logoAnimSet = new AnimatorSet();
        logoAnimSet.playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator);
        logoAnimSet.setDuration(800);
        logoAnimSet.setInterpolator(new OvershootInterpolator(1.0f));

        // Improved text animations with smoother transitions
        ObjectAnimator nameAlphaAnimator = ObjectAnimator.ofFloat(appName, View.ALPHA, 0f, 1f);
        ObjectAnimator nameTranslateAnimator = ObjectAnimator.ofFloat(appName, View.TRANSLATION_Y, 50f, 0f);

        AnimatorSet nameAnimSet = new AnimatorSet();
        nameAnimSet.playTogether(nameAlphaAnimator, nameTranslateAnimator);
        nameAnimSet.setDuration(600);
        nameAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator sloganAlphaAnimator = ObjectAnimator.ofFloat(appSlogan, View.ALPHA, 0f, 1f);
        ObjectAnimator sloganTranslateAnimator = ObjectAnimator.ofFloat(appSlogan, View.TRANSLATION_Y, 50f, 0f);

        AnimatorSet sloganAnimSet = new AnimatorSet();
        sloganAnimSet.playTogether(sloganAlphaAnimator, sloganTranslateAnimator);
        sloganAnimSet.setDuration(600);
        sloganAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());

        // Combine all animations with sequence and reduced delays for smoother flow
        AnimatorSet completeAnimSet = new AnimatorSet();
        completeAnimSet.play(logoAnimSet).before(nameAnimSet);
        completeAnimSet.play(nameAnimSet).before(sloganAnimSet);

        // Add a slight delay before starting text animations
        nameAnimSet.setStartDelay(200);
        sloganAnimSet.setStartDelay(100);

        // Start the animation sequence
        completeAnimSet.start();
    }
}
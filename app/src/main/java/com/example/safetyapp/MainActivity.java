package com.example.safetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    ImageView image, image1, image2;
    Button continue_btn;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        image = findViewById(R.id.image);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        continue_btn = findViewById(R.id.continue_btn);
        fAuth = FirebaseAuth.getInstance();

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(MainActivity.this, Home.class));
            this.finish();
        }

        TextView text_links = findViewById(R.id.link_texts);
        String text = "By clicking on continue, you are accepting our privacy policy \nand terms & conditions.";
        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://social-media-integra-1.flycricket.io/privacy.html")));
            }
        };

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://social-media-integra-1.flycricket.io/terms.html")));
            }
        };

        ss.setSpan(clickableSpan,47,61, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(clickableSpan1,67,85,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(Color.rgb(119, 193, 253)), 47, 61, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(Color.rgb(119, 193, 253)), 67, 85, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text_links.setText(ss);
        text_links.setMovementMethod(LinkMovementMethod.getInstance());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                image.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in));
                image.setVisibility(View.VISIBLE);
            }
        }, 1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                image.setVisibility(View.INVISIBLE);
                image1.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in));
                image1.setVisibility(View.VISIBLE);
            }
        }, 3500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                image1.setVisibility(View.INVISIBLE);
                image2.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in));
                image2.setVisibility(View.VISIBLE);
            }
        }, 5500);

        continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, phoneno_auth.class));
                finish();
            }
        });
    }
}
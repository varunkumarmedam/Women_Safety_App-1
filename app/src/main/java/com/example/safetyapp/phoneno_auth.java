package com.example.safetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class phoneno_auth extends AppCompatActivity {

    ImageView backto_main;
    CountryCodePicker codePicker;
    TextInputLayout phoneNumber;
    Button getOtp_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phoneno_auth);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        backto_main = findViewById(R.id.backto_mainactivity);
        codePicker = findViewById(R.id.ccp);
        phoneNumber = findViewById(R.id.phone_number);
        getOtp_btn = findViewById(R.id.getOtp_btn);

        backto_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(phoneno_auth.this, MainActivity.class));
                finish();
            }
        });

        getOtp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(phoneNumber.getEditText().getText().toString().isEmpty()){
                     phoneNumber.setError("Field can't be empty");
                 }else{
                     phoneNumber.setError(null);

                     Intent intent = new Intent(phoneno_auth.this, otp_auth.class);
                     intent.putExtra("Phone Number", "+" + codePicker.getSelectedCountryCode() + phoneNumber.getEditText().getText().toString().trim());
                     startActivity(intent);
                     finish();
                 }
            }
        });


    }

}

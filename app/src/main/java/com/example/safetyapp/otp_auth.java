package com.example.safetyapp;

import android.content.Intent;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;


public class otp_auth extends AppCompatActivity {

    TextView phoneNo, resend_otp;
    RelativeLayout resend_otp_line;
    LinearLayout backto_Phone;
    Button verify_btn;
    PinView otp;

    String phoneNum, codeBySystem;
    FirebaseAuth fAuth;
    FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_auth);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        phoneNo = findViewById(R.id.phone_number);
        resend_otp = findViewById(R.id.resend_otp);
        backto_Phone = findViewById(R.id.backto_mainactivity);
        resend_otp_line = findViewById(R.id.resendOtp_line);
        otp = findViewById(R.id.pinview);
        verify_btn = findViewById(R.id.verify_btn);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        phoneNum = intent.getStringExtra("Phone Number");
        phoneNo.setText(phoneNum);

        sendCodetoUser(phoneNum);

        backto_Phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(otp_auth.this, phoneno_auth.class));
                finish();
            }
        });

        resend_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { sendCodetoUser(phoneNum);
            }
        });

        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = otp.getText().toString();

                if (!code.isEmpty() && code.length() == 6) {
                    otp.setError(null);
                    verifyCode(code);
                }else{
                    otp.setError("Wrong OTP");
                    resend_otp_line.setVisibility(View.VISIBLE);
                    return;
                }
            }

        });
    }

    private void sendCodetoUser(String phoneNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNo, 60L, TimeUnit.SECONDS, this, mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeBySystem = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();
            if(code != null){
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            resend_otp_line.setVisibility(View.VISIBLE);
            Toast.makeText(otp_auth.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void verifyCode(String code) {

        otp.setText(code);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeBySystem, code);
        signInTheUserByCredentials(credential);

    }

    private void signInTheUserByCredentials(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(otp_auth.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        resend_otp_line.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            Toast.makeText(otp_auth.this, "Authentication Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(otp_auth.this, username.class));
                            finish();
                        } else {
                            resend_otp_line.setVisibility(View.VISIBLE);
                            Toast.makeText(otp_auth.this, "Authentication failed! Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}

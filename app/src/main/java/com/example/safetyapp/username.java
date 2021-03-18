package com.example.safetyapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class username extends AppCompatActivity {

    Button getStarted_btn;
    AlertDialog dialog;
    ProgressDialog progressDialog;

    FirebaseFirestore fstore;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.username);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getStarted_btn = findViewById(R.id.getStarted_btn);
        progressDialog = new ProgressDialog(this);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        getStarted_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(username.this);
                View mView = getLayoutInflater().inflate(R.layout.username_dialog, null);
                mBuilder.setTitle("Enter your name");
                mBuilder.setMessage("This name will be used while sending messages to Emergency contacts.");
                mBuilder.setView(mView);

                TextInputLayout username = (TextInputLayout) mView.findViewById(R.id.username);

                mBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

                dialog = mBuilder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(username.getEditText().getText().toString().isEmpty()){
                            username.setError("Name can't be empty");
                        }else if(username.getEditText().getText().toString().length() >= 15){
                            username.setError("Name should be less than 16 characters");
                        }else{
                            progressDialog.setMessage("Setting name...");
                            String newusername = username.getEditText().getText().toString();
                            Updateusername(newusername);
                        }
                    }
                });
            }
        });
    }

    private void Updateusername(String newusername) {

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()){
                    Map<String, Object> user = new HashMap<>();
                    user.put("USERNAME", newusername);

                    fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).set(user, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(username.this, "Username is set successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), Home.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(username.this, "Username is not set successfully. Try again after sometime.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    createCounter();

                    Map<String, Object> user1 = new HashMap<>();
                    user1.put("USERNAME", newusername);
                    user1.put("PHONE NUMBER", fAuth.getCurrentUser().getPhoneNumber());

                    fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).set(user1).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(username.this, "Username is set successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), Home.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(username.this, "Username is not set successfully. Try again after sometime.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

    }

    private void createCounter() {
        Map<String, Object> user = new HashMap<>();
        user.put("CALL COUNTER", "0");
        user.put("MESSAGE COUNTER", "0");
        user.put("BOTH COUNTER", "0");

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                }else{

                }
            }
        });
    }
}

package com.example.safetyapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class settings extends AppCompatActivity {

    ImageView edit_username;
    TextView username;
    SwitchCompat power_btn, volume_btn;
    AlertDialog dialog;
    ProgressDialog progressDialog;

    FirebaseFirestore fstore;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        edit_username = findViewById(R.id.edit_username);
        username = findViewById(R.id.username);
        power_btn = findViewById(R.id.power_btn);
        volume_btn = findViewById(R.id.volume_btn);
        progressDialog = new ProgressDialog(this);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        edit_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(settings.this);
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

        if(!power_btn.isChecked() && !volume_btn.isChecked()){
            Toast.makeText(this, "Please select either power or volume button...", Toast.LENGTH_LONG).show();
        }

        power_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(power_btn.isChecked()){

                }else{

                }
            }
        });

        volume_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(volume_btn.isChecked()){

                }else{

                }
            }
        });
    }

    private void Updateusername(String newusername) {
        Map<String, Object> user = new HashMap<>();
        user.put("USERNAME", newusername);

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).set(user, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    username.setText(newusername);
                    Toast.makeText(settings.this, "Username is updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(settings.this, "Username is not updated. Try again after sometime.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}

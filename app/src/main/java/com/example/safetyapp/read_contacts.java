package com.example.safetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class read_contacts extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Model> Modelist = new ArrayList<Model>();
    RecyclerView.LayoutManager layoutManager;
    CustomeAdapter_contacts adapter;

    int call_counter, message_counter, both_counter;

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_contacts);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();

        }

    }

    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 111);
        }else{
            readContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==111 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            readContacts();
        }else{
            Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
            checkPermission();
        }
    }

    private void readContacts() {

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC";
        Cursor cursor = getContentResolver().query(uri, null, null, null, sort);

        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" =?";
                Cursor phoneCursor = getContentResolver().query(uriPhone, null, selection, new String[]{id}, null);

                if(phoneCursor.moveToNext()){
                    String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    Model model = new Model();
                    model.setName(name);
                    model.setPhonenumber(number);

                    Modelist.add(model);
                    phoneCursor.close();
                }
            }
            cursor.close();
        }
        adapter = new CustomeAdapter_contacts(read_contacts.this, Modelist);
        recyclerView.setAdapter(adapter);
    }

    public void onlyCall(int position) {

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    call_counter = Integer.valueOf(documentSnapshot.getString("CALL COUNTER"));
                }else{}
            }
        });

        String phoneNumber = Modelist.get(position).getPhonenumber();

        Map<String, Object> user = new HashMap<>();
        user.put("USERNAME", Modelist.get(position).getName());
        user.put("PHONE NUMBER",phoneNumber.replaceAll("\\s", ""));

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Call").document(phoneNumber.replaceAll("\\s", "")).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Map<String, Object> user1 = new HashMap<>();
                    user1.put("CALL COUNTER", String.valueOf(call_counter+1));

                    fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).set(user1, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(read_contacts.this, "Contact added successfully for only call.", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(read_contacts.this, "Contact not added successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void onlyMessage(int position) {

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    message_counter = Integer.valueOf(documentSnapshot.getString("MESSAGE COUNTER"));
                }else{}
            }
        });

        String phoneNumber = Modelist.get(position).getPhonenumber();

        Map<String, Object> user = new HashMap<>();
        user.put("USERNAME", Modelist.get(position).getName());
        user.put("PHONE NUMBER",phoneNumber.replaceAll("\\s", ""));

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Message").document(phoneNumber.replaceAll("\\s", "")).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Map<String, Object> user1 = new HashMap<>();
                    user1.put("MESSAGE COUNTER", String.valueOf(message_counter+1));

                    fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).set(user1, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(read_contacts.this, "Contact added successfully for only message.", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(read_contacts.this, "Contact not added successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void bothCall_Message(int position) {

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    both_counter = Integer.valueOf(documentSnapshot.getString("BOTH COUNTER"));
                }else{}
            }
        });

        String phoneNumber = Modelist.get(position).getPhonenumber();

        Map<String, Object> user = new HashMap<>();
        user.put("USERNAME", Modelist.get(position).getName());
        user.put("PHONE NUMBER",phoneNumber.replaceAll("\\s", ""));

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Both Call & Message").document(phoneNumber.replaceAll("\\s", "")).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Map<String, Object> user1 = new HashMap<>();
                    user1.put("BOTH COUNTER", String.valueOf(both_counter+1));

                    fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).set(user1, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(read_contacts.this, "Contact added successfully for both call & message.", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(read_contacts.this, "Contact not added successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(read_contacts.this, Home.class));
        finish();
    }
}

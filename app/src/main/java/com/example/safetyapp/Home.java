package com.example.safetyapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Home extends AppCompatActivity implements SensorEventListener{

    Button call, message, location;
    TextView empty_contacts;
    FloatingActionButton floatingActionButton;

    RecyclerView recyclerView;
    ArrayList<Model> Modelist = new ArrayList<Model>();
    RecyclerView.LayoutManager layoutManager;
    CustomeAdapter_selectedContacts adapter;

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    ProgressDialog progressDialog;

    List<String> sms_phoneNumbers = new ArrayList<String>();
    int size, call_counter, message_counter, both_counter, no_of_calls, no_of_messages, no_of_both;

    FusedLocationProviderClient mFusedLocationClient;
    Location mLastLocation;
    LocationRequest locationRequest;
    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if(locationResult == null){
                return;
            }
            for(Location location: locationResult.getLocations()){
                Log.d("TAG", "onLocationResult: "+location.toString());
            }
        }
    };

    SensorManager sensorManager;
    Sensor accelerometerSensor;
    boolean isAccelerometerAvailable, isNotfirsttime = false;
    float currentX, currentY, currentZ, lastX, lastY, lastZ, xDifference, yDifference, zDifference;
    float shakeThreshold = 15.0f;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        call = findViewById(R.id.call);
        message = findViewById(R.id.message);
        location = findViewById(R.id.location);
        empty_contacts = findViewById(R.id.empty_contacts);
        floatingActionButton = findViewById(R.id.floatingaction_btn);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerAvailable = true;
        }else{
            isAccelerometerAvailable = false;
        }

        showData();

//        if(checkInternetConnection()){
//            getLocation();
//        }else{
//            getLastLocation();
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }

//        startService(new Intent(getApplicationContext(), MyBroadCastReciever.class));

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, read_contacts.class));
                finish();
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSettingsAndStartLocationUpdates();
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Call").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                String phoneNumber = "tel:" + doc.getString("PHONE NUMBER");
                                call(phoneNumber);
                            }
                        } else {
                        }
                    }
                });
            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                message = "HELP ME, I AM IN DANGER...and I am at "+getLocation();
                sms_phoneNumbers.clear();
                size = 0;

                fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        no_of_messages = Integer.valueOf(documentSnapshot.getString("MESSAGE COUNTER"));
                        no_of_both = Integer.valueOf(documentSnapshot.getString("BOTH COUNTER"));
                    }
                });

                fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Message").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                for(int i=0; i<no_of_messages; i++) {
                                    sms_phoneNumbers.add(doc.getString("PHONE NUMBER"));
                                    size++;
                                }
                            }
                        }
                    }
                });

                fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Both Call & Message").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                for(int i=0; i<no_of_both; i++){
                                    sms_phoneNumbers.add(size, doc.getString("PHONE NUMBER"));
                                    size++;
                                }
                            }
                        }
                    }
                });


                if (size != 0) {
                    Log.d("Tag", "onClick: " + sms_phoneNumbers);
//                    sendSms();
                }
            }
        });
    }

//    private boolean checkInternetConnection() {
//
//        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            Network[] allNetworks = new Network[0];
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                allNetworks = manager.getAllNetworks();
//
//                for (Network network : allNetworks) {
//                    NetworkCapabilities networkCapabilities = manager.getNetworkCapabilities(network);
//                    if (networkCapabilities != null) {
//                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
//                                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
//                                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
//                            return true;
//                    }
//                }
//            }else{
//                return false;
//            }
//        }else {
//            try {
//                NetworkInfo networkInfo = null;
//                if (manager != null) {
//                    networkInfo = manager.getActiveNetworkInfo();
//                }
//                return networkInfo != null && networkInfo.isConnected();
//
//            } catch (NullPointerException e) {
//                return false;
//            }
//        }
//        return false;
//    }

    public void checkSettingsAndStartLocationUpdates(){
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
               if(e instanceof ResolvableApiException){
                   ResolvableApiException apiException = (ResolvableApiException) e;
                   try {
                       apiException.startResolutionForResult(Home.this, 1001);
                   } catch (IntentSender.SendIntentException sendIntentException) {
                       sendIntentException.printStackTrace();
                   }
               }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(){
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
        }else {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        mLastLocation = task.getResult();
                        String latitude = String.format(Locale.ENGLISH, "%f", mLastLocation.getLatitude());
                        String longitude = String.format(Locale.ENGLISH, "%f", mLastLocation.getLongitude());
                        Log.d("TAG", "onClick: " + "https://maps.google.com/?daddr=" + latitude + "," + longitude);
                    } else {
                        Toast.makeText(Home.this, "Error! " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendSms() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        for(int i=0; i<=size; i++){
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage("Help Me");

            ArrayList<PendingIntent> sendList = new ArrayList<>();
            sendList.add(null);

            ArrayList<PendingIntent> deliverList = new ArrayList<>();
            deliverList.add(null);

            sms.sendMultipartTextMessage(sms_phoneNumbers.get(i), null, parts, sendList, deliverList);
        }
    }

    private void call(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(phoneNumber));
        startActivity(intent);
    }

    private void showData() {
        progressDialog.setMessage("Loading Data...");
        progressDialog.show();
        Modelist.clear();

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Call").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    for (DocumentSnapshot doc : task.getResult()) {
                        empty_contacts.setVisibility(View.INVISIBLE);
                        Model model = new Model(doc.getString("USERNAME"),
                                doc.getString("PHONE NUMBER"), "Only Call");
                        Modelist.add(model);
                    }
                    adapter = new CustomeAdapter_selectedContacts(Home.this, Modelist);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(Home.this, "Error !" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Message").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    for (DocumentSnapshot doc : task.getResult()) {
                        empty_contacts.setVisibility(View.INVISIBLE);
                        Model model = new Model(doc.getString("USERNAME"),
                                doc.getString("PHONE NUMBER"), "Only Message");
                        Modelist.add(model);
                    }
                    adapter = new CustomeAdapter_selectedContacts(Home.this, Modelist);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(Home.this, "Error !" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Both Call & Message").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    for (DocumentSnapshot doc : task.getResult()) {
                        empty_contacts.setVisibility(View.INVISIBLE);
                        Model model = new Model(doc.getString("USERNAME"),
                                doc.getString("PHONE NUMBER"), "Both Call & Message");
                        Modelist.add(model);
                    }
                    adapter = new CustomeAdapter_selectedContacts(Home.this, Modelist);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(Home.this, "Error !" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        if(Modelist.size() == 0){
            empty_contacts.setVisibility(View.VISIBLE);
        }
    }

    public void deleteData(int position) {
        progressDialog.setTitle("Deleting Data...");
        progressDialog.show();
        removeData(position);
    }

    private void removeData(int position) {

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    call_counter = Integer.valueOf(documentSnapshot.getString("CALL COUNTER"));
                    message_counter = Integer.valueOf(documentSnapshot.getString("MESSAGE COUNTER"));
                    both_counter = Integer.valueOf(documentSnapshot.getString("BOTH COUNTER"));
                }else{}
            }
        });

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Call").document(Modelist.get(position).getPhonenumber())
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Map<String, Object> user1 = new HashMap<>();
                    user1.put("CALL COUNTER", String.valueOf(call_counter-1));

                    fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).set(user1, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Home.this, "Deleted...", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(Home.this, "Error !"+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Message").document(Modelist.get(position).getPhonenumber())
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Map<String, Object> user1 = new HashMap<>();
                    user1.put("MESSAGE COUNTER", String.valueOf(message_counter-1));

                    fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).set(user1, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Home.this, "Deleted...", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(Home.this, "Error !"+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).collection("Both Call & Message").document(Modelist.get(position).getPhonenumber())
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Map<String, Object> user1 = new HashMap<>();
                    user1.put("BOTH COUNTER", String.valueOf(both_counter-1));

                    fstore.collection("Users").document(fAuth.getCurrentUser().getPhoneNumber()).set(user1, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Home.this, "Deleted...", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(Home.this, "Error !"+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        showData();
    }

    protected void checkPermission(){
        if(ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_CONTACTS)
                + ContextCompat.checkSelfPermission(Home.this, Manifest.permission.CALL_PHONE)
                + ContextCompat.checkSelfPermission(Home.this, Manifest.permission.SEND_SMS)
                + ContextCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.READ_CONTACTS)
                    || ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.CALL_PHONE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(Home.this,Manifest.permission.SEND_SMS)
                    || ActivityCompat.shouldShowRequestPermissionRationale(Home.this,Manifest.permission.ACCESS_FINE_LOCATION)){

                AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                builder.setMessage("Read Contacts, Call and Send sms" +
                        " permissions are required to do the task.");
                builder.setTitle("Please grant those permissions");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                                Home.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                }, 111
                        );
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{

                ActivityCompat.requestPermissions(
                        Home.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        111
                );
            }
        }else {
            Toast.makeText(this,"Permissions already granted!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 111:{
                if((grantResults.length >0) && (grantResults[0] + grantResults[1] + grantResults[2] + grantResults[3]  == PackageManager.PERMISSION_GRANTED)){
                    Toast.makeText(this,"Permissions granted.",Toast.LENGTH_SHORT).show();
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                    builder.setMessage("Read Contacts, Call and Send sms" +
                            " permissions are required to do the task.");
                    builder.setTitle("Please grant those permissions");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    Home.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS,
                                            Manifest.permission.ACCESS_FINE_LOCATION}, 111);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_profile:
//                startActivity(new Intent(Home.this, profile.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void Vibrator(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }else {
            vibrator.vibrate(500);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        currentX = sensorEvent.values[0];
        currentY = sensorEvent.values[1];
        currentZ = sensorEvent.values[2];

        if(isNotfirsttime){

          xDifference = Math.abs(lastX) - currentX;
          yDifference = Math.abs(lastY) - currentY;
          zDifference = Math.abs(lastZ) - currentZ;

          if((xDifference > shakeThreshold && yDifference > shakeThreshold) ||
                  (xDifference > shakeThreshold && zDifference > shakeThreshold) ||
                  (yDifference > shakeThreshold && zDifference > shakeThreshold)){

              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
              }else {
                  vibrator.vibrate(500);
              }

              getLocation();
          }
        }
        lastX = currentX;
        lastY = currentY;
        lastZ = currentZ;
        isNotfirsttime = true;

    }


    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isAccelerometerAvailable){
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        stopLocationUpdates();
    }
}

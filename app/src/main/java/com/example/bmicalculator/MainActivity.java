package com.example.bmicalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import android.os.Bundle;

import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSub, btnMeasure;
        EditText etBB, etAGE;
        TextView b_mi, tvTB, ket_bmi, tvDJ;

        btnSub = findViewById(R.id.btnsubmit);
        btnMeasure = findViewById(R.id.btnmeasure);
        etBB = findViewById(R.id.editTextBB);
        etAGE = findViewById(R.id.editTextAGE);
        b_mi = findViewById(R.id.textViewBMI);
        tvTB = findViewById(R.id.textViewTB);
        ket_bmi = findViewById(R.id.textViewKET);
        tvDJ = findViewById(R.id.editTextBPM);

        databaseReference =FirebaseDatabase.getInstance().getReference();

        btnMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("height").setValue(0);
                databaseReference.child("command").setValue("measure_value");

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        databaseReference.child("command").setValue("not_measure_value");
                    }
                }, 2000);

                int maxAttempts = 10;
                int[] attempts = {0};

                final Runnable[] checkValueTask = new Runnable[1];
                checkValueTask[0] = new Runnable() {
                    @Override
                    public void run() {
                        databaseReference.child("height").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    double height = snapshot.getValue(Double.class);

                                    if (height != 0) {

                                        tvTB.setText(String.valueOf(height));
                                    } else if (attempts[0] < maxAttempts) {

                                        attempts[0]++;
                                        handler.postDelayed(checkValueTask[0], 1000);
                                    } else {

                                        tvTB.setText("N/A");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                tvTB.setText("Error: " + error.getMessage());
                            }
                        });

                        databaseReference.child("bpm").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    double bpm = dataSnapshot.getValue(Double.class);
                                    tvDJ.setText(String.valueOf(bpm));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                };
                handler.post(checkValueTask[0]);
            }
        });
        btnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double TB, DJ, BB, AGE, bmi;
                String statusBB, statusDJ;

                TB = Double.parseDouble(tvTB.getText().toString());
                DJ = Double.parseDouble(tvDJ.getText().toString());
                BB = Double.parseDouble(etBB.getText().toString());
                AGE = Double.parseDouble(etAGE.getText().toString());

                if (AGE < 18){
                    bmi = 0;
                    statusBB = "Too young (range age 18 - 65 y/o)";
                    statusDJ = "";
                }else if (AGE >= 18 && AGE <= 65){
                    bmi = BB / ((TB / 100) * (TB / 100));
                    if (bmi < 18.5){
                        statusBB = "Underweight";
                    }else if (bmi >= 18.5 && bmi <= 24.9){
                        statusBB = "Normal (Ideal)";
                    }else if (bmi >= 25 && bmi <= 29.9){
                        statusBB = "Overweight";
                    }else {
                        statusBB = "Obesity";
                    }

                    if (DJ < 60){
                        statusDJ = " With Low Heart Rate";
                    }else if (DJ >= 60 && DJ <= 100){
                        statusDJ = " With Normal Heart Rate";
                    }else {
                        statusDJ = " With High Heart Rate";
                    }

                }else {
                    bmi = 0;
                    statusBB = "Too old (range age 18 - 65 y/o)";
                    statusDJ = "";
                }
                b_mi.setText(String.format("%.2f", bmi));
                ket_bmi.setText(statusBB + statusDJ);
            }
        });
    }
}
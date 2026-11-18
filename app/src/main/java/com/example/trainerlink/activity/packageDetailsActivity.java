package com.example.trainerlink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainerlink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.packageAdapter;

public class packageDetailsActivity extends AppCompatActivity {

    TextView tvTitle, tvTypeDuration, tvPrice, tvDescription;
    Button btnBookNow;

    String packageId, title, type, description;
    long durationWeeks, price;

    RecyclerView rvOtherPackages;
    List<module.Package> otherPackages = new ArrayList<>();
    packageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_details);

        tvTitle = findViewById(R.id.tvTitle);
        tvTypeDuration = findViewById(R.id.tvTypeDuration);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        btnBookNow = findViewById(R.id.btnBookNow);

        // get data
        packageId = getIntent().getStringExtra("packageId");
        title = getIntent().getStringExtra("title");
        type = getIntent().getStringExtra("type");
        description = getIntent().getStringExtra("description");
        durationWeeks = getIntent().getLongExtra("durationWeeks", 0);
        price = getIntent().getLongExtra("price", 0);

        // show data
        tvTitle.setText(title);
        tvTypeDuration.setText(type + " • " + durationWeeks + " weeks");
        tvPrice.setText("LKR " + price);
        tvDescription.setText(description);

        btnBookNow.setOnClickListener(v -> {
            Intent i = new Intent(packageDetailsActivity.this, BookingActivity.class);
            i.putExtra("packageId", packageId);
            i.putExtra("title", title);
            i.putExtra("type", type);
            i.putExtra("price", price);
            startActivity(i);
        });


        rvOtherPackages = findViewById(R.id.rvOtherPackages);
        rvOtherPackages.setLayoutManager(new LinearLayoutManager(this));

        adapter = new packageAdapter(otherPackages, pkg -> {

            Intent i = new Intent(this, packageDetailsActivity.class);
            i.putExtra("packageId", pkg.id);
            i.putExtra("title", pkg.title);
            i.putExtra("type", pkg.type);
            i.putExtra("durationWeeks", pkg.durationWeeks);
            i.putExtra("price", pkg.price);
            i.putExtra("description", pkg.description);

            startActivity(i);

        });

        rvOtherPackages.setAdapter(adapter);

        loadOtherPackages();
    }

    private void loadOtherPackages() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("packages")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(query -> {

                    otherPackages.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        module.Package p = doc.toObject(module.Package.class);

                        if (p != null) {
                            p.id = doc.getId();

                            // skip current selected package
                            if (!p.id.equals(packageId)) {
                                otherPackages.add(p);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void createBooking() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", u.getUid());
        booking.put("packageId", packageId);
        booking.put("packageTitle", title);
        booking.put("price", price);
        booking.put("type", type);
        booking.put("status", "PENDING");
        booking.put("createdAt", FieldValue.serverTimestamp());

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Booking created!", Toast.LENGTH_SHORT).show();
                    finish(); // go back to packages
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }


}
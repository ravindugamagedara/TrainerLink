package com.example.trainerlink.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trainerlink.R;

public class BookingDetailsActivity extends AppCompatActivity {

    TextView tvDetailPackageTitle, tvDetailType, tvDetailPrice,
            tvDetailDate, tvDetailTime, tvDetailNote,
            tvDetailPaymentStatus, tvDetailBookingStatus;

    View viewDotPaid, viewDotConfirmed;
    Button btnCancelBooking;
    String bookingId;

    LinearLayout layoutLocationDetails;
    TextView tvDetailLatitude, tvDetailLongitude;
    Button btnMarkCompleted;
    View viewDotCompleted;

    View viewLineConfirmed;

    TextView tvTimelineCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        tvDetailPackageTitle = findViewById(R.id.tvDetailPackageTitle);
        tvDetailType = findViewById(R.id.tvDetailType);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailTime = findViewById(R.id.tvDetailTime);
        tvDetailNote = findViewById(R.id.tvDetailNote);
        tvDetailPaymentStatus = findViewById(R.id.tvDetailPaymentStatus);
        tvDetailBookingStatus = findViewById(R.id.tvDetailBookingStatus);

        viewDotPaid = findViewById(R.id.viewDotPaid);
        viewDotConfirmed = findViewById(R.id.viewDotConfirmed);
        viewDotCompleted = findViewById(R.id.viewDotCompleted);
        viewLineConfirmed = findViewById(R.id.viewLineConfirmed);

        btnCancelBooking = findViewById(R.id.btnCancelBooking);
        btnMarkCompleted = findViewById(R.id.btnMarkCompleted);

        layoutLocationDetails = findViewById(R.id.layoutLocationDetails);
        tvDetailLatitude = findViewById(R.id.tvDetailLatitude);
        tvDetailLongitude = findViewById(R.id.tvDetailLongitude);
        tvTimelineCompleted = findViewById(R.id.tvTimelineCompleted);

        bookingId = getIntent().getStringExtra("bookingId");

        String packageTitle = getIntent().getStringExtra("packageTitle");
        String type = getIntent().getStringExtra("type");
        long price = getIntent().getLongExtra("price", 0);
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String note = getIntent().getStringExtra("note");
        String paymentStatus = getIntent().getStringExtra("paymentStatus");
        String bookingStatus = getIntent().getStringExtra("status");

        tvDetailPackageTitle.setText(packageTitle);
        tvDetailType.setText(type);
        tvDetailPrice.setText("LKR " + price);
        tvDetailDate.setText(date);
        tvDetailTime.setText(time);
        tvDetailNote.setText(note);
        tvDetailPaymentStatus.setText(paymentStatus);
        tvDetailBookingStatus.setText(bookingStatus);

        if ("PAID".equals(paymentStatus)) {
            viewDotPaid.setBackgroundResource(R.drawable.timeline_active_dot);
        }

        if ("CONFIRMED".equals(bookingStatus)) {
            viewDotConfirmed.setBackgroundResource(R.drawable.timeline_active_dot);
        }

        if ("CANCELLED".equals(bookingStatus)) {
            btnCancelBooking.setEnabled(false);
            btnCancelBooking.setText("Booking Cancelled");
            btnMarkCompleted.setEnabled(false);
            btnMarkCompleted.setText("Cannot Complete Cancelled Booking");
        }

        if ("COMPLETED".equals(bookingStatus)) {
            viewDotPaid.setBackgroundResource(R.drawable.timeline_active_dot);
            viewDotConfirmed.setBackgroundResource(R.drawable.timeline_active_dot);
            viewDotCompleted.setBackgroundResource(R.drawable.timeline_active_dot);
            viewLineConfirmed.setBackgroundColor(getResources().getColor(R.color.orange));

            tvTimelineCompleted.setTextColor(getResources().getColor(R.color.orange));

            btnMarkCompleted.setEnabled(false);
            btnMarkCompleted.setText("Session Completed");
            btnCancelBooking.setEnabled(false);
        }

        btnCancelBooking.setOnClickListener(v -> cancelBooking());
        btnMarkCompleted.setOnClickListener(v -> markBookingCompleted());

        loadLocationDetails();
    }

    private void loadLocationDetails() {

        if (bookingId == null || bookingId.isEmpty()) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String type = documentSnapshot.getString("type");
                        String locationStatus = documentSnapshot.getString("locationStatus");

                        Double lat = documentSnapshot.getDouble("latitude");
                        Double lng = documentSnapshot.getDouble("longitude");

                        if (type != null && type.equalsIgnoreCase("Home")
                                && locationStatus != null && locationStatus.equalsIgnoreCase("SHARED")
                                && lat != null && lng != null) {

                            layoutLocationDetails.setVisibility(View.VISIBLE);

                            tvDetailLatitude.setText("Latitude : " + lat);
                            tvDetailLongitude.setText("Longitude : " + lng);

                            layoutLocationDetails.setOnClickListener(v -> {
                                v.animate()
                                        .scaleX(0.97f)
                                        .scaleY(0.97f)
                                        .setDuration(100)
                                        .withEndAction(() -> {
                                            v.animate()
                                                    .scaleX(1f)
                                                    .scaleY(1f)
                                                    .setDuration(100)
                                                    .start();

                                            Intent intent = new Intent(BookingDetailsActivity.this, MapViewActivity.class);
                                            intent.putExtra("lat", lat);
                                            intent.putExtra("lng", lng);
                                            startActivity(intent);
                                        })
                                        .start();
                            });

                        } else {
                            layoutLocationDetails.setVisibility(View.GONE);
                        }

                    }
                });
    }

    private void markBookingCompleted() {
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Booking ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings")
                .document(bookingId)
                .update("status", "COMPLETED")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Session marked as completed", Toast.LENGTH_SHORT).show();

                    tvDetailBookingStatus.setText("COMPLETED");
                    btnMarkCompleted.setEnabled(false);
                    btnMarkCompleted.setText("Session Completed");
                    btnCancelBooking.setEnabled(false);

                    viewDotPaid.setBackgroundResource(R.drawable.timeline_active_dot);
                    viewDotConfirmed.setBackgroundResource(R.drawable.timeline_active_dot);
                    viewDotCompleted.setBackgroundResource(R.drawable.timeline_active_dot);
                    viewLineConfirmed.setBackgroundColor(getResources().getColor(R.color.orange));
                    tvTimelineCompleted.setTextColor(getResources().getColor(R.color.orange));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void cancelBooking() {

        if ("COMPLETED".equals(tvDetailBookingStatus.getText().toString())) {
            Toast.makeText(this, "Completed sessions cannot be cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Booking ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings")
                .document(bookingId)
                .update("status", "CANCELLED")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Booking cancelled successfully", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(BookingDetailsActivity.this, mainActivity.class);
                    intent.putExtra("openFragment", "bookings");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Cancel failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
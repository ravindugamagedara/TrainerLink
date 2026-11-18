package com.example.trainerlink.activity;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import android.os.Build;

import androidx.core.app.ActivityCompat;

import Util.NotificationHelper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.trainerlink.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.model.InitRequest;

public class BookingActivity extends AppCompatActivity {

    private static final int PAYHERE_REQUEST = 11001;

    TextView tvBookingPackageTitle, tvBookingPrice, tvLocationStatus;
    EditText etBookingDate, etBookingTime, etBookingNote;
    Button btnConfirmBooking, btnShareLocation;
    LinearLayout layoutLocationSection;

    String packageId, title, type;
    long price;

    Double userLatitude = null;
    Double userLongitude = null;

    FusedLocationProviderClient fusedLocationProviderClient;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        tvBookingPackageTitle = findViewById(R.id.tvBookingPackageTitle);
        tvBookingPrice = findViewById(R.id.tvBookingPrice);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);

        etBookingDate = findViewById(R.id.etBookingDate);
        etBookingTime = findViewById(R.id.etBookingTime);
        etBookingNote = findViewById(R.id.etBookingNote);

        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        btnShareLocation = findViewById(R.id.btnShareLocation);

        layoutLocationSection = findViewById(R.id.layoutLocationSection);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        packageId = getIntent().getStringExtra("packageId");
        title = getIntent().getStringExtra("title");
        type = getIntent().getStringExtra("type");
        price = getIntent().getLongExtra("price", 0);

        tvBookingPackageTitle.setText(title);
        tvBookingPrice.setText("LKR " + price);

        if (type != null && type.equalsIgnoreCase("Home")) {
            layoutLocationSection.setVisibility(View.VISIBLE);
        } else {
            layoutLocationSection.setVisibility(View.GONE);
        }

        etBookingDate.setOnClickListener(v -> showDatePicker());
        etBookingTime.setOnClickListener(v -> showTimePicker());

        btnShareLocation.setOnClickListener(v -> checkLocationPermissionAndFetch());

        btnConfirmBooking.setOnClickListener(v -> {
            if (type != null && type.equalsIgnoreCase("Home")) {
                if (userLatitude == null || userLongitude == null) {
                    Toast.makeText(this, "Please share your location for home training", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            startPayHerePayment();
        });

        NotificationHelper.createChannel(this);
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        2001
                );
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) ->
                        etBookingDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) ->
                        etBookingTime.setText(hourOfDay + ":" + String.format("%02d", minute)),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);

        dialog.show();
    }

    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();

                        tvLocationStatus.setText(
                                "Location captured successfully\nLat: " + userLatitude + "\nLng: " + userLongitude
                        );

                        Toast.makeText(this, "Location shared successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Could not get current location. Please turn on GPS.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void startPayHerePayment() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = etBookingDate.getText().toString().trim();
        String time = etBookingTime.getText().toString().trim();
        String note = etBookingNote.getText().toString().trim();

        if (date.isEmpty() || time.isEmpty() || note.isEmpty()) {
            Toast.makeText(this, "Please fill all booking details", Toast.LENGTH_SHORT).show();
            return;
        }

        InitRequest req = new InitRequest();
        req.setMerchantId("1234407");
        req.setCurrency("LKR");
        req.setAmount((double) price);
        req.setOrderId("BOOK_" + System.currentTimeMillis());
        req.setItemsDescription(title);

        req.getCustomer().setFirstName("TrainerLink");
        req.getCustomer().setLastName("User");
        req.getCustomer().setEmail(user.getEmail() != null ? user.getEmail() : "test@example.com");
        req.getCustomer().setPhone("+94770000000");
        req.getCustomer().getAddress().setAddress(note);
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {

            Serializable result = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (resultCode == Activity.RESULT_OK) {

                boolean paymentSuccess = false;

                if (result != null) {
                    try {
                        Method method = result.getClass().getMethod("isSuccess");
                        Object value = method.invoke(result);

                        if (value instanceof Boolean) {
                            paymentSuccess = (Boolean) value;
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Could not read payment result", Toast.LENGTH_LONG).show();
                    }
                }

                if (paymentSuccess) {
                    saveBookingAfterPayment();
                } else {
                    Toast.makeText(this, "Payment failed", Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPaymentSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_success);
        dialog.setCancelable(false);

        ImageView imgSuccess = dialog.findViewById(R.id.imgSuccess);

        Animation zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.success_zoom);
        imgSuccess.startAnimation(zoomAnimation);

        dialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();

            Intent intent = new Intent(BookingActivity.this, mainActivity.class);
            intent.putExtra("openFragment", "bookings");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }, 2000);
    }

    private void saveBookingAfterPayment() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = etBookingDate.getText().toString().trim();
        String time = etBookingTime.getText().toString().trim();
        String note = etBookingNote.getText().toString().trim();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", user.getUid());
        booking.put("packageId", packageId);
        booking.put("packageTitle", title);
        booking.put("price", price);
        booking.put("type", type);
        booking.put("date", date);
        booking.put("time", time);
        booking.put("note", note);
        booking.put("status", "CONFIRMED");
        booking.put("paymentMethod", "PayHere");
        booking.put("paymentStatus", "PAID");
        booking.put("createdAt", FieldValue.serverTimestamp());

        if (type != null && type.equalsIgnoreCase("Home")) {
            booking.put("latitude", userLatitude);
            booking.put("longitude", userLongitude);
            booking.put("locationStatus", "SHARED");
        } else {
            booking.put("locationStatus", "NOT_REQUIRED");
        }

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Booking saved successfully!", Toast.LENGTH_SHORT).show();

                    NotificationHelper.showBookingNotification(
                            this,
                            "Booking Confirmed",
                            "Your training session has been booked successfully."
                    );

                    NotificationHelper.scheduleSessionReminder(
                            this,
                            title,
                            date,
                            time
                    );

                    showPaymentSuccessDialog();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save booking: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
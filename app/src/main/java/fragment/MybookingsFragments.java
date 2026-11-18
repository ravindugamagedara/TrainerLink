package fragment;

import android.os.Bundle;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainerlink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import adapter.BookingAdapter;
import module.Booking;

public class MybookingsFragments extends Fragment {

    private RecyclerView rvMyBookings;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;

    public MybookingsFragments() {
        super(R.layout.fragment_mybookings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMyBookings = view.findViewById(R.id.rvMyBookings);
        rvMyBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList, booking -> {
            android.content.Intent intent = new android.content.Intent(getContext(), com.example.trainerlink.activity.BookingDetailsActivity.class);
            intent.putExtra("bookingId", booking.id);
            intent.putExtra("packageTitle", booking.packageTitle);
            intent.putExtra("type", booking.type);
            intent.putExtra("price", booking.price);
            intent.putExtra("date", booking.date);
            intent.putExtra("time", booking.time);
            intent.putExtra("note", booking.note);
            intent.putExtra("paymentStatus", booking.paymentStatus);
            intent.putExtra("status", booking.status);
            startActivity(intent);
        });
        rvMyBookings.setAdapter(bookingAdapter);

        loadMyBookings();
    }

    private void loadMyBookings() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bookings")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.id = doc.getId();
                        bookingList.add(booking);
                    }

                    bookingAdapter.notifyDataSetChanged();

                    if (bookingList.isEmpty()) {
                        Toast.makeText(getContext(), "No bookings found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
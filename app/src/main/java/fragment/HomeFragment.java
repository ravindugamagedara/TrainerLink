package fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.trainerlink.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import adapter.HomeSliderAdapter;
import module.HomeSliderItem;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerHomeSlider;
    private LinearLayout layoutSliderDots;
    private TextView txtWelcomeHome;
    private TextView txtUpcomingPackageTitle, txtUpcomingDateTime, txtUpcomingStatus;

    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private List<HomeSliderItem> sliderItems;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtWelcomeHome = view.findViewById(R.id.txtWelcomeHome);
        viewPagerHomeSlider = view.findViewById(R.id.viewPagerHomeSlider);
        layoutSliderDots = view.findViewById(R.id.layoutSliderDots);

        txtUpcomingPackageTitle = view.findViewById(R.id.txtUpcomingPackageTitle);
        txtUpcomingDateTime = view.findViewById(R.id.txtUpcomingDateTime);
        txtUpcomingStatus = view.findViewById(R.id.txtUpcomingStatus);

        MaterialCardView cardPackages = view.findViewById(R.id.cardPackages);
        MaterialCardView cardBookings = view.findViewById(R.id.cardBookings);
        MaterialCardView cardContact = view.findViewById(R.id.cardContact);
        MaterialCardView cardProfile = view.findViewById(R.id.cardProfile);

        setWelcomeText();
        setupSlider();
        loadUpcomingBooking();

        cardPackages.setOnClickListener(v -> switchBottomNavigation(R.id.packages));
        cardContact.setOnClickListener(v -> switchBottomNavigation(R.id.contact));
        cardBookings.setOnClickListener(v -> switchDrawerAndFragment(R.id.bookings, new MybookingsFragments()));
        cardProfile.setOnClickListener(v -> switchDrawerAndFragment(R.id.profile, new ProfileFragment()));
    }

    private void setWelcomeText() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            txtWelcomeHome.setText("Welcome Back");
        } else {
            txtWelcomeHome.setText("Welcome");
        }
    }

    private void setupSlider() {
        sliderItems = new ArrayList<>();
        sliderItems.add(new HomeSliderItem(
                R.drawable.fitness1,
                "Stay Strong",
                "Success starts with self-discipline."
        ));
        sliderItems.add(new HomeSliderItem(
                R.drawable.fitness2,
                "Push Your Limits",
                "Your body can stand almost anything. It’s your mind you have to convince."
        ));
        sliderItems.add(new HomeSliderItem(
                R.drawable.fitness3,
                "Keep Moving",
                "Small progress every day adds up to big results."
        ));

        HomeSliderAdapter adapter = new HomeSliderAdapter(sliderItems);
        viewPagerHomeSlider.setAdapter(adapter);

        setupDots(sliderItems.size(), 0);

        viewPagerHomeSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupDots(sliderItems.size(), position);

                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });

        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (sliderItems == null || sliderItems.isEmpty()) return;

            int nextItem = viewPagerHomeSlider.getCurrentItem() + 1;
            if (nextItem >= sliderItems.size()) {
                nextItem = 0;
            }
            viewPagerHomeSlider.setCurrentItem(nextItem, true);
        }
    };

    private void setupDots(int count, int currentPosition) {
        layoutSliderDots.removeAllViews();

        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);

            if (i == currentPosition) {
                dot.setBackgroundResource(R.drawable.dot_active);
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive);
            }

            layoutSliderDots.addView(dot);
        }
    }

    private void loadUpcomingBooking() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) return;

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        var document = queryDocumentSnapshots.getDocuments().get(0);

                        String packageTitle = document.getString("packageTitle");
                        String date = document.getString("date");
                        String time = document.getString("time");
                        String status = document.getString("status");

                        txtUpcomingPackageTitle.setText(packageTitle != null ? packageTitle : "Upcoming Session");
                        txtUpcomingDateTime.setText((date != null ? date : "-") + " at " + (time != null ? time : "-"));
                        txtUpcomingStatus.setText(status != null ? status : "PENDING");
                    } else {
                        txtUpcomingPackageTitle.setText("No upcoming session");
                        txtUpcomingDateTime.setText("Book a session to see it here");
                        txtUpcomingStatus.setText("No Booking");
                    }
                });
    }

    private void switchFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void switchBottomNavigation(int itemId) {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }

    private void switchDrawerAndFragment(int drawerItemId, Fragment fragment) {
        NavigationView navigationView = requireActivity().findViewById(R.id.navigationView);
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);

        if (navigationView != null) {
            navigationView.setCheckedItem(drawerItemId);
        }

        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.home).setChecked(false);
        }

        switchFragment(fragment);
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}
package com.example.trainerlink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.trainerlink.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fragment.ContactFragment;
import fragment.HomeFragment;
import fragment.MybookingsFragments;
import fragment.PackagesFragment;
import fragment.ProfileFragment;

public class mainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNav = findViewById(R.id.bottomNavigation);
        toolbar = findViewById(R.id.topAppBar);

        if (drawerLayout == null || navigationView == null || bottomNav == null || toolbar == null) {
            Toast.makeText(this, "Main screen failed to load", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(mainActivity.this, loginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setupDrawerMenu();
        setupBottomNavigation();
        loadHeaderUserData(currentUser);

        String openFragment = getIntent().getStringExtra("openFragment");

        if (savedInstanceState == null) {
            if ("bookings".equals(openFragment)) {
                loadFragment(new MybookingsFragments());
                navigationView.setCheckedItem(R.id.bookings);
            } else {
                bottomNav.setSelectedItemId(R.id.home);
            }
        }

        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );
    }

    private void setupDrawerMenu() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home || id == R.id.packages || id == R.id.contact) {
                bottomNav.setSelectedItemId(id);
            } else if (id == R.id.profile) {
                loadFragment(new ProfileFragment());
                clearBottomSelection();
            } else if (id == R.id.bookings) {
                loadFragment(new MybookingsFragments());
                clearBottomSelection();
            } else if (id == R.id.logout) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(mainActivity.this, loginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            navigationView.setCheckedItem(id);

            if (id == R.id.home) {
                loadFragment(new HomeFragment());
            } else if (id == R.id.packages) {
                loadFragment(new PackagesFragment());
            } else if (id == R.id.contact) {
                loadFragment(new ContactFragment());
            }

            return true;
        });
    }

    private void loadHeaderUserData(FirebaseUser currentUser) {
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        TextView tvName = headerView.findViewById(R.id.txtName);
        TextView tvEmail = headerView.findViewById(R.id.txtEmail);

        if (tvName == null || tvEmail == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentUser.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (firstName == null) firstName = "";
                    if (lastName == null) lastName = "";
                    if (email == null) email = currentUser.getEmail();

                    tvName.setText((firstName + " " + lastName).trim());
                    tvEmail.setText(email != null ? email : "");
                } else {
                    tvName.setText("TrainerLink User");
                    tvEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tvName.setText("TrainerLink User");
                tvEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void clearBottomSelection() {
        Menu menu = bottomNav.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }
    }
}
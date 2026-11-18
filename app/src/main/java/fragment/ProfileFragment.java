package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.trainerlink.R;
import com.example.trainerlink.activity.loginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView txtProfileName, txtProfileEmail, txtFullNameValue, txtEmailValue;
    private MaterialButton btnLogout;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtProfileName = view.findViewById(R.id.txtProfileName);
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail);
        txtFullNameValue = view.findViewById(R.id.txtFullNameValue);
        txtEmailValue = view.findViewById(R.id.txtEmailValue);
        btnLogout = view.findViewById(R.id.btnLogout);

        TextView txtTrainerName = view.findViewById(R.id.txtTrainerName);
        TextView txtTrainerRole = view.findViewById(R.id.txtTrainerRole);
        TextView txtTrainerBio = view.findViewById(R.id.txtTrainerBio);
        TextView txtTrainerPhone = view.findViewById(R.id.txtTrainerPhone);
        TextView txtTrainerEmail = view.findViewById(R.id.txtTrainerEmail);
        TextView txtTrainerExperience = view.findViewById(R.id.txtTrainerExperience);
        TextView txtTrainerAchievements = view.findViewById(R.id.txtTrainerAchievements);
        ImageView imgTrainerProfile = view.findViewById(R.id.imgTrainerProfile);

        loadUserData();
        loadTrainerProfile(
                imgTrainerProfile,
                txtTrainerName,
                txtTrainerRole,
                txtTrainerBio,
                txtTrainerPhone,
                txtTrainerEmail,
                txtTrainerExperience,
                txtTrainerAchievements
        );

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(requireActivity(), loginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void loadTrainerProfile(ImageView imgTrainerProfile,
                                    TextView txtTrainerName,
                                    TextView txtTrainerRole,
                                    TextView txtTrainerBio,
                                    TextView txtTrainerPhone,
                                    TextView txtTrainerEmail,
                                    TextView txtTrainerExperience,
                                    TextView txtTrainerAchievements) {

        FirebaseFirestore.getInstance()
                .collection("trainer_profile")
                .document("trainer1")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String experience = documentSnapshot.getString("experience");
                        String bio = documentSnapshot.getString("bio");
                        String phone = documentSnapshot.getString("phone");
                        String email = documentSnapshot.getString("email");
                        String achievements = documentSnapshot.getString("achievements");
                        String profileImage = documentSnapshot.getString("profileImage");

                        txtTrainerName.setText(name != null ? name : "Trainer Name");
                        txtTrainerRole.setText(experience != null ? experience : "Personal Fitness Trainer");
                        txtTrainerBio.setText(bio != null ? bio : "Trainer bio not available.");
                        txtTrainerPhone.setText("Phone: " + (phone != null ? phone : "Not available"));
                        txtTrainerEmail.setText("Email: " + (email != null ? email : "Not available"));
                        txtTrainerExperience.setText("Experience: " + (experience != null ? experience : "Not available"));
                        txtTrainerAchievements.setText("Achievements: " + (achievements != null ? achievements : "Not available"));

                        if (profileImage != null && !profileImage.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(profileImage)
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.profile)
                                    .into(imgTrainerProfile);
                        } else {
                            imgTrainerProfile.setImageResource(R.drawable.profile);
                        }

                    } else {
                        txtTrainerName.setText("Trainer Name");
                        txtTrainerRole.setText("Personal Fitness Trainer");
                        txtTrainerBio.setText("Trainer bio not available.");
                        txtTrainerPhone.setText("Phone: Not available");
                        txtTrainerEmail.setText("Email: Not available");
                        txtTrainerExperience.setText("Experience: Not available");
                        txtTrainerAchievements.setText("Achievements: Not available");
                        imgTrainerProfile.setImageResource(R.drawable.profile);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load trainer profile", Toast.LENGTH_SHORT).show();
                    imgTrainerProfile.setImageResource(R.drawable.profile);
                });
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (firstName == null) firstName = "";
                    if (lastName == null) lastName = "";
                    if (email == null) email = "No email found";

                    String fullName = (firstName + " " + lastName).trim();

                    txtProfileName.setText(fullName.isEmpty() ? "TrainerLink User" : fullName);
                    txtProfileEmail.setText(email);
                    txtFullNameValue.setText(fullName.isEmpty() ? "Not available" : fullName);
                    txtEmailValue.setText(email);
                } else {
                    txtProfileName.setText("TrainerLink User");
                    txtProfileEmail.setText("No email found");
                    txtFullNameValue.setText("Not available");
                    txtEmailValue.setText("Not available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
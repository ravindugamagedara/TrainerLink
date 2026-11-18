package fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.trainerlink.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class ContactFragment extends Fragment {

    private String trainerPhone = "";
    private String trainerEmail = "";
    private String trainerWhatsapp = "";
    private String trainerName = "";

    public ContactFragment() {
        super(R.layout.fragment_contact);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnCallTrainer = view.findViewById(R.id.btnCallTrainer);
        MaterialButton btnSmsTrainer = view.findViewById(R.id.btnSmsTrainer);
        MaterialButton btnEmailTrainer = view.findViewById(R.id.btnEmailTrainer);
        MaterialButton btnWhatsappTrainer = view.findViewById(R.id.btnWhatsappTrainer);

        TextView txtTrainerName = view.findViewById(R.id.txtTrainerName);
        TextView txtTrainerPhone = view.findViewById(R.id.txtTrainerPhone);
        TextView txtTrainerEmail = view.findViewById(R.id.txtTrainerEmail);
        TextView txtTrainerExperience = view.findViewById(R.id.txtTrainerExperience);
        TextView txtTrainerAchievements = view.findViewById(R.id.txtTrainerAchievements);
        ImageView imgTrainerProfile = view.findViewById(R.id.imgTrainerProfile);

        loadTrainerProfile(
                imgTrainerProfile,
                txtTrainerName,
                txtTrainerPhone,
                txtTrainerEmail,
                txtTrainerExperience,
                txtTrainerAchievements
        );

        btnCallTrainer.setOnClickListener(v -> {
            animateButton(v);
            openDialPad();
        });

        btnSmsTrainer.setOnClickListener(v -> {
            animateButton(v);
            openSmsApp();
        });

        btnEmailTrainer.setOnClickListener(v -> {
            animateButton(v);
            openEmailApp();
        });

        btnWhatsappTrainer.setOnClickListener(v -> {
            animateButton(v);
            openWhatsApp();
        });
    }

    private void loadTrainerProfile(ImageView imgTrainerProfile,
                                    TextView txtTrainerName,
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

                        trainerName = documentSnapshot.getString("name") != null
                                ? documentSnapshot.getString("name") : "Trainer";

                        trainerPhone = documentSnapshot.getString("phone") != null
                                ? documentSnapshot.getString("phone") : "";

                        trainerEmail = documentSnapshot.getString("email") != null
                                ? documentSnapshot.getString("email") : "";

                        String experience = documentSnapshot.getString("experience");
                        String achievements = documentSnapshot.getString("achievements");
                        String profileImage = documentSnapshot.getString("profileImage");

                        trainerWhatsapp = trainerPhone;

                        txtTrainerName.setText(trainerName);
                        txtTrainerPhone.setText("Phone: " + (trainerPhone.isEmpty() ? "Not available" : trainerPhone));
                        txtTrainerEmail.setText("Email: " + (trainerEmail.isEmpty() ? "Not available" : trainerEmail));

                        txtTrainerExperience.setText("Experience: " + (experience != null ? experience : "Not available"));
                        txtTrainerAchievements.setText("Achievements: " + (achievements != null ? achievements : "Not available"));

                        // 🔥 Load profile image
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
                        txtTrainerName.setText("Trainer");
                        txtTrainerPhone.setText("Phone: Not available");
                        txtTrainerEmail.setText("Email: Not available");
                        txtTrainerExperience.setText("Experience: Not available");
                        txtTrainerAchievements.setText("Achievements: Not available");
                        imgTrainerProfile.setImageResource(R.drawable.profile);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load trainer profile", Toast.LENGTH_SHORT).show()
                );
    }

    private void animateButton(View view) {
        view.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .setDuration(90)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(90)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start())
                .start();
    }

    private void openDialPad() {
        if (trainerPhone.isEmpty()) {
            Toast.makeText(requireContext(), "Trainer phone not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + trainerPhone));
        startActivity(intent);
    }

    private void openSmsApp() {
        if (trainerPhone.isEmpty()) {
            Toast.makeText(requireContext(), "Trainer phone not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + trainerPhone));
        intent.putExtra("sms_body", "Hello " + trainerName + ", I need more details about my training session.");
        startActivity(intent);
    }

    private void openEmailApp() {
        if (trainerEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Trainer email not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + trainerEmail));
        intent.putExtra(Intent.EXTRA_SUBJECT, "TrainerLink Inquiry");
        intent.putExtra(Intent.EXTRA_TEXT, "Hello " + trainerName + ", I would like to ask about my booking.");
        startActivity(intent);
    }

    private void openWhatsApp() {
        if (trainerWhatsapp.isEmpty()) {
            Toast.makeText(requireContext(), "Trainer WhatsApp not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String url = "https://wa.me/" + trainerWhatsapp.replace("+", "")
                    + "?text=" + Uri.encode("Hello " + trainerName + ", I need help with my booking.");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
        }
    }
}
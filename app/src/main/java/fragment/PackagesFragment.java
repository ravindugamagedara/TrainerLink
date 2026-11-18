package fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainerlink.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import adapter.packageAdapter;
import adapter.packageAdapter;
import module.Package;
import android.content.Intent;
import com.example.trainerlink.activity.packageDetailsActivity;

public class PackagesFragment extends Fragment {

    private RecyclerView rv;
    private packageAdapter adapter;
    private final List<Package> packages = new ArrayList<>();

    public PackagesFragment() {
        super(R.layout.fragment_packages);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rvPackages);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new packageAdapter(packages, pkg -> {

            Intent i = new Intent(getContext(), packageDetailsActivity.class);
            i.putExtra("packageId", pkg.id);
            i.putExtra("title", pkg.title);
            i.putExtra("type", pkg.type);
            i.putExtra("durationWeeks", pkg.durationWeeks);
            i.putExtra("price", pkg.price);
            i.putExtra("description", pkg.description);

            startActivity(i);

        });
        rv.setAdapter(adapter);

        loadPackages();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadPackages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("packages")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(query -> {
                    packages.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Package p = doc.toObject(Package.class);
                        if (p != null) {
                            p.id = doc.getId();
                            packages.add(p);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (packages.isEmpty()) {
                        Toast.makeText(getContext(), "No packages found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
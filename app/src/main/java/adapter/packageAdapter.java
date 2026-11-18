package adapter;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainerlink.R;

import java.util.List;

import module.Package;

public class packageAdapter extends RecyclerView.Adapter<packageAdapter.VH> {

    public interface OnPackageClick {
        void onClick(Package pkg);
    }

    private final List<Package> list;
    private final OnPackageClick listener;

    public packageAdapter(List<Package> list, OnPackageClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Package p = list.get(position);

        h.tvTitle.setText(p.title);
        h.tvTypeDuration.setText(p.type + " • " + p.durationWeeks + " weeks");
        h.tvPrice.setText("LKR " + p.price);

        h.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTypeDuration, tvPrice;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTypeDuration = itemView.findViewById(R.id.tvTypeDuration);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}

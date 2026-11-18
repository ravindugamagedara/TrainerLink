package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainerlink.R;

import java.util.List;

import module.HomeSliderItem;

public class HomeSliderAdapter extends RecyclerView.Adapter<HomeSliderAdapter.SliderViewHolder> {

    private final List<HomeSliderItem> sliderItems;

    public HomeSliderAdapter(List<HomeSliderItem> sliderItems) {
        this.sliderItems = sliderItems;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        HomeSliderItem item = sliderItems.get(position);

        holder.imgSlider.setImageResource(item.getImageResId());
        holder.txtSliderTitle.setText(item.getTitle());
        holder.txtSliderQuote.setText(item.getQuote());
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {

        ImageView imgSlider;
        TextView txtSliderTitle, txtSliderQuote;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);

            imgSlider = itemView.findViewById(R.id.imgSlider);
            txtSliderTitle = itemView.findViewById(R.id.txtSliderTitle);
            txtSliderQuote = itemView.findViewById(R.id.txtSliderQuote);
        }
    }
}

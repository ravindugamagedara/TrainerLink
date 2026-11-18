package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trainerlink.R;

import java.util.List;

import module.Booking;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    private final List<Booking> bookingList;
    private final OnBookingClickListener listener;

    public BookingAdapter(List<Booking> bookingList, OnBookingClickListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.tvBookingTitle.setText(booking.packageTitle);
        holder.tvBookingType.setText(booking.type);
        holder.tvBookingPrice.setText("LKR " + booking.price);
        holder.tvBookingDate.setText(booking.date);
        holder.tvBookingTime.setText(booking.time);
        holder.tvPaymentStatus.setText(booking.paymentStatus);
        holder.tvBookingStatus.setText(booking.status);

        holder.itemView.setOnClickListener(v -> listener.onBookingClick(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingTitle, tvBookingType, tvBookingPrice, tvBookingDate, tvBookingTime,
                tvPaymentStatus, tvBookingStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBookingTitle = itemView.findViewById(R.id.tvBookingTitle);
            tvBookingType = itemView.findViewById(R.id.tvBookingType);
            tvBookingPrice = itemView.findViewById(R.id.tvBookingPrice);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvBookingTime = itemView.findViewById(R.id.tvBookingTime);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
        }
    }
}
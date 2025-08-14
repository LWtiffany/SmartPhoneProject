package hku.cs.hkutopia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationListActivity extends AppCompatActivity {

    private RecyclerView rvReservations;
    private TextView tvNoReservations;
    private ImageButton btnBack;
    
    private ReservationManager reservationManager;
    private List<Reservation> reservations;
    private ReservationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);
        
        reservationManager = ReservationManager.getInstance(this);
        
        initializeViews();
        setupBackButton();
        setupRecyclerView();
        loadReservations();
    }
    
    private void initializeViews() {
        rvReservations = findViewById(R.id.rvReservations);
        tvNoReservations = findViewById(R.id.tvNoReservations);
        btnBack = findViewById(R.id.btnBack);
    }
    
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
    
    private void setupRecyclerView() {
        reservations = new ArrayList<>();
        adapter = new ReservationAdapter(reservations);
        rvReservations.setLayoutManager(new LinearLayoutManager(this));
        rvReservations.setAdapter(adapter);
    }
    
    private void loadReservations() {
        reservations.clear();
        reservations.addAll(reservationManager.getReservations());
        
        if (reservations.isEmpty()) {
            tvNoReservations.setVisibility(View.VISIBLE);
            rvReservations.setVisibility(View.GONE);
        } else {
            tvNoReservations.setVisibility(View.GONE);
            rvReservations.setVisibility(View.VISIBLE);
            
            // Sort reservations by date
            sortReservationsByDate();
            
            adapter.notifyDataSetChanged();
        }
    }
    
    private void sortReservationsByDate() {
        reservations.sort((r1, r2) -> {
            Date date1 = r1.getDate();
            Date date2 = r2.getDate();
            
            if (date1 != null && date2 != null) {
                return date1.compareTo(date2);
            }
            
            return 0;
        });
    }
    
    /**
     * Adapter for reservation items
     */
    private class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {
        
        private List<Reservation> reservationList;
        
        public ReservationAdapter(List<Reservation> reservationList) {
            this.reservationList = reservationList;
        }
        
        @NonNull
        @Override
        public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reservation, parent, false);
            return new ReservationViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
            Reservation reservation = reservationList.get(position);
            
            holder.tvDate.setText(formatDate(reservation.getDate()));
            holder.tvTime.setText(reservation.getTimeSlot());
            
            holder.btnCancel.setOnClickListener(v -> {
                // Confirm and cancel reservation
                reservationManager.deleteReservation(reservation.getId());
                
                // Refresh the list
                reservationList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, reservationList.size() - position);
                
                // Show empty state if needed
                if (reservationList.isEmpty()) {
                    tvNoReservations.setVisibility(View.VISIBLE);
                    rvReservations.setVisibility(View.GONE);
                }
                
                Toast.makeText(ReservationListActivity.this, 
                        "Reservation cancelled", Toast.LENGTH_SHORT).show();
            });
        }
        
        @Override
        public int getItemCount() {
            return reservationList.size();
        }
        
        private String formatDate(Date date) {
            if (date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE);
                return outputFormat.format(date);
            }
            return "";
        }
        
        class ReservationViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvTime;
            Button btnCancel;
            
            public ReservationViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvReservationDate);
                tvTime = itemView.findViewById(R.id.tvReservationTime);
                btnCancel = itemView.findViewById(R.id.btnCancel);
            }
        }
    }
} 
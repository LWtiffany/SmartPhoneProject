package hku.cs.hkutopia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import hku.cs.hkutopia.utils.UserSessionManager;

public class ReservationActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Button btnTimeSlot1, btnTimeSlot2, btnTimeSlot3;
    private TextView tvRemainingSlots1, tvRemainingSlots2, tvRemainingSlots3;
    private Button btnSubmitReservation;
    private String selectedDate = "";
    private String selectedTimeSlot = "";
    private Button lastSelectedTimeSlot = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_reservation);
        
        // Initialize ApiClient with context
        ApiClient.getInstance().init(this);
        
        initializeViews();
        setupCalendarView();
        setupTimeSlotButtons();
        setupSubmitButton();
        setupBackButton();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Update UI based on login status
        boolean isLoggedIn = UserSessionManager.getInstance(this).isLoggedIn();
        if (!isLoggedIn) {
            btnSubmitReservation.setText(getString(R.string.please_login_first));
        } else {
            btnSubmitReservation.setText(getString(R.string.submit_reservation));
        }
    }
    
    private void initializeViews() {
        calendarView = findViewById(R.id.calendarView);
        btnTimeSlot1 = findViewById(R.id.btnTimeSlot1);
        btnTimeSlot2 = findViewById(R.id.btnTimeSlot2);
        btnTimeSlot3 = findViewById(R.id.btnTimeSlot3);
        tvRemainingSlots1 = findViewById(R.id.tvRemainingSlots1);
        tvRemainingSlots2 = findViewById(R.id.tvRemainingSlots2);
        tvRemainingSlots3 = findViewById(R.id.tvRemainingSlots3);
        btnSubmitReservation = findViewById(R.id.btnSubmitReservation);
    }
    
    private void setupCalendarView() {
        // Set minimum date to today (past dates are automatically greyed out)
        calendarView.setMinDate(System.currentTimeMillis() - 1000);
        
        // Set maximum date to 14 days from now (instead of 3 months)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_YEAR, 14);
        calendarView.setMaxDate(maxDate.getTimeInMillis());
        
        // Set the initial date as today
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = dateFormat.format(today);
        
        // Load initial remaining slots
        loadRemainingSlots(selectedDate);
        
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Month is 0-based, so add 1
                selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                
                // Reset time slot selection when date changes
                resetTimeSlotSelection();
                
                // Load remaining slots for the selected date
                loadRemainingSlots(selectedDate);
                
                // Check if selected date is weekend 
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    Toast.makeText(ReservationActivity.this, 
                        getString(R.string.weekend_busy_message), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    /**
     * Loads the remaining slots for each time slot for the given date
     */
    private void loadRemainingSlots(String date) {
        // Reset all slot texts to loading
        tvRemainingSlots1.setText(R.string.loading_slots);
        tvRemainingSlots2.setText(R.string.loading_slots);
        tvRemainingSlots3.setText(R.string.loading_slots);
        
        // Load slot A (Morning)
        loadSlotRemaining(date, "A", tvRemainingSlots1, btnTimeSlot1);
        
        // Load slot B (Afternoon)
        loadSlotRemaining(date, "B", tvRemainingSlots2, btnTimeSlot2);
        
        // Load slot C (Evening)
        loadSlotRemaining(date, "C", tvRemainingSlots3, btnTimeSlot3);
    }
    
    /**
     * Loads the remaining slots for a specific time slot
     */
    private void loadSlotRemaining(String date, String timeSlot, TextView tvRemainingSlots, Button btnTimeSlot) {
        ApiClient.getInstance().getRemainingSlots(date, timeSlot, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        int remaining = jsonResponse.optInt("remaining", 0);
                        
                        if (remaining > 0) {
                            tvRemainingSlots.setText(getString(R.string.remaining_slots, remaining));
                            btnTimeSlot.setEnabled(true);
                        } else {
                            tvRemainingSlots.setText(R.string.no_slots_available);
                            btnTimeSlot.setEnabled(false);
                            btnTimeSlot.setAlpha(0.5f);
                        }
                    } catch (JSONException e) {
                        Log.e("ReservationActivity", "Error parsing slots JSON", e);
                        tvRemainingSlots.setText("");
                    }
                });
            }
            
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    tvRemainingSlots.setText("");
                    Log.e("ReservationActivity", "Error fetching slots: " + error);
                });
            }
        });
    }
    
    private void setupTimeSlotButtons() {
        View.OnClickListener timeSlotClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset previously selected button
                if (lastSelectedTimeSlot != null) {
                    lastSelectedTimeSlot.setBackgroundTintList(getColorStateList(R.color.hku_dark_beige));
                    lastSelectedTimeSlot.setTextColor(getColorStateList(R.color.text_secondary));
                }
                
                // Update selected time slot
                Button clickedButton = (Button) view;
                selectedTimeSlot = clickedButton.getText().toString();
                
                // Highlight the selected button
                clickedButton.setBackgroundTintList(getColorStateList(R.color.hku_brown));
                clickedButton.setTextColor(getColorStateList(R.color.white));
                lastSelectedTimeSlot = clickedButton;
            }
        };
        
        btnTimeSlot1.setOnClickListener(timeSlotClickListener);
        btnTimeSlot2.setOnClickListener(timeSlotClickListener);
        btnTimeSlot3.setOnClickListener(timeSlotClickListener);
    }
    
    private void resetTimeSlotSelection() {
        selectedTimeSlot = "";
        if (lastSelectedTimeSlot != null) {
            lastSelectedTimeSlot.setBackgroundTintList(getColorStateList(R.color.hku_dark_beige));
            lastSelectedTimeSlot.setTextColor(getColorStateList(R.color.text_secondary));
            lastSelectedTimeSlot = null;
        }
        
        // Reset button states
        btnTimeSlot1.setEnabled(true);
        btnTimeSlot2.setEnabled(true);
        btnTimeSlot3.setEnabled(true);
        btnTimeSlot1.setAlpha(1.0f);
        btnTimeSlot2.setAlpha(1.0f);
        btnTimeSlot3.setAlpha(1.0f);
    }
    
    private void setupSubmitButton() {
        // Check login status and update button text accordingly
        boolean isLoggedIn = UserSessionManager.getInstance(this).isLoggedIn();
        
        if (!isLoggedIn) {
            btnSubmitReservation.setText(getString(R.string.please_login_first));
        } else {
            btnSubmitReservation.setText(getString(R.string.submit_reservation));
        }
        
        btnSubmitReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isLoggedIn = UserSessionManager.getInstance(ReservationActivity.this).isLoggedIn();
                
                if (!isLoggedIn) {
                    // Redirect to profile/login page
                    Toast.makeText(ReservationActivity.this, getString(R.string.please_login_to_reserve), Toast.LENGTH_SHORT).show();
                    navigateToProfile();
                    return;
                }
                
                if (validateInputs()) {
                    // Show loading indicator
                    btnSubmitReservation.setEnabled(false);
                    btnSubmitReservation.setText(R.string.submitting);
                    
                    // Save locally first and attempt server upload
                    saveReservation();
                }
            }
        });
    }
    
    private void saveReservation() {
        // Create new reservation object
        Reservation reservation = new Reservation(selectedDate, selectedTimeSlot);
        
        // Get user information
        User user = UserSessionManager.getInstance(this).getUserDetails();
        if (user == null) {
            // This shouldn't happen as we check login status before, but just in case
            showReservationResultDialog(false, getString(R.string.error_user_not_found));
            return;
        }
        
        // Upload to server first
        ApiClient.getInstance().uploadReservation(reservation, user, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(final String response) {
                // Run on UI thread since we're updating UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Save locally only after server succeeds
                        ReservationManager.getInstance(ReservationActivity.this).addReservation(reservation);
                        
                        // Format success message
                        String message = getString(R.string.reservation_success, selectedDate, selectedTimeSlot);
                        
                        // Show success dialog
                        showReservationResultDialog(true, message);
                    }
                });
            }
            
            @Override
            public void onFailure(final String error) {
                // Log error
                Log.e("ReservationActivity", "Server upload failed: " + error);
                
                // Run on UI thread since we're updating UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // If it's a "already booked" error, we should show it to the user
                        showReservationResultDialog(false, error);
                        
                        // Re-enable submit button
                        btnSubmitReservation.setEnabled(true);
                        btnSubmitReservation.setText(R.string.submit_reservation);
                    }
                });
            }
        });
    }
    
    /**
     * Shows a dialog with the result of the reservation attempt
     * 
     * @param success Whether the reservation was successful
     * @param message The message to display
     */
    private void showReservationResultDialog(boolean success, String message) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        
        // Set dialog title and message based on success/failure
        if (success) {
            builder.setTitle(R.string.reservation_successful_title);
        } else {
            builder.setTitle(R.string.reservation_failed_title);
        }
        
        builder.setMessage(message);
        
        // Add OK button
        builder.setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.dismiss();
                if (success) {
                    // Only finish activity on success
                    finish();
                }
            }
        });
        
        // Create and show dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    
    private boolean validateInputs() {
        // Simplified validation - only check date and time slot
        if (selectedDate.isEmpty() || selectedTimeSlot.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_select_date_time), Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    // 添加回退按钮功能
    private void setupBackButton() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
} 
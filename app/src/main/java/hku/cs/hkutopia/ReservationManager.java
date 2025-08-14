package hku.cs.hkutopia;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages user reservations throughout the app
 */
public class ReservationManager {
    private static final String PREF_NAME = "HKUCampusReservations";
    private static final String KEY_RESERVATIONS = "userReservations";
    
    private static ReservationManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private Gson gson;
    
    private ReservationManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }
    
    public static synchronized ReservationManager getInstance(Context context) {
        if (instance == null) {
            instance = new ReservationManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Add a new reservation
     */
    public void addReservation(Reservation reservation) {
        List<Reservation> reservations = getReservations();
        reservations.add(reservation);
        saveReservations(reservations);
    }
    
    /**
     * Get all reservations
     */
    public List<Reservation> getReservations() {
        String json = sharedPreferences.getString(KEY_RESERVATIONS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<Reservation>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * Save reservations to SharedPreferences
     */
    private void saveReservations(List<Reservation> reservations) {
        String json = gson.toJson(reservations);
        editor.putString(KEY_RESERVATIONS, json);
        editor.apply();
    }
    
    /**
     * Delete a reservation by ID
     */
    public boolean deleteReservation(String id) {
        List<Reservation> reservations = getReservations();
        boolean removed = false;
        
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getId().equals(id)) {
                reservations.remove(i);
                removed = true;
                break;
            }
        }
        
        if (removed) {
            saveReservations(reservations);
        }
        
        return removed;
    }
    
    /**
     * Clear all reservations
     */
    public void clearReservations() {
        editor.remove(KEY_RESERVATIONS);
        editor.apply();
    }
} 
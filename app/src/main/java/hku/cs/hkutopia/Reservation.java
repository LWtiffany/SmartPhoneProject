package hku.cs.hkutopia;

import java.util.Date;

/**
 * Model class to store reservation information
 */
public class Reservation {
    private String id;
    private String userId;
    private String facilityId;
    private String facilityName;
    private Date date;
    private String timeSlot;
    private int numberOfVisitors;
    private boolean isConfirmed;

    public Reservation() {
        // Required empty constructor for Firebase
    }

    public Reservation(String date, String timeSlot) {
        this.id = java.util.UUID.randomUUID().toString();
        this.date = null;  // Date will be set from the string in setDate method
        this.timeSlot = timeSlot;
        this.isConfirmed = false;
        this.numberOfVisitors = 1;
        setDateFromString(date);
    }

    public Reservation(String id, String userId, String facilityId, String facilityName, 
                       Date date, String timeSlot, int numberOfVisitors, boolean isConfirmed) {
        this.id = id;
        this.userId = userId;
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.date = date;
        this.timeSlot = timeSlot;
        this.numberOfVisitors = numberOfVisitors;
        this.isConfirmed = isConfirmed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }


    /**
     * Helper method to convert string date to Date object
     */
    private void setDateFromString(String dateStr) {
        try {
            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            this.date = format.parse(dateStr);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
    }
} 
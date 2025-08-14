package hku.cs.hkutopia;

import java.util.ArrayList;
import java.util.List;

public class Facility {
    private String id;
    private String name;
    private String description;
    private String location;
    private String imageUrl;
    private int capacity;
    private List<String> availableTimeSlots;

    public Facility() {
        // Required empty constructor for Firebase
        availableTimeSlots = new ArrayList<>();
    }

    public Facility(String id, String name, String description, String location, 
                   String imageUrl, int capacity, List<String> availableTimeSlots) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.imageUrl = imageUrl;
        this.capacity = capacity;
        this.availableTimeSlots = availableTimeSlots != null ? availableTimeSlots : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getAvailableTimeSlots() {
        return availableTimeSlots;
    }

    public void setAvailableTimeSlots(List<String> availableTimeSlots) {
        this.availableTimeSlots = availableTimeSlots != null ? availableTimeSlots : new ArrayList<>();
    }
} 
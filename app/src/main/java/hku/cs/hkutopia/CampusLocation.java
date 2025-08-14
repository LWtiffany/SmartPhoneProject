package hku.cs.hkutopia;

import com.google.android.gms.maps.model.LatLng;

public class CampusLocation {
    private String name;
    private String chineseName;
    private String category;
    private String subcategory;
    private LatLng position;
    private String description;
    private int iconRes;
    private int markerBgColorRes;
    private int imageRes;
    private String distance;
    private boolean checkedIn;

    public CampusLocation(String name, String chineseName, String category, String subcategory,
                          LatLng position, String description, int iconRes, int markerBgColorRes,
                          int imageRes, String distance) {
        this.name = name;
        this.chineseName = chineseName;
        this.category = category;
        this.subcategory = subcategory;
        this.position = position;
        this.description = description;
        this.iconRes = iconRes;
        this.markerBgColorRes = markerBgColorRes;
        this.imageRes = imageRes;
        this.distance = distance;
        this.checkedIn = false;
    }

    public String getName() {
        return name;
    }

    public String getChineseName() {
        return chineseName;
    }

    public String getCategory() {
        return category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getDescription() {
        return description;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getMarkerBgColorRes() {
        return markerBgColorRes;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getDistance() {
        return distance;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}
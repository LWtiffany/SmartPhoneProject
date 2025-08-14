package hku.cs.hkutopia.model;

public class HistoricalBuilding {
    private String name;
    private String year;
    private int imageResId;

    public HistoricalBuilding(String name, String year, int imageResId) {
        this.name = name;
        this.year = year;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
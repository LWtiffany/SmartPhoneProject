package hku.cs.hkutopia.model;

public class TimelineEvent {
    private String year;
    private String title;
    private String description;

    public TimelineEvent(String year, String title, String description) {
        this.year = year;
        this.title = title;
        this.description = description;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
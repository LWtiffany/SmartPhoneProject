package hku.cs.hkutopia.model;

public class Alumni {
    private String name;
    private String degree;
    private String achievement;
    private int imageResId;

    public Alumni(String name, String degree, String achievement, int imageResId) {
        this.name = name;
        this.degree = degree;
        this.achievement = achievement;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getAchievement() {
        return achievement;
    }

    public void setAchievement(String achievement) {
        this.achievement = achievement;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
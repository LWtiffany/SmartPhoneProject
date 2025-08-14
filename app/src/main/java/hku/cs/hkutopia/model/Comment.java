package hku.cs.hkutopia.model;

import java.io.Serializable;

public class Comment implements Serializable {
    private String author;
    private String content;
    private String date;
    private String location;
    private int likes;

    public Comment() {
    }

    public Comment(String author, String content, String date, String location, int likes) {
        this.author = author;
        this.content = content;
        this.date = date;
        this.location = location;
        this.likes = likes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}

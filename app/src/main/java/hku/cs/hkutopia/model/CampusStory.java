package hku.cs.hkutopia.model;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class CampusStory implements Serializable {
    private int id;
    private String title;
    private String content;
    private String author;
    private String date;
    private String imageUrl; // 保留单图片字段以保持向后兼容
    private List<String> imageUrls; // 新增：多图片字段
    private List<String> tags;
    private int likes;
    private int comments;
    private boolean isLatest;

    public CampusStory() {
        this.imageUrls = new ArrayList<>();
    }

    public CampusStory(int id, String title, String content, String author, String date, String imageUrl, List<String> tags, int likes, int comments, boolean isLatest) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.date = date;
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.likes = likes;
        this.comments = comments;
        this.isLatest = isLatest;

        // 初始化图片列表并添加主图片
        this.imageUrls = new ArrayList<>();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            this.imageUrls.add(imageUrl);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;

        // 同时更新imageUrls列表
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }

        // 如果主图片不在列表中，添加到列表
        if (imageUrl != null && !imageUrl.isEmpty() && !this.imageUrls.contains(imageUrl)) {
            this.imageUrls.add(imageUrl);
        }
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;

        // 更新主图片为第一张图片
        if (imageUrls != null && !imageUrls.isEmpty()) {
            this.imageUrl = imageUrls.get(0);
        }
    }

    public void addImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }

        if (imageUrl != null && !imageUrl.isEmpty() && !this.imageUrls.contains(imageUrl)) {
            this.imageUrls.add(imageUrl);
        }
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public boolean isLatest() {
        return isLatest;
    }

    public void setLatest(boolean latest) {
        isLatest = latest;
    }
}
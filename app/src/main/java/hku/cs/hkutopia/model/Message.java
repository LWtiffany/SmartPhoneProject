package hku.cs.hkutopia.model;

import java.util.Date;

public class Message {
    public static final int TYPE_USER = 0;
    public static final int TYPE_ADMIN = 1;
    public static final int TYPE_SYSTEM = 2;

    private String content;
    private String sender;
    private Date timestamp;
    private int type;
    private boolean isLiked;
    private int likeCount;

    public Message() {
        this.timestamp = new Date();
        this.likeCount = 0;
        this.isLiked = false;
    }

    public Message(String content, String sender, int type) {
        this.content = content;
        this.sender = sender;
        this.type = type;
        this.timestamp = new Date();
        this.likeCount = 0;
        this.isLiked = false;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void toggleLike() {
        if (isLiked) {
            likeCount = Math.max(0, likeCount - 1);
        } else {
            likeCount++;
        }
        isLiked = !isLiked;
    }
}
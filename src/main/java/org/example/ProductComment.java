package org.example;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductComment implements Serializable {
    private int id;
    private List<Integer> associatedProductIds;
    private String commentType;
    private int authorId;
    private String status;
    private String authorName;
    private String content;
    private Integer rating;
    private Date date;
    private Date lastModified;
    private int likesCount;
    private int dislikesCount;
    private int repliesCount;
    private List<ProductCommentReply> replies;

    public ProductComment(int id, String commentType, int authorId, String status, String authorName, String content, Integer rating, Date date, Date lastModified) {
        this.id = id;
        this.commentType = commentType;
        this.authorId = authorId;
        this.status = status;
        this.authorName = authorName;
        this.content = content;
        this.rating = rating;
        this.date = date;
        this.lastModified = lastModified;
        this.associatedProductIds = new ArrayList<>();
        this.likesCount = 0;
        this.dislikesCount = 0;
        this.repliesCount = 0;
        this.replies = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public List<Integer> getAssociatedProductIds() { return associatedProductIds; }
    public void setAssociatedProductIds(List<Integer> ids) { this.associatedProductIds = ids; }
    public String getCommentType() { return commentType; }
    public void setCommentType(String commentType) { this.commentType = commentType; }
    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public Date getLastModified() { return lastModified; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public int getDislikesCount() { return dislikesCount; }
    public void setDislikesCount(int dislikesCount) { this.dislikesCount = dislikesCount; }
    public int getRepliesCount() { return repliesCount; }
    public void setRepliesCount(int repliesCount) { this.repliesCount = repliesCount; }
    public List<ProductCommentReply> getReplies() { return replies; }
    public void setReplies(List<ProductCommentReply> replies) { this.replies = replies; }

    public String getFormattedDate() {
        if (date == null) return "N/A";
        return new SimpleDateFormat("dd MMM.yyyy, HH:mm").format(date);
    }

    public int getKarma() {
        return this.likesCount - this.dislikesCount;
    }
}
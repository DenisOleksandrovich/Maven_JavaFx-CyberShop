package org.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Reply {
    private int replyId;
    private int parentReviewId;
    private Integer parentReplyId;
    private int userId;
    private String creatorStatus;
    private String authorName;
    private String replyContent;
    private Date createdAt;
    private int likesCount;
    private int dislikesCount;
    private List<Reply> nestedReplies;

    public Reply(int replyId, int parentReviewId, Integer parentReplyId, int userId, String creatorStatus, String authorName, String replyContent, Date createdAt) {
        this.replyId = replyId;
        this.parentReviewId = parentReviewId;
        this.parentReplyId = parentReplyId;
        this.userId = userId;
        this.creatorStatus = creatorStatus;
        this.authorName = authorName;
        this.replyContent = replyContent;
        this.createdAt = createdAt;
        this.likesCount = 0;
        this.dislikesCount = 0;
        this.nestedReplies = new ArrayList<>();
    }

    public int getReplyId() { return replyId; }
    public void setReplyId(int replyId) { this.replyId = replyId; }
    public int getParentReviewId() { return parentReviewId; }
    public void setParentReviewId(int parentReviewId) { this.parentReviewId = parentReviewId; }
    public Integer getParentReplyId() { return parentReplyId; }
    public void setParentReplyId(Integer parentReplyId) { this.parentReplyId = parentReplyId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCreatorStatus() { return creatorStatus; }
    public void setCreatorStatus(String creatorStatus) { this.creatorStatus = creatorStatus; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String replyContent) { this.replyContent = replyContent; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public int getDislikesCount() { return dislikesCount; }
    public void setDislikesCount(int dislikesCount) { this.dislikesCount = dislikesCount; }
    public List<Reply> getNestedReplies() { return nestedReplies; }
    public void setNestedReplies(List<Reply> nestedReplies) { this.nestedReplies = nestedReplies; }

    public String getFormattedCreationDate() {
        if (createdAt == null) return "N/A";
        return new SimpleDateFormat("dd MMM. yyyy, HH:mm").format(createdAt);
    }
    public int getKarma() {
        return this.likesCount - this.dislikesCount;
    }
}
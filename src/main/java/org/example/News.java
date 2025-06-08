import javafx.application.HostServices;

public class News {
    private int newsId;
    private String title;
    private String content;
    private String imageUrl;
    private String link;
    private String date;
    private HostServices hostServices;

    public News(int newsId, String title, String content, String imageUrl, String link, String date, HostServices hostServices) {
        this.newsId = newsId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.link = link;
        this.date = date;
        this.hostServices = hostServices;
    }

    // Getters and setters
    public int getNewsId() { return newsId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public String getLink() { return link; }
    public String getDate() { return date; }
    public HostServices getHostServices() { return hostServices; }
}
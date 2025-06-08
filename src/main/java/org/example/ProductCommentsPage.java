package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ProductCommentsPage extends Application {

    private final int currentProductId;
    private BorderPane root;
    private ListView<ProductComment> commentsListView;
    private FirstConnectionToDataBase connectionToDataBase;
    private AlertService alertService;
    private SessionManager sessionManager;
    private Stage primaryStage;
    private ToggleGroup filterGroup;

    private final Random random = new Random();
    private final Color[] avatarColors = {
            Color.valueOf("#4A90E2"), Color.valueOf("#50E3C2"), Color.valueOf("#F5A623"),
            Color.valueOf("#BD10E0"), Color.valueOf("#9013FE"), Color.valueOf("#D0021B")
    };

    public ProductCommentsPage(int productId) {
        this.currentProductId = productId;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.alertService = new AlertServiceImpl();
        this.sessionManager = SessionManager.getInstance();

        root = new BorderPane();
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, 900, 700);

        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            Platform.runLater(() -> alertService.showErrorAlert("DB connection error: " + e.getMessage()));
            return;
        }

        VBox header = createHeader();
        root.setTop(header);
        createCommentsPageUI();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Product Comments");
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(12, 15, 12, 15));
        header.setStyle("-fx-background-color: black; -fx-border-width: 0 0 1 0; -fx-border-color: #1F1F1F;");

        this.filterGroup = new ToggleGroup();

        ToggleButton allButton = new ToggleButton("All");
        allButton.setToggleGroup(filterGroup);
        allButton.setUserData("all");

        ToggleButton reviewsButton = new ToggleButton("Reviews");
        reviewsButton.setToggleGroup(filterGroup);
        reviewsButton.setUserData("review");

        ToggleButton questionsButton = new ToggleButton("Questions");
        questionsButton.setToggleGroup(filterGroup);
        questionsButton.setUserData("question");

        allButton.setSelected(true);

        ButtonStyle.styleToggleGroupButtons(this.filterGroup);

        filterGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                updateDisplayedComments();
            } else {
                filterGroup.selectToggle(oldToggle);
            }
        });

        HBox filterBox = new HBox(10, allButton, reviewsButton, questionsButton);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Button addCommentBtn = ButtonStyle.createStyledButton("Add Comment");
        addCommentBtn.setOnAction(e -> {
            ProductCommentCreateWindow create_window = new ProductCommentCreateWindow(connectionToDataBase, alertService, this.primaryStage, this);
            create_window.chooseCommentTypeAndCreate(this.currentProductId);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox headerControls = new HBox(filterBox, spacer, addCommentBtn);
        headerControls.setAlignment(Pos.CENTER);
        header.getChildren().add(headerControls);
        return header;
    }

    public void createCommentsPageUI() {
        commentsListView = new ListView<>();
        commentsListView.setStyle("-fx-background-color: black; -fx-control-inner-background: black; -fx-border-width: 0;");
        commentsListView.setCellFactory(param -> new ProductCommentListCell(this));

        VBox contentArea = new VBox(10, commentsListView);
        contentArea.setStyle("-fx-background-color: black;");
        contentArea.setPadding(new Insets(10));
        VBox.setVgrow(commentsListView, Priority.ALWAYS);
        root.setCenter(contentArea);
        updateDisplayedComments();
    }

    public List<ProductComment> getProductComments(String filterType) {
        List<ProductComment> comments = new ArrayList<>();
        String baseQuery = "SELECT c.*, " +
                "(SELECT COUNT(*) FROM product_comment_actions a WHERE a.comment_id = c.comment_id AND a.action_type = 'like') as likes_count, " +
                "(SELECT COUNT(*) FROM product_comment_actions a WHERE a.comment_id = c.comment_id AND a.action_type = 'dislike') as dislikes_count, " +
                "(SELECT COUNT(*) FROM product_comment_replies r WHERE r.parent_comment_id = c.comment_id) as replies_count " +
                "FROM product_comments c JOIN comment_product_association a ON c.comment_id = a.comment_id " +
                "WHERE a.product_id = ?";

        if (!"all".equalsIgnoreCase(filterType)) {
            baseQuery += " AND c.comment_type = ?";
        }
        baseQuery += " GROUP BY c.comment_id ORDER BY c.created_at DESC";

        try (Connection conn = connectionToDataBase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(baseQuery)) {

            pstmt.setInt(1, this.currentProductId);
            if (!"all".equalsIgnoreCase(filterType)) {
                pstmt.setString(2, filterType);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("comment_id");
                    String authorName = getAuthorNameById(conn, rs.getInt("creator_id"), rs.getString("creator_status"));

                    Integer rating = (Integer) rs.getObject("rating");
                    if (rs.wasNull()) {
                        rating = null;
                    }

                    ProductComment comment = new ProductComment(id, rs.getString("comment_type"), rs.getInt("creator_id"), rs.getString("creator_status"), authorName, rs.getString("comment_content"), rating, new Date(rs.getTimestamp("created_at").getTime()), new Date(rs.getTimestamp("last_modified_at").getTime()));
                    comment.setLikesCount(rs.getInt("likes_count"));
                    comment.setDislikesCount(rs.getInt("dislikes_count"));
                    comment.setRepliesCount(rs.getInt("replies_count"));
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> alertService.showErrorAlert("Error fetching comments: " + e.getMessage()));
        }
        return comments;
    }

    public String getAuthorNameById(Connection conn, int authorId, String status) {
        String table = "Manager".equalsIgnoreCase(status) ? "managers" : "users";
        String nameCol = "Manager".equalsIgnoreCase(status) ? "manager_name" : "user_name";
        String idCol = "Manager".equalsIgnoreCase(status) ? "id_managers" : "id";
        String sql = "SELECT " + nameCol + " FROM " + table + " WHERE " + idCol + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, authorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString(nameCol);
            }
        } catch (SQLException e) {
            System.err.println("Error in getAuthorNameById for " + authorId + "/" + status + ": " + e.getMessage());
        }
        return "Unknown";
    }

    Node createAuthorAvatar(String authorName) {
        Circle avatar = new Circle(16);
        Text letter = new Text(authorName != null && !authorName.isEmpty() ? authorName.substring(0, 1).toUpperCase() : "?");
        letter.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        letter.setFill(Color.WHITE);
        if(authorName != null) {
            avatar.setFill(avatarColors[Math.abs(authorName.hashCode()) % avatarColors.length]);
        } else {
            avatar.setFill(Color.valueOf("#333333"));
        }
        return new StackPane(avatar, letter);
    }

    public int getComplaintCount(int commentId) {
        String sql = "SELECT COUNT(*) FROM product_comment_complaints WHERE comment_id = ?";
        try (Connection conn = connectionToDataBase.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateDisplayedComments() {
        Toggle selected = filterGroup.getSelectedToggle();
        if (selected == null) return;
        String filterType = (String) selected.getUserData();
        new Thread(() -> {
            List<ProductComment> fetched = getProductComments(filterType);
            Platform.runLater(() -> commentsListView.getItems().setAll(fetched));
        }).start();
    }
}
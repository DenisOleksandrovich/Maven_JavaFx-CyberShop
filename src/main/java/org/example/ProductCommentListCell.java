package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ProductCommentListCell extends ListCell<ProductComment> {

    private final ProductCommentsPage page;
    private final SessionManager sessionManager;
    private final AlertService alertService;
    private final FirstConnectionToDataBase dbConnection;

    public ProductCommentListCell(ProductCommentsPage page) {
        this.page = page;
        this.sessionManager = SessionManager.getInstance();
        this.alertService = new AlertServiceImpl();
        try {
            this.dbConnection = FirstConnectionToDataBase.getInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    private void showLoginAlert() {
        alertService.showInfoAlert("Info Alert", "Authentication Required", "Please log in or register to perform this action.");
    }

    private HBox createRatingDisplay(int rating) {
        HBox ratingBox = new HBox(2);
        for (int i = 1; i <= 5; i++) {
            Text star = new Text(i <= rating ? "★" : "☆");
            star.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            star.setFill(Color.valueOf("#FFD700"));
            ratingBox.getChildren().add(star);
        }
        return ratingBox;
    }

    @Override
    protected void updateItem(ProductComment comment, boolean empty) {
        super.updateItem(comment, empty);
        if (empty || comment == null) {
            setText(null);
            setGraphic(null);
            setStyle("-fx-background-color: black;");
        } else {
            VBox card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: #0D0D0D; -fx-border-color: #1A1A1A; -fx-border-width: 1px; -fx-border-radius: 8; -fx-background-radius: 8;");

            HBox authorLine = new HBox(10);
            authorLine.setAlignment(Pos.CENTER_LEFT);
            Node avatar = page.createAuthorAvatar(comment.getAuthorName());
            Text authorNode = new Text(comment.getAuthorName() + " (" + comment.getStatus() + ")");
            authorNode.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            authorNode.setFill(Color.valueOf("#E0E0E0"));

            String typeText = comment.getCommentType().substring(0, 1).toUpperCase() + comment.getCommentType().substring(1);
            Label typeLabel = new Label(typeText);
            typeLabel.setStyle("-fx-background-color: #333; -fx-text-fill: #AFAFAF; -fx-padding: 2 5; -fx-background-radius: 5;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Text dateNode = new Text(comment.getFormattedDate());
            dateNode.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            dateNode.setFill(Color.valueOf("#888888"));
            authorLine.getChildren().addAll(avatar, authorNode, typeLabel, spacer, dateNode);

            VBox headerBox = new VBox(5, authorLine);

            if ("review".equalsIgnoreCase(comment.getCommentType())) {
                Integer rating = comment.getRating();
                if (rating != null) {
                    HBox ratingDisplay = createRatingDisplay(rating);
                    headerBox.getChildren().add(ratingDisplay);
                }
            }

            Text karmaNode = new Text("Karma: " + comment.getKarma());
            karmaNode.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            karmaNode.setFill(comment.getKarma() >= 0 ? Color.valueOf("#6DD400") : Color.valueOf("#D0021B"));

            Text contentNode = new Text(comment.getContent());
            contentNode.setFill(Color.valueOf("#C8C8C8"));
            contentNode.setFont(Font.font("Arial", 14));
            contentNode.setWrappingWidth(780);

            HBox actions = new HBox(12);
            actions.setAlignment(Pos.CENTER_LEFT);

            Button likeBtn = ButtonStyle.createStyledButton("👍 (" + comment.getLikesCount() + ")");
            likeBtn.setOnAction(e -> {
                if (sessionManager.isAuthenticated()) {
                    // Logic for like
                } else {
                    showLoginAlert();
                }
            });

            Button dislikeBtn = ButtonStyle.createStyledButton("👎 (" + comment.getDislikesCount() + ")");
            dislikeBtn.setOnAction(e -> {
                if (sessionManager.isAuthenticated()) {
                    // Logic for dislike
                } else {
                    showLoginAlert();
                }
            });

            Button replyBtn = ButtonStyle.expandPaneStyledButton("Reply");
            replyBtn.setOnAction(e -> {
                if (sessionManager.isAuthenticated()) {
                    // Logic for reply
                } else {
                    showLoginAlert();
                }
            });

            actions.getChildren().addAll(likeBtn, dislikeBtn, replyBtn);

            HBox adminActions = new HBox(10);
            adminActions.setAlignment(Pos.CENTER_RIGHT);

            boolean isOwner = false;
            if (sessionManager.isAuthenticated()) {
                int currentUserId = sessionManager.isClientEnter() ? sessionManager.getCurrentUserId() : sessionManager.getCurrentEmployeeId();
                isOwner = (currentUserId != -1 && currentUserId == comment.getAuthorId());
                String managerStatus = sessionManager.isManagerEnter() ? sessionManager.getEmployeeStatusByName(sessionManager.getCurrentManagerName()) : "";
                boolean isSuperAdmin = "super_admin".equalsIgnoreCase(managerStatus);

                if (isOwner || isSuperAdmin) {
                    Button editBtn = ButtonStyle.expandPaneStyledButton("Edit");
                    editBtn.setOnAction(e -> new ProductCommentEditWindow().editComment(comment, page, alertService, dbConnection));

                    Button deleteBtn = ButtonStyle.expandPaneStyledButton("Delete");
                    deleteBtn.setOnAction(e -> new ProductCommentDeleteHandler().deleteComment(comment, page, alertService, dbConnection));

                    adminActions.getChildren().addAll(editBtn, deleteBtn);
                }
            }

            if (isOwner) {
                int complaintCount = page.getComplaintCount(comment.getId());
                Label complaintsLabel = new Label("Complaints: " + complaintCount);
                complaintsLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 12px;");
                adminActions.getChildren().add(complaintsLabel);
            } else {
                Button complainBtn = ButtonStyle.expandPaneStyledButton("Complain");
                complainBtn.setOnAction(e -> {
                    if (sessionManager.isAuthenticated()) {
                        new ProductCommentComplaintWindow().complainAboutComment(comment, alertService, dbConnection);
                    } else {
                        showLoginAlert();
                    }
                });
                adminActions.getChildren().add(complainBtn);
            }

            HBox bottomBar = new HBox();
            Region bSpacer = new Region();
            HBox.setHgrow(bSpacer, Priority.ALWAYS);
            bottomBar.getChildren().addAll(actions, bSpacer, adminActions);

            card.getChildren().addAll(headerBox, karmaNode, contentNode, new Separator(), bottomBar);
            setGraphic(card);
            setStyle("-fx-background-color: black; -fx-padding: 10 5 10 5;");
        }
    }
}
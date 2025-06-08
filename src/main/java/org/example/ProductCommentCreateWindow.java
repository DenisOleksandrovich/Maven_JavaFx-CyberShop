package org.example;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProductCommentCreateWindow {

    private final SessionManager sessionManager;
    private final FirstConnectionToDataBase connectionToDataBase;
    private final AlertService alertService;
    private final ProductCommentsPage commentsPage;
    private final Stage ownerStage;
    private final List<TextField> productIdFields = new ArrayList<>();
    private final String fieldStyle = "-fx-background-color: black; -fx-text-fill: white; -fx-prompt-text-fill: grey; -fx-border-color: white; -fx-border-width: 1; -fx-border-radius: 5;";

    public ProductCommentCreateWindow(FirstConnectionToDataBase connectionToDataBase, AlertService alertService, Stage ownerStage, ProductCommentsPage commentsPage) {
        this.sessionManager = SessionManager.getInstance();
        this.connectionToDataBase = connectionToDataBase;
        this.alertService = alertService;
        this.ownerStage = ownerStage;
        this.commentsPage = commentsPage;
    }

    public void chooseCommentTypeAndCreate(int initialProductId) {
        if (!sessionManager.isAuthenticated()) {
            alertService.showErrorAlert("Log in to add a comment.");
            return;
        }

        List<String> choices = Arrays.asList("Review", "Question");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Review", choices);
        dialog.setTitle("Choose Submission Type");
        dialog.setHeaderText("What would you like to submit?");
        dialog.setContentText("Choose your type:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(type -> showCreateCommentDialog(type, initialProductId));
    }

    private void showCreateCommentDialog(String commentType, int initialProductId) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(ownerStage);
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: black;");

        productIdFields.clear();

        Label productIdsLabel = new Label("Associated Products:");
        productIdsLabel.setTextFill(Color.WHITE);

        VBox productIdsContainer = new VBox(5);
        addProductIdField(productIdsContainer, initialProductId, true);

        Label contentLabel = new Label("Your " + commentType + ":");
        contentLabel.setTextFill(Color.WHITE);
        TextArea contentTextArea = new TextArea();
        contentTextArea.setPromptText("Share your thoughts about this product...");
        contentTextArea.setStyle(fieldStyle);
        contentTextArea.setWrapText(true);

        layout.getChildren().addAll(productIdsLabel, productIdsContainer, contentLabel, contentTextArea);

        AtomicInteger currentRating = new AtomicInteger(0);
        if ("Review".equalsIgnoreCase(commentType)) {
            Label ratingLabel = new Label("Your Rating:");
            ratingLabel.setTextFill(Color.WHITE);
            HBox ratingBox = createRatingBox(currentRating);
            layout.getChildren().addAll(ratingLabel, ratingBox);
        }

        Button addButton = ButtonStyle.expandPaneStyledButton("Add " + commentType);
        addButton.setOnAction(e -> {
            List<Integer> productIds = new ArrayList<>();
            for (TextField field : productIdFields) {
                String text = field.getText().trim();
                if (!text.isEmpty()) {
                    try {
                        productIds.add(Integer.parseInt(text));
                    } catch (NumberFormatException ex) {
                        alertService.showErrorAlert("Invalid Product ID: '" + text + "'. Please enter numbers only.");
                        return;
                    }
                }
            }

            if (productIds.isEmpty()) {
                alertService.showErrorAlert("Please enter at least one Product ID.");
                return;
            }
            if (contentTextArea.getText().trim().isEmpty()) {
                alertService.showErrorAlert("Content cannot be empty.");
                return;
            }
            if ("Review".equalsIgnoreCase(commentType) && currentRating.get() == 0) {
                alertService.showErrorAlert("Please select a rating for your review.");
                return;
            }

            int creatorId = sessionManager.isManagerEnter() ? sessionManager.getCurrentEmployeeId() : sessionManager.getCurrentUserId();
            String creatorStatus = sessionManager.isManagerEnter() ? "Manager" : "Client";
            Integer ratingValue = "Review".equalsIgnoreCase(commentType) ? currentRating.get() : null;

            if (addCommentToDB(productIds, creatorId, creatorStatus, commentType.toLowerCase(), contentTextArea.getText(), ratingValue)) {
                alertService.showSuccessAlert(commentType + " added.");
                dialogStage.close();
                if (commentsPage != null) {
                    commentsPage.updateDisplayedComments();
                }
            }
        });

        layout.getChildren().add(addButton);
        dialogStage.setScene(new Scene(layout));
        dialogStage.showAndWait();
    }

    private void addProductIdField(VBox container, Integer id, boolean isFirstField) {
        HBox fieldBox = new HBox(5);
        fieldBox.setAlignment(Pos.CENTER_LEFT);

        TextField idField = new TextField();
        idField.setPromptText("Enter Product ID");
        if (id != null) {
            idField.setText(String.valueOf(id));
        }
        HBox.setHgrow(idField, Priority.ALWAYS);

        Button actionButton;
        if (isFirstField) {
            actionButton = ButtonStyle.expandPaneStyledButton("+");
            actionButton.setOnAction(e -> addProductIdField(container, null, false));
        } else {
            actionButton = ButtonStyle.expandPaneStyledButton("-");
            actionButton.setOnAction(e -> {
                container.getChildren().remove(fieldBox);
            });
        }

        fieldBox.getChildren().addAll(idField, actionButton);
        container.getChildren().add(fieldBox);
    }

    private HBox createRatingBox(AtomicInteger rating) {
        HBox box = new HBox(5);
        Button[] stars = new Button[5];
        for (int i = 0; i < 5; i++) {
            stars[i] = new Button("☆");
            stars[i].setStyle("-fx-background-color: transparent; -fx-text-fill: #FFD700; -fx-font-size: 20px; -fx-padding: 0;");
            final int starIndex = i;
            stars[i].setOnAction(e -> {
                rating.set(starIndex + 1);
                for (int j = 0; j < 5; j++) stars[j].setText(j <= starIndex ? "★" : "☆");
            });
        }
        box.getChildren().addAll(stars);
        return box;
    }

    private boolean addCommentToDB(List<Integer> productIds, int authorId, String status, String commentType, String content, Integer rating) {
        Connection connection = null;
        String commentQuery = "INSERT INTO product_comments (creator_id, creator_status, comment_type, comment_content, rating, created_at, last_modified_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String assocQuery = "INSERT INTO comment_product_association (comment_id, product_id) VALUES (?, ?)";

        try {
            connection = connectionToDataBase.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement psComment = connection.prepareStatement(commentQuery, Statement.RETURN_GENERATED_KEYS)) {
                psComment.setInt(1, authorId);
                psComment.setString(2, status);
                psComment.setString(3, commentType);
                psComment.setString(4, content);
                if (rating != null) psComment.setInt(5, rating); else psComment.setNull(5, Types.TINYINT);
                psComment.setTimestamp(6, new Timestamp(new Date().getTime()));
                psComment.setTimestamp(7, new Timestamp(new Date().getTime()));
                psComment.executeUpdate();

                try (ResultSet generatedKeys = psComment.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int commentId = generatedKeys.getInt(1);
                        try (PreparedStatement psAssoc = connection.prepareStatement(assocQuery)) {
                            for (Integer prodId : productIds) {
                                psAssoc.setInt(1, commentId);
                                psAssoc.setInt(2, prodId);
                                psAssoc.addBatch();
                            }
                            psAssoc.executeBatch();
                        }
                    } else {
                        throw new SQLException("Creating comment failed, no ID obtained.");
                    }
                }
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (connection != null) try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            alertService.showErrorAlert("DB error adding comment: " + e.getMessage());
            return false;
        } finally {
            if (connection != null) try { connection.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
}
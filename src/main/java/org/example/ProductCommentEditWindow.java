package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductCommentEditWindow {

    private final List<TextField> productIdFields = new ArrayList<>();
    private final String fieldStyle = "-fx-background-color: black; -fx-text-fill: white; -fx-prompt-text-fill: grey; -fx-border-color: white; -fx-border-width: 1; -fx-border-radius: 5;";

    public void editComment(ProductComment comment, ProductCommentsPage page, AlertService alertService, FirstConnectionToDataBase dbConnection) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(page.getPrimaryStage());
        dialog.setTitle("Edit " + comment.getCommentType());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: black;");

        productIdFields.clear();

        Label productIdsLabel = new Label("Associated Products:");
        productIdsLabel.setStyle("-fx-text-fill: white;");
        VBox productIdsContainer = new VBox(5);

        List<Integer> initialProductIds = fetchAssociatedProductIds(comment.getId(), dbConnection);
        if (initialProductIds.isEmpty()) {
            addProductIdField(productIdsContainer, null, true);
        } else {
            addProductIdField(productIdsContainer, initialProductIds.get(0), true);
            for (int i = 1; i < initialProductIds.size(); i++) {
                addProductIdField(productIdsContainer, initialProductIds.get(i), false);
            }
        }

        Label contentLabel = new Label("Edit your " + comment.getCommentType() + ":");
        contentLabel.setStyle("-fx-text-fill: white;");
        TextArea contentArea = new TextArea(comment.getContent());
        contentArea.setPromptText("Share your thoughts...");
        contentArea.setWrapText(true);
        contentArea.setStyle(fieldStyle);
        layout.getChildren().addAll(productIdsLabel, productIdsContainer, contentLabel, contentArea);

        boolean isReview = "review".equalsIgnoreCase(comment.getCommentType());
        AtomicInteger newRating = new AtomicInteger(0);

        if (isReview) {
            if (comment.getRating() != null) {
                newRating.set(comment.getRating());
            }
            Label ratingLabel = new Label("Edit Your Rating:");
            ratingLabel.setStyle("-fx-text-fill: white;");
            HBox ratingBox = createRatingBox(newRating);
            layout.getChildren().addAll(ratingLabel, ratingBox);
        }

        Button saveButton = ButtonStyle.expandPaneStyledButton("Save Changes");
        saveButton.setOnAction(e -> {
            List<Integer> updatedProductIds = new ArrayList<>();
            for(TextField field : productIdFields) {
                String text = field.getText().trim();
                if (!text.isEmpty()) {
                    try {
                        updatedProductIds.add(Integer.parseInt(text));
                    } catch (NumberFormatException ex) {
                        alertService.showErrorAlert("Invalid Product ID: '" + text + "'. Please enter numbers only.");
                        return;
                    }
                }
            }
            if (updatedProductIds.isEmpty()) {
                alertService.showErrorAlert("Please associate with at least one Product ID.");
                return;
            }
            if(contentArea.getText().trim().isEmpty()){
                alertService.showErrorAlert("Content cannot be empty.");
                return;
            }
            if (isReview && newRating.get() == 0) {
                alertService.showErrorAlert("Please select a rating for your review.");
                return;
            }

            updateCommentInDB(comment.getId(), contentArea.getText(), newRating.get(), updatedProductIds, dbConnection, alertService, isReview);
            dialog.close();
            page.updateDisplayedComments();
        });

        layout.getChildren().add(saveButton);
        dialog.setScene(new Scene(layout));
        dialog.showAndWait();
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
                productIdFields.remove(idField);
            });
        }
        fieldBox.getChildren().addAll(idField, actionButton);
        container.getChildren().add(fieldBox);
        productIdFields.add(idField);
    }

    private List<Integer> fetchAssociatedProductIds(int commentId, FirstConnectionToDataBase db) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT product_id FROM comment_product_association WHERE comment_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                ids.add(rs.getInt("product_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    private void updateCommentInDB(int commentId, String content, int rating, List<Integer> productIds, FirstConnectionToDataBase db, AlertService alert, boolean isReview) {
        Connection conn = null;
        String updateCommentSql = "UPDATE product_comments SET comment_content = ?, rating = ?, last_modified_at = ? WHERE comment_id = ?";
        String deleteAssocSql = "DELETE FROM comment_product_association WHERE comment_id = ?";
        String insertAssocSql = "INSERT INTO comment_product_association (comment_id, product_id) VALUES (?, ?)";

        try {
            conn = db.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psUpdate = conn.prepareStatement(updateCommentSql)) {
                psUpdate.setString(1, content);
                if (isReview) {
                    psUpdate.setInt(2, rating);
                } else {
                    psUpdate.setNull(2, Types.TINYINT);
                }
                psUpdate.setTimestamp(3, new Timestamp(new Date().getTime()));
                psUpdate.setInt(4, commentId);
                psUpdate.executeUpdate();
            }

            try (PreparedStatement psDelete = conn.prepareStatement(deleteAssocSql)) {
                psDelete.setInt(1, commentId);
                psDelete.executeUpdate();
            }

            try (PreparedStatement psInsert = conn.prepareStatement(insertAssocSql)) {
                for (Integer prodId : productIds) {
                    psInsert.setInt(1, commentId);
                    psInsert.setInt(2, prodId);
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }

            conn.commit();
            alert.showSuccessAlert("Comment updated successfully.");

        } catch (SQLException e) {
            if(conn != null) try { conn.rollback(); } catch(SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            alert.showErrorAlert("Database error during update: " + e.getMessage());
        } finally {
            if(conn != null) try { conn.setAutoCommit(true); } catch(SQLException ex) { ex.printStackTrace(); }
        }
    }

    private HBox createRatingBox(AtomicInteger rating) {
        HBox box = new HBox(5);
        Button[] stars = new Button[5];
        for (int i = 0; i < 5; i++) {
            stars[i] = new Button(i < rating.get() ? "★" : "☆");
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
}
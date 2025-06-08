package org.example;

import javafx.scene.control.ChoiceDialog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProductCommentComplaintWindow {
    public void complainAboutComment(ProductComment comment, AlertService alertService, FirstConnectionToDataBase dbConnection) {
        SessionManager sm = SessionManager.getInstance();
        if (!sm.isAuthenticated()) {
            alertService.showErrorAlert("You must be logged in to file a complaint.");
            return;
        }

        List<String> choices = Arrays.asList("Spam", "Insult", "Inappropriate Content", "Misinformation", "Other");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Spam", choices);
        dialog.setTitle("Submit a Complaint");
        dialog.setHeaderText("Complaint about comment from: " + comment.getAuthorName());
        dialog.setContentText("Choose a reason for your complaint:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            int reporterId = sm.isManagerEnter() ? sm.getCurrentEmployeeId() : sm.getCurrentUserId();
            String reporterStatus = sm.isManagerEnter() ? "Manager" : "Client";

            String sql = "INSERT INTO product_comment_complaints (comment_id, reporter_id, reporter_status, complaint_type, created_at) VALUES (?, ?, ?, ?, NOW())";
            try (Connection conn = dbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, comment.getId());
                ps.setInt(2, reporterId);
                ps.setString(3, reporterStatus);
                ps.setString(4, reason);
                ps.executeUpdate();
                alertService.showSuccessAlert("Complaint submitted. Thank you for your feedback.");
            } catch (SQLException ex) {
                alertService.showErrorAlert("Database error: " + ex.getMessage());
            }
        });
    }
}
package org.example;

import javafx.application.Platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ProductCommentDeleteHandler {

    public void deleteComment(ProductComment comment, ProductCommentsPage page, AlertService alertService, FirstConnectionToDataBase dbConnection) {
        boolean confirmed = alertService.showConfirmationAlert("Are you sure you want to delete this comment? This action cannot be undone.");
        if (confirmed) {
            // Логика логирования для админа
            SessionManager sm = SessionManager.getInstance();
            if (sm.isManagerEnter() && "super_admin".equalsIgnoreCase(sm.getEmployeeStatusByName(sm.getCurrentManagerName()))) {
                logAdminAction(sm.getCurrentEmployeeId(), comment.getId(), "Deleted comment by " + comment.getAuthorName(), dbConnection);
            }

            String sql = "DELETE FROM product_comments WHERE comment_id = ?";
            try (Connection conn = dbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, comment.getId());
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    alertService.showSuccessAlert("Comment deleted.");
                    page.updateDisplayedComments();
                } else {
                    alertService.showErrorAlert("Could not find the comment to delete.");
                }
            } catch (SQLException ex) {
                alertService.showErrorAlert("Database error: " + ex.getMessage());
            }
        }
    }

    private void logAdminAction(int managerId, int objectId, String details, FirstConnectionToDataBase dbConnection) {
        String sql = "INSERT INTO activity_log (manager_id, action_type, object_type, object_id, details, timestamp) VALUES (?, 'DELETE', 'PRODUCT_COMMENT', ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            ps.setInt(2, objectId);
            ps.setString(3, details);
            ps.setTimestamp(4, new Timestamp(new Date().getTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
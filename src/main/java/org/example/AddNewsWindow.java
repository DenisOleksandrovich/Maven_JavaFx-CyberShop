import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.textfield.CustomTextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AddNewsWindow extends Application {

    private FirstConnectionToDataBase connectionToDataBase;

    private HBox createButtonBar(Button leftButton, Button rightButton) {
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.getChildren().addAll(leftButton, rightButton);
        return buttonBar;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CustomTextField titleTextField = new CustomTextField();
        titleTextField.setPromptText("Enter the title of the news");
        titleTextField.setTooltip(new Tooltip("Enter the title of the news"));
        titleTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        CustomTextField contentTextField = new CustomTextField();
        contentTextField.setPromptText("Enter the content of the news");
        contentTextField.setTooltip(new Tooltip("Enter the content of the news"));
        contentTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        CustomTextField linkTextField = new CustomTextField();
        linkTextField.setPromptText("Enter the link related to the news");
        linkTextField.setTooltip(new Tooltip("Enter the link related to the news"));
        linkTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        CustomTextField photoTextField = new CustomTextField();
        photoTextField.setPromptText("Enter the URL of the photo associated with the news");
        photoTextField.setTooltip(new Tooltip("Enter the URL of the photo associated with the news"));
        photoTextField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        Button saveButton = createBorderButton("Save");
        saveButton.setOnAction(event -> {
            try {
                saveNews(titleTextField.getText(), contentTextField.getText(), linkTextField.getText(), photoTextField.getText());
                primaryStage.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        Button cancelButton = createBorderButton("Cancel");
        cancelButton.setOnAction(event -> primaryStage.close());

        HBox buttonBar = createButtonBar(saveButton, cancelButton);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: black;");
        layout.getChildren().addAll(
                titleTextField, contentTextField, linkTextField, photoTextField,
                buttonBar
        );

        Scene scene = new Scene(layout, 600, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Add News");
        primaryStage.show();

        try {
            // Создаем экземпляр FirstConnectionToDataBase
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private Connection establishDBConnection() throws SQLException {
        return connectionToDataBase.getConnection();
    }

    private void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private void saveNews(String title, String content, String link, String photoUrl) throws SQLException {
        if (title.isEmpty() || content.isEmpty() || link.isEmpty() || photoUrl.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill in all fields.");
            return;
        }

        Connection connection = establishDBConnection();
        if (connection != null) {
            String errorMessage = "";

            if (title.length() > 50) {
                errorMessage += "Title exceeds maximum length of 50 characters.\n";
            }

            if (content.length() > 255) {
                errorMessage += "Content exceeds maximum length of 255 characters.\n";
            }

            if (link.length() > 5555) {
                errorMessage += "Link exceeds maximum length of 5555 characters.\n";
            }

            if (photoUrl.length() > 5555) {
                errorMessage += "Photo URL exceeds maximum length of 5555 characters.\n";
            }

            if (!errorMessage.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", errorMessage);
                return;
            }

            String query = "INSERT INTO news (news_title, news_content, news_link, news_publication_date, news_photo) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, title);
                statement.setString(2, content);
                statement.setString(3, link);
                statement.setObject(4, LocalDateTime.now());
                statement.setString(5, photoUrl);
                statement.executeUpdate();
            }
            connection.close();
            showAlert(Alert.AlertType.INFORMATION, "Success", "News saved successfully.");
        } else {
            System.err.println("Failed to establish database connection.");
        }
    }

    private Button createBorderButton(String text) {
        Button button = new Button(text);
        button.setTextFill(javafx.scene.paint.Color.WHITE);
        button.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 15));
        button.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 1px; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        button.setOpacity(1.0);

        FadeTransition colorIn = new FadeTransition(Duration.millis(300), button);
        colorIn.setToValue(1.0);

        FadeTransition colorOut = new FadeTransition(Duration.millis(300), button);
        colorOut.setToValue(0.7);

        button.setOnMouseEntered(e -> {
            colorIn.play();
            button.setStyle("-fx-background-color: #7331FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15px; -fx-border-radius: 15px;");
        });

        button.setOnMouseExited(e -> {
            colorOut.play();
            button.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: white; -fx-border-radius: 15px;");
        });

        return button;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
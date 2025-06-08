import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SupportWindow extends Application {

    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private FirstConnectionToDataBase connectionToDataBase;
    private final SessionManager sessionManager = SessionManager.getInstance();
    private Button sendButton;
    private Timeline countdownTimer;
    private int remainingTime = 60;

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            return connectionToDataBase.getConnection();
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    @Override
    public void start(Stage supportStage) {
        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
            return;
        }

        MenuPage menuPage = new MenuPage();

        supportStage.setTitle("Support");

        HBox mainPane = new HBox(50);
        mainPane.setAlignment(Pos.CENTER);
        mainPane.setStyle("-fx-background-color: black;");

        VBox welcomePane = createWelcomePane(supportStage);
        VBox contactForm = createContactForm();

        welcomePane.setPrefWidth(900 * 0.5);
        contactForm.setPrefWidth(900 * 0.5);

        mainPane.getChildren().addAll(welcomePane, contactForm);

        Scene supportScene = new Scene(mainPane, 900, 600);
        supportStage.setScene(supportScene);
        supportStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, supportStage, supportScene);
        hotKeysHandler.addHotkeys();
    }

    private VBox createWelcomePane(Stage supportStage) {
        VBox welcomePane = new VBox(20);
        welcomePane.setAlignment(Pos.CENTER);
        welcomePane.setStyle("-fx-background-color: #7331FF;");
        welcomePane.setPadding(new Insets(30));

        ImageView profileIcon = new ImageView("file:icons/support.png");
        profileIcon.setFitHeight(120);
        profileIcon.setFitWidth(120);

        Label welcomeLabel = new Label("Support");
        welcomeLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        welcomeLabel.setTextFill(Color.WHITE);

        Button backButton = ButtonStyle.blueButton("<");
        backButton.setOnAction(event -> supportStage.close());
        backButton.setStyle("-fx-background-color: #7331FF; -fx-border-color: white; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 15px;");
        backButton.setPrefWidth(100);
        backButton.setPrefHeight(30);

        Label registerLabel = new Label("" +
                " Contacts: Phone: +380639999559\n" +
                " Email: 2024cybershop2024@gmail.com\n" +
                " Address: 13 Konovaltsia Street, Kyiv, Ukraine\n");
        registerLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 20));
        registerLabel.setTextFill(Color.DARKGRAY);

        welcomePane.getChildren().addAll(profileIcon, welcomeLabel, registerLabel, backButton);

        VBox.setMargin(backButton, new Insets(50, 0, 0, 0));
        VBox.setMargin(profileIcon, new Insets(0, 0, 50, 0));
        VBox.setMargin(welcomeLabel, new Insets(0, 0, 10, 0));

        return welcomePane;
    }

    private VBox createContactForm() {
        VBox contactForm = new VBox(20);
        contactForm.setAlignment(Pos.CENTER);
        contactForm.setStyle("-fx-background-color: #07080A;");
        contactForm.setPadding(new Insets(30));

        Label contactLabel = new Label(" Contact Form ");
        contactLabel.setFont(Font.font("Gotham", FontWeight.BOLD, 30));
        contactLabel.setTextFill(Color.WHITE);
        VBox.setMargin(contactLabel, new Insets(0, 0, 30, 0));

        TextField problemField = new TextField();
        problemField.setPromptText("Enter your problem");
        problemField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        Tooltip problemTooltip = new Tooltip("Enter description of your problem");
        problemTooltip.setFont(Font.font("Gotham", FontWeight.NORMAL, 12));
        problemField.setTooltip(problemTooltip);

        TextField emailField = new TextField();
        emailField.setPromptText("Your Email (Required)");
        emailField.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");

        Tooltip emailTooltip = new Tooltip("Enter your email");
        emailTooltip.setFont(Font.font("Gotham", FontWeight.NORMAL, 12));
        emailField.setTooltip(emailTooltip);

        ComboBox<String> topicComboBox = new ComboBox<>();
        topicComboBox.getItems().addAll("Interface Failure", "Operation Failure", "Other");
        topicComboBox.setPromptText("Select topic");
        topicComboBox.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        topicComboBox.getEditor().setStyle("-fx-text-fill: white;");

        topicComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-radius: 15px;");
                }
            }
        });

        topicComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
                }
            }
        });

        sendButton = ButtonStyle.createStyledButton("    Send Request    ");
        sendButton.setOnAction(e -> {
            String problem = problemField.getText();
            String userEmail = emailField.getText();
            String topic = topicComboBox.getValue();

            int userId = sessionManager.isAuthenticated() ? sessionManager.getCurrentUserId() : 000;

            if (userEmail.isEmpty()) {
                alertService.showErrorAlert("Please enter your email (Required)");
                return;
            }

            if (!isValidEmail(userEmail)) {
                alertService.showErrorAlert("Please enter a valid email address");
                return;
            }

            if (problem.isEmpty()) {
                alertService.showErrorAlert("Please enter your problem");
                return;
            }

            if (countWords(problem) < 5) {
                alertService.showErrorAlert("Please provide a problem description with at least 5 words");
                return;
            }

            if (topic == null) {
                alertService.showErrorAlert("Please select a topic");
                return;
            }

            Integer employeeId = sessionManager.getCurrentEmployeeId();
            String backupEmail = emailField.getText();

            if (userId == 000) {
                if (remainingTime > 0) {
                    alertService.showErrorAlert("You must wait " + remainingTime + " seconds before submitting another request.");
                    return;
                }
            }

            if (saveSupportRequest(userId, employeeId, problem, topic, backupEmail)) {
                alertService.showSuccessAlert("Support request sent successfully");
                problemField.clear();
                emailField.clear();
                topicComboBox.getSelectionModel().clearSelection();
            } else {
                alertService.showErrorAlert("Failed to save support request");
            }
        });

        if (!sessionManager.isAuthenticated()) {
            sendButton.setDisable(true);
            startCountdown();
        }

        contactForm.getChildren().addAll(contactLabel, problemField, emailField, topicComboBox, sendButton);

        return contactForm;
    }

    private void startCountdown() {
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingTime--;
            sendButton.setText("Wait " + remainingTime + "s");

            if (remainingTime <= 0) {
                sendButton.setDisable(false);
                sendButton.setText("Send Request");
                countdownTimer.stop();
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    private boolean saveSupportRequest(int userId, Integer employeeId, String problem, String topic, String email) {
        try (Connection connection = establishDBConnection()) {
            String query = "INSERT INTO support_requests (user_id, employee_id, problem, topic, backup_email, submission_time) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setObject(1, userId == 000 ? null : userId);
                preparedStatement.setObject(2, employeeId);
                preparedStatement.setString(3, problem);
                preparedStatement.setString(4, topic);
                preparedStatement.setString(5, email);
                preparedStatement.setString(6, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                return preparedStatement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to save support request: " + e.getMessage());
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private int countWords(String problem) {
        return problem.trim().split("\\s+").length;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
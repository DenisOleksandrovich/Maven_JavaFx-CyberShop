import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SupportRequestWindow extends Application {

    private BorderPane root;
    private VBox pendingRequestsContainer;
    private VBox allRequestsContainer;
    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();


    @Override
    public void start(Stage primaryStage) {
        try {
            MenuPage menuPage = new MenuPage();
            connectionToDataBase = FirstConnectionToDataBase.getInstance();

            root = new BorderPane();
            root.setStyle("-fx-background-color: black;");

            VBox center = new VBox(10);
            center.setStyle("-fx-background-color: black;");

            HeaderComponent headerComponent = new HeaderComponent(primaryStage);
            VBox headerContainer = new VBox(10);
            headerContainer.getChildren().add(headerComponent.createHeader());

            pendingRequestsContainer = new VBox(10);
            pendingRequestsContainer.setStyle("-fx-background-color: black;");
            pendingRequestsContainer.setPrefHeight(Double.MAX_VALUE);

            allRequestsContainer = new VBox(10);
            allRequestsContainer.setStyle("-fx-background-color: black;");
            allRequestsContainer.setPrefHeight(Double.MAX_VALUE);

            VBox requestsSections = new VBox(10);
            requestsSections.getChildren().addAll(
                    createSection("Pending Support Requests", pendingRequestsContainer, true),
                    createSeparator(),
                    createSection("All Support Requests", allRequestsContainer, false)
            );

            pendingRequestsContainer.setPadding(new Insets(0, 0, 0, 0));
            allRequestsContainer.setPadding(new Insets(0, 0, 0, 0));

            center.getChildren().addAll(headerContainer, requestsSections);
            VBox.setVgrow(requestsSections, Priority.ALWAYS);
            VBox.setMargin(pendingRequestsContainer, new Insets(0));
            VBox.setMargin(allRequestsContainer, new Insets(0));

            ScrollPane scrollPane = new ScrollPane(center);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: black; -fx-padding: 0;");

            root.setCenter(scrollPane);

            Scene scene = new Scene(root, 900, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Support Requests");
            primaryStage.show();

            searchRequests("");

            HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
            hotKeysHandler.addHotkeys();
        } catch (SQLException e) {
            alertService.showErrorAlert("Error initializing database connection: " + e.getMessage());
        }
    }

    private VBox createSection(String title, VBox contentContainer, boolean isPending) {
        Label sectionTitle = new Label(title);
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 10 0 10 10;");

        Button sortButton = new Button();
        Image sortingIcon = new Image("file:icons/sorting.png");
        ImageView sortingImageView = new ImageView(sortingIcon);
        sortingImageView.setFitWidth(20);
        sortingImageView.setFitHeight(20);
        sortButton.setGraphic(sortingImageView);
        sortButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        sortButton.setOnAction(e -> sortRequests(contentContainer, isPending));

        HBox sectionHeader = new HBox(10);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        sectionHeader.getChildren().addAll(sectionTitle, sortButton);
        sectionHeader.setStyle("-fx-background-color: black;");

        VBox sectionContainer = new VBox(10);
        sectionContainer.getChildren().addAll(sectionHeader, contentContainer);
        sectionContainer.setStyle("-fx-background-color: black;");
        VBox.setVgrow(contentContainer, Priority.ALWAYS);

        return sectionContainer;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        VBox.setMargin(separator, new Insets(10, 20, 10, 20));
        separator.setStyle("-fx-background-color: gray; -fx-padding: 0;");
        return separator;
    }

    private void searchRequests(String query) {
        pendingRequestsContainer.getChildren().clear();
        allRequestsContainer.getChildren().clear();

        try (Connection connection = connectionToDataBase.getConnection();
             Statement statement = connection.createStatement()) {

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twentyFourHoursAgo = now.minusHours(24);

            String pendingRequestsSql = "SELECT * FROM support_requests WHERE status = 'Pending' " +
                    "AND (id LIKE '%" + query + "%' OR problem LIKE '%" + query + "%' OR topic LIKE '%" + query + "%')";

            ResultSet pendingRequestsResultSet = statement.executeQuery(pendingRequestsSql);
            int pendingCount = 0;

            while (pendingRequestsResultSet.next()) {
                pendingCount++;
                addRequestToContainer(pendingRequestsResultSet, pendingRequestsContainer, pendingCount);
            }

            if (pendingCount == 0) {
                Label noRequestsLabel = createLabel("No pending requests found");
                noRequestsLabel.setPadding(new Insets(5, 0, 5, 10));
                pendingRequestsContainer.getChildren().add(noRequestsLabel);
            }

            String allRequestsSql = "SELECT * FROM support_requests " +
                    "WHERE (id LIKE '%" + query + "%' OR problem LIKE '%" + query + "%' OR topic LIKE '%" + query + "%')";

            ResultSet allRequestsResultSet = statement.executeQuery(allRequestsSql);
            int allCount = 0;

            while (allRequestsResultSet.next()) {
                allCount++;
                addRequestToContainer(allRequestsResultSet, allRequestsContainer, allCount);
            }

            if (allCount == 0) {
                displayNoResults(allRequestsContainer);
            }

        } catch (SQLException e) {
            alertService.showErrorAlert("Error retrieving support requests: " + e.getMessage());
        }
    }

    private void addRequestToContainer(ResultSet resultSet, VBox container, int count) throws SQLException {
        int requestId = resultSet.getInt("id");
        String problem = resultSet.getString("problem");
        String topic = resultSet.getString("topic");
        String status = resultSet.getString("status");
        LocalDateTime submissionTime = resultSet.getTimestamp("submission_time").toLocalDateTime();
        String submissionTimeString = submissionTime.toString();

        String requestLineText = String.format("№%d. ID: %d, Topic: %s, Status: %s, Submission Time: %s",
                count, requestId, topic, status, submissionTimeString);
        String problemText = String.format("Problem: %s", problem);

        Label requestLineLabel = createLabel(requestLineText);
        Label problemLabel = createLabel(problemText);

        Button answerButton = ButtonStyle.expandPaneStyledButton("Answer");
        Integer supportManagerId = SessionManager.getInstance().getCurrentEmployeeId();
        answerButton.setOnAction(e -> {
            String requestText = problem;
            openAnswerDialog(requestId, supportManagerId, requestText);
        });

        VBox requestBox = new VBox(5);
        requestBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 5px;");
        requestBox.getChildren().addAll(requestLineLabel, problemLabel, answerButton);

        container.getChildren().add(requestBox);

        Separator separator = createSeparator();
        separator.setPadding(new Insets(10, 20, 10, 20));
        container.getChildren().add(separator);
    }

    private void displayNoResults(VBox container) {
        container.getChildren().clear();
        container.setAlignment(Pos.CENTER);

        Image emptySearchImage = new Image("file:icons/empty_search.png");
        ImageView emptySearchImageView = new ImageView(emptySearchImage);
        emptySearchImageView.setFitWidth(200);
        emptySearchImageView.setFitHeight(200);

        Insets padding = new Insets(10, 20, 10, 20);
        container.setPadding(padding);

        Label noResultsLabel = new Label("No requests found");
        noResultsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        container.getChildren().addAll(emptySearchImageView, noResultsLabel);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), container);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private void sortRequests(VBox container, boolean isPending) {
        List<Node> items = new ArrayList<>(container.getChildren());
        items.removeIf(node -> node instanceof Separator);

        items.sort(Comparator.comparing(node -> {
            if (node instanceof VBox requestBox) {
                Label label = (Label) requestBox.getChildren().get(0);
                return label.getText();
            }
            return "";
        }));

        container.getChildren().clear();
        container.getChildren().addAll(items);
        for (Node item : items) {
            container.getChildren().add(createSeparator());
        }
    }

    private void openAnswerDialog(int requestId, int supportManagerId, String requestText) {
        SupportAnswerDialog dialog = new SupportAnswerDialog(requestId, supportManagerId, requestText);
        dialog.showAndWait();
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        return label;
    }

    public class HeaderComponent {

        private Stage primaryStage;

        public HeaderComponent(Stage primaryStage) {
            this.primaryStage = primaryStage;
        }

        private VBox createHeader() {
            VBox header = new VBox(10);
            header.setPadding(new Insets(10));
            header.setStyle("-fx-background-color: black");

            Image logoImage = new Image("file:icons/LOGO_our.jpg");
            ImageView logoImageView = new ImageView(logoImage);
            logoImageView.setFitWidth(50);
            logoImageView.setFitHeight(50);

            Circle logoCircle = new Circle(25);
            logoCircle.setFill(new ImagePattern(logoImage));
            logoCircle.setCursor(Cursor.HAND);

            Region rightRegion = new Region();
            HBox.setHgrow(rightRegion, Priority.ALWAYS);

            Button menuButton = ButtonStyle.createStyledButton("     Menu     ");
            menuButton.setOnAction(e -> showMenu());

            TextField searchField = new TextField();
            searchField.setPromptText("Search by Title");
            searchField.getStyleClass().add("search-field");

            searchField.setStyle(
                    "-fx-background-radius: 5em; " +
                            "-fx-background-color: black; " +
                            "-fx-background-insets: 0, 1, 2; " +
                            "-fx-padding: 0.166667em 0.25em 0.25em 0.25em; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 1.0em; " +
                            "-fx-border-radius: 5em; " +
                            "-fx-border-color: white;"
            );

            searchField.setOnMousePressed(e -> searchField.setStyle(
                    "-fx-background-radius: 5em; " +
                            "-fx-background-color: black; " +
                            "-fx-background-insets: 0, 1, 2; " +
                            "-fx-padding: 0.166667em 0.25em 0.25em 0.25em; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 1.0em; " +
                            "-fx-border-radius: 5em; " +
                            "-fx-border-color: blue, -fx-focus-color, blue;"
            ));

            searchField.setOnMouseReleased(e -> searchField.setStyle(
                    "-fx-background-radius: 5em; " +
                            "-fx-background-color: black; " +
                            "-fx-background-insets: 0, 1, 2; " +
                            "-fx-padding: 0.166667em 0.25em 0.25em 0.25em; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 1.0em; " +
                            "-fx-border-radius: 5em; " +
                            "-fx-border-color: blue, -fx-focus-color, blue;"
            ));

            searchField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    String query = searchField.getText();
                    if (!query.isEmpty()) {
                        searchRequests(query);
                    } else {
                        alertService.showErrorAlert("Please enter a search query.");
                    }
                }
            });

            Button clearButton = ButtonStyle.createStyledButton("Clear Search");
            clearButton.setOnAction(event -> {
                searchField.clear();
                searchRequests("");
            });

            Button supportButton = ButtonStyle.createStyledButton("  Support  ");
            supportButton.setOnAction(event -> showSupportWindow());

            Button privacyButton = ButtonStyle.createStyledButton("  Privacy Policy  ");
            privacyButton.setOnAction(event -> showPrivacyPolicyWindow());

            Button accountButton = ButtonStyle.createStyledButton("  Account  ");
            accountButton.setOnAction(e -> showRegistrationWindow());

            HBox topContent = new HBox(10);
            topContent.getChildren().addAll(logoCircle, menuButton, searchField, clearButton, supportButton, privacyButton, accountButton, rightRegion);
            topContent.setAlignment(Pos.CENTER_RIGHT);
            VBox.setVgrow(topContent, Priority.ALWAYS);

            HBox.setMargin(logoCircle, new Insets(0, 90, 0, 0));

            header.getChildren().addAll(topContent);

            return header;
        }

        private void showMenu() {
            primaryStage.close();
            Stage menuStage = new Stage();
            MenuPage menuPage = new MenuPage();
            menuPage.start(menuStage);
        }

        private void showSupportWindow() {
            SupportWindow supportWindow = new SupportWindow();
            Stage supportStage = new Stage();
            supportWindow.start(supportStage);
            supportStage.show();
        }

        private void showPrivacyPolicyWindow() {
            PrivacyPolicyWindow privacyPolicyWindow = new PrivacyPolicyWindow();
            Stage privacyStage = new Stage();
            privacyPolicyWindow.start(privacyStage);
            privacyStage.show();
        }

        private void showRegistrationWindow() {
            RegistrationWindow registrationWindow = new RegistrationWindow(root);
            Stage registrationStage = new Stage();
            registrationWindow.start(registrationStage);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
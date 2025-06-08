import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;
import java.util.regex.Pattern;

public class CreateEmployeeAccounts extends Application {

    private FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();

    private Connection establishDBConnection() throws SQLException {
        if (connectionToDataBase != null) {
            return connectionToDataBase.getConnection();
        } else {
            throw new SQLException("Database connection is not initialized.");
        }
    }

    CreateEmployeeAccounts() {

    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        MenuPage menuPage = new MenuPage();

        root.setStyle("-fx-background-color: black;");

        VBox centerContainer = new VBox(10);
        centerContainer.setPadding(new Insets(10));

        createInputFields(centerContainer);

        root.setCenter(centerContainer);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Account Details");
        primaryStage.show();

        HotKeysHandler hotKeysHandler = new HotKeysHandler(menuPage, primaryStage, scene);
        hotKeysHandler.addHotkeys();

        try {
            connectionToDataBase = FirstConnectionToDataBase.getInstance();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to establish database connection: " + e.getMessage());
        }
    }

    private void createInputFields(VBox container) {
        TextField managerNameField = createTextFieldWithPrompt("Manager Name");
        setFieldStyleAndTooltip(managerNameField, "Manager Name");

        TextField passwordField = createTextFieldWithPrompt("Password");
        setFieldStyleAndTooltip(passwordField, "Password");

        TextField confirmPasswordField = createTextFieldWithPrompt("Confirm Password");
        setFieldStyleAndTooltip(confirmPasswordField, "Confirm Password");

        TextField phoneNumberField = createTextFieldWithPrompt("Phone Number");
        setFieldStyleAndTooltip(phoneNumberField, "Phone Number");

        TextField emailField = createTextFieldWithPrompt("Email");
        setFieldStyleAndTooltip(emailField, "Email");

        TextField salaryField = createTextFieldWithPrompt("Salary");
        setFieldStyleAndTooltip(salaryField, "Salary");

        ComboBox<String> employeeStatusComboBox = new ComboBox<>();
        employeeStatusComboBox.setPromptText("Employee Status");
        employeeStatusComboBox.getItems().addAll("Manager", "Main Manager", "Accountant", "Admin", "Super Admin", "Support Manager");
        employeeStatusComboBox.setEditable(true);

        String comboBoxStyle = "-fx-background-color: black; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: white; " +
                "-fx-border-radius: 15px; " +
                "-fx-control-inner-background: black; " +
                "-fx-padding: 0 0 0 5;";

        employeeStatusComboBox.setStyle(comboBoxStyle);
        employeeStatusComboBox.getEditor().setStyle("-fx-text-fill: white; -fx-control-inner-background: black; -fx-font-size: 14px;");

        employeeStatusComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 0;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 0;");
                }
            }
        });

        Tooltip employeeStatusTooltip = new Tooltip("Select or type employee status");
        employeeStatusComboBox.setTooltip(employeeStatusTooltip);

        TextField salaryKeyField = createTextFieldWithPrompt("Salary Key");
        setFieldStyleAndTooltip(salaryKeyField, "Salary Key");

        Button createManagerButton = ButtonStyle.createStyledButton("Create Manager Account");
        createManagerButton.setOnAction(e -> {
            if (!arePasswordsMatching(passwordField.getText(), confirmPasswordField.getText())) {
                alertService.showErrorAlert("Passwords do not match. Please try again.");
                return;
            }

            if (!isValidEmail(emailField.getText())) {
                alertService.showErrorAlert("Invalid email format. Please enter a valid email.");
                return;
            }

            if (managerNameField.getText().isEmpty()) {
                alertService.showErrorAlert("Manager Name field is empty! Please fill it.");
                return;
            }
            if (passwordField.getText().isEmpty()) {
                alertService.showErrorAlert("Password field is empty! Please fill it.");
                return;
            }
            if (confirmPasswordField.getText().isEmpty()) {
                alertService.showErrorAlert("Confirm Password field is empty! Please fill it.");
                return;
            }
            if (phoneNumberField.getText().isEmpty()) {
                alertService.showErrorAlert("Phone Number field is empty! Please fill it.");
                return;
            }
            if (emailField.getText().isEmpty()) {
                alertService.showErrorAlert("Email field is empty! Please fill it.");
                return;
            }
            if (employeeStatusComboBox.getSelectionModel().isEmpty()) {
                alertService.showErrorAlert("Employee Status field is empty! Please select or type a status.");
                return;
            }
            if (salaryKeyField.getText().isEmpty()) {
                alertService.showErrorAlert("Salary Key field is empty! Please fill it.");
                return;
            }
            if (salaryField.getText().isEmpty()) {
                alertService.showErrorAlert("Salary field is empty! Please fill it.");
                return;
            }

            String reason = checkBlockedData(managerNameField.getText(), phoneNumberField.getText(), emailField.getText());
            if (reason != null) {
                boolean proceed = showConfirmationAlert("Blocked Data Found",
                        "The entered data (Name, Phone, or Email) is blocked for the following reason: " + reason +
                                "\nDo you still want to create the account?");
                if (!proceed) {
                    return;
                }
            }

            createManagerAccount(
                    managerNameField.getText(),
                    passwordField.getText(),
                    phoneNumberField.getText(),
                    emailField.getText(),
                    formatEmployeeStatus(employeeStatusComboBox.getSelectionModel().getSelectedItem()),
                    salaryKeyField.getText()
            );
        });

        container.getChildren().addAll(
                createBoldLabel("Create Employee Account", "-fx-text-fill: white;"),
                managerNameField,
                passwordField,
                confirmPasswordField,
                phoneNumberField,
                emailField,
                employeeStatusComboBox,
                salaryField,
                salaryKeyField,
                createManagerButton
        );
    }

    private String checkBlockedData(String accountName, String phoneNumber, String email) {
        try (Connection conn = establishDBConnection()) {
            String query = "SELECT reason FROM blocked_data WHERE phone_number = ? OR email = ? OR name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, phoneNumber);
                pstmt.setString(2, email);
                pstmt.setString(3, accountName);

                ResultSet resultSet = pstmt.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("reason");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean showConfirmationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        return result == ButtonType.OK;
    }

    private boolean arePasswordsMatching(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void setFieldStyleAndTooltip(TextField field, String promptText) {
        field.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 15px;");
        field.setPromptText(promptText);
        field.setPrefWidth(200);
        Tooltip tooltip = new Tooltip(promptText);
        field.setTooltip(tooltip);
    }

    private TextField createTextFieldWithPrompt(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        return textField;
    }

    private Label createBoldLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        label.setFont(Font.font("Gotham", FontWeight.NORMAL, 24));
        return label;
    }

    private String formatEmployeeStatus(String status) {
        switch (status.toLowerCase()) {
            case "Support Manager":
                return "support_manager";
            case "Manager":
                return "manager";
            case "Admin":
                return "admin";
            case "Super Admin":
                return "super_admin";
            case "Main Manager":
                return "main_manager";
            case "Accountant":
                return "accountant";
            default:
                return status.toLowerCase().replace(" ", "_");
        }
    }

    private void createManagerAccount(String managerName, String password, String phoneNumber, String email, String employeeStatus, String bonusKey) {
        try (Connection connection = establishDBConnection()) {
            if (isEmailUnique(email, connection)) {
                if (isPhoneNumberUnique(phoneNumber, connection)) {
                    if (isManagerNameUnique(managerName, connection)) {
                        String sql = "INSERT INTO managers (manager_name, password, manager_phone_number, manager_email, employee_status) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                            statement.setString(1, managerName);
                            statement.setString(2, password);
                            statement.setString(3, phoneNumber);
                            statement.setString(4, email);
                            statement.setString(5, formatEmployeeStatus(employeeStatus));

                            int rowsInserted = statement.executeUpdate();
                            if (rowsInserted > 0) {
                                ResultSet generatedKeys = statement.getGeneratedKeys();
                                if (generatedKeys.next()) {
                                    int managerId = generatedKeys.getInt(1);
                                    saveManagerIdToBookkeeping(managerId, bonusKey);
                                }
                                alertService.showSuccessAlert("A new manager account was created successfully.");
                            }
                        }
                    } else {
                        alertService.showErrorAlert("This name is already taken. Please choose another one.");
                    }
                } else {
                    alertService.showErrorAlert("Phone number is already registered. Please use another one.");
                }
            } else {
                alertService.showErrorAlert("Email is already registered. Please use another one.");
            }
        } catch (SQLException ex) {
            alertService.showErrorAlert("Error while creating manager account: " + ex.getMessage());
        }
    }

    private void saveManagerIdToBookkeeping(int managerId, String bonusKey) {
        try (Connection connection = establishDBConnection()) {
            String sql = "INSERT INTO bookkeeping (employee_id, employee_salary, bonus_key, change_date, successful_contracts, successful_contracts_price, bonus) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, managerId);
                statement.setString(2, "0");
                statement.setString(3, bonusKey);
                statement.setString(4, getCurrentTimestamp());
                statement.setString(5, "0");
                statement.setString(6, "0");
                statement.setString(7, "0");

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    alertService.showSuccessAlert("Manager ID and bonus key saved to bookkeeping successfully.");
                } else {
                    alertService.showErrorAlert("Failed to save manager ID and bonus key to bookkeeping.");
                }
            }
        } catch (SQLException e) {
            alertService.showErrorAlert("Error saving manager ID to bookkeeping: " + e.getMessage());
        }
    }

    private String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }

    public boolean isEmailUnique(String email, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM managers WHERE manager_email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count") == 0;
                }
            }
        }
        return false;
    }

    public boolean isPhoneNumberUnique(String phoneNumber, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM managers WHERE manager_phone_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phoneNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count") == 0;
                }
            }
        }
        return false;
    }

    private boolean isManagerNameUnique(String managerName, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM managers WHERE manager_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, managerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count") == 0;
                }
            }
        }
        return false;
    }
}

package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ContractEditDialog extends Dialog<Boolean> {

    private final Contract contract;
    private final FirstConnectionToDataBase connectionToDataBase;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private final DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ContractEditDialog(Contract contract, FirstConnectionToDataBase dbConnection) {
        this.contract = contract;
        this.connectionToDataBase = dbConnection;

        setTitle("Edit Contract #" + contract.getId());
        getDialogPane().setStyle("-fx-background-color: #2c2c2c;");

        // Add Save and Cancel buttons
        getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        // Create the content of the dialog
        VBox content = createContent();
        getDialogPane().setContent(content);

        // This is called when a dialog button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.APPLY) {
                return handleSaveChanges(content);
            }
            return false; // Return false if cancelled
        });
    }

    private VBox createContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));

        // 1. Selector for what to edit
        ComboBox<String> editChoice = new ComboBox<>();
        editChoice.getItems().addAll("Delivery Method", "Payment Method", "Deadline");
        editChoice.setPromptText("Select field to edit");

        // 2. Placeholder for the specific editor control
        StackPane editorPlaceholder = new StackPane();
        editorPlaceholder.setMinHeight(40); // Reserve some space

        // 3. Create the actual editor controls
        ComboBox<DeliveryType> deliveryEditor = new ComboBox<>();
        deliveryEditor.getItems().setAll(DeliveryType.values());
        deliveryEditor.setValue(DeliveryType.valueOf(contract.getDeliveryMethod().toUpperCase().replace(" ", "_")));


        ComboBox<PaymentType> paymentEditor = new ComboBox<>();
        paymentEditor.getItems().setAll(PaymentType.values());
        paymentEditor.setValue(PaymentType.valueOf(contract.getPaymentMethod().toUpperCase().replace(" ", "_")));


        DatePicker deadlineEditor = new DatePicker();
        deadlineEditor.setValue(LocalDate.parse(contract.getDeadline(), dbFormatter));
        // Prevent picking dates in the past
        deadlineEditor.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // 4. Logic to show the correct editor
        editChoice.valueProperty().addListener((obs, oldVal, newVal) -> {
            editorPlaceholder.getChildren().clear();
            if (newVal != null) {
                switch (newVal) {
                    case "Delivery Method":
                        editorPlaceholder.getChildren().add(deliveryEditor);
                        break;
                    case "Payment Method":
                        editorPlaceholder.getChildren().add(paymentEditor);
                        break;
                    case "Deadline":
                        editorPlaceholder.getChildren().add(deadlineEditor);
                        break;
                }
            }
        });

        // Add a tag to easily find the controls later
        container.setUserData(new Object[]{editChoice, deliveryEditor, paymentEditor, deadlineEditor});

        container.getChildren().addAll(
                new Label("What would you like to change?"),
                editChoice,
                editorPlaceholder
        );
        return container;
    }

    private boolean handleSaveChanges(VBox content) {
        Object[] controls = (Object[]) content.getUserData();
        ComboBox<String> editChoice = (ComboBox<String>) controls[0];

        String selectedField = editChoice.getValue();
        if (selectedField == null) {
            alertService.showErrorAlert("Please select a field to edit.");
            return false;
        }

        try {
            switch (selectedField) {
                case "Delivery Method":
                    ComboBox<DeliveryType> deliveryEditor = (ComboBox<DeliveryType>) controls[1];
                    DeliveryType newDelivery = deliveryEditor.getValue();
                    if (newDelivery == null) {
                        alertService.showErrorAlert("Please select a delivery method.");
                        return false;
                    }
                    updateContractField("delivery_method", newDelivery.toString());
                    break;

                case "Payment Method":
                    ComboBox<PaymentType> paymentEditor = (ComboBox<PaymentType>) controls[2];
                    PaymentType newPayment = paymentEditor.getValue();
                    if (newPayment == null) {
                        alertService.showErrorAlert("Please select a payment method.");
                        return false;
                    }
                    updateContractField("pay_method", newPayment.toString());
                    break;

                case "Deadline":
                    DatePicker deadlineEditor = (DatePicker) controls[3];
                    LocalDate newDeadline = deadlineEditor.getValue();
                    if (newDeadline == null) {
                        alertService.showErrorAlert("Please select a valid deadline.");
                        return false;
                    }
                    updateContractField("deadline", newDeadline.format(dbFormatter));
                    break;
            }
            alertService.showSuccessAlert("Contract updated successfully!");
            return true; // Indicate success
        } catch (SQLException e) {
            alertService.showErrorAlert("Database Error: " + e.getMessage());
            e.printStackTrace();
            return false; // Indicate failure
        }
    }

    private void updateContractField(String columnName, String value) throws SQLException {
        String sql = String.format("UPDATE contracts SET %s = ? WHERE id_contracts = ?", columnName);
        try (Connection conn = connectionToDataBase.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            pstmt.setInt(2, contract.getId());
            pstmt.executeUpdate();
        }
    }
}
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDateTime;

public class SupportAnswerDialog extends Stage {

    private final int supportRequestId;
    private final int supportManagerId;
    private TextArea responseTextArea;
    private File selectedPdfFile;
    private Label pdfFileLabel;
    private final AlertServiceImpl alertService = new AlertServiceImpl();
    private Label requestTextLabel;

    public SupportAnswerDialog(int supportRequestId, int supportManagerId, String requestText) {
        this.supportRequestId = supportRequestId;
        this.supportManagerId = supportManagerId;
        setupUI(requestText);
    }

    private void setupUI(String requestText) {
        this.setTitle("Support Request Response");
        this.setWidth(400);
        this.setHeight(600);
        this.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(10);
        root.setStyle("-fx-background-color: black; -fx-padding: 20px;");
        root.setAlignment(Pos.CENTER);

        requestTextLabel = createLabel('"' + requestText + '"', 14, "Gotham");
        requestTextLabel.setWrapText(true);
        requestTextLabel.setStyle("-fx-text-fill: white; -fx-padding: 0 0 10 0;");

        responseTextArea = new TextArea();
        responseTextArea.setPromptText("Enter your response...");
        responseTextArea.setStyle(
                "-fx-control-inner-background: black; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-color: white; " +
                        "-fx-border-radius: 15px; " +
                        "-fx-background-color: black;"
        );
        responseTextArea.setPrefSize(360, 200);

        Button attachPdfButton = ButtonStyle.expandPaneStyledButton("Attach PDF");
        attachPdfButton.setOnAction(e -> openFileChooser());

        pdfFileLabel = new Label("No file selected");
        pdfFileLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        pdfFileLabel.setStyle("-fx-text-fill: white;");

        Button sendButton = ButtonStyle.expandPaneStyledButton("Send");
        sendButton.setOnAction(e -> sendResponse());

        root.getChildren().addAll(
                createLabel("Request Response", 22, "Gotham"),
                requestTextLabel,
                responseTextArea,
                pdfFileLabel,
                attachPdfButton,
                sendButton
        );

        Scene scene = new Scene(root);
        this.setScene(scene);
    }

    private Label createLabel(String text, int fontSize, String fontFamily) {
        Label label = new Label(text);
        label.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize));
        label.setStyle("-fx-text-fill: white;");
        return label;
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select PDF Document");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        File file = fileChooser.showOpenDialog(this);

        if (file != null && file.exists()) {
            selectedPdfFile = file;
            pdfFileLabel.setText("Selected file: " + file.getName());
        }
    }

    private void sendResponse() {
        String responseText = responseTextArea.getText();

        if (responseText.isEmpty()) {
            alertService.showErrorAlert("Response field cannot be empty.");
            return;
        }

        if (selectedPdfFile != null) {
            try {
                byte[] fileBytes = Files.readAllBytes(selectedPdfFile.toPath());
                saveResponseWithFile(responseText, fileBytes);
            } catch (IOException ex) {
                alertService.showErrorAlert("Failed to read PDF file.");
            }
        } else {
            saveResponseWithFile(responseText, null);
        }
    }

    private void saveResponseWithFile(String response, byte[] fileData) {
        try (Connection connection = FirstConnectionToDataBase.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO request_answer (support_request_id, support_manager_id, response, response_time, pdf_document) VALUES (?, ?, ?, ?, ?)")) {

            preparedStatement.setInt(1, supportRequestId);
            preparedStatement.setInt(2, supportManagerId);
            preparedStatement.setString(3, response);
            preparedStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            if (fileData != null) {
                preparedStatement.setBytes(5, fileData);
            } else {
                preparedStatement.setNull(5, Types.BLOB);
            }

            preparedStatement.executeUpdate();
            this.close();
        } catch (SQLException e) {
            alertService.showErrorAlert("Failed to save response: " + e.getMessage());
        }
    }
}
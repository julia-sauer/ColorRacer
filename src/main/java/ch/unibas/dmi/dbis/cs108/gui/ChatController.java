package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import java.awt.desktop.ScreenSleepEvent;
import java.io.IOException;
import java.net.URL;

public class ChatController {
    public TitledPane root = null;

    // The TextArea for displaying chat messages
    @FXML
    private TextArea chatArea;

    // The TextField where the user types a message
    @FXML
    private TextField txtUsermsg;

    // Reference to your client’s protocol writer; set this after FXML loading
    private ProtocolWriterClient protocolWriter;

    public ChatController() {}

    public TitledPane getRoot() {
        return root;
    }

    /**
     * Setter to inject the ProtocolWriterClient instance.
     */
    public void setProtocolWriter(ProtocolWriterClient protocolWriter) {
        this.protocolWriter = protocolWriter;
    }

    /**
     * Handles key press events on the TextField.
     * Sends the chat when the user presses ENTER.
     */
    @FXML
    private void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
            event.consume();  // prevent further handling (like adding a newline)
        }
    }

    /**
     * Optional: Clears the TextField when it is clicked.
     */
    @FXML
    private void mouseClicked() {
        txtUsermsg.clear();
    }

    /**
     * Retrieves the message from the TextField and sends it through the network.
     */
    private void sendMessage() {
        String message = txtUsermsg.getText().trim();
        if (!message.isEmpty()) {
            // Send the message using your existing network protocol
            if (protocolWriter != null) {
                protocolWriter.sendChat(message);
            }
            // Optionally display the message in the chatArea as "You: ..."
            chatArea.appendText("You: " + message + "\n");
            txtUsermsg.clear();
        }
    }

    /**
     * This method can be called from your ProtocolReaderClient when a new chat message arrives.
     * Since network events may occur on a non-JavaFX thread, use Platform.runLater.
     */
    public void displayChat(String message) {
        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }
}
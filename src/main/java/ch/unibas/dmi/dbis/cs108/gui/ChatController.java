package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

/**
 * The {@link  ChatController} class acts as the controller for the chat GUI.
 * It is responsible for handling user input, sending messages via the {@link ProtocolWriterClient},
 * and updating the chat display area with incoming messages.
 * The corresponding FXML file is expected to define a {@link TitledPane} containing a {@link TextArea} for
 * displaying chat messages and a {@link TextField} for user input to write a message.
 *
 * @author julia
 */
public class ChatController {

    /**
     * The root {@link TitledPane} of the chat interface.
     */
    @FXML
    public TitledPane root = null;

    /**
     * The {@link TextArea} for displaying chat messages.
     */
    @FXML
    private TextArea chatArea;

    /**
     * The {@link TextField} where the user types a message.
     */
    @FXML
    private TextField txtUsermsg;

    /**
     * Instance of the client's {@link ProtocolWriterClient} for sending messages.
     */
    private ProtocolWriterClient protocolWriter;

    /**
     * Constructs a new {@link ChatController}.
     */
    public ChatController() {}

    /**
     * Returns the root node of the chat interface.
     *
     * @return the root {@link TitledPane} of this chat interface.
     */
    public TitledPane getRoot() {
        return root;
    }

    /**
     * Sets the {@link ProtocolWriterClient} to be used for sending chat messages in the GUI.
     *
     * @param protocolWriter the {@link ProtocolWriterClient} instance to inject.
     */
    public void setProtocolWriter(ProtocolWriterClient protocolWriter) {
        this.protocolWriter = protocolWriter;
    }

    /**
     * Handles key press events on the chat input field.
     * When the ENTER key is pressed, the message is sent to the server.
     *
     * @param event the {@code KeyEvent} triggered by user input.
     */
    @FXML
    private void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
        }
    }

    /**
     * Clears the TextField when it is clicked.
     */
    @FXML
    private void mouseClicked() {
        txtUsermsg.clear();
    }

    /**
     * Retrieves the message from the chat input field and sends it over the {@link ProtocolWriterClient}.
     * The message is sent via the injected {@link ProtocolWriterClient} instance. After sending,
     * the input field is cleared with {@code txtUsermsg.clear()}.
     */
    private void sendMessage() {
        String message = txtUsermsg.getText().trim();
        if (!message.isEmpty()) {
            // Send the message using your existing network protocol
            if (protocolWriter != null) {
                protocolWriter.sendChat(message);
            }
            txtUsermsg.clear();
        }
    }

    /**
     * Displays a new chat message in the chat area.
     * This method is typically called from the {@link ch.unibas.dmi.dbis.cs108.network.ProtocolReaderClient}
     * when a new message is received.
     * To ensure that the GUI is updated on the JavaFX Application Thread, the update is wrapped
     * in a call to {@link Platform#runLater(Runnable)}.
     *
     * @param message the chat message to be displayed.
     */
    public void displayChat(String message) {
        Platform.runLater(() -> chatArea.appendText(message + "\n"));
    }
}
package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * The {@code GUI} class is a JavaFX Application that loads the welcome lobby defined in the FXML
 * file WelcomeLobbyTemplate and connects it to the client. This class is responsible for launching the
 * graphical user interface, injecting the necessary protocol objects into the
 * {@link WelcomeLobbyController}, and displaying the lobby window.
 *
 * @author julia
 */
public class GUI extends Application {

    /**
     * The client instance to be used by the GUI. This should be set from the main application logic
     * before launching the JavaFX application.
     */
    private static Client client;

    /**
     * The main entry point for launching the GUI.
     *
     * @param args The command line arguments. These are passed on to the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This method loads the FXML layout for the lobby interface,retrieves the
     * {@link WelcomeLobbyController}, injects the
     * {@link ProtocolWriterClient} and {@link WelcomeLobbyController} references from the client,
     * and displays the lobby window.
     *
     * @param primaryStage The primary stage for this application, onto which the application scene
     *                     can be set.
     * @throws IOException If there is an error loading the FXML resource.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the FXML file (ensure the correct resource path is used)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/WelcomeLobbyTemplate.fxml"));
        BorderPane root = loader.load();

        // Get the controller instance from the FXML loader
        WelcomeLobbyController welcomeController = loader.getController();
        welcomeController.setClient(client);
        welcomeController.setPrimaryStage(primaryStage);
        // Inject the ProtocolWriterClient into the ChatController.
        welcomeController.setProtocolWriter(client.getProtocolWriter());
        client.setWelcomeController(welcomeController);
        client.getProtocolReader().setWelcomeController(welcomeController);

        Image icon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.jpg"))
        );
        primaryStage.getIcons().add(icon);

        // Setup and show the GUI
        primaryStage.setMaximized(true);
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Welcome Lobby");
        primaryStage.show();
    }

    /**
     * Called when the JavaFX application is stopping for example when closing the window with the x in the top right corner of the window.
     * <p>
     * Ensures that the client is properly disconnected before the application exits,
     * preventing potential resource leaks or hanging network connections.
     *
     * @throws Exception if an error occurs during shutdown.
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        client.disconnect();
    }

    /**
     * Sets the {@code Client} instance that will be used by the GUI.
     *
     * @param clientInstance The client instance to set.
     */
    public static void setClient(Client clientInstance) {
        client = clientInstance;
    }
}
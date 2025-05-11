package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.network.Command;
import ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
     * Sets the {@code Client} instance that will be used by the GUI.
     *
     * @param clientInstance The client instance to set.
     */
    public static void setClient(Client clientInstance) {
        client = clientInstance;
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
        try {
            System.out.println("[DEBUG] Loading FXML: /layout/WelcomeLobbyTemplate.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/WelcomeLobbyTemplate.fxml"));
            BorderPane root = loader.load();
            System.out.println("[DEBUG] FXML loaded successfully.");

            System.out.println("[DEBUG] Getting controller");
            WelcomeLobbyController welcomeController = loader.getController();
            System.out.println("[DEBUG] Injecting client into controller");
            welcomeController.setClient(client);
            welcomeController.setPrimaryStage(primaryStage);
            welcomeController.setProtocolWriter(client.getProtocolWriter());

            System.out.println("[DEBUG] Registering controller with client");
            client.setWelcomeController(welcomeController);
            client.getProtocolReader().setWelcomeController(welcomeController);

            System.out.println("[DEBUG] Loading window icon: /images/logo.jpg");
            InputStream iconStream = getClass().getResourceAsStream("/images/logo.jpg");
            if (iconStream == null) {
                throw new FileNotFoundException("Icon not found: /images/logo.jpg");
            }
            Image icon = new Image(iconStream);
            primaryStage.getIcons().add(icon);

            System.out.println("[DEBUG] Setting up and showing GUI");
            primaryStage.setMaximized(true);
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Welcome Lobby");
            primaryStage.show();
            System.out.println("[DEBUG] GUI launched successfully");

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to launch GUI: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Application start failed", e);
        }
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
        client.getProtocolWriter().sendCommandAndString(Command.QCNF, "YES");
        client.disconnect();
    }
}
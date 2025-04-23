package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * The {@code GUI} class is a JavaFX Application that loads the chat interface defined in the FXML file ChatTemplate
 * and connects it to the client. This class is responsible for launching the graphical user interface,
 * injecting the necessary protocol objects into the {@link WelcomeLobbyController}, and displaying the chat window.
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
     * @param args the command line arguments. These are passed on to the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This method loads the FXML layout for the chat interface,retrieves the {@link WelcomeLobbyController},
     * injects the {@link ch.unibas.dmi.dbis.cs108.network.ProtocolWriterClient} and {@link WelcomeLobbyController}
     * references from the client, and displays the chat window.
     * It assumes that the FXML file is located in the resource folder at {@code /ChatTemplate.fxml}.
     *
     * @param primaryStage the primary stage for this application, onto which the application scene can be set
     * @throws IOException if there is an error loading the FXML resource
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        Font.loadFont(getClass().getResourceAsStream("PixelEmulator-xq08.ttf"), 12);
        // Load the FXML file (ensure the correct resource path is used)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/WelcomeLobbyTemplate.fxml"));
        BorderPane root = loader.load();

        // Get the controller instance from the FXML loader
        WelcomeLobbyController welcomeController = loader.getController();
        welcomeController.setClient(client);
        welcomeController.setPrimaryStage(primaryStage);
        // Inject the ProtocolWriterClient into the ChatController.
        welcomeController.setProtocolWriter(client.getProtocolWriter());
        client.setWelcomeController(welcomeController);
        client.getProtocolReader().setWelcomeController(welcomeController);

        // Setup and show the GUI
//        primaryStage.setMaximized(true);
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Welcome Lobby");
        primaryStage.show();
    }

    /**
     * Sets the {@code Client} instance that will be used by the GUI.
     *
     * @param clientInstance the client instance to set
     */
    public static void setClient(Client clientInstance) {
        client = clientInstance;
    }
}
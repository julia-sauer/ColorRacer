package ch.unibas.dmi.dbis.cs108.example.gui.javafx;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.gui.ChatController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


/**
 * This is an example JavaFX-Application.
 */
public class GUI extends Application {

    private static Client client; // This will be set before launching the GUI.

    public static void setClient(Client clientInstance) {
        client = clientInstance;
    }
    /**
     * Launches the GUI with the ChatTemplate
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
//        String javaVersion = System.getProperty("java.version");
//        String javafxVersion = System.getProperty("javafx.version");
//
//        URL fmxl = getClass().getResource("/ChatTemplate.fxml");
//        FXMLLoader fxmlLoader = new FXMLLoader();
//        fxmlLoader.setLocation(fmxl);
//        TitledPane chatPane = fxmlLoader.load();
//
//        Scene scene = new Scene(chatPane, 440, 680);
//        stage.setScene(scene);
//        stage.show();

        // Load the FXML file (ensure the correct resource path is used)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChatTemplate.fxml"));
        Parent root = loader.load();

        // Retrieve the ChatController from the FXML loader.
        ChatController controller = loader.getController();

        // Inject the ProtocolWriterClient into the ChatController.
        controller.setProtocolWriter(client.getProtocolWriter());
        // Inject the ChatController into the client (or its ProtocolReaderClient)
        client.setChatController(controller);

        // Setup and show the GUI.
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Chat");
        primaryStage.show();
    }

}

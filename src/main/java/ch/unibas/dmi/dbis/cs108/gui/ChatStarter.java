package ch.unibas.dmi.dbis.cs108.gui;

import ch.unibas.dmi.dbis.cs108.client.Client;
import ch.unibas.dmi.dbis.cs108.example.gui.javafx.GUI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatStarter extends Application {

    private static Client client; // static field to hold the client

    public static void setClient(Client clientInstance) {
        client = clientInstance;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatTemplate.fxml"));
        Parent root = loader.load();

        // Inject the ProtocolWriter into controller
        ChatController controller = loader.getController();

        controller.setProtocolWriter(client.getProtocolWriter()); // important!
        client.setChatController(controller); // Let your client forward received messages

        // Show stage
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Chat");
        primaryStage.show();
    }
}

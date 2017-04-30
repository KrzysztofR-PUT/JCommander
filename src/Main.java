/**
 * Created by krzysztof on 11/04/2017.
 */

import helpers.BundleManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/views/root.fxml"));
        ResourceBundle bundle = ResourceBundle.getBundle("resources/lang", new Locale("en"));
        loader.setResources(bundle);

        Parent root = loader.load();

        primaryStage.setTitle("TotalCmd");
        primaryStage.setScene(new Scene(root, 900, 500));
        primaryStage.show();
    }
}

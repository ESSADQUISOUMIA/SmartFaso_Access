package com.example.facialrecognition;

import com.example.facialrecognition.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.opencv.core.Core;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    // Statique pour charger la bibliothèque OpenCV au démarrage de l'application
    static {
        try {
            // Assurez-vous que le chemin vers les bibliothèques natives d'OpenCV est correctement défini
            // dans les VM options de votre IDE ou au moment de l'exécution :
            // -Djava.library.path="/path/to/your/opencv/build/bin" (Windows/Linux)
            // -Djava.library.path="/path/to/your/opencv/build/lib" (macOS)
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV " + Core.VERSION + " loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load OpenCV native library. " + e);
            System.err.println("Make sure you have set the -Djava.library.path VM option correctly.");
            // Afficher une alerte et quitter si OpenCV ne peut pas être chargé
            showAlertAndExit("Erreur de chargement OpenCV",
                    "Impossible de charger la bibliothèque native d'OpenCV. " +
                            "Vérifiez votre installation et la configuration de -Djava.library.path.",
                    Alert.AlertType.ERROR);
        }
    }
    // Dans votre classe principale Application


    @Override
    public void start(Stage primaryStage) throws IOException {
        // Initialiser la base de données SQLite
        DatabaseManager.initializeDatabase();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/facialrecognition/view/MainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setTitle("Système de Reconnaissance Faciale");
            // Optional: Set application icon
            // primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon.png"))));
            primaryStage.setScene(scene);
            primaryStage.show();

            // Gérer la fermeture de l'application pour libérer les ressources OpenCV
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Application is closing. Releasing resources...");
                // Ici, vous devriez ajouter le code pour libérer les ressources de la webcam
                // qui sera gérée par le contrôleur principal (MainController)
                // par exemple, en appelant une méthode de nettoyage sur le contrôleur.
            });

        } catch (Exception e) {
            e.printStackTrace(); // AJOUTEZ CECI
            showAlertAndExit("Erreur de démarrage de l'application",
                    "Une erreur est survenue lors du démarrage de l'interface utilisateur : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void showAlertAndExit(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1); // Terminer l'application
    }
}
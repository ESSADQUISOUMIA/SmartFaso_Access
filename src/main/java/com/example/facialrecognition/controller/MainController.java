package com.example.facialrecognition.controller;

import com.example.facialrecognition.database.AccessLogRepository;
import com.example.facialrecognition.database.UserRepository;
import com.example.facialrecognition.model.User;
import com.example.facialrecognition.util.FacialRecognitionUtil;
import com.example.facialrecognition.util.ImageUtils;
// Ajoutez ces imports en haut de votre fichier
import com.example.facialrecognition.view.DashboardView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainController implements Initializable {

    @FXML private ImageView cameraView;
    @FXML private ImageView capturedImageView;
    @FXML private Label cameraStatusLabel;
    @FXML private Label cameraOffLabel;
    @FXML private Label noCapturedImageLabel;
    @FXML private Label recognitionResultLabel;
    @FXML private Button startCameraButton;
    @FXML private Button captureButton;
    @FXML private Button recognizeButton;
    @FXML private ProgressIndicator cameraLoadingIndicator;

    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private Mat currentFrame;
    private Mat capturedFrame;
    private boolean cameraActive = false;

    // Constante définissant le seuil de similarité à utiliser pour l'authentification
    private static final double AUTHENTICATION_THRESHOLD = 0.92; // Augmentation du seuil pour plus de sécurité

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser les composants UI
        updateUI();
    }

    @FXML
    private void startCamera() {
        if (!cameraActive) {
            // Afficher l'indicateur de chargement
            cameraLoadingIndicator.setVisible(true);

            // Démarrer la caméra dans un thread séparé
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    capture = new VideoCapture(0);
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        if (capture.isOpened()) {
                            cameraActive = true;
                            startCameraButton.setText("Arrêter la caméra");
                            captureButton.setDisable(false);
                            cameraStatusLabel.setText("Caméra active");
                            cameraOffLabel.setVisible(false);

                            // Démarrer la capture de frames
                            startFrameGrabber();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Erreur de caméra",
                                    "Impossible d'accéder à la caméra",
                                    "Vérifiez que votre caméra est connectée et n'est pas utilisée par une autre application.");
                        }
                        cameraLoadingIndicator.setVisible(false);
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Erreur de caméra",
                                "Erreur lors de l'initialisation de la caméra",
                                getException().getMessage());
                        cameraLoadingIndicator.setVisible(false);
                    });
                }
            };

            new Thread(task).start();
        } else {
            stopCamera();
        }
    }

    private void startFrameGrabber() {
        currentFrame = new Mat();
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(() -> {
            if (capture.isOpened()) {
                try {
                    capture.read(currentFrame);
                    if (!currentFrame.empty()) {
                        Image image = mat2Image(currentFrame);
                        Platform.runLater(() -> cameraView.setImage(image));
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la capture de frame: " + e.getMessage());
                }
            }
        }, 0, 33, TimeUnit.MILLISECONDS); // Environ 30 FPS
    }

    private void stopCamera() {
        if (timer != null && !timer.isShutdown()) {
            try {
                timer.shutdown();
                timer.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception lors de l'arrêt du timer: " + e.getMessage());
            }
        }

        if (capture != null && capture.isOpened()) {
            capture.release();
        }

        cameraActive = false;
        startCameraButton.setText("Démarrer la caméra");
        captureButton.setDisable(true);
        recognizeButton.setDisable(true);
        cameraStatusLabel.setText("Caméra éteinte");
        cameraOffLabel.setVisible(true);
        cameraView.setImage(null);
    }

    @FXML
    private void captureImage() {
        if (cameraActive && !currentFrame.empty()) {
            capturedFrame = currentFrame.clone();
            Image capturedImage = mat2Image(capturedFrame);
            capturedImageView.setImage(capturedImage);
            noCapturedImageLabel.setVisible(false);

            // Vérifier si un visage est détecté dans l'image capturée
            boolean faceDetected = FacialRecognitionUtil.detectFace(capturedFrame);
            if (faceDetected) {
                recognizeButton.setDisable(false);
                recognitionResultLabel.setText("Visage détecté! Prêt pour l'authentification.");
                recognitionResultLabel.setTextFill(Color.GREEN);
            } else {
                recognizeButton.setDisable(true);
                recognitionResultLabel.setText("Aucun visage détecté! Réessayez.");
                recognitionResultLabel.setTextFill(Color.RED);
            }
        }
    }

    @FXML
    private void recognizeFace() {
        if (capturedFrame == null || capturedFrame.empty()) {
            showAlert(Alert.AlertType.WARNING, "Authentification",
                    "Image requise", "Veuillez capturer une image du visage d'abord.");
            return;
        }

        // Extraire les caractéristiques du visage capturé
        Mat faceFeatures = FacialRecognitionUtil.extractFaceFeatures(capturedFrame);
        if (faceFeatures == null) {
            recognitionResultLabel.setText("Impossible d'extraire les caractéristiques du visage.");
            recognitionResultLabel.setTextFill(Color.RED);
            return;
        }

        // Lancer la reconnaissance dans un thread séparé pour ne pas bloquer l'UI
        Task<AuthenticationResult> authTask = new Task<>() {
            @Override
            protected AuthenticationResult call() throws Exception {
                try {
                    // Récupérer tous les utilisateurs
                    List<User> users = UserRepository.getAllUsers();
                    System.out.println("Nombre d'utilisateurs trouvés: " + users.size());

                    // Vérifier qu'il y a des utilisateurs dans la base de données
                    if (users.isEmpty()) {
                        System.out.println("Aucun utilisateur dans la base de données");
                        byte[] capturedBiometricData = ImageUtils.matToBytes(capturedFrame);
                        AccessLogRepository.logAccess(null, "DENIED", capturedBiometricData);
                        return new AuthenticationResult(false, null, "Aucun utilisateur enregistré dans le système");
                    }

                    User bestMatchUser = null;
                    double highestSimilarity = 0.0;

                    // Parcourir chaque utilisateur et comparer les visages
                    for (User user : users) {
                        System.out.println("Vérification de l'utilisateur: " + user.getName() + " (ID: " + user.getId() + ")");

                        byte[] storedBiometricData = user.getBiometricData();
                        if (storedBiometricData != null && storedBiometricData.length > 0) {
                            System.out.println("Données biométriques trouvées pour l'utilisateur " + user.getId() + ", taille: " + storedBiometricData.length + " octets");

                            // Convertir les données biométriques stockées en Mat
                            Mat storedImage = ImageUtils.bytesToMat(storedBiometricData);
                            if (storedImage == null || storedImage.empty()) {
                                System.err.println("Impossible de convertir les données biométriques en image pour l'utilisateur " + user.getId());
                                continue;
                            }

                            System.out.println("Image récupérée pour l'utilisateur " + user.getId() + ", dimensions: " +
                                    storedImage.width() + "x" + storedImage.height());

                            // Extraire les caractéristiques du visage stocké
                            Mat storedFeatures = FacialRecognitionUtil.extractFaceFeatures(storedImage);
                            if (storedFeatures == null) {
                                System.err.println("Impossible d'extraire les caractéristiques du visage pour l'utilisateur " + user.getId());
                                continue;
                            }

                            System.out.println("Caractéristiques extraites pour l'utilisateur " + user.getId());

                            // Comparer les caractéristiques
                            double similarityScore = FacialRecognitionUtil.getSimilarity(faceFeatures, storedFeatures);
                            System.out.println("Score de similarité avec " + user.getName() + ": " + similarityScore);

                            // Conserver l'utilisateur avec le meilleur score
                            if (similarityScore > highestSimilarity) {
                                highestSimilarity = similarityScore;
                                bestMatchUser = user;
                            }
                        } else {
                            System.out.println("Aucune donnée biométrique pour l'utilisateur " + user.getId());
                        }
                    }

                    // Vérifier si le meilleur score dépasse le seuil
                    if (bestMatchUser != null && highestSimilarity >= AUTHENTICATION_THRESHOLD) {
                        System.out.println("CORRESPONDANCE TROUVÉE avec l'utilisateur " + bestMatchUser.getName() + ", score: " + highestSimilarity);
                        // Enregistrer l'accès réussi
                        AccessLogRepository.logAccess(bestMatchUser.getId(), "GRANTED", null);
                        return new AuthenticationResult(true, bestMatchUser, "Score de similarité: " + String.format("%.2f", highestSimilarity));
                    } else {
                        // Aucune correspondance trouvée, enregistrer l'accès refusé
                        String message;
                        if (bestMatchUser != null) {
                            message = "Meilleure correspondance: " + bestMatchUser.getName() + " (Score: " + String.format("%.2f", highestSimilarity) + " < seuil " + AUTHENTICATION_THRESHOLD + ")";
                            System.out.println("CORRESPONDANCE INSUFFISANTE: " + message);
                        } else {
                            message = "Aucune correspondance détectée";
                            System.out.println("AUCUNE CORRESPONDANCE TROUVÉE avec aucun utilisateur");
                        }

                        byte[] capturedBiometricData = ImageUtils.matToBytes(capturedFrame);
                        AccessLogRepository.logAccess(null, "DENIED", capturedBiometricData);
                        return new AuthenticationResult(false, null, message);
                    }

                } catch (SQLException e) {
                    System.err.println("Erreur lors de l'authentification: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }

            @Override
            protected void succeeded() {
                AuthenticationResult result = getValue();
                Platform.runLater(() -> showAuthenticationResult(result));
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    recognitionResultLabel.setText("Erreur lors de l'authentification: " + getException().getMessage());
                    recognitionResultLabel.setTextFill(Color.RED);
                });
            }
        };

        // Mettre à jour l'UI pour indiquer que l'authentification est en cours
        recognitionResultLabel.setText("Authentification en cours...");
        recognitionResultLabel.setTextFill(Color.BLUE);
        recognizeButton.setDisable(true);

        // Lancer la tâche d'authentification
        new Thread(authTask).start();
    }

    private void showAuthenticationResult(AuthenticationResult result) {
        if (result.isAuthenticated()) {
            // Authentification réussie
            recognitionResultLabel.setText("ACCÈS AUTORISÉ - Bienvenue, " + result.getUser().getName() +
                    " (" + result.getDetails() + ")");
            recognitionResultLabel.setTextFill(Color.GREEN);

            // Vous pourriez ici déclencher une action comme l'ouverture d'une porte
            // ou le déverrouillage d'un système
            playSuccessSound();
        } else {
            // Authentification échouée
            recognitionResultLabel.setText("ACCÈS REFUSÉ - " + result.getDetails());
            recognitionResultLabel.setTextFill(Color.RED);
            playFailureSound();
        }

        // Réactiver le bouton après un court délai
        Timer timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> recognizeButton.setDisable(false));
            }
        }, 2000);
    }

    private void playSuccessSound() {
        // Implémentation du son de succès si souhaité
        // Par exemple, utilisation de JavaFX Media ou autre bibliothèque de son
        System.out.println("Son de succès");
    }

    private void playFailureSound() {
        // Implémentation du son d'échec si souhaité
        System.out.println("Son d'échec");
    }

    @FXML
    private void switchToUserManagementView() {
        try {
            // Arrêter la caméra avant de changer de vue
            stopCamera();

            // Charger la vue de gestion des utilisateurs
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/facialrecognition/view/UserManagementView.fxml"));
            Parent userManagementView = loader.load();

            // Récupérer la scène actuelle
            Scene currentScene = startCameraButton.getScene();

            // Remplacer le contenu de la scène
            currentScene.setRoot(userManagementView);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue de gestion des utilisateurs: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de charger la vue de gestion des utilisateurs",
                    e.getMessage());
        }
    }
    @FXML
    private void switchToDashbord() {
        try {
            // Arrêter la caméra avant de changer de vue
            stopCamera();

            // Charger la vue de gestion des utilisateurs
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/facialrecognition/view/Dashboard.fxml"));
            Parent userManagementView = loader.load();

            // Récupérer la scène actuelle
            Scene currentScene = startCameraButton.getScene();

            // Remplacer le contenu de la scène
            currentScene.setRoot(userManagementView);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue de gestion des utilisateurs: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible de charger la vue de gestion des utilisateurs",
                    e.getMessage());
        }
    }

    private Image mat2Image(Mat frame) {
        // Convertir Mat en Image JavaFX
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    private void updateUI() {
        cameraOffLabel.setVisible(true);
        noCapturedImageLabel.setVisible(true);
        captureButton.setDisable(true);
        recognizeButton.setDisable(true);
        cameraLoadingIndicator.setVisible(false);
        recognitionResultLabel.setText("");
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void shutdown() {
        stopCamera();
    }

    // Classe interne pour stocker le résultat de l'authentification
    private static class AuthenticationResult {
        private final boolean authenticated;
        private final User user;
        private final String details;

        public AuthenticationResult(boolean authenticated, User user, String details) {
            this.authenticated = authenticated;
            this.user = user;
            this.details = details;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public User getUser() {
            return user;
        }

        public String getDetails() {
            return details;
        }
    }
}
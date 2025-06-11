package com.example.facialrecognition.controller;

import com.example.facialrecognition.database.UserRepository;
import com.example.facialrecognition.model.User;
import com.example.facialrecognition.service.UserService;
import com.example.facialrecognition.util.FacialRecognitionUtil;
import com.example.facialrecognition.util.ImageUtils;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserManagementController implements Initializable {

    @FXML private TextField enrollmentNameField;
    @FXML private ImageView cameraView;
    @FXML private ImageView capturedImageView;
    @FXML private Label cameraStatusLabel;
    @FXML private Label cameraOffLabel;
    @FXML private Label noCapturedImageLabel;
    @FXML private Label enrollmentInfoLabel;
    @FXML private Button startCameraButton;
    @FXML private Button captureButton;
    @FXML private Button enrollButton;
    @FXML private ProgressIndicator cameraLoadingIndicator;

    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, String> biometricDataColumn;
    @FXML private TableColumn<User, String> enrollmentDateColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private Label totalUsersLabel;
    @FXML private Button refreshUsersButton;

    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private Mat currentFrame;
    private Mat capturedFrame;
    private boolean cameraActive = false;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration des colonnes du TableView
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        biometricDataColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().hasBiometricData() ? "Disponible" : "Non disponible"));

        // Configuration de la colonne d'actions
        setupActionsColumn();

        // Charger les utilisateurs existants
        loadUsersFromDatabase();

        // Mettre à jour l'interface utilisateur
        updateUI();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                final TableCell<User, Void> cell = new TableCell<User, Void>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox pane = new HBox(5, editBtn, deleteBtn);

                    {
                        // Style des boutons
                        editBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; -fx-font-weight: bold;");
                        deleteBtn.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold;");

                        // Actions des boutons
                        editBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            editUser(user);
                        });

                        deleteBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            deleteUser(user);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
                return cell;
            }
        });
    }

    @FXML
    public void loadUsersFromDatabase() {
        try {
            ObservableList<User> users = UserRepository.getAllUsers();
            userTableView.setItems(users);

            // Mettre à jour le compteur d'utilisateurs
            totalUsersLabel.setText("Total Users: " + users.size());

            // Afficher les utilisateurs dans la console (pour déboguer)
            UserRepository.displayAllUsers();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger les utilisateurs", e.getMessage());
        }
    }
    @FXML
    private void switchToAuthentication() {
        try {
            // Arrêter la caméra avant de changer de vue
            stopCamera();

            // Charger la vue de gestion des utilisateurs
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/facialrecognition/view/MainView.fxml"));
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

    private void editUser(User user) {
        try {
            // Créer un dialogue pour l'édition
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Modifier l'utilisateur");
            dialog.setHeaderText("Modifier les informations de " + user.getName());

            // Boutons
            ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Créer le formulaire
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            TextField nameField = new TextField(user.getName());
            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("active", "inactive", "blocked");
            statusCombo.setValue(user.getStatus());

            grid.add(new Label("Nom:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Statut:"), 0, 1);
            grid.add(statusCombo, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // Convertir le résultat
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    user.setName(nameField.getText());
                    user.setStatus(statusCombo.getValue());
                    return user;
                }
                return null;
            });

            Optional<User> result = dialog.showAndWait();
            result.ifPresent(updatedUser -> {
                try {
                    boolean success = UserRepository.updateUser(updatedUser);
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Modification réussie",
                                "Utilisateur modifié", "Les informations de l'utilisateur ont été mises à jour avec succès.");
                        loadUsersFromDatabase();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur de modification",
                                "Modification échouée", "Impossible de modifier l'utilisateur.");
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                            "Erreur lors de la modification", e.getMessage());
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de l'édition", e.getMessage());
        }
    }

    private void deleteUser(User user) {
        try {
            // Confirmation avant suppression
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmer la suppression");
            confirmAlert.setHeaderText("Supprimer l'utilisateur " + user.getName());
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet utilisateur ? Cette action est irréversible.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = UserRepository.deleteUser(user.getId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Suppression réussie",
                            "Utilisateur supprimé", "L'utilisateur a été supprimé avec succès.");
                    loadUsersFromDatabase();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur de suppression",
                            "Suppression échouée", "Impossible de supprimer l'utilisateur.");
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Erreur lors de la suppression", e.getMessage());
        }
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
        enrollButton.setDisable(true);
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
            enrollButton.setDisable(false);

            // Vérifier si le visage est détecté dans l'image
            boolean faceDetected = FacialRecognitionUtil.detectFace(capturedFrame);
            if (!faceDetected) {
                enrollmentInfoLabel.setText("Aucun visage détecté! Réessayez.");
                enrollmentInfoLabel.setTextFill(Color.RED);
                enrollButton.setDisable(true);
            } else {
                enrollmentInfoLabel.setText("Visage détecté! Prêt pour l'enregistrement.");
                enrollmentInfoLabel.setTextFill(Color.GREEN);
            }
        }
    }

    @FXML
    private void enrollUser() {
        String name = enrollmentNameField.getText().trim();

        // Validation du nom
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Nom requis", "Veuillez entrer un nom pour l'utilisateur.");
            return;
        }

        // Validation de l'image
        if (capturedFrame == null || capturedFrame.empty()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Image requise", "Veuillez capturer une image du visage.");
            return;
        }

        try {
            // Vérifier si l'utilisateur existe déjà
            if (UserRepository.userExists(name)) {
                showAlert(Alert.AlertType.WARNING, "Utilisateur existant",
                        "Un utilisateur avec ce nom existe déjà",
                        "Veuillez choisir un nom différent.");
                return;
            }

            // Convertir l'image en bytes pour stockage
            byte[] biometricData = ImageUtils.matToBytes(capturedFrame);

            // Insérer l'utilisateur dans la base de données
            int userId = UserRepository.insertUser(name, biometricData);

            // Rafraîchir la liste des utilisateurs
            loadUsersFromDatabase();

            // Réinitialiser les champs
            resetEnrollmentFields();

            // Afficher un message de succès
            enrollmentInfoLabel.setText("Utilisateur enregistré avec succès! (ID: " + userId + ")");
            enrollmentInfoLabel.setTextFill(Color.GREEN);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible d'enregistrer l'utilisateur", e.getMessage());
            enrollmentInfoLabel.setText("Erreur lors de l'enregistrement!");
            enrollmentInfoLabel.setTextFill(Color.RED);
        }
    }

    private void resetEnrollmentFields() {
        enrollmentNameField.clear();
        capturedFrame = null;
        capturedImageView.setImage(null);
        noCapturedImageLabel.setVisible(true);
        enrollButton.setDisable(true);
    }

    @FXML
    public void switchToUserManagementView() {
        // Déjà dans la vue de gestion des utilisateurs, rien à faire
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
        enrollButton.setDisable(true);
        cameraLoadingIndicator.setVisible(false);
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
}
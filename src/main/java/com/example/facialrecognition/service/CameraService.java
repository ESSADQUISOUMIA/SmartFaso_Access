package com.example.facialrecognition.service;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraService {
    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private boolean cameraActive = false;
    private ImageView currentFrame; // Pour afficher l'image

    public CameraService() {
        this.capture = new VideoCapture();
    }

    public boolean startCamera(ImageView imageView) {
        if (cameraActive) {
            return true; // Déjà démarré
        }

        this.currentFrame = imageView;

        // Ouvrir la caméra (0 pour la webcam par défaut)
        if (!capture.open(0, Videoio.CAP_ANY)) {
            System.err.println("Impossible d'ouvrir la caméra");
            return false;
        }

        // Démarrer la capture vidéo
        cameraActive = true;

        // Créer un thread pour mettre à jour l'image
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(this::grabFrame, 0, 33, TimeUnit.MILLISECONDS);

        return true;
    }

    public void stopCamera() {
        if (timer != null && !timer.isShutdown()) {
            try {
                timer.shutdown();
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception lors de l'arrêt de la caméra: " + e.getMessage());
            }
        }

        if (capture.isOpened()) {
            capture.release();
        }

        cameraActive = false;
    }

    public Mat captureFrame() {
        Mat frame = new Mat();
        if (capture.isOpened()) {
            capture.read(frame);
        }
        return frame;
    }

    // Ajout de la méthode captureFrameAsImage()
    public Image captureFrameAsImage() {
        Mat frame = captureFrame();
        if (!frame.empty()) {
            return mat2Image(frame);
        }
        return null;
    }

    private void grabFrame() {
        Mat frame = new Mat();
        if (capture.isOpened()) {
            try {
                capture.read(frame);
                if (!frame.empty()) {
                    Image image = mat2Image(frame); // Utilise la méthode statique de ImageUtils
                    Platform.runLater(() -> {
                        if (currentFrame != null) {
                            currentFrame.setImage(image);
                        }
                    });
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la capture d'image: " + e.getMessage());
            } finally {
                frame.release(); // Important de libérer la mémoire
            }
        }
        frame.release(); // Libérer la mémoire de la Mat après utilisation
    }

    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        org.opencv.imgcodecs.Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public boolean isCameraActive() {
        return cameraActive;
    }
}
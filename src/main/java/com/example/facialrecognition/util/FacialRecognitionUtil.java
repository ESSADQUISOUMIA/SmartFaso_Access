package com.example.facialrecognition.util;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.FaceRecognizerSF;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FacialRecognitionUtil {

    private static CascadeClassifier faceDetector;
    private static final double FACE_RECOGNITION_THRESHOLD = 0.8; // Seuil de confiance pour la reconnaissance faciale

    // Initialisation du détecteur de visages avec Haar Cascade
    static {
        try {
            // Charger le classificateur depuis les ressources
            File cascadeDir = new File(System.getProperty("java.io.tmpdir"), "haarcascades");
            if (!cascadeDir.exists()) {
                cascadeDir.mkdirs();
            }

            File cascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");

            // Si le fichier n'existe pas, le copier depuis les ressources
            if (!cascadeFile.exists()) {
                try (InputStream is = FacialRecognitionUtil.class.getResourceAsStream("/opencv/haarcascade_frontalface_alt.xml")) {
                    if (is == null) {
                        throw new IOException("Impossible de trouver le fichier de cascade dans les ressources");
                    }

                    Files.copy(is, cascadeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());

            if (faceDetector.empty()) {
                throw new RuntimeException("Impossible de charger le classificateur de visages");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du détecteur de visages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Détecte si un visage est présent dans l'image
     * @param image Image à analyser
     * @return true si un visage est détecté, false sinon
     */
    public static boolean detectFace(Mat image) {
        if (image == null || image.empty()) {
            return false;
        }

        // Convertir l'image en niveaux de gris pour la détection
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Détecter les visages
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(grayImage, faces);

        return !faces.empty();
    }

    /**
     * Extrait le rectangle du visage principal dans l'image
     * @param image Image source
     * @return Rectangle du visage ou null si aucun visage n'est détecté
     */
    public static Rect getFaceRect(Mat image) {
        if (image == null || image.empty()) {
            return null;
        }

        // Convertir l'image en niveaux de gris pour la détection
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Détecter les visages
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(grayImage, faces);

        Rect[] facesArray = faces.toArray();
        if (facesArray.length > 0) {
            // Retourner le premier visage (supposé être le principal)
            return facesArray[0];
        }

        return null;
    }

    /**
     * Extrait l'image du visage à partir de l'image source
     * @param image Image source
     * @return Image du visage ou null si aucun visage n'est détecté
     */
    public static Mat extractFace(Mat image) {
        Rect faceRect = getFaceRect(image);
        if (faceRect != null) {
            return new Mat(image, faceRect);
        }
        return null;
    }

    /**
     * Extrait les caractéristiques du visage pour la reconnaissance
     * @param faceImage Image du visage
     * @return Vecteur de caractéristiques
     */
    public static Mat extractFaceFeatures(Mat faceImage) {
        // Note: Cette méthode est un placeholder
        // Dans une implémentation réelle, vous utiliseriez un modèle de deep learning
        // comme FaceNet, ArcFace, etc. pour extraire des caractéristiques significatives

        // Pour l'exemple, nous renvoyons simplement une version redimensionnée et normalisée de l'image
        Mat resized = new Mat();
        Imgproc.resize(faceImage, resized, new Size(100, 100));

        Mat normalized = new Mat();
        Core.normalize(resized, normalized, 0, 1, Core.NORM_MINMAX, CvType.CV_32F);

        return normalized;
    }

    /**
     * Compare deux vecteurs de caractéristiques de visage
     * @param features1 Premier vecteur de caractéristiques
     * @param features2 Deuxième vecteur de caractéristiques
     * @return Score de similarité (0-1, où 1 est identique)
     */
    public static double compareFaces(Mat features1, Mat features2) {
        // Note: Dans une implémentation réelle, vous utiliseriez une méthode plus sophistiquée
        // comme la distance cosinus ou euclidienne normalisée

        // Pour l'exemple, nous utilisons une norme L2 normalisée
        double distance = Core.norm(features1, features2, Core.NORM_L2);
        // Convertir en similarité (0-1)
        return Math.max(0, 1 - distance / 10);
    }

    public static double getSimilarity(Mat features1, Mat features2) {
        if (features1 == null || features2 == null || features1.empty() || features2.empty()) {
            return 0.0;
        }

        // Utiliser la distance cosinus pour une meilleure précision
        double dotProduct = features1.dot(features2);
        double norm1 = Core.norm(features1, Core.NORM_L2);
        double norm2 = Core.norm(features2, Core.NORM_L2);

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }
    private static double calculateDistance(Mat features1, Mat features2) {
        // Cette implémentation dépend de votre algorithme d'extraction de caractéristiques
        // Voici un exemple avec une distance euclidienne normalisée

        // Si vous utilisez OpenCV, vous pourriez faire quelque chose comme:
        // return Core.norm(features1, features2, Core.NORM_L2) / features1.rows();

        // Implémentation simplifiée (à adapter)
        double sum = 0;
        int numElements = features1.rows() * features1.cols();

        for (int i = 0; i < features1.rows(); i++) {
            for (int j = 0; j < features1.cols(); j++) {
                double diff = features1.get(i, j)[0] - features2.get(i, j)[0];
                sum += diff * diff;
            }
        }

        // Normaliser la distance pour qu'elle soit entre 0 et 1
        return Math.sqrt(sum) / numElements;
    }
    /**
     * Vérifie si deux visages correspondent
     * @param features1 Premier vecteur de caractéristiques
     * @param features2 Deuxième vecteur de caractéristiques
     * @return true si les visages correspondent, false sinon
     */
    public static boolean matchFaces(Mat features1, Mat features2) {
        return getSimilarity(features1, features2) >= 0.7; // Utilise un seuil de 0.7
    }

}
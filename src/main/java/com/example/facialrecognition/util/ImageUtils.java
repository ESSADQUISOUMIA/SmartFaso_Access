package com.example.facialrecognition.util;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class ImageUtils {

    /**
     * Convertit une matrice OpenCV (Mat) en tableau d'octets
     * @param image Matrice OpenCV
     * @return Tableau d'octets représentant l'image
     */
    public static byte[] matToBytes(Mat image) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", image, buffer);
        return buffer.toArray();
    }

    /**
     * Convertit un tableau d'octets en matrice OpenCV (Mat)
     * @param imageData Tableau d'octets
     * @return Matrice OpenCV
     */
    public static Mat bytesToMat(byte[] imageData) {
        return Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_UNCHANGED);
    }

    /**
     * Redimensionne une image représentée par un Mat
     * @param image Image source
     * @param width Largeur cible
     * @param height Hauteur cible
     * @return Image redimensionnée
     */
    public static Mat resizeImage(Mat image, int width, int height) {
        Mat resized = new Mat();
        org.opencv.imgproc.Imgproc.resize(
                image,
                resized,
                new org.opencv.core.Size(width, height)
        );
        return resized;
    }

    /**
     * Convertit un BufferedImage en tableau d'octets
     * @param image BufferedImage source
     * @param format Format de sortie (png, jpg, etc.)
     * @return Tableau d'octets
     */
    public static byte[] bufferedImageToBytes(BufferedImage image, String format) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la conversion BufferedImage en bytes", e);
        }
    }

    /**
     * Convertit un tableau d'octets en BufferedImage
     * @param imageData Tableau d'octets
     * @return BufferedImage
     */
    public static BufferedImage bytesToBufferedImage(byte[] imageData) {
        try {
            return ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la conversion bytes en BufferedImage", e);
        }
    }
}
package pdl.backend.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import pdl.backend.dao.ImageDescriptorDAO;
import pdl.backend.model.ImageDescriptor;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImageSimilarityService {

    private final ImageDescriptorDAO descriptorDAO;

    public ImageSimilarityService(ImageDescriptorDAO descriptorDAO) {
        this.descriptorDAO = descriptorDAO;
    }

    // =========================
    // FIND SIMILAR
    // =========================
    public List<SimilarImageResult> findSimilar(
            Long imageId,
            int number,
            String descriptorName) {

        if (number <= 0) {
            throw new IllegalArgumentException("number doit être > 0");
        }

        List<ImageDescriptor> targetDescriptors =
                descriptorDAO.retrieveAllForImage(imageId);

        if (targetDescriptors.isEmpty()) {
            throw new NoSuchElementException(
                    "Aucun descripteur pour image id=" + imageId);
        }

        ImageDescriptor target = targetDescriptors.get(0);
        Map<String, double[]> targetMap = target.getDescriptors();
        double[] base = targetMap.get(descriptorName.toUpperCase());

        if (base == null) {
            throw new IllegalArgumentException(
                    "Descripteur invalide : " + descriptorName);
        }

        return descriptorDAO.retrieveAll()
                .stream()
                .filter(desc -> !desc.getImageId().equals(imageId))
                .map(desc -> {
                    double[] candidate = desc.getDescriptors().get(descriptorName.toUpperCase());
                    if (candidate == null) return null;
                    double distance = chiSquare(base, candidate);
                    return new SimilarImageResult(desc.getImageId(), distance);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(SimilarImageResult::getScore))
                .limit(number)
                .collect(Collectors.toList());
    }

    // =========================
    // DISTANCE CHI-SQUARE
    // =========================
    private double chiSquare(double[] h1, double[] h2) {
        double sum = 0;
        for (int i = 0; i < h1.length; i++) {
            double num = Math.pow(h1[i] - h2[i], 2);
            double denom = h1[i] + h2[i] + 1e-10;
            sum += num / denom;
        }
        return sum;
    }

    // =========================
    // GRAYSCALE HISTOGRAM
    // =========================
    public double[] computeGrayscaleHistogram(byte[] bytes) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        if (img == null) throw new IOException("Image non lisible");

        int w = img.getWidth();
        int h = img.getHeight();
        long totalPixels = (long) w * h;
        long[] histogram = new long[256];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int gray = (int)((0.299*((rgb >> 16) & 0xFF) +
                                  0.587*((rgb >> 8) & 0xFF) +
                                  0.114*(rgb & 0xFF)));
                histogram[gray]++;
            }
        }

        double[] histNorm = new double[256];
        for (int i = 0; i < 256; i++) {
            histNorm[i] = histogram[i] / (double) totalPixels;
        }

        return histNorm;
    }

    // =========================
    // GRADIENT 1D
    // =========================
    public double[] computeGradient1D(byte[] bytes) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        if (img == null) throw new IOException("Image non lisible");

        int width = 360;
        int height = 360;
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();

        double[] gradient = new double[width * height];
        double maxGrad = Math.sqrt(255*255 + 255*255);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = resized.getRGB(x, y) & 0xFF;
                int right = x < width - 1 ? (resized.getRGB(x + 1, y) & 0xFF) : rgb;
                int down = y < height - 1 ? (resized.getRGB(x, y + 1) & 0xFF) : rgb;

                double dx = right - rgb;
                double dy = down - rgb;
                double grad = Math.sqrt(dx*dx + dy*dy);
                gradient[y * width + x] = grad / maxGrad; // normalisé 0..1
            }
        }

        return gradient;
    }

    // =========================
    // RGB DESCRIPTOR
    // =========================
    public double[] computeRGBDescriptor(byte[] bytes) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
        if (img == null) throw new IOException("Image non lisible");

        int w = img.getWidth();
        int h = img.getHeight();
        long totalPixels = (long) w * h;

        double rSum = 0, gSum = 0, bSum = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                rSum += (rgb >> 16) & 0xFF;
                gSum += (rgb >> 8) & 0xFF;
                bSum += rgb & 0xFF;
            }
        }

        return new double[]{rSum / totalPixels / 255.0,
                            gSum / totalPixels / 255.0,
                            bSum / totalPixels / 255.0};
    }

    public ImageDescriptorDAO getDescriptorDAO() {
        return this.descriptorDAO;
    }

    // =========================
    // RESULT CLASS
    // =========================
    public static class SimilarImageResult {
        private final Long id;
        private final double score;

        public SimilarImageResult(Long id, double score) {
            this.id = id;
            this.score = score;
        }

        public Long getId() { return id; }
        public double getScore() { return score; }
    }
}
package pdl.backend.utils;

import boofcv.io.image.UtilImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pdl.backend.dao.ImageDescriptorDAO;
import pdl.backend.model.ImageDescriptor;



@Service
public class ImageIndexer {

    private final ImageDescriptorDAO descriptorDAO;

    public ImageIndexer(ImageDescriptorDAO descriptorDAO) {
        this.descriptorDAO = descriptorDAO;
    }

    @Transactional
    public void indexImage(String imagePath) {

        BufferedImage img = UtilImageIO.loadImage(imagePath);
        if (img == null) {
            throw new RuntimeException("Impossible de lire l'image");
        }

        Map<String, double[]> descriptors = new HashMap<>();

        descriptors.put("RGB", computeRGB(img));
        descriptors.put("GRADIENT", computeGradient(img));
        // HS optionnel pour l’instant

        File f = new File(imagePath);
        ImageDescriptor descriptor = new ImageDescriptor(f.getName(), descriptors);

        descriptorDAO.create(descriptor);
    }

    private double[] computeRGB(BufferedImage img) {
        int bins = 4;
        double[] hist = new double[bins * bins * bins];

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int ri = r * bins / 256;
                int gi = g * bins / 256;
                int bi = b * bins / 256;

                int index = ri * bins * bins + gi * bins + bi;
                hist[index]++;
            }
        }

        normalize(hist);
        return hist;
    }

    private double[] computeGradient(BufferedImage img) {
        int bins = 8;
        double[] hist = new double[bins];

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);

                int gray = (((rgb >> 16) & 0xFF)
                          + ((rgb >> 8) & 0xFF)
                          + (rgb & 0xFF)) / 3;

                int bin = gray * bins / 256;
                hist[bin]++;
            }
        }

        normalize(hist);
        return hist;
    }

    private void normalize(double[] hist) {
        double sum = 0;
        for (double v : hist) sum += v;

        if (sum == 0) return;

        for (int i = 0; i < hist.length; i++) {
            hist[i] /= sum;
        }
    }
}
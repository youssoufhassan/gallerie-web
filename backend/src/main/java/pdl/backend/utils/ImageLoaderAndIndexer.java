package pdl.backend.utils;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import pdl.backend.dao.ImageDao;
import pdl.backend.dao.ImageDescriptorDAO;
import pdl.backend.model.Image;
import pdl.backend.model.ImageDescriptor;

@Component
public class ImageLoaderAndIndexer implements CommandLineRunner {

    private final ImageDao imageDao;
    private final ImageDescriptorDAO descriptorDAO;
    private final ImageSimilarityService imageSimilarityService;

    private final String imagesFolder = "src/main/resources/images";
    private final List<String> validExtensions = List.of("jpg", "jpeg", "png", "gif", "bmp", "tiff");

    public ImageLoaderAndIndexer(
            ImageDao imageDao,
            ImageDescriptorDAO descriptorDAO,
            ImageSimilarityService imageSimilarityService) {
        this.imageDao = imageDao;
        this.descriptorDAO = descriptorDAO;
        this.imageSimilarityService = imageSimilarityService;
    }

    @Override
    public void run(String... args) throws Exception {

        File folder = new File(imagesFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException(
                "ERREUR : Le dossier 'images' est introuvable à l'emplacement : "
                + folder.getAbsolutePath()
                + "\nVeuillez créer ce dossier avant de lancer le serveur."
            );
        }

        System.out.println("✅ Dossier images trouvé : " + folder.getAbsolutePath());

        File[] files = folder.listFiles(File::isFile);
        if (files == null || files.length == 0) {
            System.out.println("⚠️ Aucun fichier trouvé dans le dossier images.");
            return;
        }

        int added = 0;

        for (File file : files) {
            String name = file.getName();
            int dotIndex = name.lastIndexOf('.');

            if (dotIndex == -1) {
                System.out.println("⏭️ Ignoré (pas d'extension) : " + name);
                continue;
            }

            String extension = name.substring(dotIndex + 1).toLowerCase();
            if (!validExtensions.contains(extension)) {
                System.out.println("⏭️ Ignoré (format non reconnu) : " + name);
                continue;
            }

            if (imageDao.existsByName(name)) {
                System.out.println("⏭️ Déjà en base : " + name);
                continue;
            }

            try {
                byte[] data = Files.readAllBytes(file.toPath());
                Image img = new Image(name, data);

                imageDao.create(img);
                System.out.println("✅ Image chargée : " + name);

                // 🔹 Calcul des descripteurs
                Map<String, double[]> descriptors = new HashMap<>();
                descriptors.put("RGB", imageSimilarityService.computeRGBDescriptor(data));
                descriptors.put("GRAYSCALE", imageSimilarityService.computeGrayscaleHistogram(data));
                descriptors.put("GRADIENT1D", imageSimilarityService.computeGradient1D(data));

                // 🔹 Création et sauvegarde du descriptor
                ImageDescriptor descriptor = new ImageDescriptor(name, descriptors);
                descriptor.setImageId(img.getId());
                descriptorDAO.create(descriptor);
                System.out.println("✅ Descripteurs calculés et sauvegardés : " + name);

                added++;

            } catch (Exception e) {
                System.err.println("❌ Erreur pour : " + name + " — " + e.getMessage());
            }
        }

        System.out.println("🎉 Terminé. " + added + " image(s) ajoutée(s) avec descripteurs complets.");
    }
}
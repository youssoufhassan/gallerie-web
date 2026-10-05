package pdl.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pdl.backend.dao.ImageDescriptorDAO;
import pdl.backend.model.ImageDescriptor;
import pdl.backend.utils.ImageIndexer;

import java.io.File;

@Component
public class ImageIndexerRunner implements CommandLineRunner {

    private final ImageIndexer indexer;
    private final ImageDescriptorDAO dao;

    public ImageIndexerRunner(ImageIndexer indexer, ImageDescriptorDAO dao) {
        this.indexer = indexer;
        this.dao = dao;
    }

    @Override
    public void run(String... args) throws Exception {

        // Dossier contenant les images
        String imagesFolder = "src/main/resources/images";
        File folder = new File(imagesFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Le dossier images n'existe pas : " + imagesFolder);
            return;
        }

        File[] imageFiles = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
        });

        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("Aucune image à indexer dans le dossier : " + imagesFolder);
            return;
        }

        // Indexation de toutes les images
        for (File img : imageFiles) {
            System.out.println("Indexation de l'image : " + img.getName());
            indexer.indexImage(img.getPath());
        }

        System.out.println("\n--- Indexation terminée pour toutes les images ---\n");

        // Affichage de tous les descripteurs en base
        dao.retrieveAll().forEach(d -> {
            System.out.println("----------------------------");
            System.out.println("Image : " + d.getImageName());
            System.out.println("Descriptor : " + d.getDescriptor());
        });
    }
}
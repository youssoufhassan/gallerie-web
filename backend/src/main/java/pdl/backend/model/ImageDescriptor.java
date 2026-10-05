package pdl.backend.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ImageDescriptor {

    private Long id;             // ID du descripteur
    private Long imageId;        // ID de l'image associée
    private String imageName;    // Nom de l'image
    private String descriptor;   // Stockage des descripteurs en string (pour la base)

    public ImageDescriptor() {}

    // Constructeur simple avec nom et descripteur en String
    public ImageDescriptor(String imageName, String descriptor) {
        this.imageName = imageName;
        this.descriptor = descriptor;
    }

    // Constructeur avec nom et Map de descripteurs (RGB3D, HS2D, etc.)
    public ImageDescriptor(String imageName, Map<String, double[]> descriptors) {
        this.imageName = imageName;
        this.descriptor = convertToString(descriptors);
    }

    // Convertit Map<String, double[]> en String pour stockage
    private String convertToString(Map<String, double[]> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, double[]> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(":");
            for (double v : entry.getValue()) {
                sb.append(v).append(",");
            }
            sb.append(";"); // sépare les descripteurs
        }
        return sb.toString();
    }

    // Convertit la String stockée en Map<String, double[]>
    public Map<String, double[]> getDescriptors() {
        Map<String, double[]> map = new HashMap<>();

        if (descriptor == null || descriptor.isEmpty()) return map;

        String[] parts = descriptor.split(";");

        for (String p : parts) {
            if (p.isEmpty()) continue;
            String[] split = p.split(":");
            if (split.length != 2) continue;

            String name = split[0];
            String[] values = split[1].split(",");
            double[] arr = Arrays.stream(values)
                                 .filter(s -> !s.isEmpty())
                                 .mapToDouble(Double::parseDouble)
                                 .toArray();
            map.put(name.toUpperCase(), arr); // Normalisation pour être compatible avec service
        }

        return map;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getImageId() { return imageId; }
    public void setImageId(Long imageId) { this.imageId = imageId; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getDescriptor() { return descriptor; }
    public void setDescriptor(String descriptor) { this.descriptor = descriptor; }

    // Méthode utilitaire pour ajouter un descripteur à la Map existante
    public void addDescriptor(String name, double[] values) {
        Map<String, double[]> map = getDescriptors();
        map.put(name.toUpperCase(), values);
        this.descriptor = convertToString(map);
    }
}
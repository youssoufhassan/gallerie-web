package pdl.backend.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import pdl.backend.model.Image;

// DAO implementation for Image entities, handling database and file system operations
@Repository
public class ImageDao implements Dao<Image> {

    // In-memory cache of images mapped by their ID
    private final Map<Long, Image> images = new HashMap<>();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Path to the folder where image files are stored on disk
    private final String imagesFolder = "src/main/resources/images";

    // Retrieves a single image by its ID, including its binary data and keywords
    @Override
    public Optional<Image> retrieve(long id) {
        String sql = "SELECT id, name, type, user_id, is_public FROM images WHERE id = ?";
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                String name = rs.getString("name");
                Long userId = rs.getLong("user_id");
                boolean isPublic = rs.getBoolean("is_public");

                // Read the image file from disk
                File file = new File(imagesFolder, name);
                byte[] data = null;
                if (file.exists()) {
                    try { data = Files.readAllBytes(file.toPath()); } 
                    catch (IOException e) { throw new RuntimeException("Erreur lecture fichier image: " + name, e); }
                }

                Image img = new Image(name, data, userId);
                img.setId(rs.getLong("id"));
                img.setPublic(isPublic);

                // Load associated keywords from the database
                String kwSql = "SELECT keyword FROM image_keywords WHERE image_id = ?";
                List<String> keywords = jdbcTemplate.query(kwSql, (kwRs, rowNum) -> kwRs.getString("keyword"), id);
                for (String kw : keywords) img.addKeyword(kw);

                return Optional.of(img);
            } else return Optional.empty();
        }, id);
    }

    // Retrieves all images from the database, including their binary data and keywords
    @Override
    public List<Image> retrieveAll() {
        String sql = "SELECT id, name, type, user_id, is_public FROM images";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String name = rs.getString("name");
            Long userId = rs.getLong("user_id");
            boolean isPublic = rs.getBoolean("is_public");

            // Read the image file from disk
            File file = new File(imagesFolder, name);
            byte[] data = null;
            if (file.exists()) {
                try { data = Files.readAllBytes(file.toPath()); } 
                catch (IOException e) { throw new RuntimeException("Erreur lecture fichier image: " + name, e); }
            }

            Image img = new Image(name, data, userId);
            img.setId(rs.getLong("id"));
            img.setPublic(isPublic);

            // Load associated keywords from the database
            String kwSql = "SELECT keyword FROM image_keywords WHERE image_id = ?";
            List<String> keywords = jdbcTemplate.query(kwSql, (kwRs, rowNum2) -> kwRs.getString("keyword"), img.getId());
            for (String kw : keywords) img.addKeyword(kw);

            return img;
        });
    }

    // Retrieves all images belonging to a specific user
    public List<Image> retrieveAllForUser(Long userId) {
        String sql = "SELECT id, name, type, user_id, is_public FROM images WHERE user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String name = rs.getString("name");
            boolean isPublic = rs.getBoolean("is_public");

            // Read the image file from disk
            File file = new File(imagesFolder, name);
            byte[] data = null;
            if (file.exists()) {
                try { data = Files.readAllBytes(file.toPath()); } 
                catch (IOException e) { throw new RuntimeException("Erreur lecture fichier image: " + name, e); }
            }

            Image img = new Image(name, data, userId);
            img.setId(rs.getLong("id"));
            img.setPublic(isPublic);

            // Load associated keywords from the database
            String kwSql = "SELECT keyword FROM image_keywords WHERE image_id = ?";
            List<String> keywords = jdbcTemplate.query(kwSql, (kwRs, rowNum2) -> kwRs.getString("keyword"), img.getId());
            for (String kw : keywords) img.addKeyword(kw);

            return img;
        }, userId);
    }

    // Saves a new image to disk and inserts its record into the database
    @Override
    public void create(Image img) {
        try {
            // Ensure the images folder exists
            File folder = new File(imagesFolder);
            if (!folder.exists()) folder.mkdirs();

            // Write the image binary data to disk
            File file = new File(folder, img.getName());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(img.getData());
            }

            // Detect the MIME type of the saved file
            String type = Files.probeContentType(file.toPath());
            if (type == null && img.getType() != null) type = img.getType().toString();
            if (type == null) type = "application/octet-stream";

            // Insert the image record into the database and retrieve the generated ID
            String sql = "INSERT INTO images (name, type, user_id, is_public) VALUES (?, ?, ?, ?) RETURNING id";
            Long id = jdbcTemplate.queryForObject(sql, Long.class, img.getName(), type, img.getUserId(), img.isPublic());
            img.setId(id);

            // Add to the in-memory cache
            images.put(id, img);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier", e);
        }
    }

    // Updates the name, type, and visibility of an existing image
    @Override
    public void update(Image img, String[] params) {
        if (params.length >= 3) {
            String newName = params[0];
            String newType = params[1];
            boolean newPublic = Boolean.parseBoolean(params[2]);

            String sql = "UPDATE images SET name = ?, type = ?, is_public = ? WHERE id = ?";
            jdbcTemplate.update(sql, newName, newType, newPublic, img.getId());

            img.setName(newName);
            img.setType(null); // Si tu utilises MediaType ici, adapter
            img.setPublic(newPublic);

            // Update the in-memory cache
            images.put(img.getId(), img);
        }
    }

    // Updates only the public/private visibility flag of an image
    public void updateVisibility(Long imageId, boolean isPublic) {
        String sql = "UPDATE images SET is_public = ? WHERE id = ?";
        jdbcTemplate.update(sql, isPublic, imageId);
    }

    // Deletes an image from the database, the in-memory cache, and the file system
    @Override
    public void delete(Image img) {
        if (img == null) return;

        jdbcTemplate.update("DELETE FROM images WHERE id = ?", img.getId());
        images.remove(img.getId());

        // Delete the physical file from disk
        File file = new File(imagesFolder, img.getName());
        if (file.exists()) file.delete();
    }

    // Inserts a keyword associated with a given image into the database
    public void addKeyword(long imageId, String keyword) {
        String sql = "INSERT INTO image_keywords (image_id, keyword) VALUES (?, ?)";
        jdbcTemplate.update(sql, imageId, keyword);
    }

    // Removes a specific keyword associated with a given image from the database
    public void removeKeyword(long imageId, String keyword) {
        String sql = "DELETE FROM image_keywords WHERE image_id = ? AND keyword = ?";
        jdbcTemplate.update(sql, imageId, keyword);
    }

    // ======================
    // Méthode pour récupérer toutes les images publiques
    // ======================
    // Retrieves all images marked as public, including their binary data and keywords
    public List<Image> retrieveAllPublic() {
        String sql = "SELECT id, name, type, user_id, is_public FROM images WHERE is_public = true";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String name = rs.getString("name");
            Long userId = rs.getLong("user_id");

            // Read the image file from disk
            File file = new File(imagesFolder, name);
            byte[] data = null;
            if (file.exists()) {
                try { data = Files.readAllBytes(file.toPath()); } 
                catch (IOException e) { throw new RuntimeException("Erreur lecture fichier image: " + name, e); }
            }

            Image img = new Image(name, data, userId);
            img.setId(rs.getLong("id"));
            img.setPublic(true);

            // Load associated keywords from the database
            String kwSql = "SELECT keyword FROM image_keywords WHERE image_id = ?";
            List<String> keywords = jdbcTemplate.query(kwSql, (kwRs, rowNum2) -> kwRs.getString("keyword"), img.getId());
            for (String kw : keywords) img.addKeyword(kw);

            return img;
        });
    }

    // Checks whether an image with the given filename already exists in the database
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM images WHERE name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name);
        return count != null && count > 0;
    }
}
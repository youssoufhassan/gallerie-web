package pdl.backend.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pdl.backend.model.ImageDescriptor;

import java.util.List;
import java.util.Optional;

// DAO implementation for ImageDescriptor entities, handling CRUD operations on image descriptors
@Repository
public class ImageDescriptorDAO implements Dao<ImageDescriptor> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =========================
    // CREATE
    // =========================
    // Inserts a new image descriptor into the database and sets the generated ID on the object
    @Override
    public void create(final ImageDescriptor desc) {
        if (desc.getImageId() == null) {
            throw new IllegalArgumentException("image_id ne peut pas être NULL");
        }
        String sql = """
            INSERT INTO image_descriptors
            (image_id, image_name, descriptor)
            VALUES (?, ?, ?)
            RETURNING id
        """;
        Long id = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                desc.getImageId(),
                desc.getImageName(),
                desc.getDescriptor()
        );
        desc.setId(id);
        System.out.println(
            "Descriptor créé : ID=" + id +
            " ImageID=" + desc.getImageId()
        );
    }

    // =========================
    // RETRIEVE BY ID
    // =========================
    // Retrieves a single descriptor by its own ID, returns empty if not found
    @Override
    public Optional<ImageDescriptor> retrieve(final long id) {

        String sql = """
            SELECT id, image_id, image_name, descriptor
            FROM image_descriptors
            WHERE id = ?
        """;

        List<ImageDescriptor> list =
            jdbcTemplate.query(
                sql,
                new Object[]{id},
                (rs, rowNum) -> {

                    ImageDescriptor desc = new ImageDescriptor();

                    desc.setId(rs.getLong("id"));
                    desc.setImageId(rs.getLong("image_id"));
                    desc.setImageName(rs.getString("image_name"));
                    desc.setDescriptor(rs.getString("descriptor"));

                    return desc;
                }
            );

        return list.isEmpty()
                ? Optional.empty()
                : Optional.of(list.get(0));
    }

    // =========================
    // RETRIEVE ALL
    // =========================
    // Retrieves all image descriptors stored in the database
    @Override
    public List<ImageDescriptor> retrieveAll() {

        String sql = """
            SELECT id, image_id, image_name, descriptor
            FROM image_descriptors
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    ImageDescriptor desc = new ImageDescriptor();

                    desc.setId(rs.getLong("id"));
                    desc.setImageId(rs.getLong("image_id"));
                    desc.setImageName(rs.getString("image_name"));
                    desc.setDescriptor(rs.getString("descriptor"));

                    return desc;
                }
        );
    }

    // =========================
    // RETRIEVE FOR IMAGE
    // =========================
    // Retrieves all descriptors associated with a specific image ID
    public List<ImageDescriptor> retrieveAllForImage(Long imageId) {

        String sql = """
            SELECT id, image_id, image_name, descriptor
            FROM image_descriptors
            WHERE image_id = ?
        """;

        return jdbcTemplate.query(
                sql,
                new Object[]{imageId},
                (rs, rowNum) -> {

                    ImageDescriptor desc = new ImageDescriptor();

                    desc.setId(rs.getLong("id"));
                    desc.setImageId(rs.getLong("image_id"));
                    desc.setImageName(rs.getString("image_name"));
                    desc.setDescriptor(rs.getString("descriptor"));

                    return desc;
                }
        );
    }

    // =========================
    // CHECK EXISTS (IMPORTANT)
    // =========================
    // Returns true if at least one descriptor exists for the given image ID
    public boolean existsForImage(Long imageId) {

        String sql = """
            SELECT COUNT(*)
            FROM image_descriptors
            WHERE image_id = ?
        """;

        Integer count =
            jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                imageId
            );

        return count != null && count > 0;
    }

    // =========================
    // UPDATE
    // =========================
    // Updates the descriptor content of an existing record using the first element of params
    @Override
    public void update(final ImageDescriptor desc,
                       final String[] params) {

        if (params != null && params.length > 0) {

            String sql = """
                UPDATE image_descriptors
                SET descriptor = ?
                WHERE id = ?
            """;

            jdbcTemplate.update(
                    sql,
                    params[0],
                    desc.getId()
            );

            System.out.println(
                "Descriptor mis à jour ID=" +
                desc.getId()
            );
        }
    }

    // =========================
    // DELETE
    // =========================
    // Deletes a descriptor by its ID and logs whether the deletion was successful
    @Override
    public void delete(final ImageDescriptor desc) {

        String sql = """
            DELETE FROM image_descriptors
            WHERE id = ?
        """;

        int rows =
            jdbcTemplate.update(
                sql,
                desc.getId()
            );

        if (rows > 0) {
            // Descriptor successfully deleted
            System.out.println(
                "Descriptor supprimé ID=" +
                desc.getId()
            );

        } else {
            // No descriptor found with the given ID
            System.out.println(
                "Descriptor introuvable ID=" +
                desc.getId()
            );
        }
    }
}
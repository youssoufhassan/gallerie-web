package pdl.backend.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImageInteractionDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ======================
    // Likes management section
    // ======================

    // Add a like for an image if the user has not already liked it
    public void addLike(Long imageId, Long userId) {
        if (!hasUserLiked(imageId, userId)) {
            String sql = "INSERT INTO image_likes (image_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, imageId, userId);
        }
    }

    // Remove a like from an image for a given user
    public void removeLike(Long imageId, Long userId) {
        String sql = "DELETE FROM image_likes WHERE image_id=? AND user_id=?";
        jdbcTemplate.update(sql, imageId, userId);
    }

    // Check if a user has already liked a specific image
    public boolean hasUserLiked(Long imageId, Long userId) {
        String sql = "SELECT COUNT(*) FROM image_likes WHERE image_id=? AND user_id=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, imageId, userId);
        return count != null && count > 0;
    }

    // Count total likes for a given image
    public int countLikes(Long imageId) {
        String sql = "SELECT COUNT(*) FROM image_likes WHERE image_id=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, imageId);
        return count != null ? count : 0;
    }

    // ======================
    // Comments management section
    // ======================

    // Add a comment to an image
    public void addComment(Long imageId, Long userId, String comment) {
        String sql = "INSERT INTO image_comments (image_id, user_id, comment) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, imageId, userId, comment);
    }

    // Retrieve all comments for a given image (without timestamp)
    public List<Comment> getComments(Long imageId) {
        String sql = """
            SELECT ic.id, ic.user_id, ic.comment, u.username as user_name
            FROM image_comments ic
            JOIN users u ON ic.user_id = u.id
            WHERE ic.image_id = ?
            ORDER BY ic.id ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Comment(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("comment"),
                rs.getString("user_name")   // mapped username from users table
        ), imageId);
    }

    // Simple Comment DTO class
    public static class Comment {
        private Long id;
        private Long userId;
        private String comment;
        private String userName;

        // Constructor
        public Comment(Long id, Long userId, String comment, String userName) {
            this.id = id;
            this.userId = userId;
            this.comment = comment;
            this.userName = userName;
        }

        // Get comment ID
        public Long getId() { return id; }

        // Get user ID
        public Long getUserId() { return userId; }

        // Get comment text
        public String getComment() { return comment; }

        // Get username (required for JSON serialization)
        public String getUserName() { return userName; }
    }
}
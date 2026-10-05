package pdl.backend.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import pdl.backend.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Insert a new user into the database and return the generated ID
    public void create(User user) {
        String sql = "INSERT INTO users (username, password, name, prenom) VALUES (?, ?, ?, ?) RETURNING id";

        Long id = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                user.getUsername(),
                user.getPassword(), // already hashed in the service layer
                user.getName(),
                user.getPrenom()
        );

        user.setId(id);
    }

    // Find a user by username and return it as Optional<User>
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        List<User> users = jdbcTemplate.query(
                sql,
                new Object[]{username},
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    user.setPrenom(rs.getString("prenom"));
                    return user;
                }
        );

        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    // Retrieve all users from the database
    public List<User> findAll() {
        String sql = "SELECT * FROM users";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setPrenom(rs.getString("prenom"));
            return user;
        });
    }

    // Find a user by ID and return it as Optional<User>
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        List<User> users = jdbcTemplate.query(
                sql,
                new Object[]{id},
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    user.setPrenom(rs.getString("prenom"));
                    return user;
                }
        );

        // Log result depending on whether the user was found or not
        if (users.isEmpty()) {
            System.out.println("❌ No user found with id = " + id);
            return Optional.empty();
        } else {
            System.out.println("✅ User found: " + users.get(0).getUsername());
            return Optional.of(users.get(0));
        }
    }
}
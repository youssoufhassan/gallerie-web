package pdl.backend.utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pdl.backend.dao.UserDao;
import pdl.backend.model.User;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Inscription
    public User register(User user) throws Exception {
        Optional<User> existing = userDao.findByUsername(user.getUsername());
        if (existing.isPresent()) {
            throw new Exception("Username déjà utilisé");
        }

        // Hash du mot de passe **une seule fois**
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userDao.create(user); // le DAO ne re-hash pas
        return user;
    }
    // Connexion
    public User login(String username, String password) throws Exception {
        Optional<User> existing = userDao.findByUsername(username);
        if (existing.isEmpty()) {
            throw new Exception("Utilisateur introuvable");
        }

        User dbUser = existing.get();

        // Comparer mot de passe
        if (!passwordEncoder.matches(password, dbUser.getPassword())) {
            throw new Exception("Mot de passe incorrect");
        }

        return dbUser;
    }
  public Optional<User> findById(Long id) {
    Optional<User> user = userDao.findById(id);
    if(user.isPresent()) {
        System.out.println("✅ User récupéré dans service : " + user.get().getUsername());
    } else {
        System.out.println("❌ User introuvable dans service pour id = " + id);
    }
    return user;
}
}
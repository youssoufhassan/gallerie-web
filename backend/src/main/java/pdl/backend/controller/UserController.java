package pdl.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pdl.backend.model.User;
import pdl.backend.utils.UserService;

// REST controller handling user-related endpoints (registration, login, lookup)
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Registers a new user and returns their generated ID
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User newUser = userService.register(user);
            return ResponseEntity.ok("Utilisateur créé avec ID : " + newUser.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Authenticates a user by username and password, returns the full User object on success
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        try {
            User loggedUser = userService.login(user.getUsername(), user.getPassword());
            return ResponseEntity.ok(loggedUser); // <-- renvoie l'objet User complet
        } catch (Exception e) {
            // Return 401 Unauthorized if credentials are invalid
            return ResponseEntity.status(401).body(null);
        }
    }

    // Retrieves a user by their ID, returns 404 if not found
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        System.out.println("🔥 API CALLED ID = " + id);
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
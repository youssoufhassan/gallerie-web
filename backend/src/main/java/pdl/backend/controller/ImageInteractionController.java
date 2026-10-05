package pdl.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pdl.backend.dao.ImageInteractionDao;
import pdl.backend.dao.ImageInteractionDao.Comment;
import java.util.List;

// REST controller handling like and comment interactions on images
@RestController
@RequestMapping("/images")
public class ImageInteractionController {

    @Autowired
    private ImageInteractionDao interactionDao;

    // -----------------------------
    // Vérifier si l'utilisateur a liké une image
    // -----------------------------
    // Checks whether a specific user has already liked a given image
    @GetMapping("/{id}/like")
    public ResponseEntity<Boolean> hasUserLiked(
            @PathVariable("id") Long imageId,
            @RequestParam Long userId) {
        try {
            boolean liked = interactionDao.hasUserLiked(imageId, userId);
            return ResponseEntity.ok(liked);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // -----------------------------
    // Liker / Deliker une image (toggle)
    // -----------------------------
    // Toggles the like status for a user on an image (adds like if not liked, removes it otherwise)
    @PostMapping("/{id}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable("id") Long imageId,
            @RequestParam Long userId) {
        try {
            if (interactionDao.hasUserLiked(imageId, userId)) {
                // User already liked the image — remove the like
                interactionDao.removeLike(imageId, userId);
                return ResponseEntity.ok("Like retiré");
            } else {
                // User has not liked the image yet — add the like
                interactionDao.addLike(imageId, userId);
                return ResponseEntity.ok("Like ajouté");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors du like");
        }
    }

    // -----------------------------
    // Nombre de likes
    // -----------------------------
    // Returns the total number of likes for a given image
    @GetMapping("/{id}/likes/count")
    public ResponseEntity<Integer> countLikes(@PathVariable("id") Long imageId) {
        try {
            int count = interactionDao.countLikes(imageId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // -----------------------------
    // Ajouter un commentaire
    // -----------------------------
    // Adds a comment from a user on a given image
    @PostMapping("/{id}/comment")
    public ResponseEntity<String> addComment(
            @PathVariable("id") Long imageId,
            @RequestParam Long userId,
            @RequestParam String comment) {
        try {
            interactionDao.addComment(imageId, userId, comment);
            return ResponseEntity.ok("Commentaire ajouté");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de l'ajout du commentaire");
        }
    }

    // -----------------------------
    // Récupérer tous les commentaires
    // -----------------------------
    // Returns all comments associated with a given image
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable("id") Long imageId) {
        try {
            List<Comment> comments = interactionDao.getComments(imageId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
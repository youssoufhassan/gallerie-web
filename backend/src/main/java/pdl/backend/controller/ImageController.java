package pdl.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import pdl.backend.dao.ImageDao;
import pdl.backend.dao.ImageInteractionDao;
import pdl.backend.dao.UserDao;
import pdl.backend.model.Image;
import pdl.backend.model.ImageMetadata;
import pdl.backend.model.User;
import pdl.backend.utils.*;
import pdl.backend.model.ImageDescriptor;
import pdl.backend.dao.ImageDescriptorDAO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// REST controller handling all image-related HTTP endpoints
@RestController
@RequestMapping("/images")
public class ImageController {

  @Autowired
  private ObjectMapper mapper;
private ImageDescriptorDAO descriptorDAO;
 private final ImageDao imageDao;
 private final UserDao userDao;
 private final ImageSimilarityService imageSimilarityService;
 private final ImageInteractionDao imageInteractionDao;

  // Constructor injecting all required dependencies
  public ImageController(ImageDao imageDao, ImageSimilarityService imageSimilarityService, UserDao userDao, ImageInteractionDao imageInteractionDao) {
    this.imageDao = imageDao;
    this.imageSimilarityService = imageSimilarityService;
    this.userDao = userDao; // <-- ajoute cette ligne
    this.imageInteractionDao = imageInteractionDao;
}
  

  // Returns the raw JPEG data of an image by its ID
   @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
 public ResponseEntity<?> getImage(@PathVariable long id) {
 Optional<Image> opt = imageDao.retrieve(id);
 if(opt.isPresent()){
      Image image = opt.get();
      return ResponseEntity.ok()
      .contentType(MediaType.IMAGE_JPEG)
      .body(image.getData());
      }else{
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
 }
 }

  // Deletes an image by its ID
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable long id) {
    Optional<Image> img = imageDao.retrieve(id);
    if (img.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    imageDao.delete(img.get());
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

/* 
 @PostMapping("/images")
public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file) {

    try {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Fichier vide");
        }

        String type = file.getContentType();

        if (type == null || 
           (!type.equals("image/jpeg") && !type.equals("image/png"))) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Seuls les formats JPEG et PNG sont acceptés");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) filename = "unknown";

        Image img = new Image(
                filename,
                MediaType.parseMediaType(type),
                file.getBytes()
        );

        img.addKeyword("nouvelle_image");

        imageDao.create(img);

        return ResponseEntity.status(HttpStatus.CREATED).body(img.getId());

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur serveur: " + e.getMessage());
    }
}*/

// Uploads a new image file and associates it with a user, then computes its descriptors
@PostMapping("")
public ResponseEntity<?> addImage(
        @RequestParam("file") MultipartFile file,
        @RequestParam Long userId) {

    try {
        if (file.isEmpty())
            return ResponseEntity.badRequest().body("Fichier vide");

        String type = file.getContentType();
        if (type == null || (!type.equals("image/jpeg") && !type.equals("image/png"))) {
            return ResponseEntity
                    .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Seuls JPEG et PNG sont acceptés");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) filename = "unknown";

        // 🔹 Sauvegarde image
        Image img = new Image(
                filename,
                MediaType.parseMediaType(type),
                file.getBytes(),
                userId
        );
        img.addKeyword("nouvelle_image");
        imageDao.create(img);

        // 🔹 Calcul des descripteurs via le service
        Map<String, double[]> descriptors = new HashMap<>();
        byte[] fileBytes = file.getBytes();

        // Compute RGB, grayscale, and gradient descriptors for similarity search
        descriptors.put("RGB", imageSimilarityService.computeRGBDescriptor(fileBytes));
        descriptors.put("GRAYSCALE", imageSimilarityService.computeGrayscaleHistogram(fileBytes));
        descriptors.put("GRADIENT1D", imageSimilarityService.computeGradient1D(fileBytes));

        // 🔹 Création et sauvegarde du descriptor
        ImageDescriptor descriptor = new ImageDescriptor(filename, descriptors);
        descriptor.setImageId(img.getId());
        imageSimilarityService.getDescriptorDAO().create(descriptor);

        return ResponseEntity.status(HttpStatus.CREATED).body(img.getId());

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur serveur: " + e.getMessage());
    }
}

    // ======================
    // Ajouter un keyword
    // ======================
    // Adds a keyword/tag to an image if not already present
  @PutMapping("/{id}/keywords")
public ResponseEntity<Void> addKeyword(
        @PathVariable Long id,
        @RequestParam String tag) {

    Optional<Image> imgOpt = imageDao.retrieve(id);
    if (imgOpt.isEmpty()) return ResponseEntity.notFound().build();

    Image img = imgOpt.get();

    if (!img.getKeywords().contains(tag)) {
        imageDao.addKeyword(id, tag);
    }

    return ResponseEntity.noContent().build();
}

  // Returns a deduplicated list of all keywords across all images
  @GetMapping("/keywords")
    public ResponseEntity<ArrayNode> getAllKeywords() {
        ArrayNode array = mapper.createArrayNode();
        List<Image> images = imageDao.retrieveAll();

        for (Image img : images) {
            for (String kw : img.getKeywords()) {
                if (!array.has(kw)) {
                    array.add(kw);
                }
            }
        }

        return ResponseEntity.ok(array); // 200 OK
    }

/* 
 @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
@ResponseBody
public ResponseEntity<ArrayNode> getImageList() {
    ArrayNode array = mapper.createArrayNode();

    for (Image img : imageDao.retrieveAll()) {
        var node = mapper.createObjectNode();
        node.put("id", img.getId());
        node.put("name", img.getName());

        var keywordsNode = mapper.createArrayNode();
        for (String kw : img.getKeywords()) {
            keywordsNode.add(kw);
        }
        node.set("keywords", keywordsNode);

        array.add(node);
    }

    return ResponseEntity.ok(array);
}*/

// Returns the list of images belonging to a specific user
@RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
@ResponseBody
public ResponseEntity<ArrayNode> getImageList(@RequestParam Long userId) {
    ArrayNode array = mapper.createArrayNode();

    for (Image img : imageDao.retrieveAllForUser(userId)) {  // <-- filtre ici
        var node = mapper.createObjectNode();
        node.put("id", img.getId());
        node.put("name", img.getName());

        var keywordsNode = mapper.createArrayNode();
        for (String kw : img.getKeywords()) keywordsNode.add(kw);
        node.set("keywords", keywordsNode);

        array.add(node);
    }

    return ResponseEntity.ok(array);
}

// Returns metadata (dimensions, MIME type, keywords) for a given image
@GetMapping("/{id}/metadata")
public ResponseEntity<ImageMetadata> getMetadata(@PathVariable long id) {
    Optional<Image> imgOpt = imageDao.retrieve(id);
    if (imgOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    Image img = imgOpt.get();

    String size = "unknown";
    String type = "unknown";

    try {
        // Use the exact file path of the stored image
        String imagePath = "src/main/resources/images/" + img.getName();
        File file = new File(imagePath);

        if (file.exists()) {
            // Read the image dimensions
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage != null) {
                size = bufferedImage.getWidth() + "*" + bufferedImage.getHeight();
            }

            // Detect the MIME type
            String probeType = Files.probeContentType(file.toPath());
            if (probeType != null) {
                type = probeType;
            } else if (img.getType() != null) {
                type = img.getType().toString();
            }
        } else {
            System.err.println("Fichier non trouvé : " + file.getAbsolutePath());
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    ImageMetadata metadata = new ImageMetadata(
            img.getName(),
            type,
            size,
            img.getKeywords()
    );

    return ResponseEntity.ok(metadata);
}

  // ======================
    // Supprimer un keyword
    // ======================
    // Removes a keyword/tag from an image
    @DeleteMapping("/{id}/keywords")
    public ResponseEntity<Void> removeKeyword(
            @PathVariable long id,
            @RequestParam String tag) {

        Optional<Image> imgOpt = imageDao.retrieve(id);
        if (imgOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Image img = imgOpt.get();

        if (!img.getKeywords().contains(tag)) {
            return ResponseEntity.badRequest().build();
        }

        img.removeKeyword(tag);                  // en mémoire
        imageDao.removeKeyword(id, tag);         // en base

        return ResponseEntity.noContent().build(); // 204 No Content
    }

  // ======================
    // Rechercher des images par keyword
    // ======================
    // Searches and returns images that contain the given keyword
 @GetMapping("/search")
public ResponseEntity<ArrayNode> searchByKeyword(@RequestParam String keyword) {
    ArrayNode array = mapper.createArrayNode();
    List<Image> images = imageDao.retrieveAll(); // toutes les images

    for (Image img : images) {
        if (img.getKeywords().contains(keyword)) {
            var node = mapper.createObjectNode();
            node.put("id", img.getId());
            node.put("name", img.getName());

            var keywordsNode = mapper.createArrayNode();
            for (String kw : img.getKeywords()) keywordsNode.add(kw);
            node.set("keywords", keywordsNode);

            array.add(node);
        }
    }

    if (array.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.ok(array);
}

 // ======================
    // Returns a list of images similar to the given image ID, based on the chosen descriptor
    @GetMapping("/{id}/similar")
    public ResponseEntity<?> getSimilar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "2") int number,
            @RequestParam String descriptor
    ) {
        try {
            // Check if the image exists
            Optional<Image> optImg = imageDao.retrieve(id);
            if (optImg.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Image introuvable pour id=" + id);
            }

            // Debug log
            System.out.println("Recherche similaire pour imageId=" + id + ", descriptor=" + descriptor);

            // Call the service to retrieve similar images
            List<ImageSimilarityService.SimilarImageResult> results =
                    imageSimilarityService.findSimilar(id, number, descriptor.trim().toUpperCase());

            // Convert results to JSON-compatible map list
            List<Map<String, Object>> similar = results.stream()
                    .map(r -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", r.getId());
                        map.put("score", r.getScore());

                        // Add the image name to the result
                        imageDao.retrieve(r.getId()).ifPresent(img -> map.put("name", img.getName()));
                        return map;
                    })
                    .toList();

            return ResponseEntity.ok(similar);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur: " + e.getMessage());
        }
    }

// Returns all public images with their owner, likes, comments, and keywords
@GetMapping("/public")
@ResponseBody
public ResponseEntity<ArrayNode> getPublicImages() {
    ArrayNode array = mapper.createArrayNode();

    List<Image> images = imageDao.retrieveAllPublic(); // récupère uniquement les images publiques

    for (Image img : images) {
        var node = mapper.createObjectNode();
        node.put("id", img.getId());
        node.put("name", img.getName());

        // Retrieve the owner of the image
        User user = userDao.findById(img.getUserId())
                           .orElse(null); // si l'utilisateur n'existe pas
        node.put("userName", user != null ? user.getUsername() : "Anonyme");

        // Count the number of likes
        int likes = imageInteractionDao.countLikes(img.getId());
        node.put("likes", likes);

        // Build the comments array
        var commentsNode = mapper.createArrayNode();
        List<ImageInteractionDao.Comment> comments = imageInteractionDao.getComments(img.getId());
        for (ImageInteractionDao.Comment c : comments) {
            var cNode = mapper.createObjectNode();
            User cUser = userDao.findById(c.getUserId()).orElse(null);
            cNode.put("userName", cUser != null ? cUser.getUsername() : "Anonyme");
            cNode.put("text", c.getComment());
            commentsNode.add(cNode);
        }
        node.set("comments", commentsNode);

        // Build the keywords array
        var keywordsNode = mapper.createArrayNode();
        for (String kw : img.getKeywords()) keywordsNode.add(kw);
        node.set("keywords", keywordsNode);

        array.add(node);
    }

    return ResponseEntity.ok(array);
}

// ======================
// Toggle public/privé
// ======================
// Toggles the visibility of an image between public and private (only the owner can do this)
@PutMapping("/{id}/visibility")
public ResponseEntity<?> toggleVisibility(
        @PathVariable Long id,
        @RequestParam Long userId) {

    Optional<Image> imgOpt = imageDao.retrieve(id);
    if (imgOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Image introuvable");
    }

    Image img = imgOpt.get();

    if (!img.getUserId().equals(userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Vous n'êtes pas le propriétaire de cette image");
    }

    // ✅ Toggle simple et direct
    boolean newVisibility = !img.isPublic();
    imageDao.updateVisibility(id, newVisibility); // ← méthode dédiée

    String message = newVisibility ? "Image rendue publique" : "Image rendue privée";
    return ResponseEntity.ok(message);
}

/*
 @GetMapping
    public ResponseEntity<ArrayNode> getPublicImageUsers() {
        ArrayNode array = new ObjectMapper().createArrayNode();

        List<Image> images = imageDao.retrieveAllPublic();

        for (Image img : images) {
            User user = userDao.findById(img.getUserId()).orElse(null);
            ObjectNode node = new ObjectMapper().createObjectNode();

            if (user != null) {
                node.put("imageId", img.getId());
                node.put("userName", user.getUsername());
                node.put("name", user.getName());
                node.put("prenom", user.getPrenom());
            } else {
                node.put("imageId", img.getId());
                node.put("userName", "Anonyme");
                node.put("name", "Anonyme");
                node.put("prenom", "Anonyme");
            }

            array.add(node);
        }

        return ResponseEntity.ok(array);
    }*/
}
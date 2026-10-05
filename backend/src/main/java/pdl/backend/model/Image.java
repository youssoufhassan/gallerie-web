package pdl.backend.model;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;

public class Image {
    private static Long count = 0L;
    private Long id;
    private String name;
    private byte[] data;
    private MediaType type;
    private Long userId;
    private boolean isPublic = false; // <-- nouveau champ, par défaut false

    private List<String> keywords = new ArrayList<>();

    // Constructeurs
    public Image(final String name, final byte[] data, Long userId) {
        id = count++;
        this.name = name;
        this.data = data;
        this.userId = userId;
    }

    public Image(String name, MediaType type, byte[] data, Long userId) {
        id = count++;
        this.name = name;
        this.type = type;
        this.data = data;
        this.userId = userId;
    }

    public Image() { }

    public Image(final String name, final byte[] data) {
        this.id = count++;
        this.name = name;
        this.data = data;
        this.type = MediaType.IMAGE_JPEG; // valeur par défaut
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    public MediaType getType() { return type; }
    public void setType(MediaType type) { this.type = type; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isPublic() { return isPublic; } // getter pour le nouveau champ
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; } // setter

    // Keywords
    public List<String> getKeywords() { return keywords; }
    public void addKeyword(String keyword) {
        if (keyword != null && !keyword.isBlank() && !keywords.contains(keyword)) {
            keywords.add(keyword);
        }
    }
    public void removeKeyword(String keyword) { keywords.remove(keyword); }
}
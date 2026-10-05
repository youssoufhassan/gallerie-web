package pdl.backend.model;

import java.util.List;

public class ImageMetadata {

    private String name;
    private String type;
    private String size;
    private List<String> keywords;

    public ImageMetadata(String name, String type, String size, List<String> keywords) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.keywords = keywords;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSize() {
        return size;
    }

    public List<String> getKeywords() {
        return keywords;
    }
}
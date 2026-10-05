package pdl.backend.model;

public class User {

    private Long id;
    private String username;
    private String password;
    private String name;
    private String prenom;

    public User() {}

    public User(String username, String password, String name, String prenom) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.prenom = prenom;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
}
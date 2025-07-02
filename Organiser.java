public class Organiser {
    private String username;
    private String hashedPassword;
    private String role;

    public Organiser(String username, String role) {
        this.username = username;
        //this.hashedPassword = hashedPassword;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getRole() {
        return role;
    }

    public void setPassword(String newHashedPassword) {
        this.hashedPassword = newHashedPassword;
    }

    public void setRole(String newRole) {
        this.role = newRole;
    }
}

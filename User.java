
public class User {
    private String username;
    private String hashedPassword;
    private String role;

    public User(String username, String hashedPassword, String role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
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

    public boolean validatePassword(String password) {
        return PasswordUtil.verifyPassword(password, hashedPassword);
    }
}

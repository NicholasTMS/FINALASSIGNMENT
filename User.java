
public class User {
    private String username;
    private String hashedPassword;
    private String role;

    public User(String username, String Password, String role) {
        this.username = username;
        this.hashedPassword = PasswordUtil.hashPassword(Password);
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
    
    public void setPassword(String newPassword) {

        this.hashedPassword = PasswordUtil.hashPassword(newPassword);
    }

    public void setRole(String newRole) {
        this.role = newRole;
    }

    public boolean validatePassword(String password) {
        return PasswordUtil.verifyPassword(password, hashedPassword);
    }
}

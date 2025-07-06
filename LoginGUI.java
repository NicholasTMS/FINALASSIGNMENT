
import javax.naming.PartialResultException;
import javax.swing.*;
import java.util.List;

public class LoginGUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserManager userManager;

    public LoginGUI() {
        userManager = new UserManager();
        frame = new JFrame("Login");
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(30, 30, 80, 25);
        frame.add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(120, 30, 165, 25);
        frame.add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(30, 70, 80, 25);
        frame.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(120, 70, 165, 25);
        frame.add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(120, 110, 80, 25);
        frame.add(loginBtn);

        loginBtn.addActionListener(e -> {
            String uname = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            if (userManager.authenticate(uname, pass)) {
                String role = userManager.getUserRole(uname);
                frame.dispose();
                switch (role.toLowerCase()) {
                    case "admin":
                        showAdminDashboard(uname, "Admin", pass);
                        break;
                    case "participant":
                        showParticipantDashboard(uname, "Participant", pass);
                        break;
                    case "organiser":
                        showOrganiserDashboard(uname, "Organiser", pass);
                        break;
                    default:
                        JOptionPane.showMessageDialog(frame, "Unknown role.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid login.");
            }
        });

        frame.setVisible(true);
    }

    private void showAdminDashboard(String adminUsername, String role, String Password) {
        JFrame frame = new JFrame("Admin Dashboard - " + adminUsername);
        frame.setSize(400, 300);
        frame.setLayout(null);

        JButton createUserBtn = new JButton("Create User");
        createUserBtn.setBounds(100, 30, 200, 30);
        frame.add(createUserBtn);

        JButton editUserBtn = new JButton("Edit User");
        editUserBtn.setBounds(100, 70, 200, 30);
        frame.add(editUserBtn);

        JButton deleteUserBtn = new JButton("Delete User");
        deleteUserBtn.setBounds(100, 110, 200, 30);
        frame.add(deleteUserBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(100, 160, 200, 30);
        frame.add(logoutBtn);

        createUserBtn.addActionListener(e -> {
            String newUsername = JOptionPane.showInputDialog(frame, "New username:");
            String newPassword = JOptionPane.showInputDialog(frame, "New password:");
            String[] roles = {"admin", "participant", "organiser"};
            String newRole = (String) JOptionPane.showInputDialog(frame, "Select role:", "User Role", JOptionPane.PLAIN_MESSAGE, null, roles, roles[0]);

            if (newUsername == null || newUsername.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty() ||
                newRole == null || newRole.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Invalid input. Please try again.");
            } else if (userManager.addUser(newUsername, PasswordUtil.hashPassword(newPassword), newRole)) {
                JOptionPane.showMessageDialog(frame, "User created.");
            } else {
                JOptionPane.showMessageDialog(frame, "User already exists.");
            }
        });

        editUserBtn.addActionListener(e -> {
            List<String> usernames = userManager.getAllUsernamesExcluding(adminUsername);
            if (usernames.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No other users to edit.");
                return;
            }
            String uname = (String) JOptionPane.showInputDialog(
                    frame, "Select user to edit:", "Edit User",
                    JOptionPane.PLAIN_MESSAGE, null,
                    usernames.toArray(), usernames.get(0)
            );

            if (uname != null) {
                String[] roles = {"admin", "participant", "organiser"};
                String newRole = (String) JOptionPane.showInputDialog(frame, "Select new role:", "Edit User Role", JOptionPane.PLAIN_MESSAGE, null, roles, roles[0]);
                String newPassword = JOptionPane.showInputDialog(frame, "New password:");

                if (newPassword == null || newPassword.trim().isEmpty() ||
                    newRole == null || newRole.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Invalid input. Please try again.");
                } else if (userManager.updateUser(uname, PasswordUtil.hashPassword(newPassword), newRole)) {
                    JOptionPane.showMessageDialog(frame, "User updated.");
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to update user.");
                }
            }
        });

        deleteUserBtn.addActionListener(e -> {
            List<String> usernames = userManager.getAllUsernamesExcluding(adminUsername);
            if (usernames.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No other users to delete.");
                return;
            }
            String uname = (String) JOptionPane.showInputDialog(
                    frame, "Select user to delete:", "Delete User",
                    JOptionPane.PLAIN_MESSAGE, null,
                    usernames.toArray(), usernames.get(0)
            );

            if (uname != null) {
                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete " + uname + "?");
                if (confirm == JOptionPane.YES_OPTION && userManager.deleteUser(uname)) {
                    JOptionPane.showMessageDialog(frame, "User deleted.");
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to delete user.");
                }
            }
        });

        logoutBtn.addActionListener(e -> {
            frame.dispose();
            new LoginGUI();
        });

        frame.setVisible(true);
    }

    private void showParticipantDashboard(String username, String role, String Password) {
        new ParticipantUI(new User(username, Password, role));
    }

    private void showOrganiserDashboard(String username, String role, String Password) {
        new OrganiserUI(new User(username, Password, role));
    }

    public static void main(String[] args) {
        new LoginGUI();
    }
}

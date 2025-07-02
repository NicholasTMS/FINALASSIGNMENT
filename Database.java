import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:Database.db";

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                conn.createStatement().execute("PRAGMA foreign_keys = ON;");
                createEventTable(conn);
                createUserTable(conn);
                //createRegistrationTable(conn);
                System.out.println("✅ All tables created (if not exist).");
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
        }
    }

    private static void createEventTable(Connection conn) throws SQLException {
        String eventTable = """
            CREATE TABLE IF NOT EXISTS Event (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                venue TEXT NOT NULL,
                datetime TEXT NOT NULL,
                capacity INTEGER NOT NULL,
                totalRegistered INTEGER NOT NULL,
                registrationFee REAL NOT NULL,
                eventType TEXT NOT NULL,
                picture BLOB,
                organiser TEXT NOT NULL
            );
        """;

        String serviceTable = """
            CREATE TABLE IF NOT EXISTS EventAdditionalServices (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_id INTEGER NOT NULL,
                service TEXT NOT NULL,
                cost REAL NOT NULL,
                FOREIGN KEY(event_id) REFERENCES Event(id) ON DELETE CASCADE
            );
        """;

        String discountTable = """
            CREATE TABLE IF NOT EXISTS EventDiscounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_id INTEGER NOT NULL,
                discountType TEXT NOT NULL,
                value REAL NOT NULL,
                FOREIGN KEY(event_id) REFERENCES Event(id) ON DELETE CASCADE
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(eventTable);
            stmt.execute(serviceTable);
            stmt.execute(discountTable);
        }
    }


    private static void createUserTable(Connection conn) throws SQLException {
        
        String userTable = """
            CREATE TABLE IF NOT EXISTS Users (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT    NOT NULL UNIQUE,
                hashedpassword TEXT    NOT NULL,
                role     TEXT    NOT NULL
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(userTable);
        }

        String insertOrganiser = """
            INSERT OR IGNORE INTO Users(username, hashedpassword, role)
            VALUES (?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(insertOrganiser)) {
            String pass = PasswordUtil.hashPassword("12345");
            ps.setString(1, "OrganiserGuest");
            ps.setString(2, pass);
            ps.setString(3, "organiser");
            ps.executeUpdate();
        }

        String insertAdmin = """
            INSERT OR IGNORE INTO Users(username, hashedpassword, role)
            VALUES (?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(insertAdmin)) {
            String pass = PasswordUtil.hashPassword("12345");
            ps.setString(1, "AdminGuest");
            ps.setString(2, pass);
            ps.setString(3, "admin");
            ps.executeUpdate();
        }

        String insertParticipant = """
            INSERT OR IGNORE INTO Users(username, hashedpassword, role)
            VALUES (?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(insertParticipant)) {
             String pass = PasswordUtil.hashPassword("12345");
            ps.setString(1, "ParticipantGuest");
            ps.setString(2, pass);
            ps.setString(3, "participant");
            ps.executeUpdate();
        }
    }


    private static void createRegistrationTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS registrations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_id INTEGER,
                user_id INTEGER,
                FOREIGN KEY(event_id) REFERENCES events(id),
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static void main(String[] args) {
        initialize();
    }
}


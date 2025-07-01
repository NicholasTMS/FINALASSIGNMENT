import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:Database.db";

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                conn.createStatement().execute("PRAGMA foreign_keys = ON;");
                createEventTable(conn);
                //createUserTable(conn);
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
                registrationFee REAL NOT NULL,
                eventType TEXT NOT NULL,
                picture BLOB
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
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
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


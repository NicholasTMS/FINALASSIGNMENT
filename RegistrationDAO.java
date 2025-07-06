import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {
    private static final String URL = "jdbc:sqlite:Database.db";

    public void insert(
        String eventIdStr,
        String userIdStr,
        int tickets,
        double servicesCost,
        double discountAmount,
        double totalPrice
    ) throws SQLException {
        String sql = """
            INSERT INTO Registration(
              event_id, user_id, tickets,
              servicesCost, discountAmount, totalPrice
            ) VALUES(?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, eventIdStr);
            ps.setString(2, userIdStr);
            ps.setInt   (3, tickets);
            ps.setDouble(4, servicesCost);
            ps.setDouble(5, discountAmount);
            ps.setDouble(6, totalPrice);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                }
            }
        }
    }
    public List<Registration> loadByUser(String userIdStr) throws SQLException {
        String sql = """
            SELECT id, event_id, user_id, tickets,
                servicesCost, discountAmount, totalPrice, registeredAt
            FROM Registration
            WHERE user_id = ?
        """;

        List<Registration> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userIdStr);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Registration r = new Registration(
                        rs.getString("event_id"),
                        rs.getString("user_id"),
                        rs.getInt("tickets"),
                        rs.getDouble("servicesCost"),
                        rs.getDouble("discountAmount"),
                        rs.getDouble("totalPrice")
                    );
                    r.setId(rs.getInt("id"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    r.setRegisteredAt(LocalDateTime.parse(rs.getString("registeredAt"), formatter));

                    list.add(r);
                }
            }
        }
        return list;
    }

    public List<Registration> loadByEvent(String eventIdStr) throws SQLException {
        String sql = """
            SELECT id,
                   event_id,
                   user_id,
                   tickets,
                   servicesCost,
                   discountAmount,
                   totalPrice,
                   registeredAt
              FROM Registration
             WHERE event_id = ?
            """;

        List<Registration> list = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, eventIdStr);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Registration r = new Registration(
                        rs.getString("event_id"),
                        rs.getString("user_id"),
                        rs.getInt   ("tickets"),
                        rs.getDouble("servicesCost"),
                        rs.getDouble("discountAmount"),
                        rs.getDouble("totalPrice")
                    );
                    r.setId(rs.getInt("id"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    r.setRegisteredAt(LocalDateTime.parse(rs.getString("registeredAt"), formatter));

                    list.add(r);
                }
            }
        }
        return list;
    }
}




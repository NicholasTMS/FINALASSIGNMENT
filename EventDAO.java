import java.sql.*;
import java.util.EnumMap;

public class EventDAO {
    private static final String URL = "jdbc:sqlite:Database.db";

    /** Inserts the Event and its services & discounts, and populates eventID */
    public void insert(Event e) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);

            // 1) Insert into Event and get the generated id
            String sqlEvent = """
                INSERT INTO Event(name, venue, datetime, capacity, registrationFee, eventType)
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlEvent, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, e.getEventName());
                ps.setString(2, e.getVenue());
                ps.setString(3, e.getDate().toString());           // ISO format
                ps.setInt(4, e.getCapacity());
                ps.setDouble(5, e.getRegisterationFee());
                ps.setString(6, e.getEventType().name());
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No ID returned");
                int id = keys.getInt(1);
                e.setEventID(String.valueOf(id));  // set the eventID
            }

            // 2) Insert each additional service
            String sqlService = """
                INSERT INTO EventAdditionalServices(event_id, service, cost)
                VALUES (?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlService)) {
                for (EnumMap.Entry<AdditionalServices, Double> entry
                        : e.getAvailableAdditionalServices().entrySet()) {
                    ps.setInt(1, Integer.parseInt(e.getEventID()));
                    ps.setString(2, entry.getKey().name());
                    ps.setDouble(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 3) Insert each discount
            String sqlDiscount = """
                INSERT INTO EventDiscounts(event_id, discountType, value)
                VALUES (?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlDiscount)) {
                for (EnumMap.Entry<DiscountType, Double> entry
                        : e.getAvailableDiscounts().entrySet()) {
                    ps.setInt(1, Integer.parseInt(e.getEventID()));
                    ps.setString(2, entry.getKey().name());
                    ps.setDouble(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        }
    }
}


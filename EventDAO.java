import java.sql.*;
import java.util.EnumMap;

public class EventDAO {
    private static final String URL = "jdbc:sqlite:Database.db";

    // Inserts the Event and its services & discounts, and populates eventID 
    public void insert(Event e) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);

            // Insert into Event and get the generated id
            String sqlEvent = """
                INSERT INTO Event(name, venue, datetime, capacity, totalRegistered, registrationFee, eventType, picture)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlEvent, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, e.getEventName());
                ps.setString(2, e.getVenue());
                ps.setString(3, e.getDate().toString());           // ISO format
                ps.setInt(4, e.getCapacity());
                ps.setInt(5, e.getTotalRegistered());
                ps.setDouble(6, e.getRegisterationFee());
                ps.setString(7, e.getEventType().name());
                ps.setBytes(8, e.getPictureData());
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No ID returned");
                int id = keys.getInt(1);
                e.setEventID(String.valueOf(id));  // set the eventID
            }

            // Insert each additional service
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

            // Insert each discount
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
            System.out.println("Successful insertion to database");
        }
    }
}


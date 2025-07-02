import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

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

    public List<Event> loadAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();

        String sql = "SELECT id, name, venue, datetime, capacity, totalRegistered, registrationFee, eventType, picture "
                   + "FROM Event";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // 1) Basic fields
                int      id              = rs.getInt("id");
                String   name            = rs.getString("name");
                String   venue           = rs.getString("venue");
                LocalDateTime datetime   = LocalDateTime.parse(rs.getString("datetime"));
                int      capacity        = rs.getInt("capacity");
                int      totalRegistered = rs.getInt("totalRegistered");
                double   fee             = rs.getDouble("registrationFee");
                EventType type           = EventType.valueOf(rs.getString("eventType"));
                byte[]   pictureBytes    = rs.getBytes("picture");

                // 2) Construct event and set picture + totalRegistered
                Event e = new Event(name, venue, datetime, capacity, fee, type);
                e.setEventID(String.valueOf(id));
                e.setTotalRegistered(totalRegistered);
                e.setPictureData(pictureBytes);

                // 3) Load its additional services
                e.setAvailableAdditionalServices(loadServicesForEvent(id, conn));

                // 4) Load its discounts
                e.setAvailableDiscounts(loadDiscountsForEvent(id, conn));

                events.add(e);
            }
        }

        return events;
    }

    /** Helper: load the service→cost map for a single event */
    private EnumMap<AdditionalServices, Double> loadServicesForEvent(int eventId, Connection conn)
            throws SQLException {
        String sql = "SELECT service, cost FROM EventAdditionalServices WHERE event_id = ?";
        EnumMap<AdditionalServices, Double> map = new EnumMap<>(AdditionalServices.class);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdditionalServices svc = AdditionalServices.valueOf(rs.getString("service"));
                    double cost = rs.getDouble("cost");
                    map.put(svc, cost);
                }
            }
        }
        return map;
    }

    /** Helper: load the discountType→value map for a single event */
    private EnumMap<DiscountType, Double> loadDiscountsForEvent(int eventId, Connection conn)
            throws SQLException {
        String sql = "SELECT discountType, value FROM EventDiscounts WHERE event_id = ?";
        EnumMap<DiscountType, Double> map = new EnumMap<>(DiscountType.class);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DiscountType dt = DiscountType.valueOf(rs.getString("discountType"));
                    double val = rs.getDouble("value");
                    map.put(dt, val);
                }
            }
        }
        return map;
    }
}


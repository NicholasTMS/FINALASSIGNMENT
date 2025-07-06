import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class EventDAO {
    private static final String URL = "jdbc:sqlite:Database.db";

    /** Inserts the Event and its services & discounts, and populates eventID. */
    public void insert(Event e) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);

            // 1) Insert main record
            String sqlEvent = """
                INSERT INTO Event(name, venue, datetime, capacity, totalRegistered,
                                  registrationFee, eventType, picture, organiser)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlEvent, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, e.getEventName());
                ps.setString(2, e.getVenue());
                ps.setString(3, e.getDate().toString());
                ps.setInt(4, e.getCapacity());
                ps.setInt(5, e.getTotalRegistered());
                ps.setDouble(6, e.getRegisterationFee());
                ps.setString(7, e.getEventType().name());
                ps.setBytes(8, e.getPictureData());
                ps.setString(9, e.getOrganiser());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No ID returned");
                    e.setEventID(String.valueOf(keys.getInt(1)));
                }
            }

            // 2) Insert services
            String sqlService = """
                INSERT INTO EventAdditionalServices(event_id, service, cost)
                VALUES (?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlService)) {
                for (var entry : e.getAvailableAdditionalServices().entrySet()) {
                    ps.setInt(1, Integer.parseInt(e.getEventID()));
                    ps.setString(2, entry.getKey().name());
                    ps.setDouble(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 3) Insert discounts
            String sqlDiscount = """
                INSERT INTO EventDiscounts(event_id, discountType, value)
                VALUES (?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sqlDiscount)) {
                for (var entry : e.getAvailableDiscounts().entrySet()) {
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

    /** Updates an existing event (and its services+discounts). */
    public void update(Event e) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);

            // 1) Update main row
            String sql = """
                UPDATE Event
                   SET name=?, venue=?, datetime=?, capacity=?, totalRegistered=?,
                       registrationFee=?, eventType=?, picture=?
                 WHERE id=?
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, e.getEventName());
                ps.setString(2, e.getVenue());
                ps.setString(3, e.getDate().toString());
                ps.setInt(4, e.getCapacity());
                ps.setInt(5, e.getTotalRegistered());
                ps.setDouble(6, e.getRegisterationFee());
                ps.setString(7, e.getEventType().name());
                ps.setBytes(8, e.getPictureData());
                ps.setInt(9, Integer.parseInt(e.getEventID()));
                ps.executeUpdate();
            }

            int id = Integer.parseInt(e.getEventID());

            // 2) Delete old services & re‑insert
            try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM EventAdditionalServices WHERE event_id=?")) {
                del.setInt(1, id);
                del.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO EventAdditionalServices(event_id, service, cost) VALUES(?,?,?)")) {
                for (var entry : e.getAvailableAdditionalServices().entrySet()) {
                    ps.setInt(1, id);
                    ps.setString(2, entry.getKey().name());
                    ps.setDouble(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 3) Delete old discounts & re‑insert
            try (PreparedStatement del = conn.prepareStatement(
                     "DELETE FROM EventDiscounts WHERE event_id=?")) {
                del.setInt(1, id);
                del.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO EventDiscounts(event_id, discountType, value) VALUES(?,?,?)")) {
                for (var entry : e.getAvailableDiscounts().entrySet()) {
                    ps.setInt(1, id);
                    ps.setString(2, entry.getKey().name());
                    ps.setDouble(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        }
    }

    /** Deletes an event (and cascades services & discounts via foreign key). */
    public void delete(String eventId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(
                  "DELETE FROM Event WHERE id = ?")) {
            ps.setInt(1, Integer.parseInt(eventId));
            ps.executeUpdate();
        }
    }

    public Event loadById(String id) throws SQLException {
        String sql = """
        SELECT id, name, venue, datetime, capacity,
                totalRegistered, registrationFee, eventType, picture, organiser
            FROM Event
        WHERE id = ?
        """;
        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Event e = new Event(
                    rs.getString("name"),
                    rs.getString("venue"),
                    LocalDateTime.parse(rs.getString("datetime")),
                    rs.getInt("capacity"),
                    rs.getDouble("registrationFee"),
                    EventType.valueOf(rs.getString("eventType"))
                );
                e.setEventID(String.valueOf(id));
                e.setTotalRegistered(rs.getInt("totalRegistered"));
                e.setPictureData(rs.getBytes("picture"));
                e.setOrganiser(rs.getString("organiser"));
                int idInt = Integer.parseInt(id);
                e.setAvailableAdditionalServices(loadServicesForEvent(idInt, conn));
                e.setAvailableDiscounts(loadDiscountsForEvent(idInt, conn));
                return e;
            }
        }
    }


    /** Loads *all* events, fully populated with services & discounts. */
    public List<Event> loadAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String mainSql = """
            SELECT id, name, venue, datetime, capacity,
                   totalRegistered, registrationFee, eventType, picture, organiser
              FROM Event
        """;

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(mainSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int    id   = rs.getInt("id");
                Event e    = new Event(
                  rs.getString("name"),
                  rs.getString("venue"),
                  LocalDateTime.parse(rs.getString("datetime")),
                  rs.getInt("capacity"),
                  rs.getDouble("registrationFee"),
                  EventType.valueOf(rs.getString("eventType"))
                );
                e.setEventID(String.valueOf(id));
                e.setTotalRegistered(rs.getInt("totalRegistered"));
                e.setPictureData(rs.getBytes("picture"));
                e.setOrganiser(rs.getString("organiser"));
                // populate maps
                e.setAvailableAdditionalServices(loadServicesForEvent(id, conn));
                e.setAvailableDiscounts     (loadDiscountsForEvent(id, conn));

                events.add(e);
            }
        }
        return events;
    }

    public List<Event> loadAllEventsForOrganiser(String organiser) throws SQLException {
        List<Event> events = new ArrayList<>();
        String mainSql = """
            SELECT id, name, venue, datetime, capacity,
                totalRegistered, registrationFee, eventType, picture, organiser
            FROM Event WHERE organiser = ?
        """;

        try (Connection conn = DriverManager.getConnection(URL);
            PreparedStatement ps = conn.prepareStatement(mainSql)) {

            // bind the organiser username to the query
            ps.setString(1, organiser);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Event e = new Event(
                    rs.getString("name"),
                    rs.getString("venue"),
                    LocalDateTime.parse(rs.getString("datetime")),
                    rs.getInt("capacity"),
                    rs.getDouble("registrationFee"),
                    EventType.valueOf(rs.getString("eventType"))
                    );
                    e.setEventID(String.valueOf(id));
                    e.setTotalRegistered(rs.getInt("totalRegistered"));
                    e.setPictureData(rs.getBytes("picture"));
                    e.setOrganiser(rs.getString("organiser"));

                    // Load related maps
                    e.setAvailableAdditionalServices(loadServicesForEvent(id, conn));
                    e.setAvailableDiscounts(loadDiscountsForEvent(id, conn));

                    events.add(e);
                }
            }
        }
        return events;
    }


    //----helper for laoding servies and discounts

    private EnumMap<AdditionalServices, Double> loadServicesForEvent(int eventId, Connection conn)
        throws SQLException {
        EnumMap<AdditionalServices, Double> map = new EnumMap<>(AdditionalServices.class);
        String sql = "SELECT service, cost FROM EventAdditionalServices WHERE event_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdditionalServices s = AdditionalServices.valueOf(rs.getString("service"));
                    map.put(s, rs.getDouble("cost"));
                }
            }
        }
        return map;
    }


    private EnumMap<DiscountType, Double> loadDiscountsForEvent(int eventId, Connection conn)
        throws SQLException {
        EnumMap<DiscountType, Double> map = new EnumMap<>(DiscountType.class);
        String sql = "SELECT discountType, value FROM EventDiscounts WHERE event_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DiscountType d = DiscountType.valueOf(rs.getString("discountType"));
                    map.put(d, rs.getDouble("value"));
                }
            }
        }
        return map;
    }

}



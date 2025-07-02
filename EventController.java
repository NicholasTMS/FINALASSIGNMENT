import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class EventController {
    private Event model;
    private final EventDAO dao = new EventDAO();

    /** Returns the current event, or null until one is created */
    public Event getModel() {
        return model;
    }

    /**
     * Called by the UI on Create button press.
     * This instantiates the Event for the first time.
     */
    public void createEvent(
        String name,
        String venue,
        LocalDateTime date,
        int capacity,
        double fee,
        EventType type,
        EnumMap<AdditionalServices, Double> services,
        EnumMap<DiscountType, Double> discounts,
        byte[] pictureData 
    ) {
        model = new Event(name, venue, date, capacity, fee, type);
        model.setAvailableAdditionalServices(services);
        model.setAvailableDiscounts(discounts);
        model.setPictureData(pictureData);

        try {
            dao.insert(model);
            System.out.println("Inserted event with ID = " + model.getEventID());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<Event> loadAllEvents() {
        try {
            return dao.loadAllEvents();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // You could also bubble up a custom exception or return empty list
            return Collections.emptyList();
        }
    }
}



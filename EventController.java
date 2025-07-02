import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public class EventController {
    private Event model;
    private final EventDAO dao = new EventDAO();

    private final List<EventObserver> listeners = new ArrayList<>();

    /** UI classes call this to register for changes */
    public void addListener(EventObserver l) {
        listeners.add(l);
    }
    public void removeListener(EventObserver l) {
        listeners.remove(l);
    }
    private void fireChange() {
        for (var l : listeners) l.onEventsChanged();
    }

    public Event getModel() {
        return model;
    }

    public void createEvent(
        String name, String venue,
        LocalDateTime date, int capacity,
        double fee, EventType type,
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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        fireChange();
    }

    public void updateEvent(
        String id,
        String name, String venue,
        LocalDateTime date, int capacity,
        double fee, EventType type,
        EnumMap<AdditionalServices, Double> services,
        EnumMap<DiscountType, Double> discounts,
        byte[] pictureData
    ) {
        model = new Event(name, venue, date, capacity, fee, type);
        model.setEventID(id);
        model.setAvailableAdditionalServices(services);
        model.setAvailableDiscounts(discounts);
        model.setPictureData(pictureData);

        try {
            dao.update(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        fireChange();
    }

    public void deleteEvent(String id) {
        try {
            dao.delete(id);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        fireChange();
    }

    public List<Event> loadAllEvents() {
        try {
            return dao.loadAllEvents();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }
}





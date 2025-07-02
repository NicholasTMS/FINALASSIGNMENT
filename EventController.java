import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class EventController implements Subject {
    private Event model;
    private final EventDAO dao = new EventDAO();
    private final List<Observer> observers = new ArrayList<>();

    // Subject methods
    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update();
        }
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

        notifyObservers(); // notify UI
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

        notifyObservers();
    }

    public void deleteEvent(String id) {
        try {
            dao.delete(id);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        notifyObservers();
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






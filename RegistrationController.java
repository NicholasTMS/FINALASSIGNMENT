// RegistrationController.java
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class RegistrationController implements Subject {
    private final RegistrationDAO dao    = new RegistrationDAO();
    private final EventDAO        eventDao = new EventDAO();
    private final List<Observer>  observers = new ArrayList<>();

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
        for (Observer o : observers) o.update();
    }

    public void registerParticipant(
        String eventIdStr,
        String userIdStr,
        int    tickets,
        double servicesTotal,
        double discountsTotal,
        double totalPrice
    ) {
        try {
            
            dao.insert(
                eventIdStr,
                userIdStr,
                tickets,
                servicesTotal,
                discountsTotal,
                totalPrice
            );

            // Then bump the event’s totalRegistered
            Event event = eventDao.loadById(eventIdStr);
            event.setTotalRegistered(event.getTotalRegistered() + tickets);
            eventDao.update(event);

            notifyObservers();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // You may want to surface an error dialog to the UI here
        }
    }

    public List<Registration> loadRegistrationsForEvent(String eventIdStr) {
        try {
            return dao.loadByEvent(eventIdStr);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public List<Registration> loadRegistrationsByUser(String userIdStr) {
        try {
            return dao.loadByUser(userIdStr);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }
}


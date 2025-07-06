// Registration.java
import java.time.LocalDateTime;

public class Registration {
    private int id;
    private String eventId;
    private String userId;
    private int tickets;
    private double servicesCost;
    private double discountAmount;
    private double totalPrice;
    private LocalDateTime registeredAt;

    // Constructor (without id & timestamp)
    public Registration(String eventId, String userId, int tickets,
                        double servicesCost, double discountAmount, double totalPrice) {
        this.eventId       = eventId;
        this.userId        = userId;
        this.tickets       = tickets;
        this.servicesCost  = servicesCost;
        this.discountAmount= discountAmount;
        this.totalPrice    = totalPrice;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getTickets() {
        return tickets;
    }
    public void setTickets(int tickets) {
        this.tickets = tickets;
    }

    public double getServicesCost() {
        return servicesCost;
    }
    public void setServicesCost(double servicesCost) {
        this.servicesCost = servicesCost;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}



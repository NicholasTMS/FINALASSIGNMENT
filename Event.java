import java.time.LocalDateTime; // for storing dates and times
import java.util.EnumMap; // for additional services

public class Event {
    private String eventID; // assigned by auto increment db
    private String eventName;
    private String venue;
    private LocalDateTime date; // uses yyyy-MM-ddTHH:mm:ss format // 2025-06-27T14:30:00 // T is just a separator char to separate date adn time
    private int capacity;
    private byte[] pictureData;
    private int totalRegistered; // not sure if this needed, check with registration class
    private double registerationFee;
    private EventType eventType;
    private EnumMap<AdditionalServices, Double> availableAdditionalServices = new EnumMap<>(AdditionalServices.class); // (service,cost)
    private EnumMap<DiscountType, Double> availableDiscounts = new EnumMap<>(DiscountType.class); // (discoun type, cost) // allow user to set how much it will minus the price or allow them to set a % which will be a hardcoded calculation then set into params
    private String organiser;
    
    public Event(String eventName, String venue, LocalDateTime date, int capacity, double registrationFee, EventType eventType) {
       
        this.eventName = eventName;
        this.venue = venue;
        this.date = date;
        this.capacity = capacity;
        this.registerationFee = registrationFee;
        this.eventType = eventType;
        this.totalRegistered = 0;
    }

    
    // setters and getters
    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getRegisterationFee() {
        return registerationFee;
    }

    public void setRegisterationFee(double registerationFee) {
        this.registerationFee = registerationFee;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public byte[] getPictureData() {
        return pictureData;
    }

   
    public void setPictureData(byte[] pictureData) {
        this.pictureData = pictureData;
    }

    public void setTotalRegistered(int totalRegistered) {
        this.totalRegistered = totalRegistered;
    }

    public int getTotalRegistered() {
        return totalRegistered;
    }

    // ----- additional services -----

    public EnumMap<AdditionalServices, Double> getAvailableAdditionalServices() {
        return availableAdditionalServices;
    }

    public void setAvailableAdditionalServices(EnumMap<AdditionalServices, Double> services) {
        this.availableAdditionalServices = services;
    }

    public void addAvailableService(AdditionalServices service, double cost) {
        this.availableAdditionalServices.put(service, cost);
    }

    public void removeAvailableService(AdditionalServices service) {
        this.availableAdditionalServices.remove(service);
    }

    public double getServiceCost(AdditionalServices service) {
        return this.availableAdditionalServices.getOrDefault(service, 0.0);
    }

    // ----- discounts -----

    public EnumMap<DiscountType, Double> getAvailableDiscounts() {
        return availableDiscounts;
    }

    public void setAvailableDiscounts(EnumMap<DiscountType, Double> discounts) {
        this.availableDiscounts = discounts;
    }

    public void addAvailableDiscount(DiscountType discountType, double value) {
        this.availableDiscounts.put(discountType, value);
    }

    public void removeAvailableDiscount(DiscountType discountType) {
        this.availableDiscounts.remove(discountType);
    }

    public double getDiscountValue(DiscountType discountType) {
        return this.availableDiscounts.getOrDefault(discountType, 0.0);
    }

    // ----- Organised by ------
    public void setOrganiser(String organiser) {
        this.organiser = organiser;
    }

    public String getOrganiser() {
        return organiser;
    }

}


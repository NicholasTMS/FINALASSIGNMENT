// EventChangeListener.java
public interface EventObserver {
    /** Called whenever the set of persisted events has changed. */
    void onEventsChanged();
}

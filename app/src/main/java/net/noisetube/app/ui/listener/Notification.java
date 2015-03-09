package net.noisetube.app.ui.listener;

/**
 * @author Humberto
 */
public class Notification {

    private int event;
    private Object value;

    public Notification() {
        this.event = -1;
        this.value = new Object();
    }

    public Notification(int event, Object value) {
        this.event = event;
        this.value = value;
    }

    public int getEvent() {
        return event;
    }

    public Object getValue() {
        return value;
    }
}

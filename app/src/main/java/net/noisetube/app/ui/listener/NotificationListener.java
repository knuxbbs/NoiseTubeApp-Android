package net.noisetube.app.ui.listener;

import java.io.Serializable;

/**
 * @author Humberto
 */
public interface NotificationListener extends Serializable {

    public void notify(Notification n);


}

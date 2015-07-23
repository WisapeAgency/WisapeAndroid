package org.cubieline.lplayer;

import android.app.Service;
import android.os.Bundle;

/**
 * Created by tonyley on 14/12/25.
 */
public interface NotificationCallback {

    /**
     * The method is invoked if player need to show foreground notification
     *
     * @param service
     * @param args
     * @param notificationId
     * @return notification id;
     */
    public int onStartForeground(Service service, Bundle args, int notificationId);
}

package com.example.clinic.observer;

import com.example.clinic.model.Notification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class NotificationSubject {

    private final List<NotificationObserver> observers = new CopyOnWriteArrayList<>();

    public void subscribe(NotificationObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyNotificationCreated(Notification notification) {
        for (NotificationObserver observer : observers) {
            observer.onNotificationCreated(notification);
        }
    }

    public void notifyNotificationsRead(Long clientId) {
        for (NotificationObserver observer : observers) {
            observer.onNotificationsRead(clientId);
        }
    }
}
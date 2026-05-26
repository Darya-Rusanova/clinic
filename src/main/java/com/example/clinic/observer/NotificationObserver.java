package com.example.clinic.observer;

import com.example.clinic.model.Notification;

public interface NotificationObserver {
    void onNotificationCreated(Notification notification);
    void onNotificationsRead(Long clientId);
}
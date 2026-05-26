package com.example.clinic.service;

import com.example.clinic.model.Client;
import com.example.clinic.model.Notification;
import com.example.clinic.model.NotificationType;
import com.example.clinic.observer.NotificationSubject;
import com.example.clinic.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private NotificationSubject notificationSubject;

    @Transactional
    public Notification createNotification(Integer clientId, String title, String message, NotificationType type) {
        Client client = clientService.getClientById(clientId);
        if (client == null) return null;

        Notification notification = new Notification();
        notification.setClient(client);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);

        notificationSubject.notifyNotificationCreated(saved);

        return saved;
    }

    public Page<Notification> getClientNotifications(Integer clientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByClient_UserIdOrderByCreatedAtDesc(clientId, pageable);
    }

    public long getUnreadCount(Integer clientId) {
        return notificationRepository.countByClient_UserIdAndIsReadFalse(clientId);
    }

    @Transactional
    public void markAllAsRead(Integer clientId) {
        notificationRepository.markAllAsRead(clientId);

        notificationSubject.notifyNotificationsRead(Long.valueOf(clientId));
    }
}
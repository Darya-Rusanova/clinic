package com.example.clinic.controller;

import com.example.clinic.model.Client;
import com.example.clinic.model.Notification;
import com.example.clinic.service.ClientService;
import com.example.clinic.service.AppointmentService;
import com.example.clinic.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;
    @Autowired
    NotificationService notificationService;

    @Autowired
    private AppointmentService appointmentService;
    @GetMapping("/client/{id}")
    public String client(@PathVariable Integer id, Model model) {
        Client client = clientService.getClientById(id);
        if (client != null) {
            model.addAttribute("client", client);
            return "client";  // ← ищет шаблон client.html
        }
        return "home";
    }

    @GetMapping("/client")
    public String redirectToClient(Authentication authentication) {
        String email = authentication.getName();
        Integer clientId = clientService.getClientIdByEmail(email);
        if (clientId != null) {
            return "redirect:/client/" + clientId;
        }
        return "redirect:/";
    }
    @PutMapping("/api/client/{id}/profile")
    @ResponseBody
    public Map<String, Object> updateProfile(@PathVariable Integer id,
                                             @RequestBody Map<String, Object> data) {
        return clientService.updateClientProfile(id, data);
    }

    @GetMapping("/api/client/{id}/notifications")
    @ResponseBody
    public Map<String, Object> getNotifications(@PathVariable Integer id,
                                                @RequestParam(defaultValue = "0") int page) {
        Map<String, Object> response = new HashMap<>();
        Page<Notification> notifications = notificationService.getClientNotifications(id, page, 10);

        List<Map<String, Object>> notificationList = notifications.getContent().stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("title", n.getTitle());
            map.put("message", n.getMessage());
            map.put("type", n.getType().name());
            map.put("read", n.isRead());
            map.put("createdAt", n.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList());

        response.put("notifications", notificationList);
        response.put("unreadCount", notificationService.getUnreadCount(id));
        response.put("totalPages", notifications.getTotalPages());
        response.put("currentPage", notifications.getNumber());

        return response;
    }

    @PostMapping("/api/client/{id}/notifications/read")
    @ResponseBody
    public Map<String, Object> markNotificationsAsRead(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        notificationService.markAllAsRead(id);
        response.put("success", true);
        return response;
    }
}
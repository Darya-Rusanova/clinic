package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Client;
import com.example.clinic.model.Status;
import com.example.clinic.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class ClientFilterController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/api/doctor/{id}/clients")
    public Map<String, Object> getClientsJson(@PathVariable Integer id,
                                              @RequestParam(defaultValue = "all") String filter) {
        List<Appointment> allAppointments = appointmentService.getDoctorAppointments(id);

        Map<Integer, Map<String, Object>> clientsMap = new LinkedHashMap<>();

        for (Appointment app : allAppointments) {
            Client client = app.getClient();
            Integer clientId = client.getUserId();

            if (!clientsMap.containsKey(clientId)) {
                Map<String, Object> clientData = new HashMap<>();
                clientData.put("name", client.getUser().getName());
                clientData.put("phone", client.getUser().getPhone());
                clientData.put("email", client.getUser().getEmail());
                clientData.put("appointments", new ArrayList<Map<String, String>>());
                clientsMap.put(clientId, clientData);
            }

            Map<String, String> appData = new HashMap<>();
            appData.put("date", app.getDateTime().toLocalDate().toString());
            appData.put("time", app.getDateTime().toLocalTime().toString().substring(0, 5));
            try {
                if (app.getService() != null) {
                    appData.put("service", app.getService().getName());
                } else {
                    appData.put("service", "Услуга удалена");
                }
            } catch (Exception e) {
                appData.put("service", "Услуга удалена");
            }
            appData.put("status", app.getStatus().name());

            if (filter.equals("all") || app.getStatus().name().equals(filter)) {
                List<Map<String, String>> apps = (List<Map<String, String>>) clientsMap.get(clientId).get("appointments");
                apps.add(appData);
            }
        }

        List<Map<String, Object>> filteredClients = new ArrayList<>();
        for (Map<String, Object> client : clientsMap.values()) {
            List<Map<String, String>> apps = (List<Map<String, String>>) client.get("appointments");
            if (!apps.isEmpty()) {
                filteredClients.add(client);
            }
        }

        long scheduledCount = allAppointments.stream().filter(a -> a.getStatus() == Status.SCHEDULED).count();
        long completedCount = allAppointments.stream().filter(a -> a.getStatus() == Status.COMPLETED).count();
        long cancelledCount = allAppointments.stream().filter(a -> a.getStatus() == Status.CANCELLED).count();

        Map<String, Object> response = new HashMap<>();
        response.put("clients", filteredClients);
        response.put("allCount", allAppointments.size());
        response.put("scheduledCount", scheduledCount);
        response.put("completedCount", completedCount);
        response.put("cancelledCount", cancelledCount);

        return response;
    }
}
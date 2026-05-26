package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Client;
import com.example.clinic.model.Status;
import com.example.clinic.service.AppointmentService;
import com.example.clinic.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/doctor")
public class DoctorApiController {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private DoctorService doctorService;

    @GetMapping("/{id}/clients")
    @ResponseBody
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
                clientData.put("appointments", new ArrayList<Map<String, Object>>());
                clientsMap.put(clientId, clientData);
            }

            Map<String, Object> appData = new HashMap<>();
            appData.put("id", app.getId());
            appData.put("date", app.getDateTime().toLocalDate().toString());
            appData.put("time", app.getDateTime().toLocalTime().toString().substring(0, 5));
            appData.put("service", app.getService() != null ? app.getService().getName() : "Услуга удалена");
            appData.put("status", app.getStatus().name());

            if (filter.equals("all") || app.getStatus().name().equals(filter)) {
                List<Map<String, Object>> apps = (List<Map<String, Object>>) clientsMap.get(clientId).get("appointments");
                apps.add(appData);
            }
        }

        List<Map<String, Object>> filteredClients = new ArrayList<>();
        for (Map<String, Object> client : clientsMap.values()) {
            List<Map<String, Object>> apps = (List<Map<String, Object>>) client.get("appointments");
            if (!apps.isEmpty()) {
                filteredClients.add(client);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("clients", filteredClients);
        response.put("allCount", allAppointments.size());
        response.put("scheduledCount", allAppointments.stream().filter(a -> a.getStatus() == Status.SCHEDULED).count());
        response.put("completedCount", allAppointments.stream().filter(a -> a.getStatus() == Status.COMPLETED).count());
        response.put("cancelledCount", allAppointments.stream().filter(a -> a.getStatus() == Status.CANCELLED).count());

        return response;
    }

    @PutMapping("/appointments/{id}/complete")
    @ResponseBody
    public Map<String, Object> completeAppointment(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean completed = appointmentService.completeAppointment(id);
            if (completed) {
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("error", "Запись не найдена или уже завершена");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
    @PutMapping("/{id}/profile")
    @ResponseBody
    public Map<String, Object> updateProfile(@PathVariable Integer id,
                                             @RequestBody Map<String, Object> data) {
        return doctorService.updateDoctorProfile(id, data);
    }
}
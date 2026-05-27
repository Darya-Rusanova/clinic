package com.example.clinic.controller;

import com.example.clinic.model.Client;
import com.example.clinic.service.ClientService;
import com.example.clinic.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/clients")
    public String clientsPage() {
        return "admin/admin-clients";
    }

    @GetMapping("/clients/{id}/appointments")
    public String clientAppointmentsPage(@PathVariable Integer id, Model model) {
        model.addAttribute("clientId", id);
        return "admin/admin-client-appointments";
    }

    @GetMapping("/api/clients")
    @ResponseBody
    public List<Map<String, Object>> getClients(@RequestParam(required = false) String search) {
        List<Client> clients = clientService.getAllClients();

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            String searchDigits = searchLower.replaceAll("[^0-9]", "");

            clients = clients.stream().filter(client -> {
                if (client.getUser().getName().toLowerCase().contains(searchLower)) return true;
                if (!searchDigits.isEmpty()) {
                    String phoneDigits = client.getUser().getPhone().replaceAll("[^0-9]", "");
                    if (searchDigits.charAt(0) == '8') {
                        phoneDigits = phoneDigits.replaceFirst("7", "8");
                    }
                    if (phoneDigits.contains(searchDigits)) return true;
                }
                return false;
            }).toList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Client client : clients) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", client.getUserId());
            info.put("name", client.getUser().getName());
            info.put("phone", client.getUser().getPhone());
            info.put("email", client.getUser().getEmail());
            info.put("birthDate", client.getBirthDate());

            String[] name = client.getUser().getName().split(" ");
            info.put("lastName", name.length > 0 ? name[0] : "");
            info.put("firstName", name.length > 1 ? name[1] : "");
            info.put("patronymic", name.length > 2 ? name[2] : "");
            info.put("gender", client.getUser().isGender());

            int appointmentsCount = appointmentService.countByClient(client.getUserId());
            info.put("appointmentsCount", appointmentsCount);
            result.add(info);
        }
        return result;
    }

    @GetMapping("/api/clients/{id}")
    @ResponseBody
    public Map<String, Object> getClient(@PathVariable Integer id) {
        Client client = clientService.getClientById(id);
        Map<String, Object> result = new HashMap<>();
        if (client != null) {
            result.put("id", client.getUserId());
            result.put("name", client.getUser().getName());
            result.put("phone", client.getUser().getPhone());
            result.put("email", client.getUser().getEmail());
        }
        return result;
    }

    @PutMapping("/api/clients/{id}/edit")
    @ResponseBody
    public Map<String, Object> editClient(@RequestBody Map<String, Object> data,
                                          @PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            clientService.updateClient(id, data);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/api/clients/{id}/delete")
    @ResponseBody
    public Map<String, Object> deleteClient(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            clientService.deleteClient(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
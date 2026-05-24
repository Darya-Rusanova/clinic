package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Client;
import com.example.clinic.model.User;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.ClientRepository;
import com.example.clinic.repository.UserRepository;
import com.example.clinic.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminClientController {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BookingService bookingService;

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
        List<Client> clients = clientRepository.findAll();

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            String searchDigits = searchLower.replaceAll("[^0-9]", "");

            clients = clients.stream().filter(client -> {
                if (client.getUser().getName().toLowerCase().contains(searchLower)) {
                    return true;
                }
                if (!searchDigits.isEmpty()) {
                    String phoneDigits = client.getUser().getPhone().replaceAll("[^0-9]", "");
                    if (searchDigits.charAt(0) == '8') {
                        phoneDigits = phoneDigits.replaceFirst("7", "8");
                    }
                    if (phoneDigits.contains(searchDigits)) {
                        return true;
                    }
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
            info.put("firstName", name[0]);
            info.put("lastName",name[1]);
            info.put("patronymic", name.length==3 ? name[2] : "");
            info.put("gender", client.getUser().isGender());

            List<Appointment> appointments = appointmentRepository.findAllByClient_UserId(client.getUserId());
            info.put("appointmentsCount", appointments.size());
            result.add(info);
        }
        return result;
    }

    @PutMapping("/api/clients/{id}/edit")
    @ResponseBody
    public Map<String,Object> editClient(@RequestBody Map<String,Object> data,
                                         @PathVariable Integer id){
        Map<String, Object> response = new HashMap<>();
        Client client = clientRepository.findById(id).orElse(null);
        if (client != null){
            User user = client.getUser();
            String fullName = data.get("lastName") + " " + data.get("firstName") + " " + data.get("patronymic");
            user.setName(fullName);
            user.setPhone((String) data.get("phone"));
            user.setEmail((String) data.get("email"));
            user.setGender((Boolean) data.get("gender"));
            client.setBirthDate(LocalDate.parse((String) data.get("birthDate")));
            userRepository.save(user);
            clientRepository.save(client);
            response.put("success",true);
        }
        else {
            response.put("success",false);
        }
        return response;
    }

    @DeleteMapping("/api/clients/{id}/delete")
    @ResponseBody
    public Map<String, Object> deleteClient(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        Client client = clientRepository.findById(id).orElse(null);
        if (client != null) {
            List<Appointment> appointments = appointmentRepository.findAllByClient_UserId(id);
            appointmentRepository.deleteAll(appointments);
            clientRepository.delete(client);
            response.put("success", true);
        } else {
            response.put("success", false);
        }
        return response;
    }

    @GetMapping("/api/clients/{id}")
    @ResponseBody
    public Map<String, Object> getClient(@PathVariable Integer id) {
        Client client = clientRepository.findById(id).orElse(null);
        Map<String, Object> result = new HashMap<>();
        if (client != null) {
            result.put("id", client.getUserId());
            result.put("name", client.getUser().getName());
            result.put("phone", client.getUser().getPhone());
            result.put("email", client.getUser().getEmail());
        }
        return result;
    }

    @GetMapping("/api/clients/{id}/appointments")
    @ResponseBody
    public Map<String, Object> getClientAppointments(@PathVariable Integer id,
                                                     @RequestParam(defaultValue = "all") String filter) {
        List<Appointment> appointmentList = appointmentRepository.findAllByClient_UserIdWithDetails(id);

        Map<Integer, Map<String, Object>> appointments = new LinkedHashMap<>();

        for (Appointment app : appointmentList) {
            if (filter.equals("all") || app.getStatus().name().equals(filter)) {
                Map<String, Object> appInfo = new HashMap<>();
                appInfo.put("id", app.getId());
                appInfo.put("date", app.getDate());
                appInfo.put("time", app.getTime().toString().substring(0, 5));
                appInfo.put("status", app.getStatus().name());
                appInfo.put("dateTime", app.getDateTime().toString());

                Map<String, Object> service = new HashMap<>();
                try {
                    if (app.getService() != null) {
                        service.put("name", app.getService().getName());
                        service.put("duration", app.getService().getDuration());
                        service.put("price", app.getService().getPrice());
                    } else {
                        service.put("name", "Услуга удалена");
                        service.put("duration", 0);
                        service.put("price", 0);
                    }
                } catch (Exception e) {
                    service.put("name", "Услуга удалена");
                    service.put("duration", 0);
                    service.put("price", 0);
                }
                appInfo.put("service", service);

                Map<String, String> doctor = new HashMap<>();
                try {
                    if (app.getDoctor() != null && app.getDoctor().getUser() != null) {
                        doctor.put("name", app.getDoctor().getUser().getName());
                        doctor.put("email", app.getDoctor().getUser().getEmail());
                    } else {
                        doctor.put("name", "Врач больше не работает");
                        doctor.put("email", "");
                    }
                } catch (Exception e) {
                    doctor.put("name", "Врач больше не работает");
                    doctor.put("email", "");
                }
                appInfo.put("doctor", doctor);

                appointments.put(app.getId(), appInfo);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("appointments", appointments);
        response.put("allCount", appointmentList.size());
        response.put("scheduledCount", appointmentList.stream().filter(a -> a.getStatus().name().equals("SCHEDULED")).count());
        response.put("cancelledCount", appointmentList.stream().filter(a -> a.getStatus().name().equals("CANCELLED")).count());
        response.put("completedCount", appointmentList.stream().filter(a -> a.getStatus().name().equals("COMPLETED")).count());

        return response;
    }
}
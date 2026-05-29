package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Status;
import com.example.clinic.model.User;
import com.example.clinic.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    UserService userService;
    @Autowired
    ClientService clientService;
    @Autowired
    DoctorService doctorService;
    @Autowired
    AppointmentService appointmentService;
    @Autowired
    ServiceService serviceService;


    @GetMapping
    public String adminPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User admin = userService.getUserByEmail(email);

        long totalClients = clientService.getAllClients().size();
        long totalDoctors = doctorService.getAllDoctors().size();
        long totalServices = serviceService.getAllServices().size();
        long todayAppointmentsCount = appointmentService.countByDateTimeBetween(LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay(),Status.SCHEDULED);

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "dateTime"));
        List<Appointment> recentAppointments = appointmentService.getFirstScheduledAppointments(Status.SCHEDULED, LocalDateTime.now(), pageable);

        model.addAttribute("admin", admin);
        model.addAttribute("totalClients", totalClients);
        model.addAttribute("totalDoctors", totalDoctors);
        model.addAttribute("totalServices", totalServices);
        model.addAttribute("todayAppointmentsCount", todayAppointmentsCount);
        model.addAttribute("recentAppointments", recentAppointments);

        return "admin/admin";
    }
    @GetMapping("/api/appointments")
    @ResponseBody
    public Map<String, Object> getAppointments(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "all") String status,
                                               @RequestParam(defaultValue = "dateTime") String sort,
                                               @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        String sortField;
        switch (sort) {
            case "clientName":
                sortField = "client.user.name";
                break;
            case "doctorName":
                sortField = "doctor.user.name";
                break;
            case "dateTime":
            default:
                sortField = "dateTime";
                break;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Appointment> appointmentPage = appointmentService.getAllWithClientsAndDoctors(pageable, status);

        List<Map<String, Object>> appointments = appointmentPage.getContent().stream().map(app -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("dateTime", app.getDateTime().toString());

            String clientName = "Клиент удалён";
            if (app.getClient() != null && app.getClient().getUser() != null) {
                clientName = app.getClient().getUser().getName();
            }
            map.put("clientName", clientName);

            String serviceName = "Услуга удалена";
            if (app.getService() != null) {
                serviceName = app.getService().getName();
            }
            map.put("serviceName", serviceName);

            String doctorName = "Врач удалён";
            if (app.getDoctor() != null && app.getDoctor().getUser() != null) {
                doctorName = app.getDoctor().getUser().getName();
            }
            map.put("doctorName", doctorName);

            map.put("status", app.getStatus().name());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("appointments", appointments);
        response.put("currentPage", appointmentPage.getNumber());
        response.put("totalPages", appointmentPage.getTotalPages());
        response.put("totalElements", appointmentPage.getTotalElements());
        response.put("sortField", sort);
        response.put("sortDirection", direction);

        return response;
    }

    @GetMapping("/appointments")
    public String adminAppointments(){
        return "admin/admin-appointments";
    }
}
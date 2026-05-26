package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Status;
import com.example.clinic.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/api/client/{id}/appointments")
    public Map<String, Object> getClientAppointments(@PathVariable Integer id,
                                                     @RequestParam(defaultValue = "all") String filter,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "5") int size,
                                                     @RequestParam(defaultValue = "dateTime") String sort,
                                                     @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<Appointment> appointmentPage = appointmentService.getClientAppointmentsPaginated(id, filter, pageable);

        List<Map<String, Object>> appointmentsList = new ArrayList<>();
        for (Appointment app : appointmentPage.getContent()) {
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("id", app.getId());
            appInfo.put("date", app.getDate());
            appInfo.put("time", app.getTime().toString().substring(0, 5));
            appInfo.put("status", app.getStatus().name());

            Map<String, Object> service = new HashMap<>();
            if (app.getService() != null) {
                service.put("name", app.getService().getName());
                service.put("duration", app.getService().getDuration());
                service.put("price", app.getService().getPrice());
            } else {
                service.put("name", "Услуга удалена");
                service.put("duration", 0);
                service.put("price", 0);
            }
            appInfo.put("service", service);

            Map<String, String> doctor = new HashMap<>();
            if (app.getDoctor() != null && app.getDoctor().getUser() != null) {
                doctor.put("name", app.getDoctor().getUser().getName());
                doctor.put("email", app.getDoctor().getUser().getEmail());
            } else {
                doctor.put("name", "Врач больше не работает");
                doctor.put("email", "");
            }
            appInfo.put("doctor", doctor);

            appointmentsList.add(appInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("appointments", appointmentsList);
        response.put("allCount", appointmentService.countByClientAndStatus(id, Status.SCHEDULED) +
                appointmentService.countByClientAndStatus(id, Status.COMPLETED) +
                appointmentService.countByClientAndStatus(id, Status.CANCELLED));
        response.put("scheduledCount", appointmentService.countByClientAndStatus(id, Status.SCHEDULED));
        response.put("completedCount", appointmentService.countByClientAndStatus(id, Status.COMPLETED));
        response.put("cancelledCount", appointmentService.countByClientAndStatus(id, Status.CANCELLED));
        response.put("totalPages", appointmentPage.getTotalPages());
        response.put("currentPage", appointmentPage.getNumber());
        response.put("totalElements", appointmentPage.getTotalElements());

        return response;
    }

    @DeleteMapping("/api/appointments/{id}/cancel")
    public void cancelAppointment(@PathVariable Integer id) {
        appointmentService.cancelAppointment(id);
    }
}
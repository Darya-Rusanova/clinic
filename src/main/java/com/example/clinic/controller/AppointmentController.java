package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Status;
import com.example.clinic.service.AppointmentService;
import com.example.clinic.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/api/client/{id}/appointments")
    public Map<String, Object> getJsonAppointments(@PathVariable Integer id,
                                                   @RequestParam(defaultValue = "all") String filter) {
        List<Appointment> appointmentList = appointmentService.getClientAppointmentsWithDetails(id);

        Map<Integer, Map<String, Object>> appointments = new LinkedHashMap<>();

        for (Appointment app : appointmentList) {
            if (filter.equals("all") || app.getStatus().name().equals(filter)) {
                Map<String, Object> appInfo = new HashMap<>();
                appInfo.put("id", app.getId());
                appInfo.put("date", app.getDate());
                appInfo.put("time", app.getTime().toString().substring(0, 5));
                appInfo.put("status", app.getStatus().name());

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
        response.put("scheduledCount", appointmentList.stream().filter(a -> a.getStatus() == Status.SCHEDULED).count());
        response.put("cancelledCount", appointmentList.stream().filter(a -> a.getStatus() == Status.CANCELLED).count());
        response.put("completedCount", appointmentList.stream().filter(a -> a.getStatus() == Status.COMPLETED).count());

        return response;
    }

    @DeleteMapping("/api/appointments/{id}/cancel")
    public void cancelAppointment(@PathVariable Integer id) {
        appointmentService.cancelAppointment(id);
    }
}
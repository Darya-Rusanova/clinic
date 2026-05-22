package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Status;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.service.BookingService;
import jakarta.servlet.ServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class AppointmentController {
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    BookingService bookingService;
    @GetMapping("/api/client/{id}/appointments")
    public Map<String,Object> getJsonAppointments(@PathVariable Integer id,
                                                  @RequestParam(defaultValue = "all") String filter){
        List<Appointment> appointmentList = appointmentRepository.findAllByClient_UserId(id);

        Map<Integer, Map<String, Object>> appointments = new LinkedHashMap<>();

        for(Appointment app: appointmentList){
            Integer appId = app.getId();
            if (filter.equals("all") || app.getStatus().name().equals(filter)) {
                Map<String, Object> appInfo = new HashMap<>();
                appInfo.put("id", app.getId());
                appInfo.put("date", app.getDate());
                appInfo.put("time", app.getTime().toString().substring(0, 5));
                appInfo.put("status", app.getStatus().name());

                Map<String,Object> service = new HashMap<>();
                service.put("name",app.getService().getName());
                service.put("duration", app.getService().getDuration());
                service.put("price",app.getService().getPrice());
                appInfo.put("service", service);

                Map<String,String> doctor = new HashMap<>();
                doctor.put("name",app.getDoctor().getUser().getName());
                doctor.put("email",app.getDoctor().getUser().getEmail());
                appInfo.put("doctor", doctor);
                appointments.put(appId, appInfo);
            }
        }
        Map<String,Object> response = new HashMap<>();
        long scheduled = appointmentList.stream().filter(app -> app.getStatus() == Status.SCHEDULED).count();
        long cancelled = appointmentList.stream().filter(app -> app.getStatus() == Status.CANCELLED).count();
        long completed = appointmentList.stream().filter(app -> app.getStatus() == Status.COMPLETED).count();

        response.put("appointments", appointments);
        response.put("allCount",appointmentList.size());
        response.put("scheduledCount",scheduled);
        response.put("cancelledCount",cancelled);
        response.put("completedCount",completed);

        return response;
    }
    @DeleteMapping("/api/appointments/{id}/cancel")
    public void cancelAppointment(@PathVariable Integer id) {
        bookingService.cancelledAppointment(id);
    }
}

package com.example.clinic.controller;

import com.example.clinic.dto.DoctorDto;
import com.example.clinic.dto.ServiceDto;
import com.example.clinic.model.Appointment;
import com.example.clinic.model.Doctor;
import com.example.clinic.model.Service;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.ServiceRepository;
import com.example.clinic.service.BookingService;
import com.example.clinic.service.ScheduleService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingApiController {
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ScheduleService scheduleService;
    @Autowired
    BookingService bookingService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping("/services")
    public List<ServiceDto> getFilterServices(@RequestParam(required = false) Integer doctorId,
                                              @RequestParam(required = false) Integer categoryId){
        List<Service> services = serviceRepository.findAll();

        if (doctorId != null){
            services = services.stream()
                    .filter(service -> service.getDoctors()
                            .stream()
                            .anyMatch(doctor -> doctor.getUserId().equals(doctorId)))
                    .toList();
        }
        if(categoryId != null){
            services = services.stream().filter(service -> service.getCategory().getId().equals(categoryId)).toList();
        }
        return services.stream().map(s -> {
            ServiceDto dto = new ServiceDto();
            dto.setId(s.getId());
            dto.setName(s.getName());
            dto.setDescription(s.getDescription());
            dto.setDuration(s.getDuration());
            dto.setPrice(s.getPrice());
            if (s.getCategory() != null) {
                dto.setCategoryId(s.getCategory().getId());
                dto.setCategoryName(s.getCategory().getName());
            }
            return dto;
        }).toList();
    }
    @GetMapping("/services/{id}/doctors")
    public List<DoctorDto> getDoctorsByService(@PathVariable Integer id){
        Service service = serviceRepository.findById(id).orElse(null);
        if (service == null) return List.of();
        return service.getDoctors().stream().map(d -> {
            DoctorDto dto = new DoctorDto();
            dto.setUserId(d.getUserId());
            dto.setName(d.getUser().getName());
            dto.setExperienceYears(d.getExperienceYears());
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/slots")
    public List<String> getFreeSlots(@RequestParam Integer doctorId,
                                                  @RequestParam Integer serviceId,
                                                  @RequestParam LocalDate date){
        Service service = serviceRepository.findById(serviceId).orElse(null);
        if (service == null) return List.of();
        List<LocalTime> slots = scheduleService.getFreeSlots(doctorId,date,service.getDuration());
        return slots.stream().map(slot -> slot.toString().substring(0,5)).toList();
    }

    @GetMapping("/appointment/{id}")
    public Map<String, Object> getAppointment(@PathVariable Integer id){
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if(appointment == null) return Map.of();

        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("doctorId", appointment.getDoctor().getUserId());
        response.put("clientId",appointment.getClient().getUserId());
        response.put("serviceId", appointment.getService().getId());
        response.put("dateTime", appointment.getDateTime().toString());
        return response;
    }

    @PostMapping("/appointment/update")
    public Map<String, Object> updateAppointment(@RequestParam Integer appointmentId,
                                                 @RequestParam Integer doctorId,
                                                 @RequestParam Integer serviceId,
                                                 @RequestParam Integer clientId,
                                                 @RequestParam String dateTime) {
        Map<String, Object> response = new HashMap<>();
        try {
            Appointment appointment = bookingService.updateAppointment(appointmentId, doctorId, serviceId, clientId, LocalDateTime.parse(dateTime));
            response.put("success", true);
            response.put("appointmentId", appointment.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}

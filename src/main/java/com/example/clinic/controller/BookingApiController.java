package com.example.clinic.controller;

import com.example.clinic.dto.DoctorDto;
import com.example.clinic.dto.ServiceDto;
import com.example.clinic.model.Appointment;
import com.example.clinic.model.Doctor;
import com.example.clinic.model.Service;
import com.example.clinic.service.BookingService;
import com.example.clinic.service.ScheduleService;
import com.example.clinic.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingApiController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/services")
    public List<ServiceDto> getFilterServices(@RequestParam(required = false) Integer doctorId,
                                              @RequestParam(required = false) Integer categoryId) {
        List<Service> services = serviceService.getAllServices();

        if (doctorId != null) {
            services = services.stream()
                    .filter(service -> service.getDoctors().stream()
                            .anyMatch(doctor -> doctor.getUserId().equals(doctorId)))
                    .toList();
        }
        if (categoryId != null) {
            services = services.stream()
                    .filter(service -> service.getCategory() != null &&
                            service.getCategory().getId().equals(categoryId))
                    .toList();
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
        }).collect(Collectors.toList());
    }

    @GetMapping("/services/{id}/doctors")
    public List<DoctorDto> getDoctorsByService(@PathVariable Integer id) {
        Service service = serviceService.getServiceById(id);
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
                                     @RequestParam LocalDate date) {
        Service service = serviceService.getServiceById(serviceId);
        if (service == null) return List.of();
        List<LocalTime> slots = scheduleService.getFreeSlots(doctorId, date, service.getDuration());
        return slots.stream().map(slot -> slot.toString().substring(0, 5)).toList();
    }
}
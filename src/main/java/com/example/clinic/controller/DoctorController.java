package com.example.clinic.controller;

import com.example.clinic.dto.SlotDto;
import com.example.clinic.model.*;
import com.example.clinic.repository.BreakRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ScheduleRepository;
import com.example.clinic.repository.ServiceRepository;
import com.example.clinic.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Controller
public class DoctorController {
    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ScheduleService scheduleService;


    @GetMapping("/doctor/{id}")
    public String doctor(@PathVariable Integer id, @RequestParam(defaultValue = "0") Integer weekOffset, Model model){
        Optional<Doctor> doctorOptional = doctorRepository.findById(id);
        if (doctorOptional.isPresent()){
            Doctor doctor = doctorOptional.get();
            List<String> categories = doctor.getCategories();
            Map<String, List<Service>> servicesByCategory = new LinkedHashMap<>();
            for (String category : categories) {
                List<Service> services = serviceRepository.findAllByCategory_Name(category);
                servicesByCategory.put(category, services);
            }
            model.addAttribute("doctor", doctor);
            model.addAttribute("servicesByCategory", servicesByCategory);

            LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
            LocalDate sunday = monday.plusDays(6);
            Map<Integer, List<SlotDto>> slotsByDayOfWeek = scheduleService.weeklySlots(id,weekOffset);
            model.addAttribute("slots",slotsByDayOfWeek);
            model.addAttribute("weekOffset", weekOffset);
            model.addAttribute("weekStart", monday);
            model.addAttribute("weekEnd", sunday);

        }
        return "doctor";
    }
}

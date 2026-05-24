package com.example.clinic.controller;

import com.example.clinic.model.Doctor;
import com.example.clinic.model.Service;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Controller
public class DoctorViewController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/doctor/{id}")
    public String showDoctorDashboard(@PathVariable Integer id, Model model) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(id);
        if (doctorOptional.isPresent()) {
            Doctor doctor = doctorOptional.get();
            List<String> categories = doctor.getCategories();
            Map<String, List<Service>> servicesByCategory = new LinkedHashMap<>();
            List<Service> allServices = doctor.getServices();
            for (String category : categories) {
                List<Service> services = allServices.stream().filter(service -> service.getCategory().getName()==category).toList();
                servicesByCategory.put(category, services);
            }
            model.addAttribute("doctor", doctor);
            model.addAttribute("servicesByCategory", servicesByCategory);
        }
        return "doctor";
    }
}
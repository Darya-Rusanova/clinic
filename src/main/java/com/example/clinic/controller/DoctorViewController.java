package com.example.clinic.controller;

import com.example.clinic.model.Doctor;
import com.example.clinic.model.Service;
import com.example.clinic.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Controller
public class DoctorViewController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/doctor/{id}")
    public String showDoctorDashboard(@PathVariable Integer id, Model model) {
        Doctor doctor = doctorService.getDoctorById(id);
        if (doctor != null) {
            List<String> categories = doctor.getCategories();
            Map<String, List<Service>> servicesByCategory = new LinkedHashMap<>();
            List<Service> allServices = doctor.getServices();
            for (String category : categories) {
                List<Service> services = allServices.stream()
                        .filter(service -> service.getCategory() != null &&
                                category.equals(service.getCategory().getName()))
                        .toList();
                servicesByCategory.put(category, services);
            }
            model.addAttribute("doctor", doctor);
            model.addAttribute("servicesByCategory", servicesByCategory);
        }
        return "doctor";
    }

    @GetMapping("/doctor")
    public String redirectToClient(Authentication authentication) {
        String email = authentication.getName();
        Integer doctorId = doctorService.getDoctorIdByEmail(email);
        if (doctorId != null) {
            return "redirect:/doctor/" + doctorId;
        }
        return "redirect:/";
    }
}
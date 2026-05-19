package com.example.clinic.controller;

import com.example.clinic.model.Doctor;
import com.example.clinic.model.Service;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ServiceRepository;
import com.example.clinic.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    DoctorRepository doctorRepository;

    @GetMapping("/")
    public String home(Model model){
        List<Service> services = serviceRepository.findAll();
        List<Doctor> doctors = doctorRepository.findAll();

        model.addAttribute("services",services);
        model.addAttribute("doctors",doctors);
        return "home";
    }
}

package com.example.clinic.controller;

import com.example.clinic.model.Doctor;
import com.example.clinic.model.Service;
import com.example.clinic.service.DoctorService;
import com.example.clinic.service.ServiceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        // Проверяем авторизацию
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String role = userDetails.getAuthorities().iterator().next().getAuthority();

            if (role.equals("ROLE_DOCTOR")) {
                Integer doctorId = doctorService.getDoctorIdByEmail(userDetails.getUsername());
                if (doctorId != null) {
                    return "redirect:/doctor/" + doctorId;
                }
            } else if (role.equals("ROLE_ADMIN")) {
                return "redirect:/admin";
            }
        }

        // Для неавторизованных и клиентов показываем главную
        List<Service> services = serviceService.getAllServices();
        List<Doctor> doctors = doctorService.getAllDoctors();

        model.addAttribute("services", services);
        model.addAttribute("doctors", doctors);
        return "home";
    }
}
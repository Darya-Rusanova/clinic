package com.example.clinic.controller;

import com.example.clinic.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/doctors")
public class AdminDoctorRegistrationController {

    @Autowired
    private AuthService authService;

    @GetMapping("/add")
    public String addDoctorForm() {
        return "admin/admin-doctor-add";
    }

    @PostMapping("/add")
    public String addDoctor(@RequestParam String lastName,
                            @RequestParam String firstName,
                            @RequestParam(required = false) String patronymic,
                            @RequestParam String phone,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam String confirmPassword,
                            @RequestParam Integer experienceYears,
                            @RequestParam(required = false) String bio,
                            @RequestParam(defaultValue = "true") Boolean gender,
                            @RequestParam(required = false) String imagePath,
                            @RequestParam(required = false) String licensePath,
                            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "admin/admin-doctor-add";
        }

        if (authService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "admin/admin-doctor-add";
        }

        try {
            authService.registerDoctor(lastName, firstName, patronymic, phone, email, password,
                    experienceYears, bio, gender, imagePath, licensePath);
            model.addAttribute("success", "Врач успешно зарегистрирован");
            return "admin/admin-doctor-add";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "admin/admin-doctor-add";
        }
    }
}
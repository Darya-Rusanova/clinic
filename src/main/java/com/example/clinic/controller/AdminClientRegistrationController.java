package com.example.clinic.controller;

import com.example.clinic.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/clients")
public class AdminClientRegistrationController {
    @Autowired
    AuthService authService;
    @GetMapping("/add")
    public String addClientForm() {
        return "admin/admin-client-add";
    }

    @PostMapping("/add")
    public String addClient(@RequestParam String lastName,
                            @RequestParam String firstName,
                            @RequestParam(required = false) String patronymic,
                            @RequestParam String phone,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam String confirmPassword,
                            @RequestParam(required = false) LocalDate birthDate,
                            @RequestParam(defaultValue = "true") Boolean gender,
                            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "admin/admin-client-add";
        }

        if (authService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "admin/admin-client-add";
        }

        try {
            authService.registerClient(lastName, firstName, patronymic, phone, email, password, birthDate, gender);
            model.addAttribute("success", "Клиент успешно добавлен");
            return "admin/admin-clients";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при добавлении: " + e.getMessage());
            return "admin/admin-client-add";
        }
    }
}

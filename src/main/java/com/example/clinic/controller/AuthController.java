package com.example.clinic.controller;

import com.example.clinic.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String lastName,
                           @RequestParam String firstName,
                           @RequestParam(required = false) String patronymic,
                           @RequestParam String phone,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(required = false) LocalDate birthDate,
                           @RequestParam(defaultValue = "true") Boolean gender,
                           Model model) {

        // Проверка пароля
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Пароли не совпадают");
            return "register";
        }

        // Проверка email
        if (authService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "register";
        }

        try {
            authService.registerClient(lastName, firstName, patronymic, phone, email, password, birthDate, gender);
            model.addAttribute("success", "Регистрация успешна! Теперь вы можете войти.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "register";
        }
    }
}
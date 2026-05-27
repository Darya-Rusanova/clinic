package com.example.clinic.controller;

import com.example.clinic.dto.RegistrationDto;
import com.example.clinic.service.AuthService;
import com.example.clinic.validator.RegistrationValidator;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;


@Controller
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private RegistrationValidator registrationValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(registrationValidator);
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        RegistrationDto dto = new RegistrationDto();
        dto.setGender(true);
        model.addAttribute("registrationDto", dto);
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationDto") RegistrationDto dto,
                           BindingResult bindingResult,
                           Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            authService.registerClient(dto);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "register";
        }
    }
}
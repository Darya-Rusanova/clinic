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
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/clients")
@Slf4j
public class AdminClientRegistrationController {
    @Autowired
    AuthService authService;
    @Autowired
    private RegistrationValidator registrationValidator;  // Добавить

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(registrationValidator);  // Добавить
    }

    @GetMapping("/add")
    public String addClientForm(Model model) {
        RegistrationDto dto = new RegistrationDto();
        dto.setGender(true);
        model.addAttribute("registrationDto", dto);
        return "admin/admin-client-add";
    }

    @PostMapping("/add")
    public String addClient(@Valid @ModelAttribute("registrationDto") RegistrationDto dto,
                            BindingResult bindingResult,
                            Model model) {

        log.info("Admin adding client: {}", dto);

        if (bindingResult.hasErrors()) {
            return "admin/admin-client-add";
        }

        try {
            authService.registerClient(dto);
            return "redirect:/admin/clients";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/admin-client-add";
        }
    }
}

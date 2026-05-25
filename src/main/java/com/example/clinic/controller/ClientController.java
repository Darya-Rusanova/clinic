package com.example.clinic.controller;

import com.example.clinic.model.Client;
import com.example.clinic.service.ClientService;
import com.example.clinic.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/client/{id}")
    public String client(@PathVariable Integer id, Model model) {
        Client client = clientService.getClientById(id);
        if (client != null) {
            model.addAttribute("client", client);
            return "client";
        }
        return "home";
    }

    @GetMapping("/client")
    public String redirectToClient(Authentication authentication) {
        String email = authentication.getName();
        Integer clientId = clientService.getClientIdByEmail(email);
        if (clientId != null) {
            return "redirect:/client/" + clientId;
        }
        return "redirect:/";
    }
}
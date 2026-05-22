package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Client;
import com.example.clinic.model.Status;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class ClientController {
    @Autowired
    ClientRepository clientRepository;

    @GetMapping("/client/{id}")
    public String client(@PathVariable Integer id, Model model){
        Optional<Client> clientOptional = clientRepository.findById(id);
        if (clientOptional.isPresent()){
            Client client = clientOptional.get();
            model.addAttribute("client",client);
            return "client";
        }
        else
            return "home";
    }

}

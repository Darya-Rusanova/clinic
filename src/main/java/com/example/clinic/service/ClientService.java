package com.example.clinic.service;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Client;
import com.example.clinic.model.User;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.ClientRepository;
import com.example.clinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(Integer id) {
        return clientRepository.findById(id).orElse(null);
    }
    public Integer getClientIdByEmail(String email) {
        return clientRepository.findByUser_Email(email).map(Client::getUserId).orElse(null);
    }

    @Transactional
    public void updateClient(Integer id, Map<String, Object> data) {
        Client client = getClientById(id);
        if (client == null) throw new RuntimeException("Клиент не найден");

        User user = client.getUser();
        String fullName = data.get("lastName") + " " + data.get("firstName") + " " + data.get("patronymic");
        user.setName(fullName);
        user.setPhone((String) data.get("phone"));
        user.setEmail((String) data.get("email"));
        user.setGender((Boolean) data.get("gender"));
        client.setBirthDate(LocalDate.parse((String) data.get("birthDate")));

        userRepository.save(user);
        clientRepository.save(client);
    }

    @Transactional
    public void deleteClient(Integer id) {
        Client client = getClientById(id);
        if (client != null) {
            List<Appointment> appointments = appointmentRepository.findAllByClient_UserId(id);
            appointmentRepository.deleteAll(appointments);
            clientRepository.delete(client);
        }
    }
}
package com.example.clinic.service;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Client;
import com.example.clinic.model.User;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.ClientRepository;
import com.example.clinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
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

    @Autowired
    private PasswordService passwordService;

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(Integer id) {
        return clientRepository.findById(id).orElse(null);
    }

    public Client getClientByEmail(String email){
        return clientRepository.findByUser_Email(email).orElse(null);
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
    public Map<String, Object> updateClientProfile(Integer id, Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();

        Client client = clientRepository.findById(id).orElseThrow(() -> new RuntimeException("Клиент не найден"));
        User user = client.getUser();

        String lastName = (String) data.get("lastName");
        String firstName = (String) data.get("firstName");
        String patronymic = (String) data.get("patronymic");

        String fullName = lastName + " " + firstName;
        if (patronymic != null && !patronymic.isEmpty()) {
            fullName += " " + patronymic;
        }
        user.setName(fullName);
        user.setPhone((String) data.get("phone"));
        user.setEmail((String) data.get("email"));
        user.setGender((Boolean) data.get("gender"));

        Object birthDateObj = data.get("birthDate");
        if (birthDateObj instanceof String) {
            client.setBirthDate(LocalDate.parse((String) birthDateObj));
        } else if (birthDateObj instanceof LocalDate) {
            client.setBirthDate((LocalDate) birthDateObj);
        }

        userRepository.save(user);
        clientRepository.save(client);

        String oldPassword = (String) data.get("oldPassword");
        String newPassword = (String) data.get("newPassword");
        String confirmPassword = (String) data.get("confirmPassword");

        if (oldPassword != null && !oldPassword.isEmpty() &&
                newPassword != null && !newPassword.isEmpty()) {

            if (!passwordService.matches(oldPassword, user.getPassword())) {
                result.put("success", false);
                result.put("error", "Неверный старый пароль");
                return result;
            }

            if (!newPassword.equals(confirmPassword)) {
                result.put("success", false);
                result.put("error", "Новый пароль и подтверждение не совпадают");
                return result;
            }

            if (newPassword.length() < 3) {
                result.put("success", false);
                result.put("error", "Новый пароль должен содержать минимум 3 символа");
                return result;
            }

            user.setPassword(passwordService.encode(newPassword));
            userRepository.save(user);
        }

        result.put("success", true);
        return result;
    }

    @Transactional
    public void deleteClient(Integer id) {
        Client client = getClientById(id);
        if (client != null) {
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                List<Appointment> appointments = appointmentRepository.findAllByClient_UserId(id);
                appointmentRepository.deleteAll(appointments);
                clientRepository.delete(client);
                userRepository.delete(user);
            }
        }
    }
}
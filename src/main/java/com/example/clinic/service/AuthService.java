package com.example.clinic.service;

import com.example.clinic.dto.RegistrationDto;
import com.example.clinic.model.Client;
import com.example.clinic.model.Doctor;
import com.example.clinic.model.Role;
import com.example.clinic.model.User;
import com.example.clinic.repository.ClientRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void registerClient(RegistrationDto dto) {
        User user = new User();

        String fullName = dto.getLastName() + " " + dto.getFirstName();
        if (dto.getPatronymic() != null && !dto.getPatronymic().isEmpty()) {
            fullName += " " + dto.getPatronymic();
        }
        user.setName(fullName);
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setGender(dto.getGender());
        user.setRole(Role.CLIENT);

        User savedUser = userRepository.save(user);

        Client client = new Client();
        client.setUser(savedUser);
        client.setBirthDate(dto.getBirthDate());

        clientRepository.save(client);
    }

    @Transactional
    public User registerDoctor(String lastName, String firstName, String patronymic,
                               String phone, String email, String password,
                               Integer experienceYears, String bio,
                               Boolean gender, String imagePath, String licensePath) {

        String fullName = lastName + " " + firstName + (patronymic != null ? " " + patronymic : "");

        User user = new User();
        user.setName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.DOCTOR);
        user.setGender(gender);

        User savedUser = userRepository.save(user);

        Doctor doctor = new Doctor();


        doctor.setUser(savedUser);
        doctor.setExperienceYears(experienceYears);
        doctor.setBio(bio);
        doctor.setImagePath(imagePath != null ? imagePath : "");
        doctor.setLicensePath(licensePath != null ? licensePath : "");

        doctorRepository.save(doctor);

        return savedUser;
    }
}
package com.example.clinic.service;

import com.example.clinic.dto.DoctorEditDto;
import com.example.clinic.model.*;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ServiceRepository;
import com.example.clinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private PasswordService passwordService;
    @Autowired
    private NotificationService notificationService;

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
    public Integer getDoctorIdByEmail(String email) {
        return doctorRepository.findByUser_Email(email).map(Doctor::getUserId).orElse(null);
    }

    public Doctor getDoctorById(Integer id) {
        return doctorRepository.findById(id).orElse(null);
    }

    public List<Integer> getDoctorServiceIds(Integer doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        if (doctor == null) return List.of();
        return doctor.getServices().stream()
                .map(Service::getId)
                .toList();
    }

    @Transactional
    public void updateDoctor(Integer id, DoctorEditDto dto) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Врач не найден"));

        List<Integer> currentServiceIds = doctor.getServices().stream()
                .map(Service::getId)
                .toList();

        List<Integer> newServiceIds = dto.getServiceIds() != null ? dto.getServiceIds() : List.of();

        List<Integer> removedServiceIds = currentServiceIds.stream()
                .filter(serviceId -> !newServiceIds.contains(serviceId))
                .toList();

        if (!removedServiceIds.isEmpty()) {
            List<Appointment> activeAppointments = appointmentRepository.findByDoctorAndServiceInAndStatus(
                    id, removedServiceIds, Status.SCHEDULED);

            if (!activeAppointments.isEmpty()) {
                String servicesNames = activeAppointments.stream()
                        .map(a -> a.getService().getName())
                        .distinct()
                        .collect(Collectors.joining(", "));
                throw new RuntimeException("Нельзя удалить услуги, на которые есть активные записи: " + servicesNames);
            }
        }

        User user = doctor.getUser();

        String fullName = dto.getLastName() + " " + dto.getFirstName();
        if (dto.getPatronymic() != null && !dto.getPatronymic().isEmpty()) {
            fullName += " " + dto.getPatronymic();
        }
        user.setName(fullName);
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender());

        doctor.setExperienceYears(dto.getExperienceYear());

        if (dto.getImagePath() != null && !dto.getImagePath().isEmpty()) {
            doctor.setImagePath(dto.getImagePath());
        }
        if (dto.getLicensePath() != null && !dto.getLicensePath().isEmpty()) {
            doctor.setLicensePath(dto.getLicensePath());
        }

        userRepository.save(user);
        doctorRepository.save(doctor);

        doctor.getServices().clear();
        if (dto.getServiceIds() != null) {
            for (Integer serviceId : dto.getServiceIds()) {
                Service service = serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new RuntimeException("Услуга не найдена: " + serviceId));
                doctor.getServices().add(service);
            }
        }
        doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Integer id) {
        List<Appointment> appointments = appointmentRepository.findAllByDoctor_UserId(id);
        for (Appointment appointment : appointments) {
            appointment.setStatus(Status.CANCELLED);
            appointmentRepository.save(appointment);

            notificationService.createNotification(
                    appointment.getClient().getUserId(),
                    "Запись отменена",
                    "Ваша запись на " + appointment.getService().getName() +
                            " к врачу " + appointment.getDoctor().getUser().getName() +
                            " на " + appointment.getDateTime().toLocalDate() + " была отменена.",
                    NotificationType.APPOINTMENT_CANCELLED
            );
        }
        doctorRepository.deleteById(id);
    }
    @Transactional
    public Map<String, Object> updateDoctorProfile(Integer id, Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Врач не найден"));

        User user = doctor.getUser();

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

        doctor.setExperienceYears((Integer) data.get("experience"));

        userRepository.save(user);
        doctorRepository.save(doctor);

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
}
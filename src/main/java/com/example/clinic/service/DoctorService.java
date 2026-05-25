package com.example.clinic.service;

import com.example.clinic.dto.DoctorEditDto;
import com.example.clinic.model.*;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ServiceRepository;
import com.example.clinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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

    // ===== Чтение =====
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
    public Integer getDoctorIdByEmail(String email) {
        return doctorRepository.findByUser_Email(email).map(Doctor::getUserId).orElse(null);
    }

    public Doctor getDoctorById(Integer id) {
        return doctorRepository.findById(id).orElse(null);
    }
    /*
    public Integer getDoctorIdByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getRole().name().equals("DOCTOR")) {
            return doctorRepository.findByUserId(user.get().getId()).map(Doctor::getUserId).orElse(null);
        }
        return null;
    }

     */

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

        // Проверка: нельзя удалить услугу, если есть активные записи на неё
        List<Integer> currentServiceIds = doctor.getServices().stream()
                .map(Service::getId)
                .toList();

        List<Integer> newServiceIds = dto.getServiceIds() != null ? dto.getServiceIds() : List.of();

        // Какие услуги пытаются удалить
        List<Integer> removedServiceIds = currentServiceIds.stream()
                .filter(serviceId -> !newServiceIds.contains(serviceId))
                .toList();

        if (!removedServiceIds.isEmpty()) {
            // Проверяем, есть ли активные записи на удаляемые услуги
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

        // Обновляем ФИО
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

        // Обновляем список услуг врача
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
        // Отменяем все записи к этому врачу
        List<Appointment> appointments = appointmentRepository.findAllByDoctor_UserId(id);
        for (Appointment appointment : appointments) {
            appointment.setStatus(Status.CANCELLED);
            appointmentRepository.save(appointment);
        }
        // Удаляем врача
        doctorRepository.deleteById(id);
    }
}
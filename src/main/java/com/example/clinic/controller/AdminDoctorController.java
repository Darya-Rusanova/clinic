package com.example.clinic.controller;

import com.example.clinic.model.*;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ServiceRepository;
import com.example.clinic.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.print.Doc;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminDoctorController {
    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    ServiceRepository serviceRepository;

    @GetMapping("/doctors")
    public String doctorsPage(){
        return "admin/admin-doctors";
    }

    @GetMapping("/api/doctors")
    @ResponseBody
    public List<Map<String, Object>> getDoctors(@RequestParam(required = false) String search){
        List<Doctor> doctors = doctorRepository.findAll();
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            String searchDigits = searchLower.replaceAll("[^0-9]", "");

            doctors = doctors.stream().filter(doctor -> {
                if (doctor.getUser().getName().toLowerCase().contains(searchLower)) {
                    return true;
                }
                if (!searchDigits.isEmpty()) {
                    String phoneDigits = doctor.getUser().getPhone().replaceAll("[^0-9]", "");
                    if (searchDigits.charAt(0) == '8') {
                        phoneDigits = phoneDigits.replaceFirst("7", "8");
                    }
                    if (phoneDigits.contains(searchDigits)) {
                        return true;
                    }
                }
                return false;
            }).toList();
        }
        List<Map<String,Object>> result = new ArrayList<>();
        for(Doctor doctor:doctors){
            Map<String,Object> info = new HashMap<>();
            info.put("id",doctor.getUser().getId());
            info.put("name", doctor.getUser().getName());
            info.put("firstName",doctor.getUser().getFirstName());
            info.put("lastName",doctor.getUser().getLastName());
            info.put("patronymic",doctor.getUser().getPatronymic());
            info.put("experienceYear",doctor.getExperienceYears());
            info.put("phone",doctor.getUser().getPhone());
            info.put("email",doctor.getUser().getEmail());
            info.put("imagePath",doctor.getImagePath());
            info.put("licensePath",doctor.getLicensePath());
            info.put("services",doctor.getServices().size());
            info.put("gender", doctor.getUser().isGender());
            result.add(info);
        }
        return result;
    }
    @GetMapping("/api/doctors/{id}/services")
    @ResponseBody
    public List<Integer> getDoctorServices(@PathVariable Integer id) {
        Doctor doctor = doctorRepository.findById(id).orElse(null);
        if (doctor == null) return List.of();
        return doctor.getServices().stream()
                .map(Service::getId)
                .toList();
    }

    @PutMapping("/api/doctors/{id}/edit")
    @ResponseBody
    public Map<String, Object> editDoctor(@RequestBody Map<String, Object> data,
                                          @PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        Doctor doctor = doctorRepository.findById(id).orElse(null);
        if (doctor != null) {
            List<Integer> currentServiceIds = doctor.getServices().stream()
                    .map(Service::getId)
                    .toList();

            List<Integer> newServiceIds = (List<Integer>) data.get("serviceIds");
            if (newServiceIds == null) newServiceIds = List.of();

            List<Integer> finalNewServiceIds = newServiceIds;
            List<Integer> removedServiceIds = currentServiceIds.stream().filter(id2 -> !finalNewServiceIds.contains(id2)).toList();

            if (!removedServiceIds.isEmpty()) {
                List<Appointment> futureAppointments = appointmentRepository.findByDoctorAndServiceInAndStatus(id, removedServiceIds, Status.SCHEDULED);

                if (!futureAppointments.isEmpty()) {
                    response.put("success", false);
                    response.put("error", "Нельзя удалить услуги, на которые есть активные записи: " +
                            futureAppointments.stream()
                                    .map(a -> a.getService().getName())
                                    .distinct()
                                    .collect(Collectors.joining(", ")));
                    return response;
                }
            }

            User user = doctor.getUser();
            String fullName = data.get("lastName") + " " + data.get("firstName") +
                    (data.get("patronymic") != null && !((String) data.get("patronymic")).isEmpty() ? " " + data.get("patronymic") : "");
            user.setName(fullName);
            user.setPhone((String) data.get("phone"));
            user.setEmail((String) data.get("email"));
            user.setGender((Boolean) data.get("gender"));

            doctor.setImagePath((String) data.get("imagePath"));
            doctor.setLicensePath((String) data.get("licensePath"));
            doctor.setExperienceYears((Integer) data.get("experienceYear"));

            if (!newServiceIds.isEmpty()) {
                List<Service> services = serviceRepository.findAllById(newServiceIds);
                doctor.setServices(services);
            } else {
                doctor.setServices(new ArrayList<>());
            }

            userRepository.save(user);
            doctorRepository.save(doctor);
            response.put("success", true);
        } else {
            response.put("success", false);
            response.put("error", "Врач не найден");
        }
        return response;
    }

    @DeleteMapping("/api/doctors/{id}/delete")
    @ResponseBody
    @Transactional
    public Map<String, Object> deleteDoctor(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Appointment> appointments = appointmentRepository.findAllByDoctor_UserId(id);
            for (Appointment appointment : appointments) {
                appointment.setStatus(Status.CANCELLED);
                appointmentRepository.save(appointment);
            }

            doctorRepository.deleteById(id);

            response.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}

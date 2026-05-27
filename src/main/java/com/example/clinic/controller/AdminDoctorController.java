package com.example.clinic.controller;

import com.example.clinic.dto.DoctorEditDto;
import com.example.clinic.model.Doctor;
import com.example.clinic.service.DoctorService;
import com.example.clinic.service.ScheduleService;
import com.example.clinic.validator.ScheduleValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminDoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleValidator scheduleValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        if (binder.getTarget() != null && Map.class.isAssignableFrom(binder.getTarget().getClass())) {
            binder.addValidators(scheduleValidator);
        }
    }

    @GetMapping("/doctors")
    public String doctorsPage() {
        return "admin/admin-doctors";
    }

    @GetMapping("/doctors/{id}/schedule")
    public String schedulePage(@PathVariable Integer id, Model model) {
        model.addAttribute("doctorId", id);
        return "admin/admin-doctor-schedule";
    }

    @GetMapping("/api/doctors")
    @ResponseBody
    public List<Map<String, Object>> getDoctors(@RequestParam(required = false) String search) {
        List<Doctor> doctors = doctorService.getAllDoctors();

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            String searchDigits = searchLower.replaceAll("[^0-9]", "");

            doctors = doctors.stream().filter(doctor -> {
                if (doctor.getUser().getName().toLowerCase().contains(searchLower)) return true;
                if (!searchDigits.isEmpty()) {
                    String phoneDigits = doctor.getUser().getPhone().replaceAll("[^0-9]", "");
                    if (searchDigits.charAt(0) == '8') {
                        phoneDigits = phoneDigits.replaceFirst("7", "8");
                    }
                    if (phoneDigits.contains(searchDigits)) return true;
                }
                return false;
            }).toList();
        }

        return doctors.stream().map(doctor -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", doctor.getUserId());
            info.put("name", doctor.getUser().getName());
            info.put("firstName", doctor.getUser().getFirstName());
            info.put("lastName", doctor.getUser().getLastName());
            info.put("patronymic", doctor.getUser().getPatronymic());
            info.put("experienceYear", doctor.getExperienceYears());
            info.put("phone", doctor.getUser().getPhone());
            info.put("email", doctor.getUser().getEmail());
            info.put("imagePath", doctor.getImagePath());
            info.put("licensePath", doctor.getLicensePath());
            info.put("services", doctor.getServices().size());
            info.put("gender", doctor.getUser().isGender());
            return info;
        }).collect(Collectors.toList());
    }

    @GetMapping("/api/doctors/{id}/services")
    @ResponseBody
    public List<Integer> getDoctorServices(@PathVariable Integer id) {
        return doctorService.getDoctorServiceIds(id);
    }

    @GetMapping("/api/doctors/{id}/schedule")
    @ResponseBody
    public Map<String, Object> getDoctorSchedule(@PathVariable Integer id) {
        return scheduleService.getDoctorSchedule(id);
    }

    @GetMapping("/api/doctors/{id}/appointments-count")
    @ResponseBody
    public Map<Integer, Integer> getDoctorAppointmentsCountByDay(@PathVariable Integer id) {
        return scheduleService.getAppointmentsCountByDay(id);
    }

    @PostMapping("/api/doctors/{id}/schedule")
    @ResponseBody
    public Map<String, Object> saveDoctorSchedule(@PathVariable Integer id,
                                                  @RequestBody Map<String, Object> scheduleData) {
        Map<String, Object> response = new HashMap<>();

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(scheduleData, "scheduleData");
        scheduleValidator.validate(scheduleData, bindingResult);

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return response;
        }

        try {
            scheduleService.saveDoctorSchedule(id, scheduleData);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PutMapping("/api/doctors/{id}/edit")
    @ResponseBody
    public Map<String, Object> editDoctor(@Valid @RequestBody DoctorEditDto doctorEditDto,
                                          @PathVariable Integer id,
                                          BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return response;
        }

        try {
            doctorService.updateDoctor(id, doctorEditDto);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/api/doctors/{id}/delete")
    @ResponseBody
    public Map<String, Object> deleteDoctor(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            doctorService.deleteDoctor(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
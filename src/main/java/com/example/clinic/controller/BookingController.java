package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Doctor;
import com.example.clinic.model.Service;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.CategoryRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ServiceRepository;
import com.example.clinic.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/booking")
@SessionAttributes({"selectedServiceId", "selectedDoctorId", "selectedDate", "selectedTime", "clientId", "editAppointmentId"})
public class BookingController {

    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    BookingService bookingService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @ModelAttribute("selectedServiceId")
    public Integer selectedServiceId() { return null; }

    @ModelAttribute("selectedDoctorId")
    public Integer selectedDoctorId() { return null; }

    @ModelAttribute("selectedDate")
    public String selectedDate() { return null; }

    @ModelAttribute("selectedTime")
    public String selectedTime() { return null; }

    @ModelAttribute("clientId")
    public Integer clientId() { return null; }

    @ModelAttribute("editAppointmentId")
    public Integer editAppointmentId() { return null; }

    @GetMapping
    public String step1(@RequestParam(required = false) Integer serviceId,
                        @RequestParam(required = false) Integer doctorId,
                        @RequestParam(required = false) Integer clientId,
                        Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("doctors", doctorRepository.findAll());

        if (serviceId != null) model.addAttribute("preSelectedServiceId", serviceId);
        if (doctorId != null) model.addAttribute("preSelectedDoctorId", doctorId);
        if (clientId != null) model.addAttribute("preSelectedClientId", clientId);

        return "booking/step1";
    }

    @PostMapping("/select")
    public String selectServiceAndDoctor(@RequestParam Integer serviceId,
                                         @RequestParam Integer doctorId,
                                         @RequestParam(required = false) Integer clientId,
                                         Model model) {
        model.addAttribute("selectedServiceId", serviceId);
        model.addAttribute("selectedDoctorId", doctorId);
        if (clientId != null) {
            model.addAttribute("clientId", clientId);
        } else {
            Integer existingClientId = (Integer) model.getAttribute("clientId");
            if (existingClientId != null) {
                model.addAttribute("clientId", existingClientId);
            }
        }
        return "redirect:/booking/step2";
    }

    @GetMapping("/step2")
    public String step2(Model model) {
        Integer serviceId = (Integer) model.getAttribute("selectedServiceId");
        Integer doctorId = (Integer) model.getAttribute("selectedDoctorId");

        if (serviceId == null || doctorId == null) {
            return "redirect:/booking";
        }

        Service service = serviceRepository.findById(serviceId).orElse(null);
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);

        model.addAttribute("service", service);
        model.addAttribute("doctor", doctor);
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("doctorId", doctorId);
        return "booking/step2";
    }

    @PostMapping("/step3")
    @ResponseBody
    public Map<String, Object> step3Json(@RequestBody Map<String, Object> request,
                                         Model model) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer serviceId = (Integer) request.get("serviceId");
            Integer doctorId = (Integer) request.get("doctorId");
            Integer clientId = (Integer) request.get("clientId");
            String date = (String) request.get("date");
            String time = (String) request.get("time");
            Integer appointmentId = (Integer) request.get("appointmentId");

            model.addAttribute("selectedServiceId", serviceId);
            model.addAttribute("selectedDoctorId", doctorId);
            model.addAttribute("selectedDate", date);
            model.addAttribute("selectedTime", time);
            model.addAttribute("clientId", clientId);

            if (appointmentId != null && appointmentId > 0) {
                model.addAttribute("editAppointmentId", appointmentId);
            }

            response.put("success", true);
            response.put("redirectUrl", "/booking/step3");

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @GetMapping("/step3")
    public String showStep3(Model model) {
        Integer serviceId = (Integer) model.getAttribute("selectedServiceId");
        Integer doctorId = (Integer) model.getAttribute("selectedDoctorId");
        String date = (String) model.getAttribute("selectedDate");
        String time = (String) model.getAttribute("selectedTime");
        Integer clientId = (Integer) model.getAttribute("clientId");

        if (serviceId == null || doctorId == null || date == null || time == null) {
            return "redirect:/booking";
        }

        Service service = serviceRepository.findById(serviceId).orElse(null);
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        LocalDateTime dateTime = LocalDateTime.parse(date + "T" + time);

        model.addAttribute("service", service);
        model.addAttribute("doctor", doctor);
        model.addAttribute("dateTime", dateTime);
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("doctorId", doctorId);
        model.addAttribute("clientId", clientId);
        model.addAttribute("editMode", model.getAttribute("editAppointmentId") != null);

        return "booking/step3";
    }

    @PostMapping("/confirm")
    @ResponseBody
    public Map<String, Object> confirmJson(Model model, SessionStatus sessionStatus) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer editId = (Integer) model.getAttribute("editAppointmentId");
            Integer serviceId = (Integer) model.getAttribute("selectedServiceId");
            Integer doctorId = (Integer) model.getAttribute("selectedDoctorId");
            Integer clientId = (Integer) model.getAttribute("clientId");
            String date = (String) model.getAttribute("selectedDate");
            String time = (String) model.getAttribute("selectedTime");
            LocalDateTime dateTime = LocalDateTime.parse(date + "T" + time);

            Appointment appointment;
            if (editId != null) {
                appointment = bookingService.updateAppointment(editId, doctorId, serviceId, clientId, dateTime);
            } else {
                appointment = bookingService.createAppointment(doctorId, serviceId, clientId, dateTime);
            }

            sessionStatus.setComplete();
            response.put("success", true);
            response.put("redirectUrl", "/booking/success?id=" + appointment.getId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @GetMapping("/success")
    public String successPage(@RequestParam(required = false) Integer id, Model model) {
        if (id != null) {
            Appointment appointment = appointmentRepository.findById(id).orElse(null);
            model.addAttribute("appointment", appointment);
        }
        return "booking/success";
    }

    @GetMapping("/edit/{id}")
    public String editAppointment(@PathVariable Integer id, Model model) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment == null) return "redirect:/";

        model.addAttribute("selectedServiceId", appointment.getService().getId());
        model.addAttribute("selectedDoctorId", appointment.getDoctor().getUserId());
        model.addAttribute("selectedDate", appointment.getDateTime().toLocalDate().toString());
        model.addAttribute("selectedTime", appointment.getDateTime().toLocalTime().toString());
        model.addAttribute("clientId", appointment.getClient().getUserId());
        model.addAttribute("editAppointmentId", appointment.getId());
        System.out.println("clientId из записи: " + appointment.getClient().getUserId());


        return "redirect:/booking/step2";
    }
}
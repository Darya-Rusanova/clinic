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
                        Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("doctors", doctorRepository.findAll());

        if (serviceId != null) model.addAttribute("preSelectedServiceId", serviceId);
        if (doctorId != null) model.addAttribute("preSelectedDoctorId", doctorId);

        return "booking/step1";
    }

    @PostMapping("/select")
    public String selectServiceAndDoctor(@RequestParam Integer serviceId,
                                         @RequestParam Integer doctorId,
                                         Model model) {
        model.addAttribute("selectedServiceId", serviceId);
        model.addAttribute("selectedDoctorId", doctorId);
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
    public String step3Post(@RequestParam(required = false) Integer appointmentId,
                            @RequestParam Integer serviceId,
                            @RequestParam Integer doctorId,
                            @RequestParam String date,
                            @RequestParam String time,
                            @RequestParam Integer clientId,
                            Model model) {
        model.addAttribute("selectedServiceId", serviceId);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedTime", time);
        model.addAttribute("clientId", clientId);

        if (appointmentId != null) {
            model.addAttribute("editAppointmentId", appointmentId);
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
        model.addAttribute("editMode", appointmentId != null);
        return "booking/step3";
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam Integer serviceId,
                          @RequestParam Integer doctorId,
                          @RequestParam Integer clientId,
                          @RequestParam String dateTime,
                          Model model,
                          SessionStatus sessionStatus) {
        try {
            Integer editId = (Integer) model.getAttribute("editAppointmentId");
            Appointment appointment;

            if (editId != null) {
                appointment = bookingService.updateAppointment(editId, doctorId, serviceId, clientId, LocalDateTime.parse(dateTime));
            } else {
                appointment = bookingService.createAppointment(doctorId, serviceId, clientId, LocalDateTime.parse(dateTime));
            }

            model.addAttribute("appointment", appointment);
            sessionStatus.setComplete();
            return "booking/success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "booking/error";
        }
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

        return "redirect:/booking/step2";
    }
}
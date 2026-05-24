package com.example.clinic.controller;

import com.example.clinic.dto.ServiceDto;
import com.example.clinic.model.*;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.CategoryRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ServiceRepository;
import com.example.clinic.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminServiceController {
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    ImageUploadService imageUploadService;
    @Autowired
    AppointmentRepository appointmentRepository;
    @GetMapping("/api/services")
    @ResponseBody
    public List<Map<String,Object>> getFilterServices(@RequestParam(required = false) Integer categoryId){
        List<Service> services = serviceRepository.findAll();
        if (categoryId != null)
            services = services.stream().filter(service -> service.getCategory().getId().equals(categoryId)).toList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Service s : services) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("doctors", s.getDoctors().size());
            map.put("duration", s.getDuration());
            map.put("price", s.getPrice());

            if (s.getCategory() != null) {
                map.put("categoryId", s.getCategory().getId());
                map.put("categoryName", s.getCategory().getName());
            }
            result.add(map);
        }
        return result;
    }
    @GetMapping("/services")
    public String getServicesPage(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/admin-services";
    }
    @GetMapping("/services/add")
    public String addServiceForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("doctors", doctorRepository.findAll());
        return "admin/admin-service-add";
    }
    @GetMapping("/services/delete")
    public String deleteService(@RequestParam Integer id){
        Service service = serviceRepository.findById(id).orElse(null);
        if (service != null){
            List<Appointment> appointments = appointmentRepository.findByService(service);
            for (Appointment appointment : appointments) {
                appointment.setStatus(Status.CANCELLED);
                appointmentRepository.save(appointment);
            }
            serviceRepository.delete(service);
        }
        return "redirect:/admin/services";
    }

    @GetMapping("/services/edit/{id}")
    public String getEditService(@PathVariable Integer id, Model model){
        Service service = serviceRepository.findById(id).orElse(null);
        if (service == null) {
            return "redirect:/admin/services";
        }

        List<Integer> selectedDoctorIds = service.getDoctors().stream().map(Doctor::getUserId).toList();

        model.addAttribute("service", service);
        model.addAttribute("selectedDoctorIds", selectedDoctorIds);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("doctors", doctorRepository.findAll());
        return "admin/admin-service-add";
    }

    @PostMapping("/services/edit/{id}")
    public String postEditService(@RequestParam String name,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam Integer duration,
                             @RequestParam Integer price,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String imagePath,
                             @RequestParam String doctorIds,
                             @PathVariable Integer id){
        Service service = serviceRepository.findById(id).orElse(null);
        if (service == null) return "redirect:/admin/services";

        service.setName(name);
        service.setDuration(duration);
        service.setPrice(price);
        service.setDescription(description);
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            service.setPath(imagePath);
        }

        if (categoryId != null && categoryId > 0) {
            ServiceCategory category = categoryRepository.findById(categoryId).orElse(null);
            service.setCategory(category);
        }

        serviceRepository.save(service);
        List<Doctor> currentDoctors = new ArrayList<>(service.getDoctors());
        for (Doctor doctor : currentDoctors) {
            doctor.getServices().remove(service);
            doctorRepository.save(doctor);
        }
        service.getDoctors().clear();

        if (doctorIds != null && !doctorIds.trim().isEmpty()) {
            List<Integer> ids = Arrays.stream(doctorIds.split(","))
                    .map(Integer::parseInt)
                    .toList();
            List<Doctor> doctors = doctorRepository.findAllById(ids);

            for (Doctor doctor : doctors) {
                doctor.getServices().add(service);
                doctorRepository.save(doctor);
            }
        }
        return "redirect:/admin/services";
    }

    @PostMapping("/services/add")
    public String addService(@RequestParam String name,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam Integer duration,
                             @RequestParam Integer price,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String imagePath,
                             @RequestParam String doctorIds){

        Service service = new Service();
        service.setName(name);
        service.setDuration(duration);
        service.setPrice(price);
        service.setDescription(description);
        service.setPath(imagePath);

        if (categoryId != null && categoryId > 0) {
            ServiceCategory category = categoryRepository.findById(categoryId).orElse(null);
            service.setCategory(category);
        }

        Service savedService = serviceRepository.save(service);

        if (doctorIds != null && !doctorIds.trim().isEmpty()) {
            List<Integer> ids = Arrays.stream(doctorIds.split(","))
                    .map(Integer::parseInt)
                    .toList();
            List<Doctor> doctors = doctorRepository.findAllById(ids);

            for (Doctor doctor : doctors) {
                doctor.getServices().add(savedService);
                doctorRepository.save(doctor);
            }
        }
        return "redirect:/admin/services";
    }

    @PostMapping("/api/upload-image")
    @ResponseBody
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            String imageUrl = imageUploadService.uploadImage(file);
            if (imageUrl != null) {
                response.put("success", true);
                response.put("url", imageUrl);
            } else {
                response.put("success", false);
                response.put("error", "Ошибка загрузки");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}

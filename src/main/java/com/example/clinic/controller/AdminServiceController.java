package com.example.clinic.controller;

import com.example.clinic.model.Service;
import com.example.clinic.service.CategoryService;
import com.example.clinic.service.DoctorService;
import com.example.clinic.service.ServiceService;
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
    private ServiceService serviceService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ImageUploadService imageUploadService;

    @GetMapping("/services")
    public String getServicesPage(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/admin-services";
    }

    @GetMapping("/services/add")
    public String addServiceForm(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "admin/admin-service-add";
    }

    @GetMapping("/services/edit/{id}")
    public String editServiceForm(@PathVariable Integer id, Model model) {
        Service service = serviceService.getServiceById(id);
        if (service == null) return "redirect:/admin/services";

        List<Integer> selectedDoctorIds = service.getDoctors().stream()
                .map(d -> d.getUserId())
                .toList();

        model.addAttribute("service", service);
        model.addAttribute("selectedDoctorIds", selectedDoctorIds);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "admin/admin-service-add";
    }

    @PostMapping("/services/add")
    public String addService(@RequestParam String name,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam Integer duration,
                             @RequestParam Integer price,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String imagePath,
                             @RequestParam(required = false) String doctorIds) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("categoryId", categoryId);
        data.put("duration", duration);
        data.put("price", price);
        data.put("description", description);
        data.put("imagePath", imagePath);
        data.put("doctorIds", doctorIds);

        serviceService.createService(data);
        return "redirect:/admin/services";
    }

    @PostMapping("/services/edit/{id}")
    public String updateService(@PathVariable Integer id,
                                @RequestParam String name,
                                @RequestParam(required = false) Integer categoryId,
                                @RequestParam Integer duration,
                                @RequestParam Integer price,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String imagePath,
                                @RequestParam(required = false) String doctorIds) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("categoryId", categoryId);
        data.put("duration", duration);
        data.put("price", price);
        data.put("description", description);
        data.put("imagePath", imagePath);
        data.put("doctorIds", doctorIds);

        serviceService.updateService(id, data);
        return "redirect:/admin/services";
    }

    @DeleteMapping("/services/delete")
    @ResponseBody
    public Map<String, Object> deleteService(@RequestParam Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            serviceService.deleteService(id);
            response.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @GetMapping("/api/services")
    @ResponseBody
    public List<Map<String, Object>> getFilterServices(@RequestParam(required = false) Integer categoryId) {
        List<Service> services = serviceService.getServicesByCategory(categoryId);
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

    @PostMapping("/api/upload-image")
    @ResponseBody
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            String imageUrl = imageUploadService.uploadImage(file);
            response.put("success", imageUrl != null);
            response.put("url", imageUrl);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
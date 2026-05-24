package com.example.clinic.controller;

import com.example.clinic.model.Service;
import com.example.clinic.model.ServiceCategory;
import com.example.clinic.repository.CategoryRepository;
import com.example.clinic.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminCategoryController {
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    CategoryRepository categoryRepository;

    @GetMapping
    public String getAdminPage(Model model){
        return "admin/admin";
    }

    @GetMapping("/categories")
    public String getCategories(Model model){
        List<ServiceCategory> categories = categoryRepository.findAll();
        Map<Integer, Integer> servicesByCategory = new LinkedHashMap<>();
        for(ServiceCategory category: categories){
            List<Service> serviceCategories = serviceRepository.findAllByCategory_Id(category.getId());
            servicesByCategory.put(category.getId(),serviceCategories.size());
        }
        model.addAttribute("categories", categories);
        model.addAttribute("servicesCount",servicesByCategory);
        return "admin/admin-categories";
    }
    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String name){
        ServiceCategory category = new ServiceCategory();
        category.setName(name);
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }
    @PostMapping("/categories/edit")
    public String editCategory(@RequestParam String name, @RequestParam Integer id){
        ServiceCategory category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            category.setName(name);
            categoryRepository.save(category);
        }
        return "redirect:/admin/categories";
    }
    @GetMapping("/categories/delete")
    public String deleteCategory(@RequestParam Integer id){
        ServiceCategory category = categoryRepository.findById(id).orElse(null);
        if (category != null){
            List<Service> services = serviceRepository.findAllByCategory_Id(id);
            for(Service service: services){
                service.setCategory(null);
                serviceRepository.save(service);
            }
            categoryRepository.delete(category);
        }
        return "redirect:/admin/categories";
    }
}

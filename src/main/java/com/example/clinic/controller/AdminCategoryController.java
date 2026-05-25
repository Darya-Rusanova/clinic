package com.example.clinic.controller;

import com.example.clinic.model.ServiceCategory;
import com.example.clinic.service.CategoryService;
import com.example.clinic.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ServiceService serviceService;

    @GetMapping("/categories")
    public String getCategories(Model model) {
        List<ServiceCategory> categories = categoryService.getAllCategories();
        Map<Integer, Integer> servicesByCategory = new LinkedHashMap<>();
        for (ServiceCategory category : categories) {
            int count = serviceService.countByCategory(category.getId());
            servicesByCategory.put(category.getId(), count);
        }
        model.addAttribute("categories", categories);
        model.addAttribute("servicesCount", servicesByCategory);
        return "admin/admin-categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String name) {
        categoryService.addCategory(name);
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/edit")
    public String editCategory(@RequestParam String name, @RequestParam Integer id) {
        categoryService.updateCategory(id, name);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete")
    public String deleteCategory(@RequestParam Integer id) {
        serviceService.setNullCategoryByCategoryId(id);
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }
}
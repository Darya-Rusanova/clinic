package com.example.clinic.service;

import com.example.clinic.model.Service;
import com.example.clinic.model.ServiceCategory;
import com.example.clinic.repository.CategoryRepository;
import com.example.clinic.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    public List<ServiceCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public ServiceCategory getCategoryById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Transactional
    public void addCategory(String name) {
        ServiceCategory category = new ServiceCategory();
        category.setName(name);
        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(Integer id, String name) {
        ServiceCategory category = getCategoryById(id);
        if (category != null) {
            category.setName(name);
            categoryRepository.save(category);
        }
    }

    @Transactional
    public void deleteCategory(Integer id) {
        ServiceCategory category = getCategoryById(id);
        if (category != null) {
            List<Service> services = serviceRepository.findAllByCategory_Id(id);
            for (Service service : services) {
                service.setCategory(null);
                serviceRepository.save(service);
            }
            categoryRepository.delete(category);
        }
    }
}
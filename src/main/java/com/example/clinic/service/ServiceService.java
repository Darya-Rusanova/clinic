package com.example.clinic.service;

import com.example.clinic.model.*;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ServiceRepository;
import com.example.clinic.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public List<Service> getServicesByCategory(Integer categoryId) {
        if (categoryId == null) return getAllServices();
        return serviceRepository.findAllByCategory_Id(categoryId);
    }

    public Service getServiceById(Integer id) {
        return serviceRepository.findById(id).orElse(null);
    }

    @Transactional
    public Service createService(Map<String, Object> data) {
        Service service = new Service();
        service.setName((String) data.get("name"));
        service.setDuration((Integer) data.get("duration"));
        service.setPrice((Integer) data.get("price"));
        service.setDescription((String) data.get("description"));
        service.setPath((String) data.get("imagePath"));

        Integer categoryId = (Integer) data.get("categoryId");
        if (categoryId != null && categoryId > 0) {
            ServiceCategory category = categoryRepository.findById(categoryId).orElse(null);
            service.setCategory(category);
        }

        Service savedService = serviceRepository.save(service);

        String doctorIdsStr = (String) data.get("doctorIds");
        if (doctorIdsStr != null && !doctorIdsStr.trim().isEmpty()) {
            List<Integer> ids = Arrays.stream(doctorIdsStr.split(","))
                    .map(Integer::parseInt)
                    .toList();
            List<Doctor> doctors = doctorRepository.findAllById(ids);
            for (Doctor doctor : doctors) {
                doctor.getServices().add(savedService);
                doctorRepository.save(doctor);
            }
        }

        return savedService;
    }

    @Transactional
    public void updateService(Integer id, Map<String, Object> data) {
        Service service = getServiceById(id);
        if (service == null) throw new RuntimeException("Услуга не найдена");

        service.setName((String) data.get("name"));
        service.setDuration((Integer) data.get("duration"));
        service.setPrice((Integer) data.get("price"));
        service.setDescription((String) data.get("description"));
        if (data.get("imagePath") != null && !((String) data.get("imagePath")).trim().isEmpty()) {
            service.setPath((String) data.get("imagePath"));
        }

        Integer categoryId = (Integer) data.get("categoryId");
        if (categoryId != null && categoryId > 0) {
            ServiceCategory category = categoryRepository.findById(categoryId).orElse(null);
            service.setCategory(category);
        } else {
            service.setCategory(null);
        }

        serviceRepository.save(service);

        List<Doctor> currentDoctors = new ArrayList<>(service.getDoctors());
        for (Doctor doctor : currentDoctors) {
            doctor.getServices().remove(service);
            doctorRepository.save(doctor);
        }
        service.getDoctors().clear();

        String doctorIdsStr = (String) data.get("doctorIds");
        if (doctorIdsStr != null && !doctorIdsStr.trim().isEmpty()) {
            List<Integer> ids = Arrays.stream(doctorIdsStr.split(","))
                    .map(Integer::parseInt)
                    .toList();
            List<Doctor> doctors = doctorRepository.findAllById(ids);
            for (Doctor doctor : doctors) {
                doctor.getServices().add(service);
                doctorRepository.save(doctor);
            }
        }
    }
    @Transactional
    public void deleteService(Integer id) {
        List<Appointment> appointments = appointmentRepository.findAllByServiceId(id);
        for (Appointment appointment : appointments) {
            appointment.setStatus(Status.CANCELLED);
            appointmentRepository.save(appointment);

            notificationService.createNotification(
                    appointment.getClient().getUserId(),
                    "Запись отменена",
                    "Ваша запись на " + appointment.getService().getName() +
                            " к врачу " + appointment.getDoctor().getUser().getName() +
                            " на " + appointment.getDateTime().toLocalDate() + " была отменена.",
                    NotificationType.APPOINTMENT_CANCELLED
            );
        }

        Service service = getServiceById(id);
        if (service != null) {
            service.getDoctors().clear();
            serviceRepository.save(service);
        }

        serviceRepository.deleteById(id);
    }

    public int countByCategory(Integer categoryId) {
        return serviceRepository.findAllByCategory_Id(categoryId).size();
    }

    @Transactional
    public void setNullCategoryByCategoryId(Integer categoryId) {
        List<Service> services = serviceRepository.findAllByCategory_Id(categoryId);
        for (Service service : services) {
            service.setCategory(null);
            serviceRepository.save(service);
        }
    }
}
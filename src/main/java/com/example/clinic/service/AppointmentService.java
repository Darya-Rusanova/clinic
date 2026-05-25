package com.example.clinic.service;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Status;
import com.example.clinic.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BookingService bookingService;

    public List<Appointment> getClientAppointments(Integer clientId) {
        return appointmentRepository.findAllByClient_UserId(clientId);
    }

    public List<Appointment> getClientAppointmentsWithDetails(Integer clientId) {
        return appointmentRepository.findAllByClient_UserIdWithDetails(clientId);
    }

    public List<Appointment> getDoctorAppointments(Integer doctorId) {
        return appointmentRepository.findAllByDoctor_UserId(doctorId);
    }

    @Transactional
    public void cancelAppointment(Integer id) {
        bookingService.cancelledAppointment(id);
    }
    public int countByClient(Integer clientId) {
        return appointmentRepository.findAllByClient_UserId(clientId).size();
    }
    public Appointment getAppointmentById(Integer id) {
        return appointmentRepository.findById(id).orElse(null);
    }
}
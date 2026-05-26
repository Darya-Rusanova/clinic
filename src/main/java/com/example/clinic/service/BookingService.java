package com.example.clinic.service;

import com.example.clinic.model.*;
import com.example.clinic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    ScheduleService scheduleService;
    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    ServiceRepository serviceRepository;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    NotificationService notificationService;

    public Appointment createAppointment(Integer doctorId, Integer serviceId, Integer clientId, LocalDateTime dateTime){
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        com.example.clinic.model.Service service = serviceRepository.findById(serviceId).orElse(null);
        Client client = clientRepository.findById(clientId).orElse(null);
        if(doctor == null || service == null || client == null) throw new RuntimeException("Некорректные данные");

        List<LocalTime> slots = scheduleService.getFreeSlots(doctorId,dateTime.toLocalDate(),service.getDuration());

        if(!slots.contains(dateTime.toLocalTime())){
            throw new RuntimeException("Выбранное время недоступно");
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setService(service);
        appointment.setClient(client);
        appointment.setDateTime(dateTime);
        appointment.setStatus(Status.SCHEDULED);

        return appointmentRepository.save(appointment);
    }
    public void cancelledAppointment(Integer id){
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment != null && appointment.getStatus() == Status.SCHEDULED) {
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
    }
    public Appointment updateAppointment(Integer id, Integer doctorId, Integer serviceId, Integer clientId, LocalDateTime dateTime){
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment == null) throw new RuntimeException("Запись не существует");

        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        com.example.clinic.model.Service service = serviceRepository.findById(serviceId).orElse(null);
        Client client = clientRepository.findById(clientId).orElse(null);
        if(doctor == null || service == null || client == null) throw new RuntimeException("Некорректные данные");
        List<LocalTime> slots = scheduleService.getFreeSlots(doctorId,dateTime.toLocalDate(),service.getDuration());

        if(!slots.contains(dateTime.toLocalTime())){
            throw new RuntimeException("Выбранное время недоступно");
        }

        appointment.setDoctor(doctor);
        appointment.setService(service);
        appointment.setClient(client);
        appointment.setDateTime(dateTime);
        appointment.setStatus(Status.SCHEDULED);

        return appointmentRepository.save(appointment);
    }
}

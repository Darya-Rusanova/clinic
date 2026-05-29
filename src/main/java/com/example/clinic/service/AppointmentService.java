package com.example.clinic.service;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.NotificationType;
import com.example.clinic.model.Status;
import com.example.clinic.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private NotificationService notificationService;


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
    @Transactional
    public boolean completeAppointment(Integer id) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment != null && appointment.getStatus() == Status.SCHEDULED) {
            appointment.setStatus(Status.COMPLETED);
            appointmentRepository.save(appointment);

            notificationService.createNotification(
                    appointment.getClient().getUserId(),
                    "Прием завершен",
                    "Прием по услуге " + appointment.getService().getName() +
                            " у врача " + appointment.getDoctor().getUser().getName() + " успешно завершен.",
                    NotificationType.APPOINTMENT_COMPLETED
            );

            return true;
        }
        return false;
    }

    public Page<Appointment> getClientAppointmentsPaginated(Integer clientId, String filter, Pageable pageable) {
        if (filter.equals("all")) {
            return appointmentRepository.findPaginatedByClientId(clientId, pageable);
        } else {
            Status status = Status.valueOf(filter);
            return appointmentRepository.findPaginatedByClientIdAndStatus(clientId, status, pageable);
        }
    }


    public long countByClientAndStatus(Integer clientId, Status status) {
        return appointmentRepository.countByClientIdAndStatus(clientId, status);
    }

    public long countByDateTimeBetween(LocalDateTime start, LocalDateTime end,Status status){
        return appointmentRepository.countByDateTimeBetweenAndStatus(start, end, status);
    }

    public List<Appointment> getFirstScheduledAppointments(Status status, LocalDateTime dateTime, Pageable pageable){
        return appointmentRepository.findByStatusAndDateTimeAfter(status, dateTime, pageable);
    }

    public Page<Appointment> getAllWithClientsAndDoctors(Pageable pageable, String status){
        if (status == null || status.equals("all")) {
            return appointmentRepository.findAllWithClientsAndDoctors(pageable);
        } else {
            Status statusEnum = Status.valueOf(status);
            return appointmentRepository.findByStatusWithClientsAndDoctors(statusEnum, pageable);
        }
    }
}

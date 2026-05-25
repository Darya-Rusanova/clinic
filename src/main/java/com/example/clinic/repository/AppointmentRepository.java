package com.example.clinic.repository;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Doctor;
import com.example.clinic.model.Service;
import com.example.clinic.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Integer> {
    List<Appointment> findAllByDoctor_UserId(Integer userId);

    List<Appointment> findAllByClient_UserId(Integer userId);
    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.client c " +
            "JOIN FETCH c.user " +
            "JOIN FETCH a.service " +
            "WHERE a.doctor.userId = :doctorId " +
            "AND DATE(a.dateTime) BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndDateBetween(@Param("doctorId") Integer doctorId,
                                                   @Param("start") LocalDate start,
                                                   @Param("end") LocalDate end);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.client c " +
            "JOIN FETCH c.user " +
            "JOIN FETCH a.service " +
            "WHERE a.doctor.userId = :doctorId " +
            "AND DATE(a.dateTime) = :date")
    List<Appointment> findByDoctorIdAndDate(@Param("doctorId") Integer doctorId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.service " +
            "LEFT JOIN FETCH a.doctor d " +
            "LEFT JOIN FETCH d.user " +
            "WHERE a.client.userId = :clientId")
    List<Appointment> findAllByClient_UserIdWithDetails(@Param("clientId") Integer clientId);

    List<Appointment> findByService(Service service);


    @Query("SELECT a FROM Appointment a " +
            "JOIN a.service s " +
            "WHERE a.doctor.userId = :doctorId " +
            "AND s.id IN :serviceIds " +
            "AND a.status = :status")
    List<Appointment> findByDoctorAndServiceInAndStatus(@Param("doctorId") Integer doctorId,
                                                        @Param("serviceIds") List<Integer> serviceIds,
                                                        @Param("status") Status status);

    List<Appointment> findAllByServiceId(Integer id);
}

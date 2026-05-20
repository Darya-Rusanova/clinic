package com.example.clinic.repository;

import com.example.clinic.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Integer> {
    List<Appointment> findAllByDoctor_UserId(Integer userId);
    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.client c " +
            "JOIN FETCH c.user " +
            "JOIN FETCH a.service s " +
            "WHERE a.doctor.userId = :doctorId AND DATE(a.dateTime) = :date")
    List<Appointment> findByDoctorIdAndDate(@Param("doctorId") Integer doctorId,
                                            @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.client c " +
            "JOIN FETCH c.user " +
            "JOIN FETCH a.service " +
            "WHERE a.doctor.userId = :doctorId " +
            "AND DATE(a.dateTime) BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndDateBetween(@Param("doctorId") Integer doctorId,
                                                   @Param("start") LocalDate start,
                                                   @Param("end") LocalDate end);
}

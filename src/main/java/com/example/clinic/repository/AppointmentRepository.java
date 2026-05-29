package com.example.clinic.repository;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findAllByDoctor_UserId(Integer userId);
    List<Appointment> findAllByClient_UserId(Integer userId);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.client c JOIN FETCH c.user " +
            "JOIN FETCH a.service " +
            "WHERE a.doctor.userId = :doctorId AND DATE(a.dateTime) BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndDateBetween(@Param("doctorId") Integer doctorId,
                                                   @Param("start") LocalDate start,
                                                   @Param("end") LocalDate end);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.client c JOIN FETCH c.user " +
            "JOIN FETCH a.service " +
            "WHERE a.doctor.userId = :doctorId AND DATE(a.dateTime) = :date")
    List<Appointment> findByDoctorIdAndDate(@Param("doctorId") Integer doctorId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a " +
            "JOIN a.service s " +
            "WHERE a.doctor.userId = :doctorId " +
            "AND s.id IN :serviceIds " +
            "AND a.status = :status")
    List<Appointment> findByDoctorAndServiceInAndStatus(@Param("doctorId") Integer doctorId,
                                                        @Param("serviceIds") List<Integer> serviceIds,
                                                        @Param("status") Status status);

    List<Appointment> findAllByServiceId(Integer id);

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.service " +
            "LEFT JOIN FETCH a.doctor d " +
            "LEFT JOIN FETCH d.user " +
            "WHERE a.client.userId = :clientId")
    Page<Appointment> findPaginatedByClientId(@Param("clientId") Integer clientId, Pageable pageable);

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.service " +
            "LEFT JOIN FETCH a.doctor d " +
            "LEFT JOIN FETCH d.user " +
            "WHERE a.client.userId = :clientId AND a.status = :status")
    Page<Appointment> findPaginatedByClientIdAndStatus(@Param("clientId") Integer clientId,
                                                       @Param("status") Status status,
                                                       Pageable pageable);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.client.userId = :clientId AND a.status = :status")
    long countByClientIdAndStatus(@Param("clientId") Integer clientId,
                                  @Param("status") Status status);

    @Query("SELECT DISTINCT a FROM Appointment a " +
            "LEFT JOIN FETCH a.client c LEFT JOIN FETCH c.user " +
            "LEFT JOIN FETCH a.service s " +
            "LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH d.user")
    Page<Appointment> findAllWithClientsAndDoctors(Pageable pageable);

    @Query("SELECT DISTINCT a FROM Appointment a " +
            "LEFT JOIN FETCH a.client c LEFT JOIN FETCH c.user " +
            "LEFT JOIN FETCH a.service s " +
            "LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH d.user " +
            "WHERE a.status = :status")
    Page<Appointment> findByStatusWithClientsAndDoctors(@Param("status") Status status, Pageable pageable);

    long countByDateTimeBetweenAndStatus(LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd, Status status);

    @Query("SELECT a FROM Appointment a WHERE a.status = :status AND a.dateTime > :now ORDER BY a.dateTime ASC")
    List<Appointment> findByStatusAndDateTimeAfter(@Param("status") Status status,
                                                   @Param("now") LocalDateTime now,
                                                   Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.dateTime BETWEEN :start AND :end")
    List<Appointment> findByDateTimeBetween(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);
}
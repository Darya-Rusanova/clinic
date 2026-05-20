package com.example.clinic.repository;

import com.example.clinic.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule,Integer> {
    List<Schedule> findAllByDoctor_UserId(Integer userId);
    Optional<Schedule> findByDoctor_UserIdAndDayOfWeek(Integer doctorId, Integer dayOfWeek);
}

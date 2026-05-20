package com.example.clinic.repository;

import com.example.clinic.model.Break;
import com.example.clinic.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BreakRepository extends JpaRepository<Break,Integer> {
    List<Break> findAllBySchedule(Schedule schedule);
    List<Break> findAllByScheduleIn(List<Schedule> schedules);
}

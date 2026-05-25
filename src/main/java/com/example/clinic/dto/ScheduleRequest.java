package com.example.clinic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ScheduleRequest {
    @NotNull(message = "Данные расписания не могут быть пустыми")
    private Map<String, Object> scheduleData;
}
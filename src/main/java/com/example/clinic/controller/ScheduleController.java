package com.example.clinic.controller;

import com.example.clinic.dto.SlotDto;
import com.example.clinic.repository.ScheduleRepository;
import com.example.clinic.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ScheduleController {
    @Autowired
    ScheduleService scheduleService;
    @GetMapping("/api/doctor/{id}/schedule")
    public Map<String, Object> getScheduleJson(@PathVariable Integer id,
                                               @RequestParam(defaultValue = "0") Integer weekOffset) {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
        LocalDate sunday = monday.plusDays(6);

        Map<Integer, List<SlotDto>> slotsByDayOfWeek = scheduleService.weeklySlots(id, weekOffset);

        List<Map<String, Object>> weekSchedule = new ArrayList<>();

        for (int day = 1; day <= 7; day++) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("dayOfWeek", day);
            dayData.put("dayName", getDayName(day));
            dayData.put("date", monday.plusDays(day - 1).toString());

            List<SlotDto> slots = slotsByDayOfWeek.getOrDefault(day, List.of());
            List<Map<String, Object>> slotsJson = new ArrayList<>();

            for (SlotDto slot : slots) {
                Map<String, Object> slotJson = new HashMap<>();
                slotJson.put("startTime", slot.getStartTime().toString());
                slotJson.put("endTime", slot.getEndTime().toString());
                slotJson.put("status", slot.getStatus().name());
                slotJson.put("statusText", slot.getStatus().name().equals("AVAILABLE") ? "Доступно" :
                        (slot.getStatus().name().equals("BOOKED") ? "Занято" : "Перерыв"));
                if (slot.getClientName() != null) {
                    slotJson.put("clientName", slot.getClientName());
                    slotJson.put("serviceName", slot.getServiceName());
                }
                slotsJson.add(slotJson);
            }

            dayData.put("slots", slotsJson);
            weekSchedule.add(dayData);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("weekSchedule", weekSchedule);
        response.put("weekStart", monday.toString());
        response.put("weekEnd", sunday.toString());
        response.put("weekOffset", weekOffset);

        return response;
    }

    private String getDayName(int day) {
        switch (day) {
            case 1: return "Понедельник";
            case 2: return "Вторник";
            case 3: return "Среда";
            case 4: return "Четверг";
            case 5: return "Пятница";
            case 6: return "Суббота";
            case 7: return "Воскресенье";
            default: return "";
        }
    }
}

package com.example.clinic.service;

import com.example.clinic.dto.SlotDto;
import com.example.clinic.dto.SlotStatus;
import com.example.clinic.model.Appointment;
import com.example.clinic.model.Break;
import com.example.clinic.model.Schedule;
import com.example.clinic.model.Status;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.BreakRepository;
import com.example.clinic.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    BreakRepository breakRepository;
    @Autowired
    AppointmentRepository appointmentRepository;

    public List<LocalTime> getFreeSlots(Integer doctorId, LocalDate date, Integer duration){
        int dayOfWeek = date.getDayOfWeek().getValue();
        Schedule schedule = scheduleRepository.findByDoctor_UserIdAndDayOfWeek(doctorId,dayOfWeek).orElse(null);
        if (schedule == null) return List.of();

        List<Break> breaks = breakRepository.findAllBySchedule(schedule);
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctorId,date);

        List<SlotDto> allSlots = buildSlots(schedule,breaks,appointments);
        List<LocalTime> free = new ArrayList<>();

        int count = (int) Math.ceil((double) duration /60);
        for (int i = 0; i < allSlots.size() - count; i++) {
            boolean isAvailable = true;
            for (int j = 0; j < count; j++) {
                SlotDto slot = allSlots.get(i+j);
                if (slot.getStatus() != SlotStatus.AVAILABLE){
                    isAvailable = false;
                    break;
                }
            }
            if (isAvailable){
                free.add(allSlots.get(i).getStartTime());
            }
        }

        free.stream().forEach(System.out::println);
        return free;
    }
    public Map<Integer, List<SlotDto>> weeklySlots(Integer doctorId, Integer weekOffset){
        LocalDate start = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
        LocalDate end = start.plusDays(6);
        List<Schedule> schedules = scheduleRepository.findAllByDoctor_UserId(doctorId);
        if (schedules.isEmpty()){
            return Map.of();
        }
        List<Break> breaks = breakRepository.findAllByScheduleIn(schedules);

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDateBetween(doctorId,start,end);

        Map<Integer, List<Break>> breaksBySchedules = breaks.stream().collect(Collectors.groupingBy(b -> b.getSchedule().getId()));
        Map<LocalDate, List<Appointment>> appointmentsByDays = appointments.stream().collect(Collectors.groupingBy(Appointment::getDate));

        Map<Integer, List<SlotDto>> slots = new LinkedHashMap<>();
        for(Schedule schedule: schedules){
            int day = schedule.getDayOfWeek();
            slots.put(day, buildSlots(schedule,
                    breaksBySchedules.getOrDefault(schedule.getId(), List.of()),
                    appointmentsByDays.getOrDefault(getNextDateForDayOfWeek(day), List.of())));
        }
        return slots;
    }
    private List<SlotDto> buildSlots(Schedule schedule, List<Break> breaks, List<Appointment> appointments){
        int slotDuration = 60;
        List<LocalTime> slots = generateSlots(schedule.getStartTime(), schedule.getEndTime(), slotDuration);

        List<SlotDto> result = new ArrayList<>();

        for (LocalTime time : slots) {
            result.add(new SlotDto(time, time.plusMinutes(slotDuration), SlotStatus.AVAILABLE, null, null, null));
        }

        for (SlotDto slot : result) {
            if (isBreak(slot.getStartTime(), breaks)) {
                slot.setStatus(SlotStatus.BREAK);
            }
        }

        for (Appointment app : appointments) {
            if (app.getStatus() == Status.CANCELLED || app.getStatus() == Status.COMPLETED) {
                continue;
            }
            LocalTime startTime = app.getDateTime().toLocalTime();
            int serviceDuration = app.getService().getDuration();
            int occupiedSlots = (int) Math.ceil((double) serviceDuration / slotDuration);;

            for (int i = 0; i < occupiedSlots; i++) {
                LocalTime currentSlotTime = startTime.plusMinutes(i * slotDuration);
                int finalI = i;
                result.stream()
                        .filter(slot -> slot.getStartTime().equals(currentSlotTime))
                        .findFirst()
                        .ifPresent(slot -> {
                            slot.setStatus(SlotStatus.BOOKED);
                            if (finalI == 0) {
                                slot.setClientName(app.getClient().getUser().getName());
                                slot.setServiceName(app.getService().getName());
                            } else {
                                slot.setClientName("↳ " + app.getClient().getUser().getName());
                                slot.setServiceName("(продолжение " + app.getService().getName() + ")");
                            }
                        });
            }
        }
        return result;
    }

    private LocalDate getNextDateForDayOfWeek(int dayOfWeek) {
        LocalDate today = LocalDate.now();
        int todayDayOfWeek = today.getDayOfWeek().getValue();

        int daysUntil = dayOfWeek - todayDayOfWeek;
        if (daysUntil <= 0) {
            daysUntil += 7;
        }

        return today.plusDays(daysUntil);
    }

    private boolean isBreak(LocalTime slot, List<Break> breaks){
        for(Break breakTime: breaks){
            if (!slot.isBefore(breakTime.getStartTime()) && slot.isBefore(breakTime.getEndTime())){
                return true;
            }
        }
        return false;
    }

    private List<LocalTime> generateSlots(LocalTime start, LocalTime end,int duration){
        List<LocalTime> times = new ArrayList<>();
        LocalTime current = start;
        while(current.isBefore(end)){
            times.add(current);
            current = current.plusMinutes(duration);
        }
        return times;
    }
}

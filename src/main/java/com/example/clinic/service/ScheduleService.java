package com.example.clinic.service;

import com.example.clinic.dto.SlotDto;
import com.example.clinic.dto.SlotStatus;
import com.example.clinic.model.*;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.BreakRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    BreakRepository breakRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    DoctorRepository doctorRepository;

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

    public Map<String, Object> getDoctorSchedule(Integer doctorId) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (int day = 1; day <= 7; day++) {
            Schedule schedule = scheduleRepository.findByDoctor_UserIdAndDayOfWeek(doctorId, day).orElse(null);

            Map<String, Object> dayData = new HashMap<>();

            if (schedule == null) {
                dayData.put("isDayOff", true);
                dayData.put("startTime", null);
                dayData.put("endTime", null);
                dayData.put("breaks", List.of());
            } else {
                dayData.put("isDayOff", false);
                dayData.put("startTime", schedule.getStartTime().toString());
                dayData.put("endTime", schedule.getEndTime().toString());

                List<Break> breaks = breakRepository.findAllBySchedule(schedule);
                List<Map<String, String>> breaksList = new ArrayList<>();
                for (Break b : breaks) {
                    Map<String, String> breakMap = new HashMap<>();
                    breakMap.put("startTime", b.getStartTime().toString());
                    breakMap.put("endTime", b.getEndTime().toString());
                    breaksList.add(breakMap);
                }
                dayData.put("breaks", breaksList);
            }

            result.put(String.valueOf(day), dayData);
        }

        return result;
    }

    public Map<Integer, Integer> getAppointmentsCountByDay(Integer doctorId) {
        List<Appointment> appointments = appointmentRepository.findAllByDoctor_UserId(doctorId);
        Map<Integer, Integer> countByDay = new HashMap<>();

        for (Appointment app : appointments) {
            if (app.getStatus() == Status.SCHEDULED) {
                int dayOfWeek = app.getDateTime().getDayOfWeek().getValue();
                countByDay.put(dayOfWeek, countByDay.getOrDefault(dayOfWeek, 0) + 1);
            }
        }

        System.out.println("Appointments count by day for doctor " + doctorId + ": " + countByDay);
        return countByDay;
    }

    @Transactional
    public void saveDoctorSchedule(Integer doctorId, Map<String, Object> scheduleData) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Врач не найден"));

        Map<Integer, Integer> activeAppointmentsCount = getAppointmentsCountByDay(doctorId);

        List<Schedule> existingSchedules = scheduleRepository.findAllByDoctor_UserId(doctorId);
        for (Schedule s : existingSchedules) {
            int dayOfWeek = s.getDayOfWeek();
            if (activeAppointmentsCount.getOrDefault(dayOfWeek, 0) == 0) {
                List<Break> breaksToDelete = breakRepository.findAllBySchedule(s);
                if (!breaksToDelete.isEmpty()) {
                    breakRepository.deleteAll(breaksToDelete);
                }
                scheduleRepository.delete(s);
            }
        }
        scheduleRepository.flush();
        breakRepository.flush();

        for (Map.Entry<String, Object> entry : scheduleData.entrySet()) {
            int dayOfWeek = Integer.parseInt(entry.getKey());

            if (activeAppointmentsCount.getOrDefault(dayOfWeek, 0) > 0) {
                System.out.println("Day " + dayOfWeek + " has " + activeAppointmentsCount.get(dayOfWeek) + " active appointments - skipping save");
                continue;
            }

            Map<String, Object> dayData = (Map<String, Object>) entry.getValue();
            Boolean isDayOff = (Boolean) dayData.getOrDefault("isDayOff", false);

            if (Boolean.TRUE.equals(isDayOff)) {
                continue;
            }

            String startTimeStr = (String) dayData.get("startTime");
            String endTimeStr = (String) dayData.get("endTime");

            if (startTimeStr == null || endTimeStr == null) {
                continue;
            }

            Schedule schedule = new Schedule();
            schedule.setDoctor(doctor);
            schedule.setDayOfWeek(dayOfWeek);
            schedule.setStartTime(LocalTime.parse(startTimeStr));
            schedule.setEndTime(LocalTime.parse(endTimeStr));
            schedule = scheduleRepository.save(schedule);

            @SuppressWarnings("unchecked")
            List<Map<String, String>> newBreaks = (List<Map<String, String>>) dayData.get("breaks");

            if (newBreaks != null) {
                for (Map<String, String> breakData : newBreaks) {
                    String breakStart = breakData.get("startTime");
                    String breakEnd = breakData.get("endTime");
                    if (breakStart != null && breakEnd != null &&
                            !breakStart.isEmpty() && !breakEnd.isEmpty()) {
                        Break breakTime = new Break();
                        breakTime.setSchedule(schedule);
                        breakTime.setStartTime(LocalTime.parse(breakStart));
                        breakTime.setEndTime(LocalTime.parse(breakEnd));
                        breakRepository.save(breakTime);
                    }
                }
            }
        }
    }
}
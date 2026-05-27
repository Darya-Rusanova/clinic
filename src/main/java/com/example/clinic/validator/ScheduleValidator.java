package com.example.clinic.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ScheduleValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        @SuppressWarnings("unchecked")
        Map<String, Object> scheduleData = (Map<String, Object>) target;

        for (Map.Entry<String, Object> entry : scheduleData.entrySet()) {
            int dayOfWeek;
            try {
                dayOfWeek = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException e) {
                continue;
            }

            Map<String, Object> dayData = (Map<String, Object>) entry.getValue();
            Boolean isDayOff = (Boolean) dayData.getOrDefault("isDayOff", false);

            if (Boolean.TRUE.equals(isDayOff)) {
                continue;
            }

            String startTimeStr = (String) dayData.get("startTime");
            String endTimeStr = (String) dayData.get("endTime");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> breaks = (List<Map<String, String>>) dayData.get("breaks");

            String workError = validateWorkingTime(startTimeStr, endTimeStr);
            if (workError != null) {
                errors.reject("day." + dayOfWeek + ".workTime", workError);
            }

            List<String> breakErrors = validateBreaks(startTimeStr, endTimeStr, breaks);
            for (String breakError : breakErrors) {
                errors.reject("day." + dayOfWeek + ".breaks", breakError);
            }
        }
    }

    private String validateWorkingTime(String startTimeStr, String endTimeStr) {
        if (startTimeStr == null || startTimeStr.isEmpty()) {
            return "Время начала работы не может быть пустым";
        }

        if (endTimeStr == null || endTimeStr.isEmpty()) {
            return "Время окончания работы не может быть пустым";
        }

        try {
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);

            if (!startTime.isBefore(endTime)) {
                return "Время окончания работы должно быть позже времени начала";
            }
        } catch (Exception e) {
            return "Некорректный формат времени";
        }

        return null;
    }

    private List<String> validateBreaks(String startTimeStr, String endTimeStr,
                                        List<Map<String, String>> breaks) {
        List<String> errors = new ArrayList<>();

        if (breaks == null || breaks.isEmpty()) {
            return errors;
        }

        LocalTime startTime = null;
        LocalTime endTime = null;

        try {
            if (startTimeStr != null && !startTimeStr.isEmpty()) {
                startTime = LocalTime.parse(startTimeStr);
            }
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                endTime = LocalTime.parse(endTimeStr);
            }
        } catch (Exception e) {
            errors.add("Некорректный формат рабочего времени");
            return errors;
        }

        List<LocalTime[]> breakTimes = new ArrayList<>();

        for (int i = 0; i < breaks.size(); i++) {
            Map<String, String> breakData = breaks.get(i);
            String breakStartStr = breakData.get("startTime");
            String breakEndStr = breakData.get("endTime");

            if (breakStartStr == null || breakStartStr.isEmpty()) {
                errors.add("Перерыв #" + (i + 1) + ": время начала не может быть пустым");
                continue;
            }

            if (breakEndStr == null || breakEndStr.isEmpty()) {
                errors.add("Перерыв #" + (i + 1) + ": время окончания не может быть пустым");
                continue;
            }

            try {
                LocalTime breakStart = LocalTime.parse(breakStartStr);
                LocalTime breakEnd = LocalTime.parse(breakEndStr);

                if (!breakStart.isBefore(breakEnd)) {
                    errors.add("Перерыв #" + (i + 1) + ": время окончания должно быть позже времени начала");
                    continue;
                }

                if (startTime != null && endTime != null) {
                    if (breakStart.isBefore(startTime) || breakEnd.isAfter(endTime)) {
                        errors.add("Перерыв #" + (i + 1) + " должен быть в пределах рабочего дня (" + startTime + " - " + endTime + ")");
                        continue;
                    }
                }

                breakTimes.add(new LocalTime[]{breakStart, breakEnd});

            } catch (Exception e) {
                errors.add("Перерыв #" + (i + 1) + ": некорректный формат времени");
            }
        }

        for (int i = 0; i < breakTimes.size(); i++) {
            LocalTime[] break1 = breakTimes.get(i);
            for (int j = i + 1; j < breakTimes.size(); j++) {
                LocalTime[] break2 = breakTimes.get(j);

                boolean notOverlapping = break1[1].isBefore(break2[0]) || break2[1].isBefore(break1[0]);

                if (!notOverlapping) {
                    errors.add("Перерывы #" + (i + 1) + " и #" + (j + 1) + " не должны пересекаться");
                    return errors;
                }
            }
        }

        return errors;
    }
}
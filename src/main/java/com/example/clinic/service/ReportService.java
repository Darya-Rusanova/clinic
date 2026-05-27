package com.example.clinic.service;

import com.example.clinic.dto.ReportDto;
import com.example.clinic.model.Appointment;
import com.example.clinic.model.Status;
import com.example.clinic.repository.AppointmentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Page<ReportDto> getFilteredAppointments(LocalDate startDate, LocalDate endDate,
                                                   String statusStr, int page, int size) {

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateTime"));

        List<Appointment> allAppointments = appointmentRepository.findByDateTimeBetween(start, end);

        List<Appointment> validAppointments = allAppointments.stream()
                .filter(this::isValidAppointment)
                .collect(Collectors.toList());

        if (statusStr != null && !statusStr.isEmpty() && !statusStr.equals("all")) {
            Status status = Status.valueOf(statusStr);
            validAppointments = validAppointments.stream()
                    .filter(a -> a.getStatus() == status)
                    .collect(Collectors.toList());
        }

        validAppointments.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));

        int startIdx = (int) pageable.getOffset();
        int endIdx = Math.min(startIdx + pageable.getPageSize(), validAppointments.size());

        List<ReportDto> pageContent = new ArrayList<>();
        if (startIdx < validAppointments.size()) {
            pageContent = validAppointments.subList(startIdx, endIdx).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, pageable, validAppointments.size());
    }

    public List<ReportDto> getAllReportData(LocalDate startDate, LocalDate endDate, String statusStr) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<Appointment> appointments = appointmentRepository.findByDateTimeBetween(start, end);

        List<Appointment> validAppointments = appointments.stream()
                .filter(this::isValidAppointment)
                .collect(Collectors.toList());

        if (statusStr != null && !statusStr.isEmpty() && !statusStr.equals("all")) {
            Status status = Status.valueOf(statusStr);
            validAppointments = validAppointments.stream()
                    .filter(a -> a.getStatus() == status)
                    .toList();
        }

        return validAppointments.stream()
                .sorted((a, b) -> b.getDateTime().compareTo(a.getDateTime()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private boolean isValidAppointment(Appointment app) {
        try {
            if (app.getDoctor() == null) return false;
            if (app.getDoctor().getUser() == null) return false;
            if (app.getClient() == null) return false;
            if (app.getClient().getUser() == null) return false;
            if (app.getService() == null) return false;
            app.getService().getName();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private ReportDto convertToDto(Appointment app) {
        ReportDto dto = new ReportDto();
        dto.setId(Long.valueOf(app.getId()));
        dto.setClientName(app.getClient().getUser().getName());
        dto.setClientPhone(app.getClient().getUser().getPhone());
        dto.setDoctorName(app.getDoctor().getUser().getName());
        dto.setServiceName(app.getService().getName());
        dto.setPrice((double) app.getService().getPrice());
        dto.setDuration(app.getService().getDuration());
        dto.setDateTime(app.getDateTime());
        dto.setStatus(app.getStatus().name());
        dto.setCreatedAt(app.getDateTime().toLocalDate());
        return dto;
    }

    public ByteArrayInputStream exportToExcel(List<ReportDto> reports) throws IOException {
        String[] columns = {"ID", "Дата и время", "Клиент", "Телефон", "Врач", "Услуга", "Цена", "Длительность", "Статус"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Отчет по записям");

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            for (ReportDto report : reports) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(report.getId());
                row.createCell(1).setCellValue(report.getDateTime() != null ? report.getDateTime().format(formatter) : "");
                row.createCell(2).setCellValue(report.getClientName());
                row.createCell(3).setCellValue(report.getClientPhone());
                row.createCell(4).setCellValue(report.getDoctorName());
                row.createCell(5).setCellValue(report.getServiceName());
                row.createCell(6).setCellValue(report.getPrice());
                row.createCell(7).setCellValue(report.getDuration());
                row.createCell(8).setCellValue(getStatusRu(report.getStatus()));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private String getStatusRu(String status) {
        if (status == null) return "Неизвестно";
        switch (status) {
            case "SCHEDULED": return "Предстоит";
            case "COMPLETED": return "Завершено";
            case "CANCELLED": return "Отменено";
            default: return status;
        }
    }

    public Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<Appointment> appointments = appointmentRepository.findByDateTimeBetween(start, end);

        List<Appointment> validAppointments = appointments.stream()
                .filter(this::isValidAppointment)
                .toList();

        long total = validAppointments.size();
        long scheduled = 0;
        long completed = 0;
        long cancelled = 0;
        double totalRevenue = 0;

        for (Appointment a : validAppointments) {
            if (a.getStatus() == Status.SCHEDULED) {
                scheduled++;
            } else if (a.getStatus() == Status.COMPLETED) {
                completed++;
                if (a.getService() != null) {
                    totalRevenue += a.getService().getPrice();
                }
            } else if (a.getStatus() == Status.CANCELLED) {
                cancelled++;
            }
        }

        stats.put("total", total);
        stats.put("scheduled", scheduled);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }
}
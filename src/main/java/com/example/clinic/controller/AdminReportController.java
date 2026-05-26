package com.example.clinic.controller;

import com.example.clinic.dto.ReportDto;
import com.example.clinic.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public String reportsPage(Model model) {
        model.addAttribute("startDate", LocalDate.now().withDayOfMonth(1));
        model.addAttribute("endDate", LocalDate.now());
        return "admin/admin-reports";
    }

    @GetMapping("/data")
    @ResponseBody
    public Map<String, Object> getReportData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> response = new HashMap<>();

        Page<ReportDto> reportPage = reportService.getFilteredAppointments(
                startDate, endDate, status, page, size);

        response.put("appointments", reportPage.getContent());
        response.put("currentPage", reportPage.getNumber());
        response.put("totalPages", reportPage.getTotalPages());
        response.put("totalElements", reportPage.getTotalElements());

        Map<String, Object> stats = reportService.getStatistics(startDate, endDate);
        response.put("statistics", stats);

        return response;
    }

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status) throws Exception {

        List<ReportDto> reports = reportService.getAllReportData(startDate, endDate, status);
        ByteArrayInputStream in = reportService.exportToExcel(reports);

        String filename = "report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
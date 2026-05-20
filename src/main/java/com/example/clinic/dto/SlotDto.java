package com.example.clinic.dto;

import com.example.clinic.model.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SlotDto {
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;
    private String clientName;
    private String serviceName;
    private Integer appointmentId;

    @Override
    public String toString() {
        return "слот{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                ", clientName='" + clientName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", appointmentId=" + appointmentId +
                '}';
    }
}


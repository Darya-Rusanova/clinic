package com.example.clinic.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class BreakDto {
    private LocalTime startTime;
    private LocalTime endTime;
}

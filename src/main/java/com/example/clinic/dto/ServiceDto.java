package com.example.clinic.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceDto {
    private Integer id;
    private String name;
    private String description;
    private Integer duration;
    private Integer price;
    private Integer categoryId;
    private String categoryName;
}
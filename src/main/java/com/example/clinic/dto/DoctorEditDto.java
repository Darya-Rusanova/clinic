package com.example.clinic.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class DoctorEditDto {

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    private String patronymic;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{10,20}$", message = "Неверный формат телефона")
    private String phone;

    @Email(message = "Неверный формат email")
    private String email;

    private Boolean gender;

    @Min(value = 0, message = "Стаж не может быть отрицательным")
    @Max(value = 70, message = "Стаж не может быть больше 70 лет")
    private Integer experienceYear;

    private String imagePath;
    private String licensePath;

    private List<Integer> serviceIds;
}
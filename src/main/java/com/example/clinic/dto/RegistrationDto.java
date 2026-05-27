package com.example.clinic.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RegistrationDto {

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    private String patronymic;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^(\\+7|8|7)[0-9]{10}$", message = "Телефон должен быть в формате +7XXXXXXXXXX, 8XXXXXXXXXX или 7XXXXXXXXXX")
    private String phone;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 4, message = "Пароль должен содержать минимум 4 символа")
    private String password;

    @NotBlank(message = "Подтверждение пароля обязательно")
    private String confirmPassword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    private Boolean gender = true;

    @AssertTrue(message = "Пароли не совпадают")
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
package com.example.clinic.validator;

import com.example.clinic.dto.RegistrationDto;
import com.example.clinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RegistrationValidator implements Validator {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RegistrationDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegistrationDto dto = (RegistrationDto) target;

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                errors.rejectValue("email", "email.duplicate", "Пользователь с таким email уже существует");
            }
        }

        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            if (userRepository.findByPhone(dto.getPhone()).isPresent()) {
                errors.rejectValue("phone", "phone.duplicate", "Пользователь с таким телефоном уже существует");
            }
        }
    }
}
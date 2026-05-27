package com.example.clinic.service;

import com.example.clinic.model.User;
import com.example.clinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public  User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElse(null);
    }
}
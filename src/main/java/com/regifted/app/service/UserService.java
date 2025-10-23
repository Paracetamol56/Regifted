package com.regifted.app.service;

import com.regifted.app.dto.LoginRequest;
import com.regifted.app.dto.LoginResponse;
import com.regifted.app.dto.RegisterRequest;
import com.regifted.app.entity.User;
import com.regifted.app.repository.UserRepository;
import com.regifted.app.security.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserService(UserRepository repo, BCryptPasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse register(RegisterRequest req) {
        if (repo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        repo.save(u);
        String token = jwtUtils.generateToken(u.getUuid(), u.getEmail());
        return new LoginResponse(token);
    }

    public LoginResponse login(LoginRequest req) {
        User u = repo.findByEmail(req.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtUtils.generateToken(u.getUuid(), u.getEmail());
        return new LoginResponse(token);
    }

    public User getByUuid(String uuid) {
        return repo.findById(uuid).orElse(null);
    }
}

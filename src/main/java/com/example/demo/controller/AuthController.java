package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository repo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 🔐 STORE OTP PER PHONE
    private Map<String, String> otpStore = new HashMap<>();


    // 📱 SEND OTP (SIMULATED)
    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam String phone) {

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        otpStore.put(phone, otp);

        System.out.println("OTP for " + phone + " = " + otp); // 🔥 PRINT IN TERMINAL

        return "OTP Sent (Check Console)";
    }


    // 📱 VERIFY OTP
    @PostMapping("/verify-otp")
    public String verifyOTP(@RequestParam String phone, @RequestParam String otp) {

        String stored = otpStore.get(phone);

        if (stored != null && stored.equals(otp)) {
            otpStore.remove(phone);
            return "Verified";
        }

        return "Invalid OTP";
    }


    // 🟢 REGISTER
    @PostMapping("/register")
    public String register(@RequestBody User user) {

        if (repo.findByUsername(user.getUsername()) != null)
            return "Username already exists";

        if (repo.findByPhone(user.getPhone()) != null)
            return "Phone already exists";

        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);

        return "Registered successfully";
    }


    // 🔵 LOGIN
    @PostMapping("/login")
    public String login(@RequestBody User user) {

        User existing = repo.findByUsername(user.getUsername());

        if (existing != null &&
            encoder.matches(user.getPassword(), existing.getPassword())) {

            return "Login successful";
        }

        return "Invalid login";
    }
}
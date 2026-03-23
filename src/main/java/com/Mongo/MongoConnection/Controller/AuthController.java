package com.Mongo.MongoConnection.Controller;

import com.Mongo.MongoConnection.Model.AuthRequest;
import com.Mongo.MongoConnection.Model.AuthResponse;
import com.Mongo.MongoConnection.Model.User;
import com.Mongo.MongoConnection.Repository.UserRepository;
import com.Mongo.MongoConnection.Config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public String signup(@RequestBody AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            return "User already exists";
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return "Signup successful";
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        return new AuthResponse(token, refreshToken);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody String refreshToken) {
        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email);
        if (user != null && jwtUtil.validateRefreshToken(refreshToken, email)
                && refreshToken.equals(user.getRefreshToken())) {
            String newToken = jwtUtil.generateToken(email);
            String newRefreshToken = jwtUtil.generateRefreshToken(email);
            user.setRefreshToken(newRefreshToken);
            userRepository.save(user);
            return new AuthResponse(newToken, newRefreshToken);
        }
        throw new RuntimeException("Invalid refresh token");
    }

    @PostMapping("/logout")
    public String logout(@RequestBody String refreshToken) {
        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email);
        if (user != null && refreshToken.equals(user.getRefreshToken())) {
            user.setRefreshToken(null);
            userRepository.save(user);
            return "Logged out successfully";
        }
        throw new RuntimeException("Invalid refresh token");
    }

    @GetMapping("/me")
    public User getCurrentUser(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7)); // Remove "Bearer "
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(null); // Don't return password
            return user;
        }
        throw new RuntimeException("User not found");
    }
}

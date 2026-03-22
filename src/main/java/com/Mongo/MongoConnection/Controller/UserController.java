package com.Mongo.MongoConnection.Controller;

import com.Mongo.MongoConnection.Model.User;
import com.Mongo.MongoConnection.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable String id) {
        return userRepository.findById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // new comment
    @PatchMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            return userRepository.save(user);
        }).orElseGet(() -> {
            userDetails.setId(id);
            return userRepository.save(userDetails);
        });
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
    }
}

package com.amin.store.controllers;

import com.amin.store.entities.User;
import com.amin.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private UserRepository userRepository;

    @GetMapping
    public Iterable<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
       var user = userRepository.findById(id).orElse(null);
       if (user == null) {
            return ResponseEntity.notFound().build();
       }
       return ResponseEntity.ok(user);
    }
}

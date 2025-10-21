package com.amin.store.controllers;

import com.amin.store.dtos.UserDto;
import com.amin.store.entities.User;
import com.amin.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private UserRepository userRepository;

    @GetMapping
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDto(user.getId(),user.getName(),user.getEmail())).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable long id) {
       var user = userRepository.findById(id).orElse(null);
       if (user == null) {
            return ResponseEntity.notFound().build();
       }

       var userDto = new UserDto(user.getId(),user.getName(),user.getEmail());
       return ResponseEntity.ok(userDto);
    }
}

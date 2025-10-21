package com.amin.store.controllers;

import com.amin.store.dtos.UserDto;
import com.amin.store.entities.User;
import com.amin.store.mappers.UserMapper;
import com.amin.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private UserRepository userRepository;
    private UserMapper userMapper;

    @GetMapping
    public List<UserDto> getAllUsers(@RequestParam(required = false, defaultValue = "", name = "sort") String sortBy) {
        if(!Set.of("name", "email").contains(sortBy)) {
            sortBy = "name";
        }

        return userRepository.findAll(Sort.by(sortBy)).stream()
                .map(user -> userMapper.toDto(user)).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable long id) {
       var user = userRepository.findById(id).orElse(null);
       if (user == null) {
            return ResponseEntity.notFound().build();
       }

       var userDto = userMapper.toDto(user);
       return ResponseEntity.ok(userDto);
    }
}

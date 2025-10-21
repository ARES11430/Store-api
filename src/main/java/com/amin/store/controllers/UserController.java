package com.amin.store.controllers;

import com.amin.store.dtos.RegisterUserRequest;
import com.amin.store.dtos.UpdateUserRequest;
import com.amin.store.dtos.UserDto;
import com.amin.store.entities.User;
import com.amin.store.mappers.UserMapper;
import com.amin.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private UserRepository userRepository;
    private UserMapper userMapper;

    @GetMapping
    public List<UserDto> getAllUsers(@RequestParam(required = false, name = "sortBy") String sortBy) {
        List<User> users;

        // 1. Check if the sortBy parameter has text and is a valid field
        if (StringUtils.hasText(sortBy) && Set.of("name", "email").contains(sortBy)) {
            // 2. If it's valid, sort the results
            users = userRepository.findAll(Sort.by(sortBy));
        } else {
            // 3. Otherwise, get all users without any sorting
            users = userRepository.findAll();
        }

        return users.stream()
                .map(user -> userMapper.toDto(user))
                .toList();
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

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @RequestBody RegisterUserRequest request,
            UriComponentsBuilder uriBuilder) {
        var user = userMapper.toEntity(request);
        userRepository.save(user);

        var userDto = userMapper.toDto(user);
        var uri = uriBuilder.path("/users/{id}").buildAndExpand(userDto.getId()).toUri();

        return ResponseEntity.created(uri).body(userDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable(name = "id") Long id,
            @RequestBody UpdateUserRequest request) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        userMapper.update(request, user);
        userRepository.save(user);

        return ResponseEntity.ok(userMapper.toDto(user));
    }
}

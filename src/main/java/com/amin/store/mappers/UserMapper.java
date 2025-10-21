package com.amin.store.mappers;

import com.amin.store.dtos.RegisterUserRequest;
import com.amin.store.dtos.UpdateUserRequest;
import com.amin.store.dtos.UserDto;
import com.amin.store.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(RegisterUserRequest request);
    void update(UpdateUserRequest request, @MappingTarget User user);
}

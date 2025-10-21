package com.amin.store.mappers;

import com.amin.store.dtos.UserDto;
import com.amin.store.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}

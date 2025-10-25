package com.amin.store.mappers;

import com.amin.store.dtos.CartDto;
import com.amin.store.entities.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartDto toDto(Cart cart);
}

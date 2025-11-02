package com.amin.store.mappers;

import com.amin.store.dtos.OrderDto;
import com.amin.store.entities.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDto toDto(Order order);
}

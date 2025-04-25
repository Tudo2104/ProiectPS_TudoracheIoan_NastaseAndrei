package com.example.demo.builder.userbuilder;


import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.dto.userdto.UserViewDTO;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserBuilder {

    public static User generateEntityFromDTO(UserDTO userDTO, Role role){
        return  User.builder().id(userDTO.getId())
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .timeStamp(LocalDateTime.now())
                .balance(userDTO.getBalance())
                .role(role)
                .build();
    }
    public static UserDTO generateDTOFromEntity(User user){
        return  UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .password(user.getPassword())
                .email(user.getEmail())
                .roleName(user.getRole().getName())
                .balance(user.getBalance())
                .build();
    }


}

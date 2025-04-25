package com.example.demo.dto.userdto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserViewDTO implements Comparable<UserViewDTO>{

    private String name;

    private String email;

    private String roleName;

    private String timeStamp;
    private Double balance;

    @Override
    public int compareTo(UserViewDTO userViewDTO) {
        return name.compareTo(userViewDTO.name);
    }
}

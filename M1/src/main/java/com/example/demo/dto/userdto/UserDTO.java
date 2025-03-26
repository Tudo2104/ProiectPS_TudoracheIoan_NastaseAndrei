package com.example.demo.dto.userdto;

import com.example.demo.entity.Friendship;
import com.example.demo.entity.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO{

    private Long id;

    private String name;

    private String email;

    private String password;

    private String roleName;

    private PostStatus status;

}

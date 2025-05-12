package com.example.demo.dto.moderatorDTO;

import com.example.demo.dto.userdto.UserDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeratorDTO {

    private Long moderatorId;
    private UserDTO userDTO;

}

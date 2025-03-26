package com.example.demo.dto.friendshipDTO;

import com.example.demo.entity.FriendshipStatus;
import com.example.demo.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendshipDTO {

    private Long id;

    private Long user;

    private Long friend;

    private FriendshipStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

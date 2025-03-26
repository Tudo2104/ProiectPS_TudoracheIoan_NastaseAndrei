package com.example.demo.builder.userbuilder;

import com.example.demo.dto.friendshipDTO.FriendshipDTO;
import com.example.demo.entity.Friendship;
import com.example.demo.entity.FriendshipStatus;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.UserException;

import java.time.LocalDateTime;

public class FriendshipBuilder {

    public static Friendship generateEntityFromDTO(FriendshipDTO friendshipDTO, User user, User friend) throws UserException {
        if (user.getId().equals(friend.getId())) {
            throw new UserException("Un utilizator nu poate fi prieten cu el însuși!");
        }

        return Friendship.builder()
                .id(friendshipDTO.getId())
                .user(user)
                .friend(friend)
                .status(friendshipDTO.getStatus() != null ? friendshipDTO.getStatus() : FriendshipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

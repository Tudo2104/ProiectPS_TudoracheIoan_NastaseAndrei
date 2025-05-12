package com.example.demo.dto.moderatoractionDTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeratorActionDTO {

    private Long id;
    private Long moderatorId;
    private String actionType;
    private Long targetUserId;
    private Long targetPostId;
    private Long targetCommentId;
    private boolean readStatus =false;
    private boolean blocked = false;
    private LocalDateTime createdAt;
}

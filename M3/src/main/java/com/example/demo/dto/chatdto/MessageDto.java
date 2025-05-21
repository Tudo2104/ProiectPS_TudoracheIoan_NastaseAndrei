package com.example.demo.dto.chatdto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    private Long id;
    private String sender;
    private String content;
    private LocalDateTime timestamp;

}

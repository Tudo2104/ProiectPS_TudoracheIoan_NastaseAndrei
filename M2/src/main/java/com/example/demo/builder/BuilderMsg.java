package com.example.demo.builder;

import com.example.demo.dto.chatdto.MessageDto;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.Message;

import java.time.LocalDateTime;

public class BuilderMsg {

    public static Message generateEntityFromDTO(MessageDto messageDto){
        return  Message.builder()
                .id(messageDto.getId())
                .content(messageDto.getContent())
                .sender(messageDto.getSender())
                .timestamp(LocalDateTime.now())
                .build();
    }
    public static MessageDto generateDTOFromEntity(Message message){
        return  MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .sender(message.getSender())
                .build();
    }
}

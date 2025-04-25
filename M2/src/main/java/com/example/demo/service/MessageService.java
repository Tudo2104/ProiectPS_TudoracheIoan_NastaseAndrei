package com.example.demo.service;

import com.example.demo.builder.BuilderMsg;
import com.example.demo.dto.chatdto.MessageDto;
import com.example.demo.entity.Message;
import com.example.demo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public void save(MessageDto messageDto) {

        Message entity = BuilderMsg.generateEntityFromDTO(messageDto);
        messageRepository.save(entity);

    }

}


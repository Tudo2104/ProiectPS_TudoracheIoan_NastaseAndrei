package com.example.demo.controller;

import com.example.demo.dto.chatdto.MessageDto;
import com.example.demo.service.ChatWebSocketHandler;
import com.example.demo.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ChatWebSocketHandler chatWebSocketHandler;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/chat")
    public ResponseEntity<?> processChatForm(@RequestBody(required = false) MessageDto messageDto){
        return chatWebSocketHandler.broadcast(messageDto);
    }

}


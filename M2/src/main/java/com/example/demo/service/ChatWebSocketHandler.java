package com.example.demo.service;

import com.example.demo.builder.SimpleGameResponseBuilder;
import com.example.demo.dto.chatdto.MessageDto;
import com.example.demo.dto.game.GameDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    @Autowired
    private WebClient.Builder webClientBuilder;
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper mapper;
    private final MessageService messageService;

    public ChatWebSocketHandler(MessageService messageService) {
        this.messageService = messageService;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public ResponseEntity<?> broadcast(MessageDto messageDto,Long gameId) {
        try {
            messageService.save(messageDto);
            String mes = messageDto.getContent();

            String message = mapper.writeValueAsString(messageDto);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(message));
                }
            }
            if(mes.equals("hit")){
                if(gameId == null){
                    gameId = 0L;
                }
                try {
                    GameDTO response = webClientBuilder.build()
                            .method(HttpMethod.PUT)
                            .uri("http://localhost:8083/api/games/" + gameId +"/"+messageDto.getSender()+  "/hit")
                            .retrieve()
                            .bodyToMono(GameDTO.class)
                            .block();

                    if (response == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from Blackjack service.");
                    }

                    return ResponseEntity.ok(SimpleGameResponseBuilder.generateFromGameDTO(response));
                } catch (WebClientResponseException e) {
                    String errorBody = e.getResponseBodyAsString();
                    return ResponseEntity.status(e.getStatusCode()).body(errorBody);
                }
            }
            if(mes.equals("stand")){
                if(gameId == null){
                    gameId = 0L;
                }
                try {
                    GameDTO response = webClientBuilder.build()
                            .method(HttpMethod.PUT)
                            .uri("http://localhost:8083/api/games/" + gameId +"/"+messageDto.getSender()+  "/stand")
                            .retrieve()
                            .bodyToMono(GameDTO.class)
                            .block();

                    if (response == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from Blackjack service.");
                    }

                    return ResponseEntity.ok(SimpleGameResponseBuilder.generateFromGameDTO(response));
                } catch (WebClientResponseException e) {
                    String errorBody = e.getResponseBodyAsString();
                    return ResponseEntity.status(e.getStatusCode()).body(errorBody);
                }
            }
            return ResponseEntity.ok("Message was sent");
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error");
        }

    }


}

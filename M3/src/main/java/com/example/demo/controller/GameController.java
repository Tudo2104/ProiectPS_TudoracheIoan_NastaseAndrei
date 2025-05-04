package com.example.demo.controller;

import com.example.demo.dto.game.GameDTO;
import com.example.demo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/start")
    public ResponseEntity<?> startGame(@RequestBody(required = false) GameDTO gameDTO) {
        return gameService.startGame(gameDTO);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{gameId}/hit")
    public ResponseEntity<?> hit(@PathVariable("gameId") Long gameId) {
        return gameService.hit(gameId);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{gameId}/stand")
    public ResponseEntity<?> stand(@PathVariable("gameId") Long gameId) {
        return gameService.stand(gameId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{gameId}")
    public ResponseEntity<?> getGameStatus(@PathVariable("gameId") Long gameId) {
        return gameService.getGameStatus(gameId);
    }
    @RequestMapping(method = RequestMethod.GET, value = "/{userId}/history")
    public ResponseEntity<?> getHistory(@PathVariable("userId") Long userId) {
        return gameService.getHistoryByUserId(userId);
    }
}

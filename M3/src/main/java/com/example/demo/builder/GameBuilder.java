package com.example.demo.builder;

import com.example.demo.dto.game.GameDTO;
import com.example.demo.entity.Dealer;
import com.example.demo.entity.Game;
import com.example.demo.entity.Hand;


public class GameBuilder {

    public static Game generateNewGame(Long userId, Hand playerHand, Dealer dealer, Double betValue) {
        Game game = new Game();
        game.setUserId(userId);
        game.setPlayerHand(playerHand);
        game.setDealer(dealer);
        game.setStatus("IN_PROGRESS");
        game.setBetValue(betValue);
        return game;
    }

    public static GameDTO generateDTOFromEntity(Game game) {
        return GameDTO.builder()
                .id(game.getId())
                .userId(game.getUserId())
                .status(game.getStatus())
                .playerHand(HandBuilder.generateDTOFromEntity(game.getPlayerHand()))
                .dealer(DealerBuilder.generateDTOFromEntity(game.getDealer()))
                .betValue(game.getBetValue())
                .build();
    }
}

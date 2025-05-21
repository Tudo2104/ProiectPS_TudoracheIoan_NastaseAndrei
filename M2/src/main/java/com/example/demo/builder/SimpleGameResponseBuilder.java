package com.example.demo.builder;

import com.example.demo.dto.card.CardDTO;
import com.example.demo.dto.game.GameDTO;
import com.example.demo.dto.simpleGameResponse.SimpleGameResponse;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleGameResponseBuilder {
    public static SimpleGameResponse generateFromGameDTO(GameDTO gameDTO) {
        return SimpleGameResponse.builder()
                .status(gameDTO.getStatus())
                .playerCards(mapCardsToStrings(gameDTO.getPlayerHand().getCards()))
                .dealerCards(mapCardsToStrings(gameDTO.getDealer().getHand().getCards()))
                .gameId(gameDTO.getId())
                .build();
    }

    private static List<String> mapCardsToStrings(List<CardDTO> cards) {
        return cards.stream()
                .map(card -> card.getRank() + "_of_" + card.getSuit())
                .collect(Collectors.toList());
    }
}

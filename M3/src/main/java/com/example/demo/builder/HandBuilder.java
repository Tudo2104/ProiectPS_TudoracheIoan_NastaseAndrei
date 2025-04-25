package com.example.demo.builder;

import com.example.demo.dto.hand.HandDTO;
import com.example.demo.entity.Hand;


import java.util.ArrayList;
import java.util.stream.Collectors;

public class HandBuilder {

    public static Hand generateEmptyHand() {
        Hand hand = new Hand();
        hand.setCards(new ArrayList<>());
        hand.setTotalValue(0);
        hand.setIsBust(false);
        hand.setIsBlackjack(false);
        return hand;
    }

    public static HandDTO generateDTOFromEntity(Hand hand) {
        return HandDTO.builder()
                .id(hand.getId())
                .totalValue(hand.getTotalValue())
                .isBust(hand.getIsBust())
                .isBlackjack(hand.getIsBlackjack())
                .cards(hand.getCards().stream()
                        .map(CardBuilder::generateDTOFromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}


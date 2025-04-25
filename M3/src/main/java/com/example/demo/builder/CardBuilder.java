package com.example.demo.builder;


import com.example.demo.dto.card.CardDTO;
import com.example.demo.entity.Card;

public class CardBuilder {

    public static Card generateCard(String suit, String rank, Integer value) {
        Card card = new Card();
        card.setSuit(suit);
        card.setRank(rank);
        card.setValue(value);
        return card;
    }

    public static CardDTO generateDTOFromEntity(Card card) {
        return CardDTO.builder()
                .id(card.getId())
                .suit(card.getSuit())
                .rank(card.getRank())
                .value(card.getValue())
                .build();
    }
}

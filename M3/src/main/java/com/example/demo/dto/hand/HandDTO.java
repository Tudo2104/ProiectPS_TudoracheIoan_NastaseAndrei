package com.example.demo.dto.hand;

import com.example.demo.dto.card.CardDTO;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandDTO {
    private Long id;
    private Integer totalValue;
    private Boolean isBust;
    private Boolean isBlackjack;
    private List<CardDTO> cards;
}


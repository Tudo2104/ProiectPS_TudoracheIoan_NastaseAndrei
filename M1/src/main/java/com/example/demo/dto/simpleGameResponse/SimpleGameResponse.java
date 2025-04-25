package com.example.demo.dto.simpleGameResponse;

import com.example.demo.dto.card.CardDTO;
import lombok.*;

import java.util.List;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleGameResponse {
    private Long gameId;
    private String status;
    private List<String> playerCards;
    private List<String> dealerCards;
}

package com.example.demo.dto.game;

import com.example.demo.dto.dealer.DealerDTO;
import com.example.demo.dto.hand.HandDTO;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDTO {
    private Long id;
    private Long userId;
    private String status;
    private HandDTO playerHand;
    private DealerDTO dealer;
    private Double betValue;
}

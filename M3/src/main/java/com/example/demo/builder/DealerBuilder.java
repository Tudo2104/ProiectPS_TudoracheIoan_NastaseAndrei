package com.example.demo.builder;

import com.example.demo.dto.dealer.DealerDTO;
import com.example.demo.entity.Dealer;
import com.example.demo.entity.Hand;

public class DealerBuilder {

    public static Dealer generateDealer(Hand hand) {
        Dealer dealer = new Dealer();
        dealer.setHand(hand);
        return dealer;
    }

    public static DealerDTO generateDTOFromEntity(Dealer dealer) {
        return DealerDTO.builder()
                .id(dealer.getId())
                .hand(HandBuilder.generateDTOFromEntity(dealer.getHand()))
                .build();
    }
}

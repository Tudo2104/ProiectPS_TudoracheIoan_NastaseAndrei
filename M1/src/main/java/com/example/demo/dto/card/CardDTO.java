package com.example.demo.dto.card;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDTO {
    private Long id;
    private String suit;
    private String rank;
    private Integer value;

}


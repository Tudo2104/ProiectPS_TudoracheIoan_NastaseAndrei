package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "dealer_id", referencedColumnName = "id")
    private Dealer dealer;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hand_id", referencedColumnName = "id")
    private Hand playerHand;

    @Column(nullable = false)
    private String status;

    @Column(name = "bet_value")
    private Double betValue;
}

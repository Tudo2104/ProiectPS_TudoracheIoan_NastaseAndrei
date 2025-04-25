package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "hand", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();

    @Column(nullable = false)
    private Integer totalValue;

    @Column(nullable = false)
    private Boolean isBust;

    @Column(nullable = false)
    private Boolean isBlackjack;
}

